package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class SortedIndex {
	
	public static ArrayList<Integer> indiceSort(final ArrayList<Integer> coArray){
		ArrayList<Integer> sortedIndList = new ArrayList<Integer>();
		for(int i=0;i<coArray.size();i++){
			sortedIndList.add(i);
		}
		Collections.sort(sortedIndList, new Comparator<Integer>() {
		    @Override public int compare(final Integer o1, final Integer o2) {
		    	return compare(coArray.get(o1), coArray.get(o2));
		    }
		});
		for(int i=0;i<sortedIndList.size();i++){
			System.out.println(sortedIndList.get(i));
		}
		return sortedIndList;
	}
 
	public static Integer[] indiceSort(final float[] coArray){
		final Integer[] idx = new Integer[coArray.length];
		for(int i=0;i<idx.length;i++){
			idx[i]=i;
		}
		Arrays.sort(idx, new Comparator<Integer>() {
		    @Override public int compare(final Integer o1, final Integer o2) {
		        return Float.compare(coArray[o1], coArray[o2]);
		    }
		});
		
		for(int i=0;i<idx.length;i++){
			System.out.println(idx[i]);
		}
		return idx;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		final Integer[] idx = { 0, 1, 2, 3 };
		final float[] data = { 1.7f, -0.3f,  2.1f,  0.5f };

		SortedIndex.indiceSort(data);
		

	}

}
