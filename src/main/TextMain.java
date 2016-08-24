package main;

import data.DataDAO;
import happy.coding.io.Configer;
import model.Recommender;
import model.TextFactor;

public class TextMain {

	public static void main(String[] args) throws Exception {

		Configer cf = new Configer(args[0]);

		String trainRatingPath = cf.getString("dataset.train.rating");
		String trainTextPath = cf.getString("dataset.train.text");

		String testRatingPath = cf.getString("dataset.test.rating");
		String testTextPath = cf.getString("dataset.test.text");

		boolean replaceMissing = cf.isOn("replace.missing");
		int numTextWords = cf.getInt("num.text.words");
		if (replaceMissing) {
			numTextWords += 1;
		}

		DataDAO trainDao = new DataDAO();
		trainDao.readData(trainRatingPath, trainTextPath, null, replaceMissing, numTextWords);

		DataDAO testDao = new DataDAO(trainDao.getUserIds());
		testDao.readData(testRatingPath, testTextPath, null, replaceMissing, numTextWords);

		Recommender rec = new TextFactor(trainDao, testDao);
		rec.algoName = cf.getString("recommender");
		rec.initParameters(cf, args[0]);
		rec.execute();
	}

}
