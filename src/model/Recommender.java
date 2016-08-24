package model;

import happy.coding.io.Configer;
import happy.coding.io.FileIO;
import happy.coding.io.Lists;
import happy.coding.io.Logs;
import happy.coding.io.Strings;
import happy.coding.system.Dates;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import matrix.DenseMatrix;
import matrix.SparseMatrix;
import util.EvalUtil;
import util.FileUtil;
import util.VectorUtil;

import com.google.common.base.Stopwatch;
import com.google.common.io.Files;

import data.DataDAO;
import data.RatingTuple;

public abstract class Recommender {

	public Configer cf;
	public String configPath;
	public String algoName;
	public boolean sample = false;
	public String mode = "train_test";
	public boolean replaceMissing = false;

	// log and result files
	private String resultFolder, modelFolder;
	private PrintWriter logPW, resultPW, predictPW, userPW;

	/*********************** Dataset *******************/

	public DataDAO trainDao, testDao;
	// <user, pos, neg>
	public ArrayList<RatingTuple> ratingTuples, testTuples;

	public SparseMatrix testMatrix;

	protected int numUsers, numItems, numRates;
	public int numTextWords, numVisualWords;
	protected int tupleSize;

	// the approximate number, used to create hashmap key
	protected long userNum = 1000;
	protected long tweetNum = 10000000;

	/******************* Model initialization ****************/

	// initial models using normal distribution
	protected boolean initByNorm = false;
	protected float initMean, initStd;

	protected float globalMean; // global average of training rates

	/********************* Model Tunable Parameters ***************/

	protected float regU, regT, regV;
	protected int numSharedFactors;
	protected double ratio = 0.1; // sample visual words
	protected String textPath;

	protected float initLRate, maxLRate, minLRate;

	protected static int numIters; // number of iterations
	protected int bestIters = 0;

	// whether to adjust learning rate automatically
	protected boolean isBoldDriver;
	// whether to undo last weight changes if negative loss observed
	// using bold drivier
	protected boolean isUndoEnabled;
	protected float decay; // decay of learning rate
	protected boolean undo = false;

	/*********************** Model learning parameters *************/

	protected DenseMatrix U, last_U; // user-factor matrix
	protected DenseMatrix T, last_T; // text-factor matrix
	protected DenseMatrix V, last_V; // visual-factor matrix

	protected float lRate; // adaptive learn rate
	protected double errs = 0, last_errs = 0, delta_errs = 0; // training errors
	protected double loss = 0, last_loss = 0, delta_loss = 0; // objective loss

	/*************************** Evaluation **********************/
	public Map<Measure, Double> measures; // performance measures

	protected boolean verbose;
	private static int itersResults, itersPrediction, itersModel;

	protected long start = -1;
	protected double prec = 0, last_prec = 0, delta_prec = 0;
	protected double map = 0, last_map = 0, delta_map = 0;

	protected static double epsilon = 1.0e-5; // 5.0e-4;
	protected static double smallLRate = 1.0e-5;

	public enum Measure {
		Pre1, Pre3, Pre5, Pre10, Pre20, MAP, TrainTime, TestTime
	}


	public void initParameters(Configer cf, String configPath) {
		this.cf = cf;
		this.configPath = configPath;
		numSharedFactors = cf.getInt("num.factors");

		regU = cf.getFloat("reg.user");
		if (cf.containsKey("reg.text")) {
			regT = cf.getFloat("reg.text");
		}
		if (cf.containsKey("reg.visual")) {
			regV = cf.getFloat("reg.visual");
		}

		initLRate = cf.getFloat("init.learn.rate");
		maxLRate = cf.getFloat("max.learn.rate");
		minLRate = cf.getFloat("min.learn.rate");
		lRate = initLRate;

		isBoldDriver = cf.isOn("is.bold.driver");
		isUndoEnabled = cf.isOn("is.undo.change");
		decay = cf.getFloat("val.decay.rate");

		if (cf.containsKey("num.text.words")) {
			numTextWords = cf.getInt("num.text.words");

			if (cf.containsKey("replace.missing"))
				replaceMissing = cf.isOn("replace.missing");
			if (replaceMissing) {
				numTextWords += 1;
			}
		}
		if (cf.containsKey("num.visual.words")) {
			numVisualWords = cf.getInt("num.visual.words");
		}

		numIters = cf.getInt("num.max.iter");

		verbose = cf.isOn("is.verbose");
		itersResults = cf.getInt("iters.result.out");
		itersPrediction = cf.getInt("iters.prediction.out");
		itersModel = cf.getInt("iters.model.out");

		initByNorm = cf.isOn("init.latent.norm");
		initMean = cf.getFloat("init.latent.mean");
		initStd = cf.getFloat("init.latent.std");
	}

	public Recommender(DataDAO trainDao, DataDAO testDao) {
		algoName = this.getClass().getSimpleName();

		this.trainDao = trainDao;
		this.testDao = testDao;
		this.ratingTuples = trainDao.ratingTuple;
		this.testMatrix = testDao.ratingMatrix;

		numUsers = trainDao.numUsers();
		numItems = trainDao.numItems();
		numRates = trainDao.numRates;
		tupleSize = trainDao.ratingTuple.size();

		this.mode = "train";
	}

	public Recommender(DataDAO trainDao, DataDAO testDao, String mode) {
		this(trainDao, testDao);
		this.mode = mode;
	}


	public void execute() throws Exception {
		Stopwatch sw = Stopwatch.createStarted();
		initFolders();

		// train
		if (mode.equals("train")) {
			initModel();
			buildModel();
		} else { // test
			loadModel(cf.getString("load.model.path"));
		}
		long trainTime = sw.elapsed(TimeUnit.MILLISECONDS);

		// test
		measures = evalRankings(true, numIters);
		sw.stop();

		// result
		long testTime = sw.elapsed(TimeUnit.MILLISECONDS) - trainTime;
		printEvalInfo(trainTime, testTime);

		// save model
		saveModel();
		cleanUp();
	}

	// results/algorithmName/Sn_Tn_Vn_date/
	private void initFolders() {
		String resultPath = cf.getString("result.dir");
		File f = new File(resultPath + File.separator + algoName + File.separator + getFactors() + "_"
				+ Dates.now().replace(" ", "_"));
		f.mkdirs();
		resultFolder = f.getAbsolutePath();
		try {
			logPW = FileUtil.createWriter(resultFolder + File.separator + "log.txt");

			String info = algoName + ": \n" + toString();
			logPW.println(info);

			logPW.println("Training set: " + trainDao.getRatingPath());
			logPW.println("Testing set: " + testDao.getRatingPath());
			logPW.println("Text:" + trainDao.getTextPath());

			logPW.println("-----Training----");
			logPW.println("User: " + numUsers);
			logPW.println("Items: " + numItems);
			logPW.println("Ratings: " + numRates);
			logPW.println();

			logPW.flush();

			resultPW = FileUtil.createWriter(resultFolder + File.separator + "result.csv");
			resultPW.println(info);
			String evalInfo = "Iter,Pre1,Pre3,Pre5,Pre10,Pre20,MAP";
			resultPW.println(evalInfo);

			FileIO.makeDirectory(resultFolder + File.separator + "prediction");

			// copy config file
			Files.copy(new File(configPath), new File(resultFolder + File.separator + "config.txt"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printEvalInfo(long trainTime, long testTime) throws IOException {
		String result = getEvalInfo(measures, numIters);

		measures.put(Measure.TrainTime, (double) trainTime);
		measures.put(Measure.TestTime, (double) testTime);

		String evalInfo = result + "\nTime: " + Dates.parse(measures.get(Measure.TrainTime).longValue()) + ", "
				+ Dates.parse(measures.get(Measure.TestTime).longValue());

		resultPW.println(evalInfo);
		resultPW.flush();

	}

	public void printEvalInfo(int iter) throws IOException {
		String result = getEvalInfo(measures, iter);
		resultPW.println(result);
		resultPW.flush();
	}

	protected void cleanUp() throws Exception {
		logPW.close();
		resultPW.close();
		predictPW.close();
		userPW.close();
	}

	public String getEvalInfo(Map<Measure, Double> measures, int iter) {
		String evalInfo = String.format("%d, %.6f, %.6f, %.6f, %.6f, %.6f, %.6f", iter, measures.get(Measure.Pre1),
				measures.get(Measure.Pre3), measures.get(Measure.Pre5), measures.get(Measure.Pre10),
				measures.get(Measure.Pre20), measures.get(Measure.MAP));
		return evalInfo;
	}

	protected void initModel() throws Exception {

	}

	/**
	 * @return the evaluation results of ranking predictions
	 */
	public Map<Measure, Double> evalRankings(boolean lastIter, int iter) throws Exception {

		if (iter != 0) {
			if (iter % itersModel == 0 && !lastIter) {
				saveModel();
			}

			if (iter % itersPrediction == 0 || lastIter) {
				if (predictPW != null) {
					predictPW.flush();
					predictPW.close();
				}
				predictPW = FileUtil.createWriter(
						resultFolder + File.separator + "prediction" + File.separator + "prediction_" + iter);
			}

		}

		if (lastIter) {
			userPW = FileUtil.createWriter(resultFolder + File.separator + "result_user.csv");
			String evalInfo = "User,Pre1,Pre3,Pre5,Pre10,Pre20";
			userPW.println(evalInfo);
		}

		// # of users
		int capacity = testMatrix.numRows();

		// initialization capacity to speed up
		List<Double> precs1 = new ArrayList<>(capacity);
		List<Double> precs3 = new ArrayList<>(capacity);
		List<Double> precs5 = new ArrayList<>(capacity);
		List<Double> precs10 = new ArrayList<>(capacity);
		List<Double> precs20 = new ArrayList<>(capacity);
		List<Double> aps = new ArrayList<>(capacity);

		if (verbose && lastIter) {
			logPW.println("\n------Testing------");
			logPW.println(String.format("%s has candidate items: %d", algoName, testMatrix.columns().size()));
		}

		// for each test user
		int um = capacity;
		for (int u = 0; u < um; u++) {
			List<Integer> testItems = testMatrix.getColumns(u);
			Collections.shuffle(testItems);
			List<Integer> posItems = testMatrix.getPositiveColumns(u);
			HashSet<Integer> posSet = new HashSet<Integer>(posItems);

			// predict the ranking scores (unordered) of all candidate items
			List<Map.Entry<Integer, Double>> itemScores = new ArrayList<>(Lists.initSize(testItems));
			for (final Integer j : testItems) {
				final double rank = ranking(u, j, testDao);
				if (!Double.isNaN(rank)) {
					itemScores.add(new SimpleImmutableEntry<Integer, Double>(j, rank));
				}
			}

			// order the ranking scores from highest to lowest
			Lists.sortList(itemScores, true);
			if (iter != 0 && (iter % itersPrediction == 0 || lastIter))
				printPrediction(u, itemScores);

			List<Integer> rankedItems = new ArrayList<>();
			for (Map.Entry<Integer, Double> kv : itemScores)
				rankedItems.add(kv.getKey());

			Double AP = EvalUtil.AP(posSet, rankedItems);
			Map<Integer, Double> precs = EvalUtil.precisionAll(posSet, rankedItems);

			if (precs != null) {
				precs1.add(precs.get(1));
				precs3.add(precs.get(3));
				precs5.add(precs.get(5));
				precs10.add(precs.get(10));
				precs20.add(precs.get(20));

				if (lastIter) {
					userPW.print(this.trainDao.getUserId(u) + "," + precs.get(1) + "," + precs.get(3) + ","
							+ precs.get(5) + "," + precs.get(10) + "," + precs.get(20));
				}
			}
			aps.add(AP);

			if (lastIter) {
				userPW.println("," + AP);
			}

		}

		Map<Measure, Double> measures = new HashMap<>();
		prec = EvalUtil.mean(precs1);
		map = EvalUtil.mean(aps);
		measures.put(Measure.Pre1, prec);
		measures.put(Measure.Pre3, EvalUtil.mean(precs3));
		measures.put(Measure.Pre5, EvalUtil.mean(precs5));
		measures.put(Measure.Pre10, EvalUtil.mean(precs10));
		measures.put(Measure.Pre20, EvalUtil.mean(precs20));
		measures.put(Measure.MAP, map);

		return measures;
	}

	private void printPrediction(int u, List<Map.Entry<Integer, Double>> scores) {
		predictPW.print(u);
		for (Map.Entry<Integer, Double> entry : scores) {
			int item = entry.getKey();
			double score = entry.getValue();
			predictPW.print(" " + item + ":" + score);
		}
		predictPW.println();
		predictPW.flush();
	}

	/**
	 * predict a specific rating for user u on item j, note that the prediction
	 * is not bounded. It is useful for building models with no need to bound
	 * predictions.
	 * 
	 * @param u
	 *            user id
	 * @param j
	 *            item id
	 * @return raw prediction without bounded
	 */
	protected double predict(int u, int j, DataDAO dateDao) throws Exception {
		return globalMean;
	}

	protected double ranking(int u, int j, DataDAO dateDao) throws Exception {
		return predict(u, j, dateDao);
	}

	protected boolean isConverged(int iter) {
		if (iter % itersResults == 0) {
			try {
				measures = evalRankings(false, iter);
				printEvalInfo(iter);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		delta_errs = (last_errs - errs) / errs;
		delta_loss = (last_loss - loss) / loss;
		delta_map = map - last_map;
		delta_prec = prec - last_prec;


		if (!(isBoldDriver && isUndoEnabled) && (Double.isNaN(errs))) {
			logPW.println(
					"Loss = NaN or Infinity: current settings cannot train the recommender! Try other settings instead!");
			Logs.error(
					"Loss = NaN or Infinity: current settings cannot train the recommender! Try other settings instead!");
			System.exit(-1);
		}

		// check if converged
		boolean cond1 = Math.abs(delta_loss) < epsilon;
		// boolean cond2 = (delta_map > 0) && (delta_map < epsilon)
		// && (delta_prec < epsilon);

		boolean cond2 = ((Math.abs(delta_map) < epsilon) && (Math.abs(delta_prec) < epsilon)) || lRate < epsilon;

		boolean converged = cond1 || cond2;

		// if not converged, update learning rate
		if (!converged) {
			updateLRate(iter);
		} else {
			// if (delta_map < 0) {
			if (delta_map < -epsilon) {
				undo = true;
				undos(iter);
			} else {
				bestIters = iter;
			}
		}

		if (!undo) {
			last_errs = errs;
			last_loss = loss;
			last_prec = prec;
			last_map = map;
		}

		logPW.flush();
		return converged;
	}

	// start with bold-driver, but switch to decay once undo, and keep
	// bold-driver's decreasing style (50%) in the decay mode.
	protected void updateLRate(int iter) {
		if (isBoldDriver) {
			if ((delta_loss > -epsilon && delta_map > -epsilon) || iter <= 2) {
				lRate *= 1.05;

				if (isUndoEnabled) {
					updates();
					bestIters = iter;
					undo = false;
				}
			} else {
				lRate *= 0.5;
				isBoldDriver = false;

				if (isUndoEnabled) {
					undos(iter);
					undo = true;
				}
			}
		} else if (decay > 0 && decay < 1) {
			if ((delta_loss > -epsilon && delta_map > -epsilon) || iter <= 2) {
				lRate *= decay;
				if (isUndoEnabled) {
					undo = false;
					updates();
					bestIters = iter;
				}
			} else {
				lRate *= 0.5;

				if (isUndoEnabled) {
					undos(iter);
					undo = true;
				}
			}
		}

		// limit to max-learn-rate after update
		if (maxLRate > 0 && lRate > maxLRate)
			lRate = maxLRate;
		if (lRate < minLRate)
			lRate = minLRate;
	}

	/**
	 * updates last weights
	 */
	protected void updates() {
		if (U != null)
			last_U = U.clone();
		if (T != null)
			last_T = T.clone();
		if (V != null)
			last_V = V.clone();
	}

	/**
	 * undo last weight changes
	 */
	protected void undos(int iter) {
		logPW.println(String.format("!%d: undo last weight changes and sharply decrease the learning rate ! %s", iter,
				isBoldDriver));

		if (last_U != null)
			U = last_U.clone();
		if (last_T != null)
			T = last_T.clone();
		if (last_V != null)
			V = last_V.clone();
	}

	protected String saveModel() throws Exception {
		// make a folder
		modelFolder = resultFolder + File.separator + "model/";
		FileIO.makeDirectory(modelFolder);

		if (!FileIO.exist(modelFolder + "config"))
			FileIO.copyFile(configPath, modelFolder + "config");

		// save the rating matrix and dao as binary to save space
		String suffix = ".bin";
		if (cf.isOn("save.model.bin")) {
			suffix = ".bin";
			FileIO.serialize(U, modelFolder + "userFactors" + suffix);
			FileIO.serialize(T, modelFolder + "textFactors" + suffix);
			if (V != null)
				FileIO.serialize(V, modelFolder + "visualFactors" + suffix);
		} else {
			suffix = ".txt";
			if (U != null)
				VectorUtil.saveMatrix(U, modelFolder + "userFactors" + suffix);
			if (T != null)
				VectorUtil.saveMatrix(T, modelFolder + "textFactors" + suffix);
			if (V != null)
				VectorUtil.saveMatrix(V, modelFolder + "visualFactors" + suffix);
		}

		suffix = ".txt";
		// save name-id maps
		VectorUtil.saveMap(trainDao.getIdUsers(), modelFolder + "idUsers" + suffix);
		VectorUtil.saveMap(trainDao.getIdItems(), modelFolder + "train_idItems" + suffix);
		VectorUtil.saveMap(testDao.getIdItems(), modelFolder + "test_idItems" + suffix);
		Logs.debug("Learned models are saved to folder \"{}\"", modelFolder);

		return modelFolder;
	}

	protected void loadModel(String dirPath) throws Exception {
		logPW.println(String.format("A recommender model is loaded from %s", dirPath));
		dirPath += File.separator;

		String suffix = ".txt";
		U = VectorUtil.loadMatix(dirPath + "userFactors" + suffix);
		T = VectorUtil.loadMatix(dirPath + "textFactors" + suffix);
		if (FileIO.exist(dirPath + "visualFactors" + suffix))
			V = VectorUtil.loadMatix(dirPath + "visualFactors" + suffix);

		// load vectors
		suffix = ".txt";

	}

	@Override
	public String toString() {
		return Strings
				.toString(new Object[] { "initLRate", "maxLRate", "regU", "regT", "regV", "numSharedFactors",
						"numIters", "isBoldDriver", "isUndoEnabled", "initByNorm" }, ",")
				+ "\n" + Strings.toString(new Object[] { initLRate, maxLRate, regU, regT, regV, numSharedFactors,
						numIters, isBoldDriver, isUndoEnabled, initByNorm }, ",");
	}

	protected void buildModel() throws Exception {

	}

	private String getFactors() {
		return "S" + numSharedFactors;
	}

}
