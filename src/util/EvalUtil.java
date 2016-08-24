package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class EvalUtil {
	static int topK = 20;

	public static Double AP(List<Integer> posItems, List<Integer> rankedItems) {
		if (posItems.size() == 0) {
			return null;
		}
		HashSet<Integer> posSet = new HashSet<Integer>(posItems);
		return AP(posSet, rankedItems);
	}

	public static Double AP(HashSet<Integer> posSet, List<Integer> rankedItems) {
		if (posSet.size() == 0) {
			return null;
		}

		double avg = 0;
		for (int i = 1; i <= rankedItems.size(); i++) {
			Double precision = precisionAtK(posSet, rankedItems, i);
			int retweet = posSet.contains(rankedItems.get(i - 1)) ? 1 : 0;
			avg += precision * retweet;
		}
		return avg / posSet.size();
	}

	// Here we limit to top 20
	public static Map<Integer, Double> precisionAll(List<Integer> posItems,
			List<Integer> rankedItems) {
		if (posItems.size() == 0) {
			return null;
		}
		HashSet<Integer> posSet = new HashSet<Integer>(posItems);
		return precisionAll(posSet, rankedItems);
	}

	public static Map<Integer, Double> precisionAll(HashSet<Integer> posSet,
			List<Integer> rankedItems) {
		if (posSet.size() == 0) {
			return null;
		}

		int N = Math.min(rankedItems.size(), topK);
		Map<Integer, Double> precisions = new HashMap<Integer, Double>();
		for (int i = 1; i <= N; i++) {
			double prec = precisionAtK(posSet, rankedItems, i);
			precisions.put(i, prec);
		}
		return precisions;
	}


	// K starts from 1
	public static Double precisionAtK(HashSet<Integer> posSet,
			List<Integer> rankedItems, int K) {
		if (K <= 0 || K > rankedItems.size()) {
			return null;
		}
		int correct = 0;
		for (int i = 0; i < K; i++) {
			int item = rankedItems.get(i);
			if (posSet.contains(item)) {
				correct++;
			}
		}
		return (double) correct / (double) K;
	}

	public static double mean(List<Double> list) {
		if (list == null || list.size() == 0) {
			return 0;
		} else {
			double sum = 0;
			int num = 0;
			for (Double d : list) {
				if (d != null) {
					sum += d;
					num++;
				}
			}
			return sum / num;
		}
	}
}
