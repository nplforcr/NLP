package utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Pair <T1,T2> implements Comparable<Pair<T1,T2>>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -627033812379793927L;
	private static boolean naturalOrder;
	/**
	 * Direct access is deprecated.  Use first().
	 *
	 * @serial
	 */
	public T1 first;

	/**
	 * Direct access is deprecated.  Use second().
	 *
	 * @serial
	 */
	public T2 second;

	public Pair() {
		// first = null; second = null; -- default initialization
	}

	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	public Pair(T1 k, T2 v, boolean naturalOrder){
		first = k;
		second = v;
		Pair.naturalOrder = naturalOrder; 
	}

	public T1 getFirst() {
		return first;
	}

	public T2 getSecond() {
		return second;
	}

	public void setFirst(T1 o) {
		first = o;
	}

	public void setSecond(T2 o) {
		second = o;
	}

	@Override
	public String toString() {
		//use two brackets in order to avoid cases where the phrase involve brackets as well
		return "\"((" + first + ":" + second + "))\"";
		//return "\"" + first + ":" + second + "\"";
	}


	public boolean same(T1 t1, T2 t2) {
		return t1 == null ? t2 == null : t1.equals(t2);
	}


	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o instanceof Pair) {
			Pair p = (Pair) o;
			return (first == null ? p.first == null : first.equals(p.first)) && (second == null ? p.second == null : second.equals(p.second));
		} else {
			return false;
		}
	}

	/**
	 * Compares this <code>Pair</code> to another object.
	 * If the object is a <code>Pair</code>, this function will work providing
	 * the elements of the <code>Pair</code> are themselves comparable.
	 * It will then return a value based on the pair of objects, where
	 * <code>p &gt; q iff p.first() &gt; q.first() ||
	 * (p.first().equals(q.first()) && p.second() &gt; q.second())</code>.
	 * If the other object is not a <code>Pair</code>, it throws a
	 * <code>ClassCastException</code>.
	 *
	 * @param another the <code>Object</code> to be compared.
	 * @return the value <code>0</code> if the argument is a
	 *         <code>Pair</code> equal to this <code>Pair</code>; a value less than
	 *         <code>0</code> if the argument is a <code>Pair</code>
	 *         greater than this <code>Pair</code>; and a value
	 *         greater than <code>0</code> if the argument is a
	 *         <code>Pair</code> less than this <code>Pair</code>.
	 * @throws ClassCastException if the argument is not a
	 *                            <code>Pair</code>.
	 * @see java.lang.Comparable
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(Pair<T1,T2> another) {
		int comp;
		if (naturalOrder){
			comp = ((Comparable<T1>) getFirst()).compareTo(another.getFirst());
		}else{
			comp = -((Comparable<T1>) getFirst()).compareTo(another.getFirst());
		}
		if (comp != 0) {
			return comp;
		} else {
			return ((Comparable<T2>) getSecond()).compareTo(another.getSecond());
		}
	}

	//redefine hashCode is important since only this way, HashMap can directly be used with Pair as 
	//the key.
	private volatile int hashCode = 0; // (See Item 48)
	public Object o1;
	public Object o2;
	@Override
	public int hashCode() {
		if (hashCode == 0) {      
			int firstHash  = (first == null ? 0 : first.hashCode());
			int secondHash = (second == null ? 0 : second.hashCode());

			hashCode = firstHash*31 + secondHash;      
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
