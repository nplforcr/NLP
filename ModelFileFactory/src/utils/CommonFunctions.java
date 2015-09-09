package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Pair;

public class CommonFunctions {
	
	static List<String> symList = new ArrayList<String>();
	static boolean debug = false;
	static boolean strip = false;
	//the following list is only for debugging. Now, it works very well.
	//a lesson needs drawing here. Don't take things for granted. If wrong for three times, use your pen rather than only use your head!
	static public void fillSymList(){
		symList.add(".");
		symList.add(",");
	}

	static public String exPattern(String line,Pattern pattern){
		String wantWord = "";
		String match = "";
		//reExp = fixedL;
		//Pattern pattern = Pattern.compile(reExp);
		Matcher matcher = pattern.matcher(line);
		boolean found = matcher.find();
		if(found){
			match = matcher.group();
			//System.out.println(match);
			int startInd = match.indexOf("\"");
			//int endInd = match.indexOf("\"", startInd+1);
			int endInd = match.lastIndexOf("\"");
			if(endInd>0){
				wantWord = match.substring(startInd+1,endInd);
				return wantWord;
			}
		}
		return match;
	}
	
	/**
	 * 
	 * @param sentNo1
	 * @param sentNo2
	 * @return
	 */
	public static boolean checkSentNo(int sentNo1,int sentNo2){
		if(sentNo1==sentNo2){
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param phrase
	 * @param range
	 * @return
	 */
	public static Triple<Integer,Integer,String>getConTriple(String phrase, String range){
		CommonFunctions.fillSymList();
		String[] phraseArray = phrase.split("\\s");
		String phraseHead = "";
		int backpoint = 0;
		String[] rangeArray = range.split("\\s");
		for(int i=phraseArray.length-1;i>=0;i--){
			phraseHead = phraseArray[i].trim();
			if(debug){
				System.out.println(phraseHead);
			}
			
			//if(!symList.contains(phraseHead)){
			if(!phraseHead.matches(ReExp.symbols)){
				break;
			}
			//else if(symList.contains(phraseHead)){
			else if(phraseHead.matches(ReExp.symbols)){
				//backpoint is where the first real word appears. 
				backpoint = phraseArray.length-i;
				if(debug){
					//phrase head: mvi therapeutic w / minerals ( therap vits / minerals ) 11 10 53
					//say, we see 11 10 53 where 11 is the length of phrase array. 10 is the bracket index, the range index for 
					//the bracket is 53. Now, we want it to change to 52 where the word "minerals" is located. It should be the 
					//head word. So, we should set backpoint as 11-10 = 1. Then, 53-1=52 is the index of minerals.
					System.out.println("phrase head: "+phrase+" "+phraseArray.length+" "+i+" "+rangeArray[1].split(":")[1]);
				}
				continue;
			}
//			if(!phraseHead.matches("["+ReExp.symbols+"]+")){
//				break;
//			}
		}
		//in order to unify the data from .txt and .txt.con, I have to strip the end puncts such as 
		//. in m.d. and so on. But if the token is itself a puncts, we cannot do that, otherwise,
		//the token is reduced to a blank space.
		
		if(strip){
			if(phraseHead.length()>1 && !phraseHead.equals("__________")){	
				phraseHead = CorefUsers.stripWordPunc(phraseHead);
			}
		}
		
		//System.out.println("phraseHead: "+phraseHead);

		int senNo = Integer.valueOf(rangeArray[1].split(":")[0]);
		int headPos = Integer.valueOf(rangeArray[1].split(":")[1])-backpoint;
		Triple<Integer,Integer,String> conTriple=new Triple<Integer,Integer,String>(senNo,headPos,phraseHead);
		return conTriple;
	}
	
	static public void updateMap(SortedMap<String,Integer> hashMap,String key){
		int count = 0;
		if(hashMap.containsKey(key)){
			count=hashMap.get(key)+1;
			hashMap.put(key, count);
		}else{
			count=1;
			hashMap.put(key, count);
		}
	}
	
	static public void updateMapOfMap(SortedMap<Pair,SortedMap<Pair,Integer>> mapOfMap,Pair keyPair,Pair keyValuePair){
		if(!mapOfMap.containsKey(keyPair)){
			//in this case, I should add keyPair to posModelHm, the value of the keyPair should be created since
			//it means that the keyValuePair is not in the map yet. 
			SortedMap<Pair,Integer> keyValueHm = new TreeMap<Pair,Integer>();
			keyValueHm.put(keyValuePair, 1);
			mapOfMap.put(keyPair, keyValueHm);
		}else{
			//the map is in the posModelHm now. So, we need to update it. 
			SortedMap<Pair,Integer> keyValueHm = mapOfMap.get(keyPair);
			//after we grab the keyValueHm from posModelHm, update the count of the map.
			CommonFunctions.updateHashMap(keyValueHm, keyValuePair);
			mapOfMap.put(keyPair, keyValueHm);
		}
	}
	
	/**
	 * 
	 * @param aMap
	 * @param aPair
	 */
	public static void updateHashMap(SortedMap<Pair,Integer> aMap,Pair aPair){
		if(aMap.containsKey(aPair)){
			int count = aMap.get(aPair);
			aMap.put(aPair, count+1);
		}else{
			int count = 1;
			aMap.put(aPair, count);
		}
	}
	
	/**
	 * 
	 * @param <T>
	 * @param aMap
	 * @param aPair
	 */
	public static <T> void updateGenHashMap(SortedMap<T,Integer> aMap,T object){
		if(aMap.containsKey(object)){
			int count = aMap.get(object);
			aMap.put(object, count+1);
		}else{
			int count = 1;
			aMap.put(object, count);
		}
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isWordNum(String str){
		boolean found = false;
		for(int i=0;i<str.length();i++){
			if ((str.charAt(i) >= 48 && str.charAt(i) <= 57) || (str.charAt(i) >= 65 && str.charAt(i) <= 90) || (str.charAt(i) >= 97 &&
					str.charAt(i) <= 122)) {
				found = true;
				break;
			}
		}
		return found;
	}
}
