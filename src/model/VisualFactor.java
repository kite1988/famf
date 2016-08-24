package model;

import java.util.Collections;

import data.DataDAO;
import data.RatingTuple;
import data.Tweet;
import matrix.DenseMatrix;
import matrix.DenseVector;
import util.VectorUtil;

/**
 * Feature-aware MF model using visual features
 */

public class VisualFactor extends Recommender {

	public VisualFactor(DataDAO trainDao, DataDAO testDao) {
		super(trainDao, testDao);
	}

	

	@Override
	protected void initModel() throws Exception {
		U = new DenseMatrix(numUsers, numSharedFactors);
		V = new DenseMatrix(numVisualWords, numSharedFactors);

		// initialize model
		if (initByNorm) {
			U.init(initMean, initStd);
			V.init(initMean, initStd);
		} else {
			U.init(); 
			V.init(); 
		}
	}

	@Override
	protected void buildModel() throws Exception {
		Collections.shuffle(ratingTuples);

		measures = evalRankings(false, 0);
		printEvalInfo(0);

		// loss = errs + regularization
		for (int iter = 1; iter <= numIters; iter++) {
			loss = 0;
			errs = 0;
			start = System.currentTimeMillis();

			for (int i = 0; i < tupleSize; i++) {
				RatingTuple tuple = ratingTuples.get(i);
				int uid = tuple.user;
				int tid_pos = tuple.posId;
				int tid_neg = tuple.negId;

				/********** get necessary data structure *********/
				// two tweets
				Tweet tweet_pos = trainDao.tweets[tid_pos];
				Tweet tweet_neg = trainDao.tweets[tid_neg];

				double z_v_k = Math.sqrt(tweet_pos.visualLength);
				double z_v_h = Math.sqrt(tweet_neg.visualLength);

				// user
				DenseVector user_factor = U.row(uid);

				DenseVector avg_q_t_k = VectorUtil.averageWordFactor(
						tweet_pos.visualUnique, tweet_pos.visualFreq, z_v_k, V,
						numSharedFactors);
				DenseVector avg_q_t_h = VectorUtil.averageWordFactor(
						tweet_neg.visualUnique, tweet_neg.visualFreq, z_v_h, V,
						numSharedFactors);
				DenseVector diff_q_t = avg_q_t_k.minus(avg_q_t_h);

				// prediction
				double pred_pos = predict(user_factor, avg_q_t_k);
				double pred_neg = predict(user_factor, avg_q_t_h);

				double err = Math.log(1 + Math.exp(pred_neg - pred_pos));
				errs += err;
				double e = 1.0 / (1.0 + Math.exp(pred_pos - pred_neg));

				/************** Updating parameters ******************/
				// update factors for user
				for (int j = 0; j < numSharedFactors; j++) {
					double old_f = user_factor.get(j);
					double sgd_u = diff_q_t.get(j) * (-e) + old_f * regU;
					double new_f = old_f - sgd_u * lRate;
					U.set(uid, j, new_f);
				}

				short[] visual = tweet_pos.visualUnique;
				int length = visual.length;
				
				for (int j = 0; j < length; j++) {
					int w = visual[j];

					double[] w_factor = V.row(w, false).data;

					for (int k = 0; k < numSharedFactors; k++) {
						double old_w = w_factor[k];
						double sgd = user_factor.get(k) * (-e / z_v_k) + old_w
								* regV;
						double new_w = old_w - sgd * lRate;
						w_factor[k] = new_w;
					}
				}

				visual = tweet_neg.visualUnique;
				
				for (int j = 0; j < visual.length; j++) {
					int w = visual[j];

					double[] w_factor = V.row(w, false).data;

					for (int k = 0; k < numSharedFactors; k++) {
						double old_w = w_factor[k];
						double sgd = user_factor.get(k) * (e / z_v_h) + old_w
								* regV;
						double new_w = old_w - sgd * lRate;
						w_factor[k] = new_w;
					}
				}

			} // end of one tuple

			errs *= 0.5;
			loss = errs + regU * U.normSquare() + regV * V.normSquare();

			if (isConverged(iter)) {
				break;
			}
		}// end of one iteration
	}// end of training

	protected double predict(int u, int i, DataDAO dataDao) throws Exception {
		Tweet tweet = dataDao.tweets[i];
		DenseVector u_factor = U.row(u, false);

		// words
		DenseVector avg_t = VectorUtil.averageWordFactor(tweet.visualUnique,
				tweet.visualFreq, Math.sqrt(tweet.visualLength), V,
				numSharedFactors);
		return u_factor.inner(avg_t);
	}

	protected double predict(DenseVector u, DenseVector avg_t) {
		return u.inner(avg_t);
	}

	protected String saveModel() throws Exception {
		String dirPath = super.saveModel();
		return dirPath;
	}

	protected void loadModel(String path) throws Exception {
		super.loadModel(path);
	}
}
