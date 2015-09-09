package utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Triple<T1,T2,T3> implements Comparable<Triple<T1,T2,T3>>{
	public T1 o1;
	public T2 o2;
	public T3 o3;
	static String delimiter = "|";
	public Triple(T1 o1, T2 o2, T3 o3) { this.o1 = o1; this.o2 = o2; this.o3= o3;}
	private volatile int hashCode = 0; // (See Item 48)
	public boolean same(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	public T1 getFirst() { return o1; }
	public T2 getSecond() { return o2; }
	public T3 getThird() { return o3; }

	void setFirst(T1 o) { o1 = o; }
	void setSecond(T2 o) { o2 = o; }
	void setThird(T3 o) { o3 = o; }

	public boolean equals(Object obj) {
		if( ! (obj instanceof Triple))
			return false;
		Triple p = (Triple)obj;
		return same(p.o1, this.o1) && same(p.o2, this.o2) && same(p.o3,this.o3);
	}

	@Override
	public String toString() {
		//return "Pair{"+o1+", "+o2+"}";
		return o1+delimiter+o2+delimiter+o3;
	}

	public int compareTo(Triple aTriple) {
		int indexCmp = ((Comparable) o1).compareTo((Comparable) aTriple.getFirst());
		int indexCmp2 = ((Comparable) o2).compareTo((Comparable) aTriple.getSecond());
		return indexCmp != 0 ? indexCmp :
			(indexCmp2 !=0 ? indexCmp2 : ((Comparable) o3).compareTo((Comparable)aTriple.getThird()));			
	}
	
	//redefine hashCode is important since only this way, HashMap can directly be used with Pair as 
	//the key.
	public int hashCode() {
		if (hashCode == 0) {
			int result = 17;
			result = 67*result + this.o1.hashCode();
			result = 67*result + this.o2.hashCode();
			result = 67*result + this.o3.hashCode();
			hashCode = result;
		}
		return hashCode;
	}
	
	
    public static void main(String[] args) {
    	SortedMap<Triple,Integer> testMap = new TreeMap<Triple,Integer>();
        Triple[] nameArray = {
//            new Pair("John", "Lennon"),
//            new Pair("Karl", "Marx"),
//            new Pair("Groucho", "Marx"),
//            new Pair("Oscar", "Grouch"),
//            new Triple(20, "Lennon","watt"),
//            new Triple(10, "Marx","century"),
//            new Triple(15, "Marx","means"),
//            new Triple(8, "Grouch","future"),
//            new Triple(10, "Marx","century"),
//            new Triple(15, "Marx","means"),
//            new Triple(20, "Lennon","watt"),
//            new Triple(10, "Marx","century"),
//            new Triple(15, "Marx","means"),
//            new Triple(8, "Grouch","future"),
//            new Triple(10, "Marx","century"),
//            new Triple(20, "Lennon","century"),
//            new Triple(20, "Marx","century"),
//            new Triple(20, "Grouch","century"),
//            new Triple(8, "Grouch","future"),
            new Triple("Atypical nevus","T191","C0205748"),
            new Triple("Atypical nevus","T191","C0205748")
        };
        Triple testPair = new Triple(20,"Lennon","century");
        List<Triple> names = Arrays.asList(nameArray);
        Collections.sort(names);
        System.out.println(names);
        System.out.println(names.contains(testPair));
        
        for(int i=0;i<names.size();i++){
        	Triple keyTriple = names.get(i);
        	if(testMap.containsKey(keyTriple)){
        		int count = testMap.get(keyTriple) + 1;
        		testMap.put(names.get(i), count);        		
        	}else{
        		int count = 1;
        		testMap.put(names.get(i), count);
        	}
        }
        
        Iterator<Triple> iterNamesHm = testMap.keySet().iterator(); 
        while(iterNamesHm.hasNext()){
        	Triple next = iterNamesHm.next();
        	int count = testMap.get(next);
        	System.out.println(next+" "+count);
        }
    }
}
