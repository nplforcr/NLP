package corpusProcessor;

/**
 * 
 * @author Nodar Nutsubidze -3271259
 * @author Dingcheng Li - 3330182
 * @author Nathan Kerns - 3286000
 */
/*
 * This class is used to sort the numOcurences based on count.
 */
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import utils.Pair;


public class SortedPairList extends ArrayList{
	private List<Pair> theList;
	private Pair aPair;

	public SortedPairList() {
		super();
		theList = new ArrayList<Pair>();
		this.aPair=aPair;
	}

	public String toString(){
		return theList.toString();
	}

	public Pair set(Pair pair){
		return this.aPair=pair;
	}
	public Pair get(int index){
		return theList.get(index);
	}
	public int size(){
		return theList.size();
	}

	public void addNInOrder(Pair aPair) {	
		if(theList.isEmpty()){
			theList.add(0,aPair);
		}
		else{
			int counter=0;
			Iterator<Pair> itList=theList.iterator();
			Integer first=(Integer) aPair.getFirst();
			while(itList.hasNext()){	
				Pair ithPair =(Pair) itList.next();
				Object ithFirst=ithPair.getFirst();
				int comparision=((Integer) first).compareTo((Integer) ithFirst);
				if(comparision==0){
					return;
				}
				else if(comparision<0){
					theList.add(counter, aPair);
					return;
				}
				else{
					counter++;
				}
			}
			theList.add(counter,aPair);
		}
	}
}	






