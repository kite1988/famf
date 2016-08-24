package data;

import java.io.Serializable;

public class RatingTuple implements Serializable {
	
	private static final long serialVersionUID = 369452183329260661L;
	public int user;
	public int posId;
	public int negId;

	public RatingTuple(int user, int posId, int negId) {
		this.user = user;
		this.posId = posId;
		this.negId = negId;
	}

	public String toString() {
		return user + " " + posId + " " + negId;
	}

	public RatingTuple(RatingTuple tuple) {
		this.user = tuple.user;
		this.posId = tuple.posId;
		this.negId = tuple.negId;
	}
}
