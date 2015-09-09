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

public class CorefUser {
	static boolean debug = false;
	static boolean nootherpron;
	static boolean headonly = true;
	static boolean debugBytesMatch = false;
	static ArrayList<String> abbreList = new ArrayList<String>();
	static ArrayList<String> specialList = new ArrayList<String>();
	static ArrayList<String> properNameList = new ArrayList<String>();
	static ArrayList<String> specialList2 = new ArrayList<String>();
	static int phraseLen = 4;

	public static void consSpeicalList(){
		abbreList.add("a.m.");
		abbreList.add("Co.");
		abbreList.add("Mass.");
		abbreList.add("U.S.");
		abbreList.add("Calif.");
		abbreList.add("Corp.");
		abbreList.add("Inc.");
		abbreList.add("Md.");
		abbreList.add("Dept.");
		specialList.add("no.");


		specialList2.add("cannot");
		specialList2.add("GTE");
		properNameList.add("U.S. Holocaust Memorial Museum");
		properNameList.add("U.S. Holocaust Memorial Council");
		properNameList.add("U.S. Aid in Limbo");
		properNameList.add("U.S. Embassy");
		properNameList.add("U.S. District Judge");
		properNameList.add("U.S. Emphasis on Curbing"); //specifically for 9802.108.sgm though the phrase is not in fact a proper namer at all since it appears on the title
		//it leads to misjudgement. Namely, it regards U.S. is at the end of sentence. This is a simple but not elegant way to solve this problem.
		properNameList.add("U.S. Ambassador"); //also for 9802.108.sgm
		properNameList.add("U.S. Treasury"); //from 9802.233.sgm
		properNameList.add("U.S. Troops Capture Alleged"); //from 9801.139.sgm
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
				//				if(endInd==-1){
				//					continue;
				//				}else{
				//					phrase = mention.getExtentCoveredText().substring(startInd,endInd).toLowerCase();
				//				}
			}else{
				startInd = mention.getExtentCoveredText().indexOf("\"")+1;
				endInd = mention.getExtentCoveredText().lastIndexOf("\"");
				//System.out.println(mention.getExtentCoveredText()+" ");
				//System.out.println(startInd+" "+endInd);
				if(endInd==-1){
					continue;
				}else{
					phrase = mention.getExtentCoveredText().substring(startInd,endInd).toLowerCase();
				}

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
					if(endInd==-1){
						continue;
					}else{
						phrase = mention.getExtentCoveredText().substring(startInd,endInd).toLowerCase();
					}
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
		System.out.println("now, you are in fillIdMenListM");
	}


	/**
	 * The logic here is that if the space between two words are larger than 1, we need to shorten it to 1 since Stanford MaxenTagger tokenize the text
	 * and the word in each sentence is only separated by one byte.
	 * Now, we need to split punctuation marks for the same reason, Stanford MaxentTagger also split punctuation marks as well. 
	 *      
	 */
	public static void adjustACEBytespan(String filename,StringBuffer docBuf,HashMap<Pair,String> bytespanIdM,HashMap<String, Mention> idMentionM,HashMap<String,List<Pair>> idMentionListM, List<Pair> mentionSpanList){
		//Pattern pattern = Pattern.compile("[\\w!\\"\\#\\$\\%\\&\\'\\(\\)*+\\,\\.\/\:;<=>?@\^_`{|}~-]\\s+");
		Collections.sort(mentionSpanList);
		CorefUser.consSpeicalList();
		//I remember. \\s+ means that more than one white space. I don't remember 
		//why expressions before \\s+ can refer to digits. But obviously, it can. I will think about it later. 
		//"\\([-\\w!,?:;.$\"`\'%]+\\s[-\\w!,?:;.$\"`\'%/\\\\]+\\)"
		Pattern pattern = Pattern.compile("[-)\\w!$,?:;.\"\'%]\\s+");
		if(debugBytesMatch){
			System.out.println(docBuf.toString());
		}
		
		//Matcher matcher = pattern.matcher(docBuf.toString());
		String tempDoc = docBuf.toString();
		StringBuffer shorterBuf = new StringBuffer();
		Matcher matcher = pattern.matcher(tempDoc);
		boolean found = matcher.find();
		int countFound = 0;
		////the while loop finish one thing: remove extra space between words. 
		//I feel that methods used here are kind of stupid. But I don't want to bother to change since it is after all a detailed way 
		//to make use of regular expressions. But I do need good documentation to remember what I have done here. 
		//basically, I firstly found the last letter of each word and all the while space after the word. 
		//then, inside the if clause, I am trying to find the first letter of the word. Then, catch the whole word and append it to shorterBuf.
		//The method works but it may miss some letters if the regular expression is incomplete. For example, If the word is "(At", namely, the word
		//starts with a bracket, I need to add [(] to firstL pattern, otherwise, we can only find "At" rather than "(At". For regular expressions, 
		//inside "[]", for special symbols such as "(", we can still write "(" rather than "\\(" when they are alone. This is not always true, yet. 
		//I don't quite understand yet why "[-\\$\\w!,\\(\\[?:;.\"\'%]" works but "[-\\(\\$\\w!,\\[?:;.\"\'%]" doesn't work.
		while(found){
			//note: match here includes white spaces. So, we are not confused by what we see. "9   " in fact, the length is 4.
			String match = matcher.group();
			//for example, match is "S " in the following tempDoc: "1   NEWS STORY"
			//then, spaceStart is the index of the match, which is 7 here. 
			int matchLength = match.length();
			//here, the method is kind of stupid since in Java, it has a good method indexOf(string, fromIndex);
			//but I forgot about it and then each time, I form a new string by cutting tempDoc shorter. But anyway, it is a good 
			//training for me to figure out how to find bytes accurately.
			int spaceStart = tempDoc.substring(1).indexOf(match)+1;
			if(countFound>0){
				StringBuffer firstPart = new StringBuffer();				
				//firstPart.append(tempDoc.charAt(0));

				Pattern firstL = Pattern.compile("[-\\$\\w!,)(\\[?:;.\"\'%]");
				//Pattern firstL = Pattern.compile("w");
				//Pattern firstL = Pattern.compile("\\w!,.\"\'%.*");
				//Matcher matchFirstL = firstL.matcher("word");
				Matcher matchFirstL = firstL.matcher(tempDoc.substring(1));
				boolean foundLetter = matchFirstL.find();
				if(foundLetter){
					String matchedFl = matchFirstL.group();
					//System.out.println("matchedF1 in foundLetter : "+matchedFl+" the first index: "+tempDoc.substring(1).indexOf(matchedFl)+" spaceStart: "+spaceStart+" "+tempDoc);
					//firstPart.append(tempDoc.substring(tempDoc.substring(1).indexOf(matchedFl)-1));
					//note: since matchedFl starts from the last letter of each word. So,tempDoc.substring(1).indexOf(matchedFl) will start from 
					//the space. If we add 1 to it, we will get the full word after the space.
					String appendToken = tempDoc.substring(tempDoc.substring(1).indexOf(matchedFl)+1,spaceStart+1);
					//System.out.println(appendToken);
					if(appendToken.equals(".")){
						firstPart.append(appendToken);
					}else{
						firstPart.append(" "+appendToken);
					}
					//					if(debugBytesMatch){
					//						System.out.println("appendToken: "+appendToken+" firstPart: "+firstPart.toString());
					//					}

					//tempDoc.
				}	
				shorterBuf.append(firstPart);
			}else{
				//it seems that tempDoc should start from 1 since there is always a white space before the first letter like the following: 
				//<DOCNO> CNN19981012.2130.0981 </DOCNO>
				String firstToken = tempDoc.substring(2,spaceStart+1);
				shorterBuf.append(firstToken);
				//no problem to add 2 bytes here since now countFound == 0. It means that the two bytes are added to the first match. 
				matchLength+=2;
			}
			//matchLength is larger than 2, this means that there is at least 1 white space should be removed.
			if(matchLength>2){
				//the following lines aims at replacing mentionSpanPair from idMentionListM. 
				//so, mentionSpanList and bytespanIdM are a pair
				for(int i=0;i<mentionSpanList.size();i++){
					Pair mentionSpanPair = mentionSpanList.get(i);
					String menId = bytespanIdM.get(mentionSpanPair);
					String neId = menId.substring(0,menId.indexOf("-"));
					Mention mention = idMentionM.get(menId);
					String refphrase = mention.getExtentCoveredText();
					//System.out.println(refphrase);
					List<Pair> menList = idMentionListM.get(neId);
					//pos here means position
					int posInmenList = -1;
					for(int j=0;j<menList.size();j++){
						Pair menBytePair = menList.get(j);
						if(menBytePair.equals(mentionSpanPair)){
							posInmenList = j;
							break;
						}
					}
					bytespanIdM.remove(mentionSpanPair);
					int menStart =Integer.parseInt(mentionSpanPair.o1.toString());
					int menEnd =Integer.parseInt(mentionSpanPair.o2.toString());
					//System.out.println("before reducing in adjustACEBytesspan: match length: "+match.length()+" "+menStart+" "+menEnd);
					if(menStart>shorterBuf.length()-1){
						//menStart = menStart - match.length()+2;
						//menEnd = menEnd - match.length()+2;
						//since matchLength is the length of space between two words plus one letter from the previous word.
						//therefore, after menStart or menEnd subtract the matchLength, two more bytes should be added back
						menStart = menStart - matchLength+2;
						menEnd = menEnd - matchLength+2;
					}
					//System.out.println("after reducing in adjustACEBytesspan: shorterBuf length: "+shorterBuf.length()+" "+menStart+" "+menEnd);
					mentionSpanPair = new Pair(menStart,menEnd);
					//therefore, mentionSpanList should set the new mentionSpanPair and we should put the new mentionSpanPair
					//its menId into bytespanIdM. Meanwhile, idMentionListM also replace new menList in which the new mentionSpanPair is set.
					mentionSpanList.set(i, mentionSpanPair);
					bytespanIdM.put(mentionSpanPair, menId);
					if(posInmenList>=0){
						menList.set(posInmenList, mentionSpanPair);
						idMentionListM.put(neId, menList);
					}
				}
			}
			tempDoc = tempDoc.substring(spaceStart);
			//System.out.println(tempDoc.toString());
			//System.out.println("shorterBuf: "+shorterBuf.toString());
			found = matcher.find();
			countFound++;
		}

		if(debugBytesMatch){
			System.out.println("shorterBuf leng: "+shorterBuf.length());
			System.out.println(shorterBuf);
		}
		
		//after we get shorterBuf, we need to further revise it to map to stanford sentence format. 
		//so far, the punctuations are not split yet. 
		//that is, the following for loop aims at split punctuation marks. 
		String[] bufArray = shorterBuf.toString().split("\\s");
		String cutShortBuf = shorterBuf.toString();
		StringBuffer matchBuf2Stanform = new StringBuffer(); 
		int bufleng = 0;
		int preIndPunc = 0;
		String preWord = "";
		int countIncrease = 0;
		//bufArray loops each word in the shorterBuf and find punctuation marks and then, separate them from words.
		for(int i=0;i<bufArray.length;i++){
			int addNumber = 0;
			//in order to guarantee the word doesn't have extra space, we remove the possible white space though there may not be. 
			String word = bufArray[i].replace("\\s+", "");
			StringBuffer potentialPN = new StringBuffer();
			StringBuffer potentialPN2 = new StringBuffer();
			StringBuffer potentialPN3 = new StringBuffer();
			if(i<bufArray.length-phraseLen-1){
				for(int j=0;j<phraseLen;j++){
					String jthToken = bufArray[i+j];
					if(jthToken.matches("[\\w]+[.,:;]")){
						jthToken=jthToken.substring(0,jthToken.length()-1);
					}
					if(j<phraseLen-1){
						potentialPN.append(jthToken+" ");
					}else{
						potentialPN.append(jthToken);
					}

				}

				for(int j=0;j<phraseLen-1;j++){
					String jthToken = bufArray[i+j];
					if(jthToken.matches("[\\w]+[.,:;]")){
						jthToken=jthToken.substring(0,jthToken.length()-1);
					}
					if(j<phraseLen-2){
						potentialPN3.append(jthToken+" ");
					}else{
						potentialPN3.append(jthToken);
					}

				}

				for(int j=0;j<phraseLen-2;j++){
					String jthToken = bufArray[i+j];
					if(jthToken.matches("[\\w]+[.,:;]")){
						jthToken=jthToken.substring(0,jthToken.length()-1);
					}
					if(j<phraseLen-3){
						potentialPN2.append(jthToken+" ");
					}else{
						potentialPN2.append(jthToken);
					}

				}
			}

			String nextWord = "";

			if(i<bufArray.length-1){
				nextWord = bufArray[i+1].replace("\\s+", "");
			}
			//bufleng needs to add the byte of white space
			//meanwhile, we should count from 0 since although it is the length of buf, we compare it with the byteStart or bytesEnd.
			//So, we need to deduct the length to 1 in order to avoid missing.
			if(i==0){
				bufleng+=word.length();
			}else{
				bufleng+=word.length()+1;
			}

			//it seems that [\\w] also include punctuation marks, oh, I seem probably, because I didn't add + after ].
			//note: if we change to w+, there will be some errors. Not sure why. probably, due to the complex interactions here
			Pattern puncP = Pattern.compile("[\\w][!,?:;.\"\'%]+$");
			//Pattern puncP = Pattern.compile("[a-zA-z]+[!,?:;.\"\'%]+$");
			Matcher matchPunc = puncP.matcher(word);
			boolean foundPunc = matchPunc.find();
			//for example, like "corridor" in 9801.139.sgm in ace2_train/three2one/
			Pattern puncP2 = Pattern.compile("\"\\w+[\"]*$");
			Matcher matchPunc2 = puncP2.matcher(word);
			boolean foundPunc2 = matchPunc2.find();

			//special is used to handle cases like cannot it is a word without punctuations. but Stanford parser split cannot into "can" and "not".
			//so, we need to add one byte to all mentions after it. We need to put such words into the following if clause.
			boolean special = false;
			if(specialList2.contains(word)){
				//other specials include "GTE Corp." in 9801.395.sgm. Probably, due to the prob model, GTE Corp. is regarded to 
				//be located at the end of sentence. But in fact, it is not since after it is "and". Or because the sentence 
				//is too long. So, I have to hard code it.
				special = true;
			}

			//I want to include cases like "It's. in this case, it will be decomposed as '' It 's. I don't want to add more patterns here.
			//instead, I want to consider the difference between "It's and It's when adding bytes as follows.
			Pattern puncP3 = Pattern.compile("\\w+\'\\w+$");
			Matcher matchPunc3 = puncP3.matcher(word);
			boolean foundPunc3 = matchPunc3.find();

			Pattern puncP4 = Pattern.compile("\\$\\w+");
			Matcher matchPunc4 = puncP4.matcher(word);
			boolean foundPunc4 = matchPunc4.find();

			Pattern puncP5 = Pattern.compile("\\w+-$");
			Matcher matchPunc5 = puncP5.matcher(word);
			boolean foundPunc5 = matchPunc5.find();

			Pattern puncP6 = Pattern.compile("[.,?:;][\"\'%]+$");
			Matcher matchPunc6 = puncP6.matcher(word);
			boolean foundPunc6 = matchPunc6.find();

			Pattern puncP7 = Pattern.compile("[\\[]\\w+$");
			Matcher matchPunc7 = puncP7.matcher(word);
			boolean foundPunc7 = matchPunc7.find();

			Pattern puncP8 = Pattern.compile("^\\w+[\\]][,;:]*$");
			Matcher matchPunc8 = puncP8.matcher(word);
			boolean foundPunc8 = matchPunc8.find();

			Pattern puncP9 = Pattern.compile("[\\(]+\\w+$");
			Matcher matchPunc9 = puncP9.matcher(word);
			boolean foundPunc9 = matchPunc9.find();

			Pattern puncP10 = Pattern.compile("\\w+[.][\\)]$");
			Matcher matchPunc10 = puncP10.matcher(word);
			boolean foundPunc10 = matchPunc10.find();

			//puncP11 and puncP3 may be overlapping, I will consider this later
			//"Riche's for example.
			Pattern puncP11 = Pattern.compile("[\'\"]\\w+$");
			Matcher matchPunc11 = puncP11.matcher(word);
			boolean foundPunc11 = matchPunc11.find();

			Pattern puncP12 = Pattern.compile("\\w+[.]\\d+[.]\\d+$");
			Matcher matchPunc12 = puncP12.matcher(word);
			boolean foundPunc12 = matchPunc12.find();
			//although pattern 13 and pattern 14 can be unified, I may do it later for clear.
			Pattern puncP13 = Pattern.compile("[\\[]\\w+[\\]]$");                                  
			Matcher matchPunc13 = puncP13.matcher(word);
			boolean foundPunc13 = matchPunc13.find();
			//not sure if the pattern can be comprehensive enough, but there are mistakes without doubt, I will study it later: "[\\[\\(]+\\w+[-]*\\w+[.]*[\\]\\(]+[,.]*$"
			Pattern puncP14 = Pattern.compile("[\\(]\\w+[-]*\\w+[.]*[\\)][,.]*$");                                  
			Matcher matchPunc14 = puncP14.matcher(word);
			boolean foundPunc14 = matchPunc14.find();

			//so annoying since so many weird cases:
			//note: [.] refers to . but if there is no [] only . refers to any symbols. So, we must add [] to constraint it.
			Pattern puncP15 = Pattern.compile("[.][,]*[\"]*");                                  
			Matcher matchPunc15 = puncP15.matcher(word);
			boolean foundPunc15 = matchPunc15.find();

			Pattern puncP16 = Pattern.compile("/");                                  
			Matcher matchPunc16 = puncP16.matcher(word);
			boolean foundPunc16 = matchPunc16.find();

			Pattern puncP17 = Pattern.compile("[\\(\\[]\\w+[-]*\\w+[.]*\\w+[.]*[\\)\\]][-]*[,]*");

			Matcher matchPunc17 = puncP17.matcher(word);
			boolean foundPunc17 = matchPunc17.find();

			Pattern puncP18 = Pattern.compile("\"\\w+");
			Matcher matchPunc18 = puncP18.matcher(word);
			boolean foundPunc18 = matchPunc18.find();



			if(foundPunc || foundPunc2 ||foundPunc3 || foundPunc4 || foundPunc5||foundPunc6||foundPunc7||foundPunc8||foundPunc9||foundPunc10||foundPunc11||foundPunc12
					||foundPunc13||foundPunc14||foundPunc15||foundPunc16||foundPunc17 ||foundPunc18|| special){
				//////////////////////////////////
				/// Dingcheng! working from here. You must finish this tonight!
				///////////////////////////////////
				String match = "";
				if(foundPunc){
					match=matchPunc.group();
				}else if(foundPunc2){
					match=matchPunc2.group();
				}else if(foundPunc3){
					match=matchPunc3.group();
				}else if(foundPunc4){
					match=matchPunc4.group();
				}else if(foundPunc5){
					match=matchPunc5.group();
				}else if(foundPunc6){
					match=matchPunc6.group();
				}else if(foundPunc7){
					match=matchPunc7.group();
				}else if(foundPunc8){
					match=matchPunc8.group();
				}else if(foundPunc9){
					match=matchPunc9.group();
				}else if(foundPunc10){
					match=matchPunc10.group();
				}else if(foundPunc11){
					match=matchPunc11.group();
				}else if(foundPunc12){
					match=matchPunc12.group();
				}else if(foundPunc13){
					match=matchPunc13.group();
				}else if(foundPunc14){
					match=matchPunc14.group();
				}else if(foundPunc15){
					match=matchPunc15.group();
				}else if(foundPunc16){
					match=matchPunc16.group();
				}

				//the above code seems useless. But I still want to keep it for recording need.
				if(word.equals("no.")){
					addNumber = 1;
					countIncrease++;
					bufleng+=addNumber;
				}
				else if(match.endsWith("'") && match.charAt(match.length()-2)=='.'){
					addNumber = 1;
					countIncrease++;
					bufleng+=addNumber;
				}
				else if(word.matches("\\w+[.]\\d+[.]\\d+")){
					addNumber = 1;
					countIncrease++;
					bufleng+=addNumber;
				}
				else if(match.matches("\\w+\\.,")){
					addNumber = 1;
					countIncrease++;
					bufleng+=addNumber;
				}
				//the following else if is the same the next one, so it is right
				//else if(match.matches("\"\\w+[\"]*$")){
				else if(word.matches("'\\w+'\\w+")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				else if(word.matches("'\\w+$")){
					//current token need changes as well
					addNumber = 1;
					countIncrease++;
					bufleng+=addNumber;
				}
				else if(word.startsWith("'") && !word.endsWith("'")){ 
					//current token will not change at all.
					addNumber = 1;
					countIncrease++;
					bufleng+=addNumber;
				}else if(word.endsWith("'") && !word.startsWith("'")){ 
					//current token will not change at all.
					addNumber = 1;
					countIncrease++;
					bufleng+=addNumber;

				}else if(word.startsWith("'") && word.endsWith("'")){
					//note: current token will only increase by 2 since current token only changes due to the left quotes.
					addNumber = 2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				else if(word.startsWith("'") && word.endsWith("'.")){
					addNumber =1;
					countIncrease++;
					bufleng+=addNumber;
				}
				//it seems that we need to separate cases where the whole word is ".\"" and where "w+.\"" since
				//".\"" will become ". '' ". that is, one more byte is added.
				else if(word.matches("w+[.]\"")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;

				}else if(word.matches("[\\[\\(]\\w+[,:;]")){
					//I am not still clear why we only need to add 5, it seems that we should add 6. But it is wrong in the result. 
					//I will consider it later.
					addNumber=5;
					countIncrease+=5;
					bufleng+=addNumber;
				}
				else if(match.matches("\\w[,:;]$")&& !word.contains("'") && !word.contains("/") && !word.startsWith("\"") && !word.startsWith("[")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}else if(word.matches("\\w+'\\w+[,:;]")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				else if(match.matches("\\w[,:;]")&& !word.contains("/") && word.startsWith("\"")){
					addNumber=3;
					countIncrease+=3;
					bufleng+=addNumber;

				}else if(match.matches("\\w[,:;]")&& word.matches("\\d+/\\d+")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				//the following else if is the same the next one, so it is right
				//else if(match.matches("\"\\w+[\"]*$")){
				else if(match.matches("\"\\w+$")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;

				}
				else if(!word.startsWith("\"") &&match.endsWith("\"") && match.matches("\\w+\"")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				else if(word.startsWith("\"") && word.endsWith("\"")){
					addNumber=4;
					countIncrease+=4;
					bufleng+=addNumber;

				}else if(match.endsWith("\".")){
					addNumber=1;
					countIncrease+=1;
					bufleng+=addNumber;

				}else if(word.startsWith("\"") && word.endsWith("\".")){
					addNumber=3;
					countIncrease+=3;
					bufleng+=addNumber;

				}else if(word.matches("\\w+\'\\w+$")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;


				}else if(word.matches("\\w+-$")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}else if(word.matches("\\w+%")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}else if(word.matches("\\$\\w+")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}
				//else if(match.endsWith(".")&&abbreList.contains(word) && Character.isUpperCase(nextWord.charAt(0)) && !properNameList.contains(potentialPN.toString())){
				else if(match.endsWith(".")&&abbreList.contains(word) && Character.isUpperCase(nextWord.charAt(0)) && !properNameList.contains(potentialPN.toString())
						&& !properNameList.contains(potentialPN2.toString()) && !properNameList.contains(potentialPN3.toString())){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}else if(match.endsWith(",\"")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;

				}else if(word.startsWith("{") && !word.contains("}")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}else if(word.startsWith("}")&& !word.contains("{")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}else if(word.contains("[") && !word.contains("]")){
					addNumber=5;
					countIncrease+=5;
					bufleng+=addNumber;

				}
				//note: the order cannot be changed.
				//this morning of June 11, 2011, I met a very tricky thing:
				//-LRB- Details , Page A25 . -RRB- in 9802.233.sgm. The oringal text is [Details, Page A25.] in an independent line.
				//its bytes span is 1195-1197. But the output from CovertCot2Prob.java or from CorefUser.java is 1197-1199. I have spent a long time in figuring out
				//what wrong here. I counted from beginning to this place and didn't find anything wrong until I noticed that the part is regarded to be one 
				//independent sentence. This way, its beginning and its end should not count the space. So, 2 bytes should be subtracted. 
				else if(word.endsWith("],")){
					addNumber+=6;
					countIncrease+=6;
					bufleng+=addNumber;
				}
				else if(word.contains(".]")){
					addNumber=4;
					countIncrease+=4;
					bufleng+=addNumber;
				}
				else if(word.contains("]")&& !word.contains("[")){
					addNumber=5;
					countIncrease+=5;
					bufleng+=addNumber;

				}else if(word.contains("(") && !word.contains(")")){
					addNumber=5;
					countIncrease+=5;
					bufleng+=addNumber;

				}else if(word.contains(")")&& !word.contains("(")){
					addNumber=5;
					countIncrease+=5;
					bufleng+=addNumber;

				}else if(word.matches("[\"]\\w+\'\\w+$")){
					addNumber=3;
					countIncrease+=3;
					bufleng+=addNumber;

				}else if(word.matches("\\w+\';")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;

				}
				else if(word.matches("[\\[\\(]\\w+[-]*\\w+[\\]\\)]$")){
					addNumber=10;
					countIncrease+=10;
					bufleng+=addNumber;
				}
				else if(word.matches("[\\(\\[]\\w+[-]*\\w+[\\)\\]][,]$")){
					//cases like (R-Mo.) then it becomes -LRB- R-Mo . -RRB-
					///since the period is misregarded to be sentence delimiter, then, only 10 bytes is added. 
					//not sure if every case is like this. 
					addNumber=10;
					countIncrease+=10;
					bufleng+=addNumber;

				}else if(word.matches("[\\(\\[]\\w+[-]*\\w+[.][\\)\\]]$")){
					//cases like (R-Fla.), then it becomes -LRB- R-Fla . -RRB- ,
					//it seems that we should add 10 rather 11 since it becomes -LRB- R Fla. -RRB- ,
					addNumber=10;
					countIncrease+=10;
					bufleng+=addNumber;

				}else if(word.matches("[\\(\\[]\\w+[-]*\\w+[.][\\)\\]][,]$")){
					//cases like (R-Fla.), then it becomes -LRB- R-Fla . -RRB- ,
					//not sure which one is correct. In 9801.193.sgm. (R-Miss.), becomes -LRB- R-Miss . -RRB-, so, the final number should be 
					//11 rather than 13, I will check consistency later.
					addNumber=11;
					countIncrease+=11;
					bufleng+=addNumber;

				}else if(word.equals(".\"")){
					addNumber=4;
					countIncrease+=4;
					bufleng+=addNumber;

				}else if(word.matches("\\w+[?.]\"")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;

				}else if(word.matches("\\d+[/]\\d+[.]*$")){
					addNumber=1;
					countIncrease+=1;
					bufleng+=addNumber;

				}else if(word.matches("[\\(\\[]\\w+[-]*\\w+[.]\\w+[.][\\)\\]],")){
					//weird cases like (D-W.Va.),
					addNumber=13;
					countIncrease+=13;
					bufleng+=addNumber;
				}else if(word.matches("[\\(]\\w+[-]*\\w+[\\)][-]$")){
					addNumber=11;
					countIncrease+=11;
					bufleng+=addNumber;
				}else if(word.matches("\\w+[.]\\d+-")){
					addNumber+=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}else if(word.matches("\"\\w+-\\w+")){
					addNumber+=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				//else if(word.endsWith("...")){
				//menStart-=2;
				//menEnd-=2;
				//}						//note: this is a special case. In sgm file, ... is writeent as . . . . together with the period of previous sentence.
				//while stanford parser convert . . . . to ... Therefore, menStart and menEnd should reduce 3 rather than adding like other punctuations.
				else if(word.endsWith("....")){
					addNumber=3;
					countIncrease-=3;
					bufleng-=addNumber;
				}else if(word.endsWith("...")){
					//what on earth should we reduce three or two? In 9802.67.sgm at least, we should reduce 3. 
					//but in 9802.197.sgm and many others, we should reduce 2. I will recheck this later.
					addNumber=2;
					countIncrease-=2;
					bufleng-=addNumber;
				}

				if(special){
					addNumber=1;
					countIncrease+=1;
					bufleng+=addNumber;
				}
				//I am afraid that I need to take [word] into considerations

				//in the following, based on bufleng, we decide how many bytes should be added to mention spans. 
				//here, we always add bytes since, in the first while loop, we have removed all extra white spaces between words.
				//now, what we are doing is to split punctuation marks from the word. thus, bytes spans should become large.
				//but bufleng is the length where a punctuation is discovered. Thus, before that punctuation mark, the bytes span 
				//cannot change. After that punctuation, the bytes span should become large. 
				//it seems wordy. But I have to since after some time, it is always hard for me to remember that I have done. poor memory.
				boolean dup = false;
				Pair dupMentionSpan = null;
				for(int j=0;j<mentionSpanList.size();j++){
					//I think that I have found the true bug here. In this step, menBytePair may be called more than once. so, 
					//some items are added more bytes
					//another interesting happens here. In 9802.162.sgm, when i==142 and j==37, (At, the item for "New York", its span is 1235-1238.
					//after 5 bytes added to it, its span becomes 1240-1243. Coincidentally, the next word "area" has span as 1240-1243.
					//As a result, in last put of bytespanIdM, area disappears, instead, (1240-1243, New York) is there. 
					//Come to j==38, 1240-1243 is invoked and as a result, this item is deleted. In bytespanIdM, (1245-1248, New York) is added. 
					Pair mentionSpanPair = mentionSpanList.get(j);
					//now return here, if mentionSpanPair is duplicated, since we have marked the incoming mentionSpanPair, now, we need to check if 
					//the new mentionSpanPair is the duplicated one, if so, we need to make changes on this pair so that we can find the corresponding menId.
					//Note: in case you forget what you are doing, I add detailed comments here and also please read associated comments at the end of the 
					//following if clause.
					//it seems that we have to do such redundant check at each step even if the case is in fact rare. I don't know if there are better 
					//methods. There may be. For the moment, I can only do this way.
					Pair tempSpanPair = new Pair(mentionSpanPair.getFirst(),mentionSpanPair.getSecond()+"-2");
					String menId = "";
					//the first if is true, it implies that mentionSpanPair is duplicated. Otherwise, it is not.
					if(bytespanIdM.containsKey(tempSpanPair)){
						menId = bytespanIdM.get(tempSpanPair);
					}else{
						menId = bytespanIdM.get(mentionSpanPair);
					}


					//					if(debugBytesMatch){
					//						if(menId==null){
					//							System.out.println(mentionSpanPair);
					//						}else{
					//							System.out.println(menId);
					//						}
					//					}


					String neId = menId.substring(0,menId.indexOf("-"));
					Mention mention = idMentionM.get(menId);
					String refword = mention.getExtentCoveredText();
					//					if(debugBytesMatch){
					//						System.out.println(refword);
					//					}

					List<Pair> menList = idMentionListM.get(neId);
					//find the position in the menList
					int posInmenList = -1;
					for(int k=0;k<menList.size();k++){
						Pair menBytePair = menList.get(k);
						if(bytespanIdM.containsKey(tempSpanPair)){
							if(menBytePair.equals(tempSpanPair)){
								posInmenList = k;
								break;
							}
						}else{
							if(menBytePair.equals(mentionSpanPair)){
								posInmenList = k;
								break;
							}
						}

					}
					if(bytespanIdM.containsKey(tempSpanPair)){
						bytespanIdM.remove(tempSpanPair);
						dup=false;
					}else{
						bytespanIdM.remove(mentionSpanPair);
					}

					int menStart =Integer.parseInt(mentionSpanPair.o1.toString());
					int menEnd =Integer.parseInt(mentionSpanPair.o2.toString());
					if(debugBytesMatch){
						if(j==156||j==157||j==158||j==159){
							System.out.println(filename+" index i: "+i+" "+bufArray[i]+" j: "+j+" before reducing in adjustACEBytesspan: match length: bufleng: "+bufleng+" mention: "+mention.getHeadCoveredText()+" menStart: "+menStart+" menEnd: "+menEnd);
						}

					}
					//it seems that the following line should not be needed since the ending punctuation mark will not change the byte number.
					//					if(menStart>bufleng && !match.matches("\\w[!?.]$")){
					//						menStart = menStart+1;
					//						menEnd = menEnd+1;
					//					}else 
					//therefore, menStart should only add 2 bytes, cases like "ahead."" should change as "ahead . "". Namely, bytes number changes from 7 to 9. 	
					if(menStart>=bufleng-word.length()-addNumber){
						//the first if is unnecessary say, U.S. Troops will be changed as U.S .Troops. In this case, the bytes no changes at all
						//if(match.matches("\\w[.?!]")){
						//continue;
						//}else 
						//firstly, it is the single quote cases: 

						//cool, it seems that we can only use the following one if clause to include everything except that we need to consider 
						//some special cases which involve the beginning of the word such as [word]. 
						if(word.equals("no.")){
							if(menStart>=bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}

						}
						else if(match.endsWith("'") && match.charAt(match.length()-2)=='.'){
							if(menStart>=bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}	
						}
						else if(word.matches("\\w+[.]\\d+[.]\\d+")){
							if(menStart>=bufleng-addNumber){
								menStart++;
								menEnd++;
							}

						}
						//the following one is problematic???, I see, it means such cases "Calif.,"
						else if(match.matches("\\w+\\.,")){
							if(menStart>=bufleng-addNumber){
								menStart+=1;
								menEnd+=1;
							}
						}
						//the following else if is the same the next one, so it is right
						//else if(match.matches("\"\\w+[\"]*$")){
						//else if(word.matches("'\\w+$")){
						//cases like 'that's
						else if(word.matches("'\\w+'\\w+$")){
							//current token need changes as well
							if(menStart>=bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}
							else{
								menStart+=1;
								menEnd+=1;
							}

						}
						else if(word.startsWith("'") && !word.endsWith("'")){ 
							//should only increase by 2 since "what" becomes '' what ''. The right quotes only add one. 
							//current token will not change at all.
							if(menStart>=bufleng-addNumber){
								menStart+=1;
								menEnd+=1;
							}

						}else if(word.endsWith("'") && !word.startsWith("'")){ 
							//should only increase by 2 since "what" becomes '' what ''. The right quotes only add one. 
							//current token will not change at all.
							if(menStart>=bufleng-addNumber){
								menStart+=1;
								menEnd+=1;
							}
						}
						else if(word.startsWith("'") && word.endsWith("'")){
							//should only increase by 2 since "what" becomes '' what ''. The right quotes only add one. 
							//current token will not change at all.
							if(menStart>=bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}else{
								menStart+=1;
								menEnd+=1;
							}
						}

						else if(word.startsWith("'") && word.endsWith("'.")){
							//should only increase by 4 since "what" becomes '' what '' . The right quotes add 2. (but 
							//if it is the end of sentence, it only adds one).
							//note: begin bytes only increase by 2 and end byte should increase by 3. for the same reason as below
							if(menStart>=bufleng-addNumber){
								menStart+=1;
								menEnd+=1;
							}
						}
						//now go to the double quotes cases:
						//						else if(match.endsWith("\"") && match.charAt(match.length()-2)=='.'){
						//							if(menStart>=bufleng-2){
						//								menStart+=2;
						//								menEnd+=2;
						//							}	
						//						}
						//it seems that we need to separate cases where the whole word is ".\"" and where "w+.\"" since
						//".\"" will become ". '' ". that is, one more byte is added.
						else if(word.matches("w+[.]\"")){
							if(menStart>=bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}	
						}
						else if(match.matches("\\w[,:;]")&& !word.contains("'")&& !word.contains("/")&&!word.startsWith("\"")){
							if(menStart>=bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}

						}else if(word.matches("\\w+'\\w+[,:;]")){
							if(menStart>=bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}
						}
						else if(match.matches("\\w[,:;]")&& !word.contains("/")&&word.startsWith("\"")){
							if(menStart>=bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}else{
								menStart+=2;
								menEnd+=2;
							}

						}
						else if(match.matches("\\w[,:;]")&& word.contains("/")){
							if(menStart>=bufleng-addNumber){
								menStart+=2;
								menEnd+=2;
							}

						}
						//						else if(match.matches("\\w[,:;]")){
						//							if(menStart>=bufleng){
						//								menStart++;
						//								menEnd++;
						//							}
						//						}
						//the following else if is the same the next one, so it is right
						//else if(match.matches("\"\\w+[\"]*$")){
						else if(match.matches("\"\\w+$")){
							//current token need changes as well
							menStart+=2;
							menEnd+=2;
						}
						else if(!word.startsWith("\"") &&match.endsWith("\"") && match.matches("\\w+\"")){
							//should only increase by 2 since "what" becomes '' what ''. The right quotes only add one. 
							//current token will not change at all.
							if(menStart>=bufleng-addNumber){
								menStart+=2;
								menEnd+=2;
							}
						}
						else if(word.startsWith("\"") && word.endsWith("\"")){
							//should only increase by 4 since "what" becomes '' what '' . The right quotes add 2. (but 
							//if it is the end of sentence, it only adds one). 
							//note: current token will only increase by 2 since current token only changes due to the left quotes.
							if(menStart>=bufleng-addNumber){
								menStart+=4;
								menEnd+=4;
							}else{
								menStart+=2;
								menEnd+=2;
							}
						}
						else if(match.endsWith("\".")){
							//should only increase by 2 since "what" becomes '' what ''. The right quotes only add one. 
							//the current one will not change since the quite is at the end of the token rather than before.
							if(menStart>=bufleng-addNumber){
								menStart+=1;
								menEnd+=1;
							}
						}
						else if(match.startsWith("\"") && match.endsWith("\".")){
							//should only increase by 4 since "what" becomes '' what '' . The right quotes add 2. (but 
							//if it is the end of sentence, it only adds one).
							//note: begin bytes only increase by 2 and end byte should increase by 3. for the same reason as below
							if(menStart>=bufleng-addNumber){
								menStart+=3;
								menEnd+=3;
							}else{
								menStart+=2;
								menEnd+=2;
							}

						}
						else if(word.matches("\\w+\'\\w+$")){
							//because the quote is in the middle, so, the right of bytes will increase, namely, the endByte increases
							//but the beginByte is intact.

							if(menStart>=bufleng-addNumber){
								menStart++;
								menEnd++;
							}

						}else if(word.matches("\\w+-$")){
							if(menStart>=bufleng-addNumber){
								menStart++;
								menEnd++;
							}

						}else if(word.matches("\\w+%")){

							if(menStart>=bufleng-addNumber){
								menStart++;
								menEnd++;
							}
						}else if(word.matches("\\$\\w+")){
							menStart++;
							menEnd++;
						}
						else if(match.endsWith(".")&&abbreList.contains(word)&& Character.isUpperCase(nextWord.charAt(0)) && !properNameList.contains(potentialPN.toString())&& 
								!properNameList.contains(potentialPN2.toString()) && !properNameList.contains(potentialPN3.toString())){
							if(menStart>=bufleng-addNumber){
								menStart++;
								menEnd++;	
							}

						}
						else if(match.endsWith(",\"")){
							if(menStart>=bufleng-addNumber){
								menStart+=3;
								menEnd+=3;
							}

						}
						else if(word.startsWith("{") && !word.contains("}")){

							menStart++;
							menEnd++;


						}else if(word.startsWith("}")&& !word.contains("{")){
							if(menStart>=bufleng-addNumber){
								menStart++;
								menEnd++;
							}
						}
						else if(word.contains("[") && !word.contains("]")){
							menStart+=5;
							menEnd+=5;
						}
						//note the order of the following two cannot be changed. It is not good. 
						//but I don't have time to writer better code here. I may return it in future.
						else if(word.endsWith("],")){
							if(menStart>=bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}
						}else if(word.contains("]")&& !word.contains("[")){
							if(menStart>=bufleng-addNumber){
								menStart+=5;
								menEnd+=5;
							}
						}else if(word.contains("(") && !word.contains(")")){

							menStart+=5;
							menEnd+=5;


						}else if(word.contains(")")&& !word.contains("(")){
							if(menStart>=bufleng-addNumber){
								menStart+=5;
								menEnd+=5;
							}

						}else if(word.matches("[\"]\\w+\'\\w+$")){
							if(menStart>=bufleng-addNumber){
								menStart+=3;
								menEnd+=3;
							}else{
								menStart+=2;
								menEnd+=2;
							}

						}else if(word.matches("\\w+\';")){
							//cases like Strong';
							if(menStart>bufleng-addNumber){
								menStart+=2;
								menEnd+=2;
							}

						}
						//repeation, so, comment it.

						//					else if(word.matches("\'\\w+")){
						//						
						//							menStart++;
						//							menEnd++;
						//
						//		
						//					}
						else if(word.matches("[\\[\\(]\\w+[-]*\\w+[\\]\\)]$")){
							if(menStart>=bufleng-addNumber){
								menStart+=10;
								menEnd+=10;
							}else{
								menStart+=5;
								menEnd+=5;
							}
						}
						//I don't quite understand here????????, should add 11. but in the article, it seems that it is not correct to add 11.
						//I will check them later.
						//I know now, in 9801.193, (R-Mo.), the period after Mo. is misregarded to be sentence delimiter. Thus, new line 
						//incur and then only add 10 rather than add 11. But I am not sure if other places are like this.
						else if(word.matches("[\\[\\(]\\w+[-]*\\w+.[\\]\\)]$")){
							//cases like (R-Mo.) it becomes -LRB- R-Mo . -RRB-
							if(menStart>=bufleng-addNumber){
								menStart+=10;
								menEnd+=10;
							}else{
								menStart+=5;
								menEnd+=5;
							}
						}
						else if(word.matches("[\\[\\(]\\w+[-]*\\w+[\\]\\)][,]$")){

							if(menStart>bufleng-addNumber){
								menStart+=11;
								menEnd+=11;
							}else{
								menStart+=5;
								menEnd+=5;
							}
						}else if(word.matches("[\\(]\\w+[-]*\\w+[.][\\)][,]$")){
							//cases like (R-Fla.), it becomes -LRB- R-Fla . -RRB- ,
							//weird thing is that (R-Miss.), in 9801.193.sgm becomes -LRB- R-Fla . -RRB-,  
							if(menStart>bufleng-addNumber){
								menStart+=11;
								menEnd+=11;
							}else{
								if(menStart>=bufleng){
									menStart+=5;
									menEnd+=5;
								}

							}
						}else if(word.matches("[\\(]\\w+[-]*\\w+[\\)][-]$")){
							//cases like (Tuesday)- becomes -LRB- Tuesday -RRB- - from 9801.219.s	gm,  
							if(menStart>bufleng-addNumber){
								menStart+=11;
								menEnd+=11;
							}else{
								if(menStart>=bufleng){
									menStart+=5;
									menEnd+=5;
								}

							}
						}else if(word.equals(".\"")){
							if(menStart>=bufleng-addNumber){
								menStart+=2;
								menEnd+=2;
							}

						}else if(word.matches("\\w+[?.]\"")){
							if(menStart>=bufleng-addNumber){
								menStart+=2;
								menEnd+=2;
							}

						}else if(word.matches("\\w+/\\w+$")){
							if(menStart>=bufleng-addNumber){
								menStart++;
								menEnd++;
							}

						}else if(word.matches("[\\(\\[]\\w+[-]*\\w+[.]\\w+[.][\\)\\]],")){
							if(menStart>bufleng-addNumber){
								menStart+=13;
								menEnd+=13;
							}else{
								if(menStart>=bufleng){
									menStart+=5;
									menEnd+=5;
								}

							}
						}else if(word.matches("\\w+[.]\\d+-")){
							if(menStart>bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}
						}else if(word.matches("\\d+/\\d+[.]*")){
							if(menStart>bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}
						}else if(word.matches("\"\\w+-\\w+")){
							if(menStart>bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}
						}

//						else if(word.endsWith("...")){
//							menStart-=2;
//							menEnd-=2;
//						}						//note: this is a special case. In sgm file, ... is writeent as . . . . together with the period of previous sentence.
						//while stanford parser convert . . . . to ... Therefore, menStart and menEnd should reduce 3 rather than adding like other punctuations.
						//four dots should go before three dots. Otherwise, the if clause will never check four dots.
						else if(word.endsWith("....")){
							//it seems that bufleng plus addNumber should be correct one. But it seems that it doesn't matter since menStart should be always smaller
							//than it. But menEnd is not so. Since we only consider one word, so, it is not so strict.
							if(menStart>=bufleng+addNumber){
								menStart-=addNumber;
								menEnd-=addNumber;
							}
						}else if(word.endsWith("...")){
							if(menStart>=bufleng+addNumber){
								menStart-=addNumber;
								menEnd-=addNumber;
							}
						}

						if(special){
							if(menStart>=bufleng-addNumber){
								menStart+=addNumber;
								menEnd+=addNumber;
							}
						}
					}


					//else if(menStart>bufleng && match.) //please consider cases like he's, in stanford parser, it is split as he 's. 
					//Dingchegn Li smart guy though slow guys as well sometimes. 
					//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
					if(debugBytesMatch){
						if(j==156||j==157||j==158||j==159){
							System.out.println("after reducing in adjustACEBytesspan: shorterBuf length: "+shorterBuf.length()+" bufleng: "+bufleng+" mention: "+mention.getHeadCoveredText()+" "+menStart+" "+menEnd+" countIncrease: "+countIncrease);
						}
					}

					mentionSpanPair = new Pair(menStart,menEnd);
					mentionSpanList.set(j, mentionSpanPair);
					//the solution for duplicated item is to check if mentionSpanPair is existent in bytespanIdM.
					if(bytespanIdM.containsKey(mentionSpanPair)){
						dup=true;
						//if mentionSpanPair is in bytespanIdM, we need to mark it
						String dupMentionId = bytespanIdM.get(mentionSpanPair);
						dupMentionSpan = new Pair(mentionSpanPair.getFirst(),mentionSpanPair.getSecond()+"-2");
						bytespanIdM.put(dupMentionSpan, dupMentionId);
						bytespanIdM.put(mentionSpanPair, menId);
					}else{
						bytespanIdM.put(mentionSpanPair, menId);
					}

					if(posInmenList>=0){
						if(dup==true){
							menList.set(posInmenList, dupMentionSpan);
							idMentionListM.put(neId, menList);
						}else{
							menList.set(posInmenList, mentionSpanPair);
							idMentionListM.put(neId, menList);
						}

					}

				}

			}else{
				matchBuf2Stanform.append(word+" ");
			}


			int testleng = bufleng;
			testleng = 0;
			testleng ++;
			//System.out.println(i);
			preWord=word;
			addNumber=0;
		}
		if(debugBytesMatch){
			System.out.println("bufleng: "+bufleng+" matchBuf2Stanform length: "+matchBuf2Stanform.length());
			System.out.println(matchBuf2Stanform.toString());
		}

		Pattern resultPuncP = Pattern.compile("[\\s\'][!,?:;.\"\'%]\\w");
		Matcher matchResultPunc = resultPuncP.matcher(matchBuf2Stanform);
		boolean foundResultPunc = matchResultPunc.find();
		//repeat to use the same variable, preIndPunc, I don't think that this will lead to any problem as long as 
		//I reassign the value to it.
		preIndPunc = 0;
		while(foundResultPunc){
			String resultPunc = matchResultPunc.group();
			int indexPeriod = matchBuf2Stanform.indexOf(resultPunc,preIndPunc)+1;
			String sent = matchBuf2Stanform.substring(preIndPunc,indexPeriod+1);
			//cutShortBuf = cutShortBuf.substring(indexPeriod);
			//System.out.println(preIndPunc+" "+indexPeriod+" "+sent);
			//keep preIndPunc for outputing next sentence which should be a string between preIndPunc and indexPeriod
			preIndPunc = indexPeriod+1;
			//			if(resultPunc.matches("\\s.\\w")){
			//				int indexPeriod = matchBuf2Stanform.indexOf(resultPunc,preIndPunc)+1;
			//				String sent = matchBuf2Stanform.substring(preIndPunc,indexPeriod+1);
			//				//cutShortBuf = cutShortBuf.substring(indexPeriod);
			//				System.out.println(preIndPunc+" "+indexPeriod+" "+sent);
			//				//keep preIndPunc for outputing next sentence which should be a string between preIndPunc and indexPeriod
			//				preIndPunc = indexPeriod+1;
			//				//"\"" means ". Don't confuse \. If we want to print out \, we need \\.
			//			}else if(resultPunc.matches("\\s\"\\w") && resultPunc.charAt(resultPunc.length()-2)=='.'){
			//				int indexQuote = matchBuf2Stanform.indexOf(resultPunc,preIndPunc)+2;
			//				String sent = matchBuf2Stanform.substring(preIndPunc,indexQuote+1);
			//				//cutShortBuf = cutShortBuf.substring(indexPeriod);
			//				System.out.println(preIndPunc+" "+indexQuote+" "+sent);
			//				preIndPunc = indexQuote+1;
			//			}
			//although we can use indexOfPunc as an argument of find. It seems that we don't need to do so.
			//foundPunc = matchResultPunc.find(indexOfPunc);
			foundResultPunc = matchResultPunc.find();
		}
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
	public static ArrayList<CorefData> getKeyData(List<String> bsTokenList,HashMap<Pair,String> bytespanIdM,HashMap<String, Mention> idMentionM,HashMap<String,List<Pair>> idMentionListM, List<Pair> mentionSpanList){
		boolean firstCata = false;
		ArrayList<CorefData> keyDataList = new ArrayList<CorefData>();  
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



			if(sortedIndex==0 && ThirdPersonPron.thirdPerPronList2.contains(phrase)){
				firstCata = true;
				continue;
			}

			//System.out.println(menId+" "+word+" "+ThirdPersonPron.thirdPerPronList2.contains(word));
			if(debug){
				System.out.println("Entity "+sortedIndex+" "+menId+" "+phrase+" ");
				bsTokenList.add(bytespan+" "+phrase);
			}
			if(ThirdPersonPron.thirdPerPronList2.contains(phrase)||ThirdReflexives.thirdRefList2.contains(phrase)){

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
				if(preIndex != -1){
					corefByteSpan= menList.get(preIndex);	
					//System.out.println("corefByteSpan: "+corefByteSpan);
					String corefMenId = bytespanIdM.get(corefByteSpan);
					//System.out.println("corefMenId: "+corefMenId);

					Mention corefMention = idMentionM.get(corefMenId);
					//System.out.println("corefMention: "+corefMention.getHeadCoveredText());
					//since the word looks like the following: string = "he", we need to find the index of the left quote ".
					int corefStInd = 0;
					int corefEndInd = 0;
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
					CorefData keyData = new CorefData();
					keyData.setCorefIndex(sortedCorefIndex);
					keyData.setCorefWord(corefPhrase);
					keyData.setAnaIndex(sortedIndex);
					keyData.setAnaphor(phrase);
					keyDataList.add(keyData);
				}
			}
			sortedIndex ++ ;
		}
		return keyDataList;
	}
}
