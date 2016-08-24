package main;

import data.DataDAO;
import happy.coding.io.Configer;
import model.Recommender;
import model.TextFactor;

public class VisualMain {

	public static void main(String[] args) throws Exception {

		Configer cf = new Configer(args[0]);

		String trainRatingPath = cf.getString("dataset.train.rating");
		String trainVisualPath = cf.getString("dataset.train.visual");

		String testRatingPath = cf.getString("dataset.test.rating");
		String testVisualPath = cf.getString("dataset.test.visual");


		DataDAO trainDao = new DataDAO();
		trainDao.readData(trainRatingPath, null, trainVisualPath, false, 0);

		DataDAO testDao = new DataDAO(trainDao.getUserIds());
		testDao.readData(testRatingPath, null, testVisualPath, false, 0);

		Recommender rec = new TextFactor(trainDao, testDao);
		rec.algoName = cf.getString("recommender");
		rec.initParameters(cf, args[0]);
		rec.execute();
	}

}
