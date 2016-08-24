package model;

import java.util.Collections;

import data.DataDAO;
import data.RatingTuple;
import data.Tweet;
import matrix.DenseMatrix;
import matrix.DenseVector;
import util.VectorUtil;

/**
 * Feature-aware MF model using textual features
 */
public class TextFactor extends Recommender {

	public TextFactor(DataDAO trainDao, DataDAO testDao) {
		super(trainDao, testDao);
	}

	@Override
	protected void initModel() throws Exception {
		U = new DenseMatrix(numUsers, numSharedFactors);
		T = new DenseMatrix(numTextWords, numSharedFactors);

		// initialize model
		if (initByNorm) {
			U.init(initMean, initStd);
			T.init(initMean, initStd);
		} else {
			U.init(); 
			T.init(); 
		}
	}

	@Override
	protected void buildModel() throws Exception {
		System.out.println("training...");
		System.out.print("iter ");
		Collections.shuffle(ratingTuples);

		measures = evalRankings(false, 0);
		printEvalInfo(0);

		// loss = errs + regularization
		for (int iter = 1; iter <= numIters; iter++) {
			System.out.print(iter);
			errs = 0;
			loss = 0;
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
				double z_t_k = Math.sqrt(tweet_pos.textLength);
				double z_t_h = Math.sqrt(tweet_neg.textLength);

				// user
				DenseVector user_factor = U.row(uid);

				DenseVector avg_q_t_k = VectorUtil.averageWordFactor(
						tweet_pos.textUnique, tweet_pos.textFreq, z_t_k, T,
						numSharedFactors);
				DenseVector avg_q_t_h = VectorUtil.averageWordFactor(
						tweet_neg.textUnique, tweet_neg.textFreq, z_t_h, T,
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

				// update word factor
				int[] text = tweet_pos.textUnique;
				if (text != null) {
					for (int j = 0; j < text.length; j++) {
						int w = text[j];
						DenseVector w_factor = T.row(w, false);

						for (int k = 0; k < numSharedFactors; k++) {
							double old_w = w_factor.get(k);
							double sgd_w = user_factor.get(k) * (-e / z_t_k)
									+ old_w * regT;
							double new_w = old_w - sgd_w * lRate;
							w_factor.set(k, new_w);
						}
					}
				}

				text = tweet_neg.textUnique;
				if (text != null) {
					for (int j = 0; j < text.length; j++) {
						int w = text[j];
						DenseVector w_factor = T.row(w, false);

						for (int k = 0; k < numSharedFactors; k++) {
							double old_w = w_factor.get(k);
							double sgd_w = user_factor.get(k) * (e / z_t_h)
									+ old_w * regT;
							double new_w = old_w - sgd_w * lRate;
							w_factor.set(k, new_w);
						}
					}
				}

			} // end of one tuple

			errs *= 0.5;
			loss = errs + regU * U.normSquare() + regT * T.normSquare();

			if (isConverged(iter)) {
				return;
			}
		}// end of one iteration
	}// end of training

	protected double predict(int u, int i, DataDAO dataDao) throws Exception {
		Tweet tweet = dataDao.tweets[i];
		DenseVector u_factor = U.row(u, false);

		// words
		DenseVector avg_t = VectorUtil.averageWordFactor(tweet.textUnique,
				tweet.textFreq, Math.sqrt(tweet.textLength), T,
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
