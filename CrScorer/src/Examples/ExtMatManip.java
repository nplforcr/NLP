package Examples;

import Jama.Matrix;

public class ExtMatManip {

	// Octave: diag(m)
	public static Matrix diag (Matrix m) {
		int r = m.getRowDimension();
		int c = m.getColumnDimension();
		if (c==1) {
			Matrix ret = new Matrix(r,r);
			for (int i = 0; i < r; i++)
				ret.set(i, i, m.get(i,0));
			return ret;
		} else {
			Matrix ret = new Matrix(r,1);
			for (int i = 0; i < r; i++)
				ret.set(i, 0, m.get(i,i));
			return ret;
		}
	}

	// Octave: sum(m,2)
	public static Matrix getRowSum (Matrix m) {
		Matrix ret = new Matrix(m.getRowDimension(),1);
		int[] c = new int[1];
		for (int i = 0; i < m.getColumnDimension(); i++) {
			c[0] = i;
			ret.plusEquals(m.getMatrix(0, ret.getRowDimension()-1, c));
		}
		return ret;
	}

	// Octave: sum(m,1)
	public static Matrix getColumnSum (Matrix m) {
		return getRowSum(m.transpose()).transpose();
	}

	// Octave: repmat(m,n,1)
	public static Matrix repeatRow (Matrix m, int n) {
		Matrix ret = new Matrix(n,m.getColumnDimension());
		int[] r = new int[1];
		for (int i = 0; i < ret.getRowDimension(); i ++) {
			r[0] = i;
			ret.setMatrix(r, 0, m.getColumnDimension()-1, m);
		}
		return ret;
	}

	// Octave: repmat(m,1,n)
	public static Matrix repeatColumn (Matrix m, int n) {
		return repeatRow(m.transpose(), n).transpose();
	}

	// Octave: sum(m>0,2)
	public static Matrix numOfPositiveColumns (Matrix m) {
		Matrix ret = m.copy();
		for (int i = 0; i < ret.getRowDimension(); i++)
			for (int j = 0; j < ret.getColumnDimension(); j++)
				if (ret.get(i, j)>0) ret.set(i, j, 1.0); 
		return getRowSum(ret);
	}
}
