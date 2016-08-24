package data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

public class Tweet implements Serializable {

	private static final long serialVersionUID = -476493665475018739L;
	public String tid = null; // twitter id
	public int id = -1; // internal id

	public int[] textUnique = null;
	public short[] visualUnique = null;
	public short[] visualFreq = null, textFreq = null;

	public int textLength = 0;
	public int visualLength = 0;


	public Tweet() {

	}

	public Tweet(Tweet t) {

	}

	public Tweet(String tid, int id, String[] textStr, String[] visualStr) {
		this(tid, id);
		setText(textStr);
		setVisual(visualStr);
	}

	public Tweet(String tid, int id) {
		this.tid = tid;
		this.id = id;
	}

	public void setText(String[] textStr) {
		HashMap<Integer, Short> textMap = new HashMap<Integer, Short>();
		textLength = textStr.length;

		for (String w : textStr) {
			int wid = Integer.parseInt(w);
			Short count = textMap.get(wid);
			if (count == null) {
				count = 0;
			}
			count++;
			textMap.put(wid, count);
		}

		textUnique = new int[textMap.size()];
		textFreq = new short[textMap.size()];
		int idx = 0;
		for (Entry<Integer, Short> entry : textMap.entrySet()) {
			textUnique[idx] = entry.getKey();
			textFreq[idx] = entry.getValue();
			idx++;
		}
	}

	// for missing value
	public void setText(int wid) {
		HashMap<Integer, Short> textMap = new HashMap<Integer, Short>();
		textLength = 1;

		textMap.put(Integer.valueOf(wid), Short.valueOf("1"));

		textUnique = new int[textMap.size()];
		textFreq = new short[textMap.size()];
		int idx = 0;
		for (Entry<Integer, Short> entry : textMap.entrySet()) {
			textUnique[idx] = entry.getKey();
			textFreq[idx] = entry.getValue();
			idx++;
		}
	}

	public void setVisual(String[] textStr) {
		HashMap<Short, Short> visualMap = new HashMap<Short, Short>();
		visualLength = textStr.length;

		for (String w : textStr) {
			short wid = Short.parseShort(w);
			Short count = visualMap.get(wid);
			if (count == null) {
				count = 0;
			}
			count++;
			visualMap.put(wid, count);
		}

		visualUnique = new short[visualMap.size()];
		visualFreq = new short[visualMap.size()];
		int idx = 0;
		for (Entry<Short, Short> entry : visualMap.entrySet()) {
			visualUnique[idx] = entry.getKey();
			visualFreq[idx] = entry.getValue();
			idx++;
		}
	}
}
