package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtil {

	public static PrintWriter createWriter(String file) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(file));
		return pw;
	}

	public static BufferedReader createReader(String file)
			throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		return br;
	}
}
