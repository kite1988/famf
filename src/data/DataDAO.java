package data;

import happy.coding.io.FileIO;
import happy.coding.io.Logs;
import matrix.SparseMatrix;
import util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

public class DataDAO implements Serializable {

	private static final long serialVersionUID = -324313269423744273L;

	private boolean train = true;
	private String ratingFile, textPath, visualPath;
	private boolean replaceMissing;
	private int numTextWords;
	// store data as {user/item rate} matrix
	public SparseMatrix ratingMatrix;
	public ArrayList<RatingTuple> ratingTuple;

	public int numRates;
	public Tweet[] tweets;

	// user/item {raw id, inner id} map
	private BiMap<String, Integer> userIds, itemIds;
	// inverse views of userIds, itemIds
	private BiMap<Integer, String> idUsers, idItems;


	// for training
	public DataDAO() {
		this.train = true;
		userIds = HashBiMap.create();
		itemIds = HashBiMap.create();
		ratingTuple = new ArrayList<RatingTuple>();
	}

	// for testing
	public DataDAO(BiMap<String, Integer> trainUserIds) {
		this.train = false;
		this.itemIds = HashBiMap.create();
		userIds = trainUserIds;
	}

	public SparseMatrix readData(String ratingFile, String textFile, 
			String visualFile, boolean replace, int numTextWords) throws Exception {
		this.ratingFile = ratingFile;
		this.replaceMissing = replace;
		this.numTextWords = numTextWords;
		
		if (train) {
			readRatingDataTrain(ratingFile);
		} else {
			readRatingDataTest(ratingFile);
		}

		if (textFile != null) {
			readText(textFile);
		}
		if (visualFile != null) {
			readVisual(visualFile);
		}
		return ratingMatrix;
	}

	private int getId(BiMap<String, Integer> map, String key) {
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			if (train) {
				int size = map.size();
				map.put(key, size);
				return size;
			} else {
				return -1;
			}
		}
	}

	/**
	 * Read rating data from the data file.
	 * 
	 * Each line is: user_id item_id 1, user_id item_id 0, user_id item_id 0, ...
	 */
	public void readRatingDataTrain(String path) throws Exception {
		System.out.println("Loading rating from " + path);

		BufferedReader br = FileUtil.createReader(path);
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] tuples = line.trim().split(",");

			boolean first = true;
			int posUser = -1, posItem = -1;
			for (String data : tuples) {
				String[] tuple = data.trim().split(" ");
				String user = tuple[0].trim();
				String item = tuple[1].trim();
				Float rate = Float.valueOf(tuple[2].trim());
				if (rate == 0) {
					rate = -1.0f;
				}

				// inner id starting from 0
				int row = userIds.containsKey(user) ? userIds.get(user) : userIds.size();
				int col = itemIds.containsKey(item) ? itemIds.get(item) : itemIds.size();

				userIds.put(user, row);
				itemIds.put(item, col);

				if (first) {
					posUser = row;
					posItem = col;
					first = false;
				} else {
					RatingTuple t = new RatingTuple(posUser, posItem, col);
					ratingTuple.add(t);
				}
				numRates++;
			}
		}
		br.close();

		int numRows = numUsers(), numCols = numItems();
		Logs.debug("Dataset: {Users, {}} = {{}, {}, {}}", ("Items, Ratings"), numRows, numCols, numRates);
	}

	/**
	 * Read data from the data file. Note that we didn't take care of the
	 * duplicated lines.
	 * 
	 * Each line is: user_id item_id publisher_id 1, user_id item_id
	 * publisher_id 0, ...
	 */
	public void readRatingDataTest(String path) throws Exception {
		System.out.println("Loading rating from " + path);
		// Table {row-id, col-id, rate}
		Table<Integer, Integer, Float> dataTable = HashBasedTable.create();

		// Map {col-id, multiple row-id}: used to fast build rate matrix
		Multimap<Integer, Integer> colMap = HashMultimap.create();

		BufferedReader br = FileUtil.createReader(path);
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] tuples = line.trim().split(",");

			for (String data : tuples) {
				String[] tuple = data.trim().split(" ");
				String user = tuple[0].trim();
				String item = tuple[1].trim();
				Float rate = Float.valueOf(tuple[2].trim());
				if (rate == 0) {
					rate = -1.0f;
				}

				int row = getId(userIds, user);
				int col = itemIds.containsKey(item) ? itemIds.get(item) : itemIds.size();

				itemIds.put(item, col);
				dataTable.put(row, col, rate);
				colMap.put(col, row);
			}
		}
		br.close();

		numRates = dataTable.size();
		int numRows = numUsers(), numCols = numItems();

		Logs.debug("Dataset: {Users, {}} = {{}, {}, {}, {}}", ("Items, Ratings"), numRows, numCols, numRates);

		// build rating matrix
		ratingMatrix = new SparseMatrix(numRows, numCols, dataTable, colMap);
		dataTable = null;
	}

	
	/*
	 * Each line is: tweet_id, w1 w2 w3 ...
	 */
	public void readText(String path) throws IOException {
		tweets = new Tweet[numItems()];
		
		System.out.println("Loading text from " + path);
		BufferedReader br = FileIO.getReader(path);
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				String[] items = line.split(",");
				String id = items[0];
				Integer tid = itemIds.get(id);
				if (tid != null) {
					Tweet t = new Tweet(items[0], tid);
					tweets[tid] = t;

					if (items.length == 2) {
						t.setText(items[1].trim().split(" "));
					} else {
						if (replaceMissing) {
							t.setText(numTextWords - 1);
						} else {
							t.setText(new String[0]);
						}
					}
				}
			}
		}
		br.close();
	}

	/*
	 * Each line is: tweet_id v1 v2 v3 ...
	 */
	public void readVisual(String path) throws IOException {
		System.out.println("Loading visual from " + path);

		BufferedReader br = FileIO.getReader(path);
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				String[] items = line.trim().split(",");
				String id = items[0];
				Integer tid = itemIds.get(id);
				if (tid != null) {
					Tweet t = tweets[tid];
					t.setVisual(items[1].trim().split("\\s+"));
				}
			}
		}
		br.close();
	}

	
	public int numUsers() {
		return userIds.size();
	}

	public int numItems() {
		return itemIds.size();
	}

	public int numRates() {
		return numRates;
	}

	public int getUserId(String rawId) {
		return userIds.get(rawId);
	}

	public String getUserId(int innerId) {
		if (idUsers == null)
			idUsers = userIds.inverse();

		return idUsers.get(innerId);
	}

	public int getItemId(String rawId) {
		return itemIds.get(rawId);
	}

	public String getItemId(int innerId) {
		if (idItems == null) {
			idItems = itemIds.inverse();
		}

		return idItems.get(innerId);
	}

	public BiMap<String, Integer> getUserIds() {
		return userIds;
	}

	public BiMap<String, Integer> getItemIds() {
		return itemIds;
	}

	public String getRatingPath() {
		return ratingFile;
	}

	public BiMap<Integer, String> getIdUsers() {
		if (idUsers == null)
			idUsers = userIds.inverse();
		return idUsers;
	}

	public BiMap<Integer, String> getIdItems() {
		if (idItems == null)
			idItems = itemIds.inverse();
		return idItems;
	}

	public String getTextPath() {
		return textPath;
	}

	public String getVisualPath() {
		return visualPath;
	}
}