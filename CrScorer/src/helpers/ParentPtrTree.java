package helpers;

public class ParentPtrTree {
	private int[] parents;
	private int size;

	public ParentPtrTree (int sz) {
		size = sz;
		parents = new int[size];
		for (int i = 0; i < size; i++)
			parents[i] = -1;
	}

	public ParentPtrTree (int[][] pairs) {
		int sz = 0;
		for (int i = 0; i < pairs.length; i++) {
			if (pairs[i][0] > sz) sz = pairs[i][0];
			if (pairs[i][1] > sz) sz = pairs[i][1];
		}
		size = sz + 1;
		parents = new int[size];
		for (int i = 0; i < size; i++)
			parents[i] = -1;

		for (int i = 0; i < pairs.length; i++)
			if (this.differ(pairs[i][0], pairs[i][1]))
				this.union(pairs[i][0], pairs[i][1]);
		return;
	}

	public int getSize () { return size; }

	private int find (int n) {
		if (parents[n] == -1) return n;
		return parents[n] = find(parents[n]);
	}

	public void union (int a, int b) {
		int root1 = find(a);
		int root2 = find(b);
		if (root1 != root2) parents[root2] = root1;
	}

	public boolean differ (int a, int b) {
		return find(a) != find(b);
	}

	// returns the number of equivalence classes
	public int equivCls (int[] ec) {
		for (int i = 0; i < size; i++)
			ec[i] = -1;
		int classes = -1;
		for (int i = 0; i < size; i++) {
			if (ec[i] >= 0) continue;
			int r = find(i);
			if (ec[r] == -1) ec[r] = ++classes;
			for (int j = i; j != r; j = parents[j])
				ec[j] = ec[r];
		}
		return classes + 1;
	}

	public void print () {
		for (int i = 0; i < size; i++)
			System.out.print(parents[i]+" ");
		System.out.println();
		for (int i = 0; i < size; i++)
			System.out.println(i+1+":"+find(i));
	}
}
