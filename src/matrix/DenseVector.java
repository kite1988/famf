// Copyright (C) 2014 Guibing Guo
//
// This file is part of LibRec.
//
// LibRec is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// LibRec is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with LibRec. If not, see <http://www.gnu.org/licenses/>.
//

package matrix;

import happy.coding.math.Randoms;

import java.io.Serializable;

/**
 * Data Structure: dense vector
 * 
 * @author guoguibing
 * 
 */
public class DenseVector implements Serializable {

	private static final long serialVersionUID = -2930574547913792430L;

	protected int size;
	public double[] data;

	/**
	 * Construct a dense vector with a specific size
	 * 
	 * @param size
	 *            the size of vector
	 */
	public DenseVector(int size) {
		this.size = size;
		data = new double[size];
	}

	/**
	 * Construct a dense vector by deeply copying data from a given array
	 */
	public DenseVector(double[] array) {
		this(array, true);
	}

	/**
	 * Construct a dense vector by copying data from a given array
	 * 
	 * @param array
	 *            a given data array
	 * @param deep
	 *            whether to deep copy array data
	 */
	public DenseVector(double[] array, boolean deep) {
		this.size = array.length;
		if (deep) {
			data = new double[array.length];
			for (int i = 0; i < size; i++)
				data[i] = array[i];
		} else {
			data = array;
		}
	}

	// override the values
	public DenseVector assign(DenseVector vec) {
		double[] array = vec.data;
		if (data == null) {
			data = new double[array.length];
			size = array.length;
		}

		for (int i = 0; i < size; i++)
			data[i] = array[i];
		return this;
	}

	public void reset() {
		for (int i = 0; i < size; i++) {
			data[i] = 0;
		}
	}

	/**
	 * Construct a dense vector by deeply copying data from a given vector
	 */
	public DenseVector(DenseVector vec) {
		this(vec.data);
	}

	/**
	 * Make a deep copy of current vector
	 */
	public DenseVector clone() {
		return new DenseVector(this);
	}

	/**
	 * Initialize a dense vector with Gaussian values
	 */
	public void init(double mean, double sigma) {
		for (int i = 0; i < size; i++)
			data[i] = Randoms.gaussian(mean, sigma);
	}

	/**
	 * Initialize a dense vector with uniform values in (0, 1)
	 */
	public void init() {
		for (int i = 0; i < size; i++)
			data[i] = Randoms.uniform();
	}

	/**
	 * Initialize a dense vector with uniform values in (0, range)
	 */
	public void init(double range) {
		for (int i = 0; i < size; i++)
			data[i] = Randoms.uniform(0, range);
	}
	
	

	/**
	 * Get a value at entry [index]
	 */
	public double get(int idx) {
		return data[idx];
	}

	/**
	 * @return vector's data
	 */
	public double[] getData() {
		return data;
	}

	/**
	 * @return mean of current vector
	 */
	public double mean() {
		double sum = 0.0f;
		for (double d : data) {
			sum += d;
		}
		return sum / data.length;
		// return Stats.mean(data);
	}

	/**
	 * Set a value to entry [index]
	 */
	public void set(int idx, double val) {
		data[idx] = val;
	}

	/**
	 * Add a value to entry [index]
	 */
	public void add(int idx, double val) {
		data[idx] += val;
	}

	/**
	 * Substract a value from entry [index]
	 */
	public void minus(int idx, double val) {
		data[idx] -= val;
	}

	/**
	 * @return a dense vector by adding a value to all entries of current vector
	 */
	public DenseVector add(double val) {
		DenseVector result = new DenseVector(size);

		for (int i = 0; i < size; i++)
			result.data[i] = this.data[i] + val;

		return result;
	}

	public DenseVector add(double val, boolean deep) {
		if (deep) {
			return add(val);
		} else {
			for (int i = 0; i < size; i++)
				this.data[i] += val;

			return this;
		}
	}

	/**
	 * @return a dense vector by substructing a value from all entries of
	 *         current vector
	 */
	public DenseVector minus(double val) {

		DenseVector result = new DenseVector(size);

		for (int i = 0; i < size; i++)
			result.data[i] = this.data[i] - val;

		return result;
	}

	public DenseVector minus(double val, boolean deep) {
		if (deep) {
			return minus(val);
		} else {
			for (int i = 0; i < size; i++)
				this.data[i] -= -val;
			return this;
		}
	}

	/**
	 * @return a dense vector by scaling a value to all entries of current
	 *         vector
	 */
	public DenseVector scale(double val) {

		DenseVector result = new DenseVector(size);
		for (int i = 0; i < size; i++)
			result.data[i] = this.data[i] * val;

		return result;
	}

	public DenseVector scale(double val, boolean deep) {
		if (deep) {
			return scale(val);
		} else {
			for (int i = 0; i < size; i++)
				this.data[i] *= val;
			return this;
		}
	}

	public DenseVector scaleAssign(DenseVector vec, double val) {
		for (int i = 0; i < size; i++)
			this.data[i] = vec.get(i) * val;
		return this;
	}

	/**
	 * Do vector operation: {@code a + b}
	 * 
	 * @return a dense vector with results of {@code c = a + b}
	 */
	public DenseVector add(DenseVector vec) {
		assert size == vec.size;

		DenseVector result = new DenseVector(size);
		for (int i = 0; i < result.size; i++)
			result.data[i] = this.data[i] + vec.data[i];

		return result;
	}

	public DenseVector add(DenseVector vec, boolean deep) {
		if (deep) {
			return add(vec);
		} else {
			assert size == vec.size;
			for (int i = 0; i < vec.size; i++)
				this.data[i] += vec.data[i];

			return this;
		}
	}

	public DenseVector add(DenseVector vec, DenseVector vec2) {
		for (int i = 0; i < vec.size; i++)
			this.data[i] += vec.data[i] + vec2.data[i];

		return this;
	}

	public DenseVector addAssign(DenseVector vec, DenseVector vec2) {
		for (int i = 0; i < vec.size; i++)
			this.data[i] = vec.data[i] + vec2.data[i];

		return this;
	}

	/**
	 * Do vector operation: {@code a - b}
	 * 
	 * @return a dense vector with results of {@code c = a - b}
	 */
	public DenseVector minus(DenseVector vec) {
		assert size == vec.size;

		DenseVector result = new DenseVector(size);
		for (int i = 0; i < vec.size; i++)
			result.data[i] = this.data[i] - vec.data[i];

		return result;
	}

	public DenseVector minus(DenseVector vec, boolean deep) {
		if (deep) {
			return minus(vec);
		} else {
			assert size == vec.size;
			for (int i = 0; i < vec.size; i++)
				this.data[i] -= vec.data[i];

			return this;
		}
	}

	// K*1 (vector) -> 1*K (matrix)
	public DenseMatrix vectorToTransMatrix() {
		int length = data.length;
		DenseMatrix matrix = new DenseMatrix(1, length);
		for (int i = 0; i < length; i++)
			matrix.data[0][i] = this.data[i];
		return matrix;
	}

	// K*1 (vector) -> K*1 (matrix)
	public DenseMatrix vectorToMatrix() {
		int length = data.length;
		DenseMatrix matrix = new DenseMatrix(length, 1);
		for (int i = 0; i < length; i++)
			matrix.data[i][0] = this.data[i];
		return matrix;
	}

	// new matrix(this-vec)^T
	public DenseMatrix minusAsTransMatrix(DenseVector vec) {
		DenseMatrix matrix = new DenseMatrix(1, vec.data.length);
		for (int i = 0; i < vec.size; i++)
			matrix.data[0][i] = this.data[i] - vec.data[i];
		return matrix;
	}

	// new matrix(this-vec)
	public DenseMatrix minusAsMatrix(DenseVector vec) {
		DenseMatrix matrix = new DenseMatrix(vec.data.length, 1);
		for (int i = 0; i < vec.size; i++)
			matrix.data[i][0] = this.data[i] - vec.data[i];
		return matrix;
	}

	public DenseVector minusAssign(DenseVector vec, DenseVector vec2) {
		for (int i = 0; i < size; i++)
			this.data[i] = vec.data[i] - vec2.data[i];
		return this;
	}

	// this.vec - vec*d
	public DenseVector minusScale(DenseVector vec, double d) {
		assert size == vec.size;
		for (int i = 0; i < vec.size; i++)
			this.data[i] -= vec.data[i] * d;

		return this;
	}

	public DenseVector minusScale(DenseVector vec, double d, boolean deep) {
		assert size == vec.size;

		if (deep) {
			DenseVector result = new DenseVector(size);
			for (int i = 0; i < vec.size; i++)
				result.data[i] -= vec.data[i] * d;
			return result;
		} else {
			return minusScale(vec, d);
		}
	}

	// this.vec + vec*d
	public DenseVector addScale(DenseVector vec, double d) {
		assert size == vec.size;
		for (int i = 0; i < vec.size; i++)
			this.data[i] += vec.data[i] * d;

		return this;
	}

	public DenseVector addScale(DenseVector vec, double d, boolean deep) {
		assert size == vec.size;

		if (deep) {
			DenseVector result = new DenseVector(size);
			for (int i = 0; i < vec.size; i++)
				result.data[i] += vec.data[i] * d;
			return result;
		} else {
			return addScale(vec, d);
		}
	}

	// this*d1 + vec*d2
	public DenseVector scaleAddScale(double d1, DenseVector vec, double d2) {
		assert size == vec.size;
		for (int i = 0; i < vec.size; i++)
			this.data[i] = this.data[i] * d1 + vec.data[i] * d2;

		return this;
	}

	// this*d1 + vec*d2
	public DenseVector scaleAddScale(double d1, DenseVector vec, double d2,
			boolean deep) {
		assert size == vec.size;
		if (deep) {
			DenseVector result = new DenseVector(size);
			for (int i = 0; i < vec.size; i++)
				result.data[i] = this.data[i] * d1 + vec.data[i] * d2;
			return result;
		} else {
			return scaleAddScale(d1, vec, d2);
		}
	}

	/**
	 * Do vector operation: {@code a^t * b}
	 * 
	 * @return the inner product of two vectors
	 */
	public double inner(DenseVector vec) {
		assert size == vec.size;

		double result = 0;
		for (int i = 0; i < vec.size; i++)
			result += get(i) * vec.get(i);

		return result;
	}

	// this* (vec-vec2)
	public double innerMinus(DenseVector vec, DenseVector vec2) {
		assert size == vec.size;

		double result = 0;
		for (int i = 0; i < vec.size; i++)
			result += get(i) * (vec.get(i) - vec2.get(i));

		return result;
	}

	public double innerAddScale(DenseVector vec1, DenseVector vec2, double d) {
		double result = 0;
		for (int i = 0; i < vec1.size; i++) {
			result += this.data[i] * (vec1.get(i) + vec2.get(i) * d);
		}
		return result;
	}

	/**
	 * Do vector operation: {@code a^t * b}
	 * 
	 * @return the inner product of two vectors
	 */
	public double inner(SparseVector vec) {
		double result = 0;
		for (int j : vec.getIndex())
			result += vec.get(j) * get(j);

		return result;
	}

	/**
	 * Do vector operation: {@code a * b^t}
	 * 
	 * @return the outer product of two vectors
	 */
	public DenseMatrix outer(DenseVector vec) {
		DenseMatrix mat = new DenseMatrix(this.size, vec.size);

		for (int i = 0; i < mat.numRows; i++)
			for (int j = 0; j < mat.numColumns; j++)
				mat.set(i, j, get(i) * vec.get(j));

		return mat;
	}

	@Override
	public String toString() {
		// return Strings.toString(data);

		StringBuilder sb = new StringBuilder();
		sb.append(size + ": ");
		for (int i = 0; i < size; i++) {
			sb.append(data[i] + " ");
		}
		return sb.toString().trim();
	}

	/**
	 * @return the matrix norm-2
	 */
	public double normSquare() {
		double res = 0;
		for (int i = 0; i < size; i++)
			res += data[i] * data[i];
		return res;
	}

	public void averageWordFactorAssign(int[] words, short[] freq, int total,
			DenseMatrix Q) {
		this.reset();
		int length = words.length;
		if (words == null || length == 0) {
			return;
		} else {
			DenseVector s = new DenseVector(size);
			for (int i = 0; i < length; i++) {
				int w = words[i];
				short f = freq[i];
				s.assign(Q.row(w, false));
				if (f > 1) {
					s = s.scale(f, false);
				}
				this.add(s, false);
			}
			this.scale(1.0 / total, false);
		}
	}

	public void averageWordFactorAssign(short[] words, short[] freq, int total,
			DenseMatrix Q) {
		this.reset();
		int length = words.length;
		if (words == null || length == 0) {
			return;
		} else {
			DenseVector s = new DenseVector(this.size);
			for (int i = 0; i < length; i++) {
				int w = words[i];
				short f = freq[i];
				s.assign(Q.row(w, false));
				if (f > 1) {
					s = s.scale(f, false);
				}
				this.add(s, false);
			}
			this.scale(1.0 / total, false);
		}
	}

	// with projection
	public static DenseVector averageWordFactor(int[] words, short[] freq,
			int total, DenseMatrix Q, DenseMatrix F, int factors) {
		DenseVector sum = new DenseVector(factors);
		int length = words.length;
		if (words == null || length == 0) {
			return sum;
		} else {
			for (int i = 0; i < length; i++) {
				int w = words[i];
				short f = freq[i];
				DenseVector s = Q.row(w, false);
				DenseVector proj = F.mult(s);
				if (f > 1) {
					proj = proj.scale(f, false);
				}
				sum = sum.add(proj, false);
				/*
				 * if (check("sum", sum)) { System.out.println("proj " + proj);
				 * System.out.println("sum " + sum); }
				 */

			}
			return sum.scale(1.0 / total, false);
		}
	}

	// with projection
	public void averageWordFactor(short[] words, short[] freq, DenseMatrix Q,
			int total, DenseMatrix F, int factors) {
		this.reset();
		if (words == null || words.length == 0) {
			return;
		} else {
			int length = words.length;
			DenseVector proj = new DenseVector(factors);
			for (int i = 0; i < length; i++) {
				short w = words[i];
				short f = freq[i];
				DenseVector s = Q.row(w, false);
				proj.assign(F.mult(s));
				if (f > 1) {
					proj = proj.scale(f, false);
				}
				this.add(proj, false);
			}
			this.scale(1.0 / total, false);
		}
	}
}
