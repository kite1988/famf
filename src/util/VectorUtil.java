package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import matrix.DenseMatrix;
import matrix.DenseVector;

public class VectorUtil {

	public static DenseVector averageWordFactor(short[] words, double total,
			DenseMatrix Q, int factors) {
		DenseVector sum = new DenseVector(factors);
		if (words == null || words.length == 0) {
			return sum;
		} else {
			int length = words.length;
			for (int i = 0; i < length; i++) {
				int w = words[i];
				DenseVector s = Q.row(w, false);
				sum = sum.add(s, false);
			}
			return sum.scale(1.0 / total, false);
		}
	}

	public static DenseVector averageWordFactor(int[] words, short[] freq,
			double total, DenseMatrix Q, int factors) {
		DenseVector sum = new DenseVector(factors);
		if (words == null || words.length == 0 || total == 0.0) {
			return sum;
		} else {
			int length = words.length;
			for (int i = 0; i < length; i++) {
				int w = words[i];
				short f = freq[i];
				DenseVector s = Q.row(w, false);
				if (f > 1) {
					s = s.scale(f);
				}
				sum = sum.add(s, false);
			}
			return sum.scale(1.0 / total, false);
		}
	}

	public static DenseVector averageWordFactor(short[] words, short[] freq,
			double total, DenseMatrix Q, int factors) {
		DenseVector sum = new DenseVector(factors);
		if (words == null || words.length == 0) {
			return sum;
		} else {
			int length = words.length;
			for (int i = 0; i < length; i++) {
				short w = words[i];
				short f = freq[i];
				DenseVector s = Q.row(w, false);
				if (f > 1) {
					s = s.scale(f);
				}
				sum = sum.add(s, false);
			}
			return sum.scale(1.0 / total, false);
		}
	}

	public static void saveVector(DenseVector vector, String path)
			throws IOException {
		saveArray(vector.getData(), path);
	}

	public static void saveArray(double[] data, String path)
			throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(path));
		for (int i = 0; i < data.length - 1; i++) {
			pw.print(data[i] + ",");
		}
		pw.println(data[data.length - 1]);
		pw.close();
	}

	public static void save2DArray(double[][] data, String path)
			throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(path));
		int row = data.length, col = data[0].length;
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col - 1; j++) {
				pw.print(data[i][j] + ",");
			}
			pw.println(data[i][col - 1]);
		}
		pw.close();
	}

	public static void saveMatrix(DenseMatrix data, String path)
			throws IOException {
		save2DArray(data.getData(), path);
	}

	public static double[] loadArrayDouble(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = null;
		double[] d = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				String[] items = line.split(",");
				d = new double[items.length];
				for (int i = 0; i < items.length; i++) {
					d[i] = Double.parseDouble(items[i]);
				}
			}
		}
		br.close();
		return d;
	}

	public static DenseMatrix loadMatix(String path) throws IOException {
		return new DenseMatrix(load2DArrayDouble(path));
	}

	public static double[][] load2DArrayDouble(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = null;
		ArrayList<double[]> data = new ArrayList<double[]>();
		while ((line = br.readLine()) != null) {
			String[] items = line.split(",");
			double[] d = new double[items.length];
			for (int i = 0; i < items.length; i++) {
				d[i] = Double.parseDouble(items[i]);
			}
			data.add(d);
		}
		br.close();

		int row = data.size(), col = data.get(0).length;
		double[][] array = new double[row][col];
		for (int i = 0; i < row; i++) {
			double[] d = data.get(i);
			for (int j = 0; j < col; j++) {
				array[i][j] = d[j];
			}
		}
		return array;
	}

	public static void saveMap(Map<Integer, String> map, String path)
			throws IOException {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(map.keySet());
		Collections.sort(list);
		PrintWriter pw = FileUtil.createWriter(path);
		for (Integer id : list) {
			pw.println(id + " " + map.get(id));
		}
		pw.close();
	}

}
