package helpers;

public class SpanAlignment {

	int[] id1;
	int[] id2;

	public SpanAlignment (Span[] s1, Span[] s2) {
		double[][] scores;
		int[][] track; // 0 = left, 1 = upper left, 2 = up
		int l1 = s1.length;
		int l2 = s2.length;
		scores = new double[l1+1][l2+1];
		track = new int[l1+1][l2+1];
		id1 = new int[l1];
		id2 = new int[l2];
		scores[0][0] = 0.0;
		track[0][0] = -1;

		for (int i = 1; i <= l1; i++) {
			scores[i][0] = s1[i-1].gap() * i;
			track[i][0] = 2;
		}
		for (int i = 1; i <= l2; i++) {
			scores[0][i] = s2[i-1].gap() * i;
			track[0][i] = 0;
		}

		for (int i = 1; i <= l1; i++)
			for (int j = 1; j <=l2; j++) {
				double match = scores[i-1][j-1] + Span.score(s1[i-1], s2[j-1]);
				double gap1 = scores[i][j-1] + s1[i-1].gap();
				double gap2 = scores[i-1][j] + s2[j-1].gap();
				if (match>=gap1 && match>=gap2) {
					scores[i][j] = match;
					track[i][j] = 1;
				}
				else if (gap1>=match && gap1>=gap2) {
					scores[i][j] = gap1;
					track[i][j] = 0;
				}
				else {
					scores[i][j] = gap2;
					track[i][j] = 2;
				}
			}

		int i = l1;
		int j = l2;
		int id = 0;
//		StringBuffer sb1 = new StringBuffer();
//		StringBuffer sb2 = new StringBuffer();
		while (i>0 || j>0) {
			int dir = track[i][j];
			switch (dir) {
			case 0: id2[--j] = id++; break;//sb1.insert(0, "_"); sb2.insert(0,s2[j]); break;
			case 1: id1[--i] = id; id2[--j] = id++; break;//sb1.insert(0,s1[i]); sb2.insert(0,s2[j]); break;
			case 2: id1[--i] = id++; //sb1.insert(0,s1[i]); sb2.insert(0,"_");
			}
		}
		for (int k = 0; k < l1; k++)
			id1[k] = id - id1[k];
		for (int k = 0; k < l2; k++)
			id2[k] = id - id2[k];

//		for (int m = 0; m < scores.length; m++) {
//			for (int n = 0; n < scores[0].length; n++)
//				System.out.print(scores[m][n]+" ");
//			System.out.println();
//		}
//		for (int m = 0; m < track.length; m++) {
//			for (int n = 0; n < track[0].length; n++)
//				System.out.print(track[m][n]==0?"-":(track[m][n]==1?"\\":"|"));
//			System.out.println();
//		}
	}

	public int[] get1 () { return id1; }
	public int get1 (int i) { return id1[i]; }
	public int[] get2 () { return id2; }
	public int get2 (int i) { return id2[i]; }
}
