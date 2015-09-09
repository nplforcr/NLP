package Examples;

import helpers.*;

import java.util.Vector;
import java.io.FileReader;
import java.io.BufferedReader;

public class KAlphaExample {

	public static void main (String[] args) {
		int[][][] annot = new int[args.length][][];
		try {
			Vector<String[]> bufI = new Vector<String[]>();
			Vector<String[]> bufN = new Vector<String[]>();
			for (int i = 0; i < annot.length; i++) {
				BufferedReader br = new BufferedReader(new FileReader(args[i]));
				String l;
				while ((l = br.readLine()) != null) {
					if (l.startsWith("#")) continue;
					String[] s = l.split("\\s+");
					PairType pt = PairType.valueOf(s[0]);
					if (pt==PairType.IDENT) bufI.add(s);
					else bufN.add(s);
				}
				annot[i] = new int[bufI.size()][2];
				for (int j = 0; j < annot[i].length; j++) {
					String[] s = bufI.get(j);
					annot[i][j][0] = Integer.parseInt(s[1]);
					annot[i][j][1] = Integer.parseInt(s[2]);
				}
				bufI.clear(); bufN.clear();
			}
		} catch (Exception e) { e.printStackTrace(); }
		
		ParentPtrTree[] ppts = new ParentPtrTree[annot.length];
		for (int i = 0; i < ppts.length; i++)
			// hate throwing away memory,
			// but don't know a better way
			ppts[i] = new ParentPtrTree(annot[i]);

		double d = MatScore.KAlpha(ppts);
		System.out.println("KAlpha             = " + d);
	}
}
