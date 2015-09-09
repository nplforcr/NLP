package Examples;

import java.util.HashSet;
import java.util.Vector;
import Jama.Matrix;
import km.*;
import helpers.*;

public class MatScore {

	public static double MUC_recall (Matrix m) {
		Matrix rowSum = ExtMatManip.getRowSum(m);
		Matrix partSum = ExtMatManip.numOfPositiveColumns(m);
		double nom = ExtMatManip.getColumnSum(rowSum.minus(partSum)).get(0,0);
		double denom = ExtMatManip.getColumnSum(rowSum).get(0,0)-rowSum.getRowDimension();
		return nom / denom;
	}

	public static double MUC (Matrix m) {
		double r = MUC_recall(m);
		double p = MUC_recall(m.transpose());
		return 2*r*p / (r+p);
	}

	public static double B3_recall (Matrix m, B3Type flavor) {
		Matrix rowSum = ExtMatManip.getRowSum(m); // sum(m,2) i.e., |S_i|
//		The following line works, but hardly readable
//		Matrix mat = MyJama.getRowSum(MyJama.repeatColumn(rowSum, m.getColumnDimension()).minus(m).arrayTimes(m)). // step1 = (repmat(rowSum,1,columns(m)) - m) .* m
//		             arrayRightDivide(rowSum.arrayTimes(rowSum)). // step2 = step1 ./ (rowSum .^ 2)
//		             uminus().plus(new Matrix(m.getRowDimension(),1,1.0)); // step3 = 1 - step2
		int r = m.getRowDimension();
		Matrix rowSumRep = ExtMatManip.repeatColumn(rowSum, m.getColumnDimension()); // repmat(rowSum,1,columns(m))
		Matrix nom = m.times(rowSumRep.minus(m).transpose()); // m * (rowSumRep-m)'
		Matrix denom = ExtMatManip.diag(rowSum.arrayTimes(rowSum)).inverse().uminus(); // -inv(diag(rowSum .^ 2))
		Matrix mat = ExtMatManip.diag(nom.times(denom).plus(Matrix.identity(r,r))); // diag(nom * denom)

		Matrix weight = flavor==B3Type.CLASS ? new Matrix(mat.getRowDimension(), 1, 1) :
		                flavor==B3Type.ENTITY ? ExtMatManip.getRowSum(m) :
		                null; // this will throw an Exception
		return weight.transpose().times(mat).get(0, 0) / ExtMatManip.getColumnSum(weight).get(0, 0);
	}

	public static double B3 (Matrix m, B3Type flavor) {
		double r = B3_recall(m, flavor);
		double p = B3_recall(m.transpose(), flavor);
		return 2*r*p / (r+p);
	}

	public static double CEAF (Matrix phi, CEAFType flavor) {
		int d = phi.getRowDimension();
		if (phi.getColumnDimension()>d) d = phi.getColumnDimension();
		float[][] fm = new float[d][d];
		for (int i = 0; i < phi.getRowDimension(); i++)
			for (int j = 0; j < phi.getColumnDimension(); j++)
				fm[i][j] = (float) -phi.get(i,j);

		AssignmentProblem ap = new AssignmentProblem(fm);
		int[][] g = ap.solve(new HungarianAlgorithm());

		double phiOpt = 0;
		for (int i = 0; i < g.length; i++)
			phiOpt -= fm[g[i][0]][g[i][1]];

		if (flavor == CEAFType.MENTION)
			return phiOpt / ExtMatManip.getColumnSum(ExtMatManip.getRowSum(phi)).get(0,0);
		else if (flavor == CEAFType.ENTITY)
			return 2*phiOpt / (phi.getRowDimension()+phi.getColumnDimension());
		else return -1;
	}

	private static int[] grow (int[] a, int toSize, int numClasses) {
		int[] temp = new int[toSize];
		int i = 0;
		for (; i < a.length; i++)
			temp[i] = a[i];
		for (; i < toSize; i++)
			temp[i] = numClasses++;
		return temp;
	}

	public static Matrix calcPhi (ParentPtrTree key, ParentPtrTree response, MatType flavor) {
		int[] k = new int[key.getSize()];
		int[] r = new int[response.getSize()];
		int kk = key.equivCls(k);
		int rr = response.equivCls(r);
		if (k.length > r.length) {
			r = grow(r, k.length, rr);
			rr = r[r.length-1] + 1; // the last element of the the grown array will always be the largest numbered class ID
		}
		else if (k.length < r.length) {
			k = grow(k, r.length, kk);
			kk = k[k.length-1] + 1;
		}

		double[][] tab = new double[kk][rr];
		for (int i = 0; i < k.length; i++)
			tab[k[i]][r[i]]++;
		Matrix m = new Matrix(tab);
		if (flavor == MatType.ABSOLUTE) return m;
		else if (flavor == MatType.RELATIVE) {
			Matrix sumK = ExtMatManip.repeatColumn(ExtMatManip.getRowSum(m), rr);
			Matrix sumR = ExtMatManip.repeatRow(ExtMatManip.getColumnSum(m), kk);
			return m.times(2).arrayRightDivide(sumK.plus(sumR));
		} else return null;
	}

	public static Matrix partition (ParentPtrTree key, ParentPtrTree response) {
		return calcPhi(key, response, MatType.ABSOLUTE);
	}

	public static double Kappa (Matrix m) {
		Matrix rowSum = ExtMatManip.getRowSum(m);
		Matrix partSum = ExtMatManip.numOfPositiveColumns(m);
		double nom = ExtMatManip.getColumnSum(rowSum.minus(partSum)).get(0,0);
		double denom = ExtMatManip.getColumnSum(rowSum).get(0,0)-rowSum.getRowDimension();
		return nom / denom;
	}

	private static double KAlpha_dist (Vector<Integer> a, Vector<Integer> b, KAlphaType flavor) {
		flavor = KAlphaType.RAW;
		HashSet<Integer> a1 = new HashSet<Integer>();
		HashSet<Integer> b1 = new HashSet<Integer>();
		a1.addAll(a);
		b1.addAll(b);
		if (a1.containsAll(b1))
			if (a1.size() == b1.size()) return 0; // a==b
			else return flavor==KAlphaType.RAW ? 1d/3 : 1d/3*(1-(b1.size()-1)/(a1.size()-1)); // b is proper subset of a
		else {
			HashSet<Integer> ints = new HashSet<Integer>();
			ints.addAll(a1);
			if (!ints.retainAll(b1)) return flavor==KAlphaType.RAW ? 2d/3 : 2d/3*(1-(a1.size()-1)/(b1.size()-1)); // a is proper subset of b
			else if (ints.size() > 0) return flavor==KAlphaType.RAW ? 1d/3 : 1d/3*(1-(ints.size()-1)/(a1.size()+b1.size()-ints.size()-1)); // intersection
			else return 1; // disjoint
		}
	}

	public static double KAlpha (ParentPtrTree... ppt) {
		// find the number of mentions
		int n = 0;
		for (ParentPtrTree t : ppt)
			if (t.getSize() > n) n = t.getSize();

		Vector<Vector<Integer>> s = new Vector<Vector<Integer>>(); //global list of unique eqvCls
		int[][] eqvCls = new int[ppt.length][n];
		for (int i = 0; i < ppt.length; i++) {
			int sz = ppt[i].getSize();
			int num = ppt[i].equivCls(eqvCls[i]);
			// every 'mention' that does not appear in output gets a eqvCls by itself
			for (int j = sz; j < n; j++)
				eqvCls[i][j] = num++;
			Vector<Vector<Integer>> v = new Vector<Vector<Integer>>(num);
			for (int j = 0; j < num; j++)
				v.add(new Vector<Integer>());
			// record what's in each eqvCls (in this ppt)
			for (int j = 0; j < n; j++)
				v.get(eqvCls[i][j]).add(j);
			// add eqvCls in this ppt to the global list, if not already there
			for (int j = 0; j < num; j++) {
				Vector<Integer> vec = v.get(j);
				int clsInd = s.indexOf(vec);
				if (!s.contains(vec)) {
					clsInd = s.size();
					s.add(vec);
				}
				// update the eqvCls number so it's globally unique
				for (int k = 0; k < vec.size(); k++)
					eqvCls[i][vec.get(k)] = clsInd;
			}
		}

		double[][] o = new double[s.size()][s.size()]; // coincidence matrix
		for (int i = 0; i < n; i++)
			for (int j = 0; j < ppt.length; j++)
				for (int k = j+1; k < ppt.length; k++) {
					o[eqvCls[j][i]][eqvCls[k][i]] += 1.0 / (ppt.length-1);
					o[eqvCls[k][i]][eqvCls[j][i]] += 1.0 / (ppt.length-1);
				}

		double[][] d = new double[s.size()][s.size()]; // distance matrix
		for (int i = 0; i < s.size(); i++)
			for (int j = i+1; j < s.size(); j++) {
				d[i][j] = KAlpha_dist(s.get(i), s.get(j), KAlphaType.RAW);
				d[j][i] = d[i][j];
			}

		Matrix O = new Matrix(o);
		Matrix Delta = new Matrix(d);
		Matrix T = ExtMatManip.getRowSum(O);

		double Do = O.times(Delta).trace();
		return Do==0 ? 1 : 1 - (n * ppt.length - 1) * (Do / T.times(T.transpose()).times(Delta).trace());
	}
}
