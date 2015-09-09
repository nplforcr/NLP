package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Distribution;

/**
 * 
 * @author m048100 Dingcheng Li
 * @since July 22, 2011
 *
 */
public class SmoothingFunctions {
	static boolean debug = true;
	static boolean compareDistrib = false;

	/**
	 * the following function aims at call functions from SimpleGoodTuring.java to smooth model files.  
	 * @param posModelHm
	 * @param posModelCondHm
	 * @param valueList
	 */
	static public List<Counter<Pair>> smoothWithGT(SortedMap<Pair, SortedMap<Pair, Integer>> posModelHm,SortedMap<Pair, Integer> posModelCondHm, List<String> valueList){
		Iterator<Pair> iterPosModelHm = posModelHm.keySet().iterator();
		//in the following Map, the first integer is value of some keyValuePairs. Namely, the count of keyValuePairs. 
		//the second integer is the number of times some keyValuePairs have the same count.
		List<Counter<Pair>> distList = new ArrayList<Counter<Pair>>();
		while(iterPosModelHm.hasNext()){
			Pair item = iterPosModelHm.next();	    
			System.out.println(item);
			SortedMap<Pair,Integer> keyValuePairMap = posModelHm.get(item);
			//SmoothingFunctions.sgtOneMap(keyValuePairMap);
			Counter<Pair> dist = SmoothingFunctions.smOneMap(keyValuePairMap,valueList);
			distList.add(dist);
		}
		return distList;
	}
	
	static public void sgtOneMap(SortedMap<Pair,Integer> keyValuePairMap){
		Iterator<Pair> iterKeyValueMap = keyValuePairMap.keySet().iterator();
		SortedMap<Integer,Integer> countOfCountHm = new TreeMap<Integer,Integer>();
		List<String> trueValueList = new ArrayList<String>();
		while(iterKeyValueMap.hasNext()){
			Pair key = iterKeyValueMap.next();
			int count = keyValuePairMap.get(key);
			if(!countOfCountHm.containsKey(count)){
				countOfCountHm.put(count,1);
			}else{
				int countOfcount = countOfCountHm.get(count)+1;
				countOfCountHm.put(count, countOfcount);
			}
			Object value = key.getSecond();
			if(value instanceof String){
				if(!trueValueList.contains(value)){
					trueValueList.add((String) value);
				}
			}
		}
		
		int[][] turingInput = new int[2][];
		// this is nVals used in SimpleGoodTuring.java of Stanford nlp or Geoffrey Sampson's good_turing_estimator.cpp
		//ftp://ftp.informatics.susx.ac.uk/pub/users/grs2/SGT.c <p/>
	    List<Integer> countVals = new ArrayList<Integer>(); 
	    //this is rVals used in SimpleGoodTuring.java
	    List<Integer> countOfcountVals = new ArrayList<Integer>();
	    
	    Iterator<Integer> iterCountOfCountHm = countOfCountHm.keySet().iterator();
	    while(iterCountOfCountHm.hasNext()){
	    	int count = iterCountOfCountHm.next();
	    	
	    	int countOfcount = countOfCountHm.get(count);
	    	countVals.add(countOfcount);
	    	countOfcountVals.add(count);
	    }
		//in order to keep the original index
	    List<Integer> cOcValsCopyList = new ArrayList<Integer>();
	    for(int i=0;i<countOfcountVals.size();i++){
	    	cOcValsCopyList.add(countOfcountVals.get(i));
	    }
	    List<Integer> countValsCopyList = new ArrayList<Integer>();
	    Collections.sort(countOfcountVals);
	    int origInd = -1;
	    int preCOC = -1;
	    for(int i=0;i<countOfcountVals.size();i++){
	    	int sortedCOC = countOfcountVals.get(i);
	    	//the reason we use subList is that the repeated element in the list will be 
	    	//repeated extracted. There is not a method like indexOf a string, we can start from some index
	    	List<Integer> subList = cOcValsCopyList.subList(origInd+1,countVals.size());
	    	if(subList.contains(preCOC)){
	    		//but if subList doesn't contain preCOC any more, it means that the repeated element is extracted up
	    		//since countOfcountVals is sorted. Then, we can start from the beginning of the list again.
	    		origInd = origInd+1+subList.indexOf(sortedCOC);
	    	}else{
	    		origInd = cOcValsCopyList.indexOf(sortedCOC);
	    	}
	    	
	    	countValsCopyList.add(countVals.get(origInd));
	    	preCOC = sortedCOC;
	    }
	    if(countOfcountVals.size()>=5){
	    	//turingInput[0] = SimpleGoodTuring.integerList2IntArray(countOfcountVals);
		    //turingInput[1] = SimpleGoodTuring.integerList2IntArray(countValsCopyList);
		    SimpleGoodTuring sgt = new SimpleGoodTuring(turingInput[0],turingInput[1]);
		    if(debug){
		    	sgt.print();
		    }
	    }
	}
	
	static public Counter<Pair> smOneMap(SortedMap<Pair,Integer> keyValuePairMap,List<String> valueList){
		Counter<Pair> countKeyMap = new ClassicCounter<Pair>();
		Iterator<Pair> iterSortedMap = keyValuePairMap.keySet().iterator();
		List<String> trueValList = new ArrayList<String>();
		while(iterSortedMap.hasNext()){
			Pair key = iterSortedMap.next();
			Object cond = key.getFirst();
			String value = (String) key.getSecond();
			if(!trueValList.contains(value)){
				trueValList.add(value);
			}
			int count = keyValuePairMap.get(key);
			countKeyMap.setCount(key, count);
			System.out.println("key: "+key+" count: "+count);
		}
		Set<Pair> sKeySet = keyValuePairMap.keySet();
		Counter<Pair> dist = new ClassicCounter<Pair>();;
		if(compareDistrib){
			Distribution<Pair> n = Distribution.getDistribution(countKeyMap);
		    Distribution<Pair> prior = Distribution.getUniformDistribution(sKeySet);
		    Distribution<Pair> dir1 = Distribution.distributionWithDirichletPrior(countKeyMap, prior, valueList.size());
		    Distribution<Pair> dir2 = Distribution.dynamicCounterWithDirichletPrior(countKeyMap, prior, valueList.size());
		    Distribution<Pair> add1 = Distribution.laplaceSmoothedDistribution(countKeyMap, valueList.size());
		    Distribution<Pair> gt = Distribution.goodTuringSmoothedCounter(countKeyMap,valueList.size());
		}else{
			Distribution<Pair> sgt = Distribution.simpleGoodTuring(countKeyMap,valueList.size());
			dist = sgt.getCounter();
			//set the default return value of dist as the reserved mass in order that we can retrieve the prob of unkword.
			if(dist!=null){
				dist.setDefaultReturnValue(sgt.getReservedMass());
			}
		}
		return dist;
	}
}
