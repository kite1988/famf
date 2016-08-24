package data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import util.FileUtil;

public class EncryptUser {
	static HashMap<String, Integer> users = new HashMap<String, Integer>();
	static HashMap<String, Integer> publishers = new HashMap<String, Integer>();
	
	static HashMap<Integer, HashSet<String>> userTuples = new HashMap<Integer, HashSet<String>>();


	public static void main(String[] args) throws IOException {
		//process("dataset/train_rating.txt", "dataset_release/train_rating.txt");
		//process("dataset/test_rating.txt", "dataset_release/test_rating.txt");
		read("dataset/train_rating.txt");
		read("dataset/test_rating.txt");
		write("dataset_release/rating.txt");
		
	}

	public static void process(String fileName, String newFile) throws IOException {
		String line = null;

		BufferedReader br = FileUtil.createReader(fileName);
		PrintWriter pw = FileUtil.createWriter(newFile);

		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				String[] instances = line.split(",");
				for (int i = 0; i < instances.length; i++) {
					String instance = instances[i];
					String[] tuple = instance.split("\\s+");
					String user = tuple[0];
					String publisher = tuple[2];

					Integer userId = users.get(tuple[0]);

					if (userId == null) {
						userId = users.size() + 1;
						users.put(user, userId);
					}

					Integer publisherId = publishers.get(publisher);
					if (publisherId == null) {
						publisherId = publishers.size() + 1;
						publishers.put(publisher, publisherId);
					}

					if (i == instances.length - 1) {
						pw.println("user_" + userId + " " + tuple[1] + " pub_" + publisherId + " " + tuple[3]);
					} else {
						pw.print("user_" + userId + " " + tuple[1] + " pub_" + publisherId + " " + tuple[3] + ",");
					}
				}
			}
		}
		br.close();
		pw.close();

	}
	
	public static void read(String fileName) throws IOException {
		String line = null;

		BufferedReader br = FileUtil.createReader(fileName);

		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				String[] instances = line.split(",");
				for (int i = 0; i < instances.length; i++) {
					String instance = instances[i];
					String[] tuple = instance.split("\\s+");
					String user = tuple[0];

					Integer userId = users.get(tuple[0]);

					if (userId == null) {
						userId = users.size() + 1;
						users.put(user, userId);
					}
					String output = tuple[1] + " " + tuple[3];
					HashSet<String> set = userTuples.get(userId);
					if (set==null) {
						set = new HashSet<String>();
					}
					set.add(output);
					userTuples.put(userId, set);
				}
			}
		}
		br.close();
	}
	
	public static void write(String file) throws IOException {
		int num = 0;
		PrintWriter pw = FileUtil.createWriter(file);
		for (int i=1; i<users.size()+1; i++) {
			String user = "user_"+ i;
			HashSet<String> set = userTuples.get(i);
			pw.print(user);
			for (String tuple: set) {
				pw.print("," + tuple);
				if (++num%10000==0) {
					System.out.println(num);
				}
			}
			pw.println();
		}
		System.out.println(num);
		pw.close();
	}
	

}
