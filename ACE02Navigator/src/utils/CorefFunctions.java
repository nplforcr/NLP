package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import annotation.Mention;
import annotation.NamedEntity;

public class CorefFunctions {
	static boolean debug = false;
	static boolean nootherpron;
	static boolean headonly = false;

    /**
     * sort keyDataList (added on 07-21,2015)
     * @param keyDataList
     * @param currCorefIndex
     * @return insert position
     */
	private static int insertPos(ArrayList<CorefClusters> keyDataList, int currCorefIndex)
	{
		int pos = keyDataList.size();
		for(int i = 0; i < keyDataList.size(); i++)
		{
			if(currCorefIndex < keyDataList.get(i).getCorefIndex())
			{
		        pos = i;
		        break;
			}
		}
		return pos;
	}
	
	
	/**
	 *
	 * idMentionM is the list of mentions which belong to the same named entity. Namely, they are coreferring
	 * @param idMentionM
	 * @param mentionSpanList
	 * @param bytespanIdM
	 */
	public static void fillMentionList(HashMap<String, Mention> idMentionM,List<Pair> mentionSpanList,HashMap<Pair, String> bytespanIdM){

		//idNeM;
		//idMentionM;
		Iterator<String> menIter = idMentionM.keySet().iterator();
		while(menIter.hasNext()){
			String menId = menIter.next();
			Mention mention = idMentionM.get(menId);
			//int[] bytespan = new int[2];
			//bytespan[0]=mention.getExtentSt();
			//bytespan[1]=mention.getExtentEd();
			//Pair bytespan = new Pair(mention.getExtentSt(),mention.getExtentEd());
			int startInd = 0;
			int endInd = 0;
			String phrase = "";
			if(headonly){
				startInd = mention.getHeadCoveredText().indexOf("\"")+1;
				endInd = mention.getHeadCoveredText().lastIndexOf("\"");
				phrase = mention.getHeadCoveredText().substring(startInd,endInd).toLowerCase();
			}else{
				startInd = mention.getExtentCoveredText().indexOf("\"")+1;
				endInd = mention.getExtentCoveredText().lastIndexOf("\"");
				phrase = mention.getExtentCoveredText().substring(startInd,endInd).toLowerCase();
			}
			
			
			
			if(nootherpron){
				if(OtherPronouns.otherPronList2.contains(phrase)||phrase.equals("'s")){
					continue;
				}else{
					if(headonly){
						if (phrase.contains(" ")) {
							String[] tokenArray = phrase.split(" ");
							phrase = tokenArray[tokenArray.length - 1];
						}
					}
					
					int startByte = 0;
					int endByte = 0;
					Pair bytespan = null;
					if(headonly){
						startByte = mention.getHeadEd()-phrase.length()+1;
						endByte = mention.getHeadEd();
						bytespan = new Pair(startByte,endByte);
					}else{
						startByte = mention.getExtentEd()-phrase.length()+1;
						endByte = mention.getExtentEd();
						bytespan = new Pair(startByte,endByte);
					}
					
					
					if(!mentionSpanList.contains(bytespan)){
						mentionSpanList.add(bytespan);
					}
					
					bytespanIdM.put(bytespan, menId);
					//System.out.println(mention.getType() + " "+menId+" "+mention.getHeadCoveredText()+" "+bytespan.toString());
				}
			}else{
				if(headonly){
					if (phrase.contains(" ")) {
						String[] tokenArray = phrase.split(" ");
						phrase = tokenArray[tokenArray.length - 1];
					}
				}
				int startByte = 0;
				int endByte = 0;
				Pair bytespan = null;
				if(headonly){
					startByte = mention.getHeadEd()-phrase.length()+1;
					endByte = mention.getHeadEd();
					bytespan = new Pair(startByte,endByte);
				}else{
					startByte = mention.getExtentEd()-phrase.length()+1;
					endByte = mention.getExtentEd();
					bytespan = new Pair(startByte,endByte);
				}
				if(!mentionSpanList.contains(bytespan)){
					mentionSpanList.add(bytespan);
				}
				bytespanIdM.put(bytespan, menId);
				//sSystem.out.println(mention.getType() + " "+menId+" "+mention.getHeadCoveredText()+" "+bytespan.toString()+" "+mention.getExtentCoveredText());
			}
		}
		//System.out.println("mentionSpanList size above: "+mentionSpanList.size());
	}
	
	/**
	 * 	
	 *
	 * Id is named entity ID. Each coreferring mention is under each named entity. So, if we have idNeM and idMenM
	 * we can find which one is coreferring with which one. 
	 *
	 * @param idMentionListM
	 * @param idNeM
	 */
	public static void fillIdMenListM(HashMap<String,List<Pair>> idMentionListM,HashMap<String, NamedEntity> idNeM){
		Iterator<String> iterIdNeM = idNeM.keySet().iterator();
		while(iterIdNeM.hasNext()){
			String id = iterIdNeM.next();
			NamedEntity ne = idNeM.get(id);
			List<Mention> menList = ne.getMentions();
			List<Pair> menByteList = new ArrayList<Pair>();
			for(Mention mention:menList){
				int startInd = 0;
				int endInd = 0;
				String phrase = "";
				if(headonly){
					startInd = mention.getHeadCoveredText().indexOf("\"")+1;
					endInd = mention.getHeadCoveredText().lastIndexOf("\"");
					phrase = mention.getHeadCoveredText().substring(startInd,endInd).toLowerCase();
					if (phrase.contains(" ")) {
						String[] tokenArray = phrase.split(" ");
						phrase = tokenArray[tokenArray.length - 1];
					}
				}else{
					//System.out.println("mention text in fillIdMenListM: "+mention.getExtentCoveredText());
					//System.out.println("mention text in fillIdMenListM: "+mention.getHeadCoveredText());
					startInd = mention.getExtentCoveredText().indexOf("\"")+1;
					endInd = mention.getExtentCoveredText().lastIndexOf("\"");
					//System.out.println("startInd: "+startInd+" endInd: "+endInd);
					phrase = mention.getExtentCoveredText().substring(startInd,endInd).toLowerCase();
				}
				
				//String word = mention.getHeadCoveredText().substring(startInd,endInd).toLowerCase();
				

				
				if(OtherPronouns.otherPronList2.contains(phrase)||phrase.equals("'s")){
					continue;
				}else{
					int startByte = 0;
					int endByte = 0;
					if(headonly){
						startByte = mention.getHeadEd()-phrase.length()+1;
						endByte = mention.getHeadEd();
					}else{
						startByte = mention.getExtentEd()-phrase.length()+1;
						endByte = mention.getExtentEd();
					}
					
					Pair bytespan = new Pair(startByte,endByte);
					menByteList.add(bytespan);
				}
			}
			Collections.sort(menByteList);
			String numId = id.substring(id.indexOf("-E")+2);
			if(debug){
				System.out.println("id in fillIdMenListM: "+id+" numId: "+numId);
			}
			idMentionListM.put(numId, menByteList);
		}
	}
	
	/**
	 * 
	 * @param docBuf
	 * @param bsTokenList
	 * @param bytespanIdM
	 * @param idMentionM
	 * @param idMentionListM
	 * @param mentionSpanList
	 * @return
	 */
	public static ArrayList<Pair> adjustByteSpan(StringBuffer docBuf,List<String> bsTokenList,HashMap<Pair,String> bytespanIdM,HashMap<String, Mention> idMentionM,HashMap<String,List<Pair>> idMentionListM, List<Pair> mentionSpanList){
		ArrayList<Pair> sortedMentionSpanList = new ArrayList<Pair>();
		Collections.sort(sortedMentionSpanList);
		Pattern pattern = Pattern.compile("\\s+");
		Matcher matcher = pattern.matcher(docBuf.toString());
		String tempDoc = docBuf.toString();
		boolean found = matcher.find();
		while(found){
			String match = matcher.group();
			int spaceStart = tempDoc.indexOf(match);
			tempDoc = tempDoc.substring(spaceStart);
		}
		return sortedMentionSpanList;
	}

	/**
	 * getKeyData: the streamline goes like this: firstly, we sort the metionSpanList. Then loop thruough the list
	 * for each bytespan, we get menId from bytespanIdM in which the key is bytespan and the value is menId.
	 * then mention is obtained from idMentionM where the key is menId and the value is the mention. The covered text
	 * can be obatined from Mention thereby. Now, we check if the phrase in the Mention is a pronoun. If so, neId 
	 * is extracted by simply split medId by "-". The number before "-" is neId. With neId, we can get a mention list 
	 * from idMentionListM. The mention list is a list of mentions which are all coreferring. Let us extract mentions 
	 * before the pronoun. With this algorithm we can build a coreferring chain with the following format:
	 *  IDENT 2 Pair{226, 253} Microtel chairman Mike leven 3 Pair{280, 281} he
		IDENT 8 Pair{433, 437} Leven 12 Pair{575, 577} him
		IDENT 12 Pair{575, 577} him 13 Pair{583, 585} his
		IDENT 28 Pair{1528, 1597} 84% of the people who fly on corporate jets are not the top management 30 Pair{1602, 1605} they
		IDENT 30 Pair{1602, 1605} They 32 Pair{1629, 1632} they
		IDENT 35 Pair{1727, 1731} Leven 36 Pair{1737, 1739} his
		IDENT 49 Pair{2194, 2201} this CEO 50 Pair{2274, 2275} he
	 * The number before bytespans is the index of each mention which are in the mentionSpanList. The list stores all mentions by its order
	 *  
	 * @param bsTokenList
	 * @param bytespanIdM
	 * @param idMentionM
	 * @param idMentionListM
	 * @param mentionSpanList
	 * @return
	 */
	public static ArrayList<CorefClusters> getKeyData(List<String> bsTokenList,HashMap<Pair,String> bytespanIdM,HashMap<String, Mention> idMentionM,HashMap<String,List<Pair>> idMentionListM, List<Pair> mentionSpanList){
		boolean firstCata = false;
		ArrayList<CorefClusters> keyDataList = new ArrayList<CorefClusters>();  
		List<Pair> sortedMentionSpanList = mentionSpanList;
		//System.out.println("mentionSpanList size below: "+mentionSpanList.size());
		Collections.sort(sortedMentionSpanList);
		int sortedIndex  =0 ;
		int sortedCorefIndex = 0;
		if(debug){
			System.out.println("In debug: if you want to close debug and do evalutions, \n" +
					" please change the value of the global variable 'debug' in PrepareACEKey.java to false");
		}
		for(Pair bytespan: sortedMentionSpanList){
			String menId = bytespanIdM.get(bytespan);
			/*
			if(debug){
				System.out.println("bytespan: "+bytespan+" menId: "+menId);
			}*/
			Mention mention = idMentionM.get(menId);
			int startInd = 0;
			int endInd = 0;
			IntArray anaIntArray = new IntArray(startInd,endInd);
			String phrase = "";
			if(headonly){
				startInd = mention.getHeadCoveredText().indexOf("\"")+1;
				endInd = mention.getHeadCoveredText().lastIndexOf("\"");
				phrase = mention.getHeadCoveredText().substring(startInd,endInd).toLowerCase();
				//String word = mention.getHeadCoveredText().substring(startInd,endInd).toLowerCase();
				if (phrase.contains(" ")) {
					String[] tokenArray = phrase.split(" ");
					phrase = tokenArray[tokenArray.length - 1];
				}
			}else{
				startInd = mention.getExtentCoveredText().indexOf("\"")+1;
				endInd = mention.getExtentCoveredText().lastIndexOf("\"");
				phrase = mention.getExtentCoveredText().substring(startInd,endInd).toLowerCase();
			}
			

			
			/*if(sortedIndex==0 && ThirdPersonPron.thirdPerPronList2.contains(phrase)){
				firstCata = true;
				continue;
			}
			*/
			//System.out.println(menId+" "+word+" "+ThirdPersonPron.thirdPerPronList2.contains(word));
			if(debug){
				System.out.println("Entity "+sortedIndex+" "+menId+" "+phrase+" ");
				bsTokenList.add(bytespan+" "+phrase);
			}
			//-->modify if(ThirdPersonPron.thirdPerPronList2.contains(phrase)||ThirdReflexives.thirdRefList2.contains(phrase)){
				
				String neId = menId.substring(0,menId.indexOf("-"));
				
				List<Pair> menList = idMentionListM.get(neId);
				//if(debug){
					//System.out.println(neId);
					//System.out.println(menList.size());
				//}
				
				int index =-1;
				
				index = menList.indexOf(bytespan);
				int preIndex = index-1;
				Pair corefByteSpan = null;
				if(preIndex > -1){            //-->modify preIndex != -1  date:2015-07-21 
					corefByteSpan= menList.get(preIndex);
					//System.out.println("corefByteSpan: "+corefByteSpan);
					String corefMenId = bytespanIdM.get(corefByteSpan);
					//System.out.println("corefMenId: "+corefMenId);
					
					Mention corefMention = idMentionM.get(corefMenId);
					//System.out.println("corefMention: "+corefMention.getHeadCoveredText());
					//since the word looks like the following: string = "he", we need to find the index of the left quote ".
					int corefStInd = 0;
					int corefEndInd = 0;
					IntArray corefIntArr = new IntArray(corefStInd,corefEndInd);
					String corefPhrase = "";
					if(headonly){
						corefStInd = corefMention.getHeadCoveredText().indexOf("\"")+1;
						//since the word looks like the following: string = "he", we need to find the index of the right quote ". 
						corefEndInd =corefMention.getHeadCoveredText().lastIndexOf("\"");
						corefPhrase = corefMention.getHeadCoveredText().substring(corefStInd,corefEndInd);
						if (corefPhrase.contains(" ")) {
							String[] tokenArray = corefPhrase.split(" ");
							corefPhrase = tokenArray[tokenArray.length - 1].toLowerCase();
						}
					}else{
						//System.out.println("corefMention in getKeyData: "+corefMention.getHeadCoveredText());
						//System.out.println("corefMention in getKeyData: "+corefMention.getExtentCoveredText());
						corefStInd = corefMention.getExtentCoveredText().indexOf("\"")+1;
						//since the word looks like the following: string = "he", we need to find the index of the right quote ". 
						corefEndInd =corefMention.getExtentCoveredText().lastIndexOf("\"");
						corefPhrase = corefMention.getExtentCoveredText().substring(corefStInd,corefEndInd);
					}
					
					sortedCorefIndex = sortedMentionSpanList.indexOf(corefByteSpan);
					//System.out.println(mentionSpanList.get(index));
					
					if(firstCata==true){
						sortedCorefIndex -= 1;
					}
					System.out.println("IDENT "+sortedCorefIndex+" "+corefByteSpan+" "+corefPhrase+" "+sortedIndex+" "+bytespan+" "+phrase);
					CorefClusters keyData = new CorefClusters();
					keyData.setCorefIndex(sortedCorefIndex);
					keyData.setCorefWord(corefPhrase);
					keyData.setCorefIntArray(corefIntArr);
					keyData.setAnaIndex(sortedIndex);
					keyData.setAnaphor(phrase);
					keyData.setAnaIntArray(anaIntArray);
					int pos = insertPos(keyDataList, sortedCorefIndex);
					keyDataList.add(pos, keyData);
				}
			sortedIndex++ ;
	    }
		return keyDataList;
	}
}
