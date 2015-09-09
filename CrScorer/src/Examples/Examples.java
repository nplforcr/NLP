package Examples;

import java.util.Vector;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import Jama.Matrix;
import helpers.*;

///home/dingcheng/Documents/NLPWorkspace/modelblocks/wsjparse/corefKey/349699.key
///home/dingcheng/Documents/NLPWorkspace/modelblocks/wsjparse/corefResult/349699newsML.cochain
public class Examples {
	public static void main (String[] args) {
		Vector<String[]> bufI = new Vector<String[]>();
		Vector<String[]> bufN = new Vector<String[]>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("349699Mar12.cochain"));
			String l;
			while ((l = br.readLine()) != null) {
				if (l.startsWith("#")) continue;
				String[] s = l.split("\\s+");
				PairType pt = PairType.valueOf(s[0]);
				if (pt==PairType.IDENT) bufI.add(s);
				else bufN.add(s);
			}
		} catch (Exception e) { e.printStackTrace(); }

		int[][] pK = new int[bufI.size()][2];
		for (int i = 0; i < pK.length; i++) {
			String[] s = bufI.get(i);
			pK[i][0] = Integer.parseInt(s[1]);
			pK[i][1] = Integer.parseInt(s[2]);
		}
		ParentPtrTree key = new ParentPtrTree(pK);
//		int[] a = new int[key.getSize()];
//		key.equivCls(a);

		bufI.clear(); bufN.clear();
		try {
			BufferedReader br = new BufferedReader(args.length==1 ? new InputStreamReader(System.in) : new FileReader("349699Mar13.cochain"));
			String l;
			while ((l = br.readLine()) != null) {
				if (l.startsWith("#")) continue;
				String[] s = l.split("\\s+");
				PairType pt = PairType.valueOf(s[0]);
				if (pt==PairType.IDENT) bufI.add(s);
				else bufN.add(s);
			}
		} catch (Exception e) { e.printStackTrace(); }

		int[][] pR = new int[bufI.size()][2];
		for (int i = 0; i < pR.length; i++) {
			String[] s = bufI.get(i);
			pR[i][0] = Integer.parseInt(s[1]);
			pR[i][1] = Integer.parseInt(s[2]);
		}
		ParentPtrTree res = new ParentPtrTree(pR);

		Matrix m = MatScore.partition(key, res);
		System.out.println("MUC                = " + MatScore.MUC(m));
		System.out.println("class-B3           = " + MatScore.B3(m,B3Type.CLASS));
		System.out.println("entity-B3          = " + MatScore.B3(m,B3Type.ENTITY));
		System.out.println("mention-based CEAF = " + MatScore.CEAF(m,CEAFType.MENTION));
		System.out.println("entity-based CEAF  = " + MatScore.CEAF(MatScore.calcPhi(key,res,MatType.matchingType(CEAFType.ENTITY)),CEAFType.ENTITY));
		System.out.println("KAlpha             = " + MatScore.KAlpha(key,res));
	}
}

