package helpers;

public class Span {

	int[][] s;
	int length;

	public Span () {}
	public Span (int[] i) {
		if (i.length % 2 == 0) {
			length = 0;
			s = new int[i.length/2][2];
			for (int j = 0; j < i.length; j+=2) {
				s[j/2][0] = i[j];
				s[j/2][1] = i[j+1];
				length += i[j+1] - i[j];
			}
		}
	}

	public int size () { return s.length; }
	public int length () { return length; }
	public int[] get (int i) { return s[i]; }

	// 2 * intersect / (length of s1 + length of s2)
	public static double score (Span s1, Span s2) {
		double a = 0;
		double b = 0;
		// there is a more efficient way
		for (int i = 0; i < s1.size(); i++)
			for (int j = 0; j < s2.size(); j++)
				a += overlap(s1.get(i), s2.get(j));
		for (int i = 0; i < s1.size(); i++)
			b += s1.get(i)[1] - s1.get(i)[0];
		for (int i = 0; i < s2.size(); i++)
			b += s2.get(i)[1] - s2.get(i)[0];
		return a==0 ? -1 : a/b;
	}

	private static int overlap (int[] a, int[] b) {
	    int ret;
		if (a[0] >= b[0])
			ret = (a[1]>b[1] ? b[1] : a[1]) - a[0];
		else
			ret = (a[1]<b[1] ? a[1] : b[1]) - b[0];
		if ((ret*=2) < 0) ret= 0;
		return ret;
	}

	public double gap () {
		return 0;
	}

	public String toString () {
		StringBuffer sb = new StringBuffer();
		for (int i[] : s)
			sb.append(i[0]).append("-").append(i[1]).append(":");
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
//////////////////////////////////////////////////////////////////////////////
//	int c;
//	static int[][] m;
//	public Span (char c) {
//		switch(c) {
//		case 'A': this.c=0; break;
//		case 'C': this.c=1; break;
//		case 'G': this.c=2; break;
//		case 'T': this.c=3; break;
//		}
//		mat();
//	}
//	public Span () {}
//	private void mat() {
//		m = new int[4][4];
//		m[0][0] = 2;
//		m[0][1] = -1;
//		m[0][2] = 1;
//		m[0][3] = -1;
//		m[1][0] = -1;
//		m[1][1] = 2;
//		m[1][2] = -1;
//		m[1][3] = 1;
//		m[2][0] = 1;
//		m[2][1] = -1;
//		m[2][2] = 2;
//		m[2][3] = -1;
//		m[3][0] = -1;
//		m[3][1] = 1;
//		m[3][2] = -1;
//		m[3][3] = 2;
//	}
//	public int get () { return c; }
//	public String toString() { if (c==0) return "A"; if (c==1) return "C"; if (c==2) return "G"; if (c==3) return "T"; return ""; }
//	public static double score (Span s1, Span s2) {
//		return m[s1.get()][s2.get()];
//	}
//	public double gap () { return -2; }
}
