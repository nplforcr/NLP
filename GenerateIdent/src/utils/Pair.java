package utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Pair implements Comparable<Pair>{
	public Object o1;
	public Object o2;
	public Pair(Object o1, Object o2) { this.o1 = o1; this.o2 = o2; }
	private volatile int hashCode = 0; // (See Item 48)
	public boolean same(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	public Object getFirst() { return o1; }
	public Object getSecond() { return o2; }

	void setFirst(Object o) { o1 = o; }
	void setSecond(Object o) { o2 = o; }

	public boolean equals(Object obj) {
		if( ! (obj instanceof Pair))
			return false;
		Pair p = (Pair)obj;
		return same(p.o1, this.o1) && same(p.o2, this.o2);
	}

	public String toString() {
		return "Pair{"+o1+", "+o2+"}";
	}

	public int compareTo(Pair aPair) {
		int indexCmp = ((Comparable) o1).compareTo((Comparable)aPair.getFirst());
		return indexCmp != 0 ? indexCmp :
			((Comparable) o2).compareTo((Comparable)aPair.getSecond());
	}
	
	//redefine hashCode is important since only this way, HashMap can directly be used with Pair as 
	//the key.
	public int hashCode() {
		if (hashCode == 0) {
			int result = 17;
			result = 47*result + this.o1.hashCode();
			result = 47*result + this.o2.hashCode();
			hashCode = result;
		}
		return hashCode;
	}
	
	
    public static void main(String[] args) {
        Pair[] nameArray = {
//            new Pair("John", "Lennon"),
//            new Pair("Karl", "Marx"),
//            new Pair("Groucho", "Marx"),
//            new Pair("Oscar", "Grouch"),
            new Pair(20, "Lennon"),
            new Pair(10, "Marx"),
            new Pair(15, "Marx"),
            new Pair(8, "Grouch")
        };
        Pair testPair = new Pair(20,"Lennon");
        List<Pair> names = Arrays.asList(nameArray);
        Collections.sort(names);
        System.out.println(names);
        System.out.println(names.contains(testPair));
    }
}

