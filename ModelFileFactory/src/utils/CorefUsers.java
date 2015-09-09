package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*import edu.mayo.bsi.nlp.ace02.utils.CorefData;*/
import annotation.Mention;
import annotation.NamedEntity;

public class CorefUsers {
	static boolean debug = false;
	static boolean nootherpron;
	static boolean headonly = true;
	static boolean debugBytesMatch = true;
	static ArrayList<String> abbreList = new ArrayList<String>();
	static ArrayList<String> specialList = new ArrayList<String>();
	static ArrayList<String> properNameList = new ArrayList<String>();
	static ArrayList<String> specialList2 = new ArrayList<String>();
	static ArrayList<String> specialList3 = new ArrayList<String>();
	static ArrayList<String> specialList4 = new ArrayList<String>();
	static ArrayList<String> weirdNameList = new ArrayList<String>();
	static HashMap<Integer,Boolean> redDotHm = new HashMap<Integer,Boolean>();
	static int phraseLen = 4;
	static boolean stripHead = false;

	public static void consSpeicalList(){
		//think about how to handle cases BC-Palestinian-Remembrance from APW19980514.1284.sgm. 
		//also in APW19980514.1284.sgm, I don't understand much why some sentences are missing so that 24 of 31 are handled while 7 of them are missing. probably,
		//it is Ok for the moment since we don't need accurate annotations of all data
		//weird things in APW19980219.0476.sgm, last lines are not parsed so that only 148 of 158 are extracted. 
		//Singapore-registered from APW19980219.0472.sgm, 
		abbreList.add("a.m.");
		abbreList.add("p.m.");
		abbreList.add("Co.");
		abbreList.add("Mass.");
		abbreList.add("U.S.");
		abbreList.add("Calif.");
		abbreList.add("Corp.");
		abbreList.add("Inc.");
		abbreList.add("Md.");
		abbreList.add("Dept.");
		abbreList.add("PLC.");
		abbreList.add("INC.");
		abbreList.add("Corp."); //from 9801.395.sgm
		abbreList.add("Va."); //from 9803.226.sgm
		specialList.add("no."); //from 9801.266, there is one sentence as "They are saying no. It's as simple as that. We". For some reason, "no." is parsed as "no. .", so a byte should be added.
		//the following words have been handled by some regular pattern 
		specialList.add("No.");
		specialList.add("Wis."); //from NYT19980212.0025.sgm
		//specialList.add("couldn't");
		//specialList.add("would've");
		specialList.add("cannot");
		//specialList.add("didn't");
		specialList.add("BC-Russia-Kalashnikov'sM");
		specialList.add("R.D.F.P,");
		specialList.add("c.1998");
		specialList.add("c.1997");
		specialList.add("F.2d");
		//in APW19980401.1133.sgm, there is a weird problem, due to annotator's error. after.'He two words are accidently typed together.
		specialList.add("RM4.00");
		specialList.add("level.'However");
		specialList.add("ENTR'ACTE");
		specialList.add("Colo."); //from NYT19980212.0018.sgm

		//I don't remember why I include GTE as a special one. I will check later. But now, it seems to bring troubels in NYT19980212.0014.sgm.
		//specialList2.add("GTE");

		specialList3.add("16-to-$");
		//firstly, I add the words to specialList3 in order to add 2 bytes. but later, I remember that ' within words will be added one byte. So, if 
		//added to specialList3, one extra byte will be added. Then, it should be added specialList instead.
		specialList.add("after.'He");
		specialList4.add("travelling"); //from APW19980304.0327.sgm
		specialList4.add("neighbours"); //from APW19980401.1133.sgm
		specialList4.add("colour"); //from APW19980522.0823.sgm
		specialList4.add("behaviour"); //from APW19980522.0823.sgm
		specialList4.add("d'etat"); //from APW19980219.0476.sgm
		specialList4.add("downpour"); //from NYT19980212.0016.sgm


		properNameList.add("U.S. Holocaust Memorial Museum");
		properNameList.add("U.S. Holocaust Memorial Council");
		properNameList.add("U.S. Aid in Limbo");
		properNameList.add("U.S. Embassy");
		properNameList.add("U.S. District");
		properNameList.add("W. STEVENSON");
		properNameList.add("U.S. Emphasis on Curbing"); //specifically for 9802.108.sgm though the phrase is not in fact a proper namer at all since it appears on the title
		//it leads to misjudgement. Namely, it regards U.S. is at the end of sentence. This is a simple but not elegant way to solve this problem.
		properNameList.add("U.S. Ambassador"); //also for 9802.108.sgm
		properNameList.add("U.S. Treasury"); //from 9802.233.sgm
		properNameList.add("U.S. Troops Capture Alleged"); //from 9801.139.sgm
		properNameList.add("U.S. Again on Using"); //from 9802.369.sgm
		properNameList.add("U.S. Support on Iraq"); //from 9802.417.sgm
		properNameList.add("U.S. Air Force"); //from 9802.431.sgm
		properNameList.add("Gen. Joseph"); //from 9802.431.sgm
		properNameList.add("U.S. Patriot"); //from 9802.431.sgm
		properNameList.add("U.S. May Fight Murdoch-MCI"); //from 9803.62.sgm
		properNameList.add("U.S. Urges"); //from 9803.107.sgm
		properNameList.add("U.S. Thrift"); //from 9803.224.sgm
		properNameList.add("U.S. Attorney"); //from 9804.319.sgm
		properNameList.add("J.P. Morgan,"); //from 9804.319.sgm
		properNameList.add("U.S. Ties"); //from 9806.90	.sgm
		properNameList.add("N.Y. Times"); //from 9806.71.sgm
		properNameList.add("U.S. Court"); //from 9806.122.sgm
		properNameList.add("U.S. Circuit"); //NYT19980212.0014.sgm
		properNameList.add("U.S. National"); //APW19980213.1304.sgm
		properNameList.add("H. Speights"); //from 9806.71.sgm
		properNameList.add("U.S. Undersecretary"); //from APW19980213.1309.sgm
		properNameList.add("U.S. Defense"); //from APW19980213.1337.sgm
		properNameList.add("U.S. Soccer Federation"); //from APW19980305.1201.sgm
		properNameList.add("U.S. Major League Soccer"); //from APW19980305.1201.sgm
		properNameList.add("U.S. Supreme"); //from 9801.175.sgm
		properNameList.add("U.S. Joint"); //from 9801.219.sgm
		properNameList.add("U.S. Pacific"); //from 9801.219.sgm
		properNameList.add("U.S. President"); //from APW19980517.0187.sgm
		properNameList.add("U.S. Secretary"); //from APW19980517.0187.sgm
		properNameList.add("U.S. AGENCIES"); //from NYT19980519.0466.sgm
		properNameList.add("W. STEVENSON"); //from NYT19980206.0460.sgm



		//the following is not a proper name but in order to avoid p.m. W makes p.m. . W. I temporarily add it to properNameList.
		properNameList.add("p.m. Wednesday"); //from APW19980213.1380.sgm

		weirdNameList.add("O'Brien"); //from 9803.62.sgm
		weirdNameList.add("O'Riordan"); //from 9806.122.sgm
		weirdNameList.add("O'Neil"); //from 9806.122.sgm
		weirdNameList.add("D'Amato"); //from 9806.122.sgm
		weirdNameList.add("O'Neal");
		weirdNameList.add("O'Leary"); //from NYT19980212.0012.sgm
		weirdNameList.add("O'Connor"); //from NYT19980212.0012.sgm
		weirdNameList.add("O'SMACH"); //from NYT19980212.0012.sgm
		weirdNameList.add("D'EMILIO"); //from APW19980219.0485.sgm
		weirdNameList.add("O'Donnell"); //from 9801.175.sgm
		weirdNameList.add("O'Hare"); //from 9801.327.sgm
		weirdNameList.add("O'Smach"); //from 9801.327.sgm
		weirdNameList.add("O'Dowd"); //from NYT19980526.0447.sgm
		weirdNameList.add("Ma'am"); //from NYT19980313.0434.sgm

	}

	/**
	 * the following code is copied from http://www.velocityreviews.com/forums/t138330-remove-punctuation-from-string.html
	 * @param s
	 * @return
	 */
	static public String stripPunctuation(String s) {

		//str = str.replaceAll("[^A-Za-z]", "");
		//or, if you want more than just ASCII characters:
		//str = str.replaceAll("[^\\p{L}]", "");
		//My original plan was to loop round the chars in the String and add them to
		//an array if the value of the chars are alphabetic (i.e. >=65 and <=122).
		//I've ran into problems with this and it seems more complex than the problem
		//should be.
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < s.length(); i++) {
			if ((s.charAt(i) >= 65 && s.charAt(i) <= 90) || (s.charAt(i) >= 97 &&
					s.charAt(i) <= 122)) {

				sb = sb.append(s.charAt(i));
			}
		}

		return sb.toString();
	}


	/**
	 * the following code is revised for my need
	 * @param s
	 * @return
	 */
	static public String stripWordPunc(String s) {

		//str = str.replaceAll("[^A-Za-z]", "");
		//or, if you want more than just ASCII characters:
		//str = str.replaceAll("[^\\p{L}]", "");
		//48 to 57 is the number 0-9

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < s.length(); i++) {
			if ((s.charAt(i) >= 48 && s.charAt(i) <= 57) || (s.charAt(i) >= 65 && s.charAt(i) <= 90) || (s.charAt(i) >= 97 &&
					s.charAt(i) <= 122)) {
				sb = sb.append(s.charAt(i));
			}else{
				if(stripHead){
					//the following if aims at not stripping puncts in the middle of word. But we should remove puncts in the beginning and in the end.
					if(i!=0){
						for(int j=i+1;j<s.length();j++){
							if((s.charAt(j) >= 48 && s.charAt(j) <= 57)||(s.charAt(j) >= 65 && s.charAt(j) <= 90) || (s.charAt(j) >= 97 &&
									s.charAt(j) <= 122)){
								sb = sb.append(s.charAt(i));
								break;
							}
						}
					}
				}else{
					for(int j=i+1;j<s.length();j++){
						if((s.charAt(j) >= 48 && s.charAt(j) <= 57)||(s.charAt(j) >= 65 && s.charAt(j) <= 90) || (s.charAt(j) >= 97 &&
								s.charAt(j) <= 122)){
							sb = sb.append(s.charAt(i));
							break;
						}
					}
				}
			}

		}
		return sb.toString();
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
		//System.out.println(" 21th: "+mentionSpanList.get(21)+" 22th: "+mentionSpanList.get(22)+" 187 th: "+mentionSpanList.get(187)+" 188 th: "+mentionSpanList.get(188)+" 189 th: "+mentionSpanList.get(189)+" 190 th: "+mentionSpanList.get(190));
		CorefUsers.consSpeicalList();
		//I remember. \\s+ means that more than one white space. I don't remember 
		//why expressions before \\s+ can refer to digits. But obviously, it can. I will think about it later. I know why \\w include letters and digits
		//"\\([-\\w!,?:;.$\"`\'%]+\\s[-\\w!,?:;.$\"`\'%/\\\\]+\\)"
		//new bugs are found. June 12, 2011. The bugs are it is not enough to only count spaces between words. The reason is that for cases like "We in 9802.417.sgm
		//the annotation is We rather than "We. If we only remove spaces, the starting index would start from " rather than W. So, now, I need to add punctuation marks as part of
		//bytes between two words and these bytes should not be counted into menStart. But it seems that menEnd doesn't have such problems since I always use the annotated length to 
		//add. Then, punctuation marks at the end of the word would not be counted.
		//Pattern pattern = Pattern.compile("[-)\\w!$,?:;.\"\'%]\\s+[\"'\\[\\(]*");
		//the above reasoning is wrong since we should not exclude punctuation of the counts of bytes here since we have considered that in the following steps. Further, unlike space,
		//punctuation marks play roles and they are still existent after Stanford parsing.
		Pattern pattern = Pattern.compile("[-)\\w\\]!$,?:;.\"\'%]\\s+");
		if(debugBytesMatch){
			System.out.println(docBuf.toString());
		}

		//Matcher matcher = pattern.matcher(docBuf.toString());
		String tempDoc = docBuf.toString();
		StringBuffer shorterBuf = new StringBuffer();
		Matcher matcher = pattern.matcher(tempDoc);
		//Matcher matcher = pattern.matcher(docBuf);
		boolean found = matcher.find();
		//countFound counts how many spaces between words are found. it should be the number of words in an article
		int countFound = 0;
		//countFound2 counts how many cases which involve more than one space. That is what matters.
		int countFound2=0;
		////the while loop finish one thing: remove extra space between words. 
		//I feel that methods used here are kind of stupid. But I don't want to bother to change since it is after all a detailed way 
		//to make use of regular expressions. But I do need good documentation to remember what I have done here. 
		//basically, I firstly found the last letter of each word and all the while space after the word. 
		//then, inside the if clause, I am trying to find the first letter of the word. Then, catch the whole word and append it to shorterBuf.
		//The method works but it may miss some letters if the regular expression is incomplete. For example, If the word is "(At", namely, the word
		//starts with a bracket, I need to add [(] to firstL pattern, otherwise, we can only find "At" rather than "(At". For regular expressions, 
		//inside "[]", for special symbols such as "(", we can still write "(" rather than "\\(" when they are alone. This is not always true, yet. 
		//I don't quite understand yet why "[-\\$\\w!,\\(\\[?:;.\"\'%]" works but "[-\\(\\$\\w!,\\[?:;.\"\'%]" doesn't work.
		int tokenCount = 0;
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
			//int spaceStart = docBuf.indexOf(match, spaceStart+1);
			String matchedFl = "";
			String appendToken = "";
			if(countFound>0){
				StringBuffer firstPart = new StringBuffer();				
				//firstPart.append(tempDoc.charAt(0));

				Pattern firstL = Pattern.compile("[-\\$\\w!,)(\\[?:;.`\"\'%]");
				//Pattern firstL = Pattern.compile("w");
				//Pattern firstL = Pattern.compile("\\w!,.\"\'%.*");
				//Matcher matchFirstL = firstL.matcher("word");
				Matcher matchFirstL = firstL.matcher(tempDoc.substring(1));
				boolean foundLetter = matchFirstL.find();

				if(foundLetter){
					matchedFl = matchFirstL.group();
					//System.out.println("matchedF1 in foundLetter : "+matchedFl+" the first index: "+tempDoc.substring(1).indexOf(matchedFl)+" spaceStart: "+spaceStart+" "+tempDoc);
					//firstPart.append(tempDoc.substring(tempDoc.substring(1).indexOf(matchedFl)-1));
					//note: since matchedFl starts from the last letter of each word. So,tempDoc.substring(1).indexOf(matchedFl) will start from 
					//the space. If we add 1 to it, we will get the full word after the space.
					appendToken = tempDoc.substring(tempDoc.substring(1).indexOf(matchedFl)+1,spaceStart+1);
					//System.out.println(appendToken);
					//I remember now since in Stanford parser, elliptical marks are always reduced to some dots without space. Say, word . . . will become word...
					//I don't remember why I append "." to appendToken without space. It seems troubles may be brought about in NYT19980206.0485.sgm since the shorterBuf
					//has fewer bytes than the actual bytes for mentions. Let me temporarily add space and check if it works and in future, if other troubles incur, I will 
					//redo this issue. Since the issue in NYT19980206.0485.sgm is minority, I will not use the space again. But in order to handle the inconsisentency in ACE corpus
					//such as in APW19980227.0469.sgm, the token has been justify... . in this case, if we follow the convention, 2 bytes will be subtracted in RE expressions below.
					//in order to handle this case, I need to a boolean list to record this. I think.
					if(appendToken.equals(".")){
						firstPart.append(appendToken);
						//the following if clause may not be necessary. But I think that I should following programming convention to always make checks.
						if(redDotHm.get(tokenCount)==false){
							redDotHm.put(tokenCount, true);
						}
					}else{
						tokenCount++;
						firstPart.append(" "+appendToken);
						//note: if there is one word where elliptical marks follows word . . .
						//redDotHm firstly will still put a false value here. But when dot comes in, the true value will be reset for the key. So, the following line should be Ok.
						redDotHm.put(tokenCount, false);

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
					//String refphrase = mention.getExtentCoveredText();
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
					int menStart =Integer.parseInt(mentionSpanPair.getFirst().toString());
					int menEnd =Integer.parseInt(mentionSpanPair.getSecond().toString());
					if(debug){
						if(i==112||i==113||i==114){
							System.out.println((shorterBuf.length()-1)+" menStart: "+menStart+" "+appendToken+" countFound2: "+countFound2 +" i="+i);
						}
					}

					//System.out.println("before reducing in adjustACEBytesspan: match length: "+match.length()+" "+menStart+" "+menEnd);
					//note: here, shorterBuf is increasing from beginning to end. It doesn't include all articels yet.
					if(menStart>shorterBuf.length()){
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
				countFound2++;
			}
			tempDoc = tempDoc.substring(spaceStart);
			//System.out.println(tempDoc.toString());
			//System.out.println("shorterBuf: "+shorterBuf.toString());
			found = matcher.find();
			countFound++;
		}


		System.out.println("shorterBuf leng: "+shorterBuf.length()+" "+tokenCount+" "+countFound);
		System.out.println(shorterBuf);

		//after we get shorterBuf, we need to further revise it to map to stanford sentence format. 
		//so far, the punctuations are not split yet. 
		//that is, the following for loop aims at split punctuation marks. 
		//in fact, the space between words among shorterBuf is only one. So, split("\\s+") and replace("\\s+","") is not necessary
		//if there are really more than one space between words, trouble will be brought about. The key thing is to guarantee from docBuf to shorterBuf is correct.  
		//Now, it seems that there are troubles. It is found that when line changes in the original texts, space between two words is in fact two rather than one. 
		String[] bufArray = shorterBuf.toString().split("\\s");
		//String cutShortBuf = shorterBuf.toString();
		System.out.println("bufArray length: "+bufArray.length);
		StringBuffer matchBuf2Stanform = new StringBuffer(); 
		int bufleng = 0;
		int preIndPunc = 0;
		String preWord = "";
		int countIncrease = 0;
		//System.out.println("21: "+bufArray[21]+" 22th: "+bufArray[22]+" 187 "+bufArray[187]+" 188: "+bufArray[188]+" 189 "+bufArray[189]);
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
			//assign dummy string to them if they don't have values 
			if(potentialPN.toString().isEmpty()){
				potentialPN.append("-");
			}
			if(potentialPN2.toString().isEmpty()){
				potentialPN2.append("-");
			}
			if(potentialPN3.toString().isEmpty()){
				potentialPN3.append("-");
			}

			String nextWord = "";

			if(i<bufArray.length-1){
				nextWord = bufArray[i+1].replace("\\s+", "");
			}
			if(nextWord.isEmpty()){
				nextWord="-";
			}
			//bufleng needs to add the byte of white space
			//meanwhile, we should count from 0 since although it is the length of buf, we compare it with the byteStart or bytesEnd.
			//So, we need to deduct the length to 1 in order to avoid missing.
			if(i==0){
				bufleng+=word.length();
			}else if(word.equals("\\(")||word.equals("\\)")){
				//if word is only the bracket, we should not add anything since later, 5 will be added. If added now. one more byte is added.
				bufleng+=0;
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
			//it seems that we don't need to keep a specialList2, since the pattern is handled in the regular expressions.
			//word.matches("[\\w+-]+\\w+[,.]\\w+") && !word.matches("\\d+[,.]\\d+") 
			String word1 =CorefUsers.stripWordPunc(word);
			if(specialList.contains(word)){
				//other specials include "GTE Corp." in 9801.395.sgm. Probably, due to the prob model, GTE Corp. is regarded to 
				//be located at the end of sentence. But in fact, it is not since after it is "and". Or because the sentence 
				//is too long. So, I have to hard code it.
				special = true;
			}

			boolean special3=false;
			if(specialList3.contains(word)){
				special3=true;
			}

			boolean special4=false;

			//stripWordPunc will not remove puncs in the middle like d'etat from APW19980219.0476.sgm
			String word4 = CorefUsers.stripWordPunc(word);
			if(specialList4.contains(word4)){
				special4=true;
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

			//puncP7 and puncP9 can be reduced to one
			Pattern puncP7 = Pattern.compile("[\\[\\(]\\w+[-]*\\w+$");
			Matcher matchPunc7 = puncP7.matcher(word);
			boolean foundPunc7 = matchPunc7.find();

			Pattern puncP8 = Pattern.compile("^\\w+[\\]][,;:]*$");
			Matcher matchPunc8 = puncP8.matcher(word);
			boolean foundPunc8 = matchPunc8.find();

			Pattern puncP9 = Pattern.compile("[\\(\\[]+\\w+$");
			Matcher matchPunc9 = puncP9.matcher(word);
			boolean foundPunc9 = matchPunc9.find();

			Pattern puncP10 = Pattern.compile("\\w+[.]*[\\)\\]][,;:]*$");
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

			Pattern puncP19 = Pattern.compile("[\\(\\[]+\\w+[\\)\\]+][,]*");
			Matcher matchPunc19 = puncP19.matcher(word);
			boolean foundPunc19 = matchPunc19.find();
			System.out.println(word);
			Pattern puncP20 = Pattern.compile("[`]+\\w+");
			Matcher matchPunc20 = puncP20.matcher(word);
			boolean foundPunc20 = matchPunc20.find();

			Pattern puncP21 = Pattern.compile("\\)");
			Matcher matchPunc21 = puncP21.matcher(word);
			boolean foundPunc21 = matchPunc21.find();

			Pattern puncP22 = Pattern.compile("\\w+[;,]\\w+");
			Matcher matchPunc22 = puncP22.matcher(word);
			boolean foundPunc22 = matchPunc22.find();

			if(foundPunc || foundPunc2 ||foundPunc3 || foundPunc4 || foundPunc5||foundPunc6||foundPunc7||foundPunc8||foundPunc9||foundPunc10||foundPunc11||foundPunc12
					||foundPunc13||foundPunc14||foundPunc15||foundPunc16||foundPunc17 ||foundPunc18||foundPunc19
					||foundPunc20|| foundPunc21 || foundPunc22 || special||special3||special4){
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
				//I don't remember that the first if aims at what? In fact, it hurts in APW19980306.1001.sgm where no. doesn't add anything.
				//I temporarily comment it.
				//				if(word.equals("no.")){
				//					addNumber = 1;
				//					countIncrease++;
				//					bufleng+=addNumber;
				//				}

				//else. Weird, I didn't take the following pattern into consideration until now.
				//sample is 'Lies' from 9801.404.sgm. 
				if(word.matches("'{1,2}\\w+'{1,2}[\\.]*")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				//handle cases like ''human'', from APW19980522.0823.sgm
				if(word.matches("'{1,2}\\w+'{1,2},")){
					addNumber=3;
					countIncrease+=3;
					bufleng+=addNumber;
				}
				//the following one refers to pattern like ``word
				else if(word.matches("`{1,2}[\\w]+[!.?]*$")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;
				}
				else if(word.matches("`{3}[\\w]+[,:;]$")){
					addNumber=3;
					countIncrease+=3;
					bufleng+=addNumber;
				}
				//the following one refers to pattern like ``Michael, in NYT19980206.0471.sgm.
				else if(word.matches("`{1,2}[\\w]+[,:;]$")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				else if(word.matches("`{1,2}[\\w]+['][\\w]+$")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				////cases from NYT19980206.0461.sgm. This paper is kind of annoying since it involves many double quotes using latex traditions. i.e. " is written as ''
				else if(word.matches("'{1,2}[\\w]+[,]")){
					addNumber = 2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				//cases from NYT19980206.0461.sgm. Also, we should include cases like 1,000,'' from APW19980227.0469.sgm
				else if(word.matches("[\\d+,]*[\\w]+[,;:]'{1,2}")){
					addNumber = 2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				//cases like CANB101,102,103 from APW19980213.1310.sgm
				//the following pattern can be ["\\w+,\\w+,\\w+"]. But since w+ covers wider choice and I am afraid that this is not true for letters, I use the 
				//following narrower pattern instead.
				else if(word.matches("\\w+,\\d+,\\d+")){
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
				//the following two may be merged into one.
				else if(word.matches("'{1,2}[a-zA-Z]+$")){
					//current token need changes as well
					addNumber = 1;
					countIncrease++;
					bufleng+=addNumber;
				}
				//else if(word.matches("'[a-zA-Z]+$")){
				//current token need changes as well
				//addNumber = 1;
				//countIncrease++;
				//bufleng+=addNumber;
				//}
				//cases like "hang-ups,''" from APW19980319.0652.sgm
				else if(word.matches("\\w+\\-\\w+,'{1,2}")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;	
				}
				//cool, it works for cases like ``nonsense,'' from APW19980227.0487.sgm.
				else if(word.matches("`{1,2}\\w+,'{1,2}")){
					addNumber=3;
					countIncrease+=3;
					bufleng+=addNumber;
				}
				else if(word.matches("`{1,2}\\w+[\\.]*'{1,2}")){//``immediate.'' from NYT19980313.0434.sgm
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				//so, the following pattern can be changed to
				//else if((word.startsWith("`")||word.startsWith("'")) && word.endsWith("'")){
				//cool, cases like ``soullessness''. from APW19980526.1320.sgm is handled.
				else if(word.matches("`{1,2}\\w+'{1,2}[\\.\\?!]*")){
					//note: current token will only increase by 2 since current token only changes due to the left quotes.
					addNumber = 2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				else if(word.matches("\\w+'{1,2}[\\.\\?!]*")){//going''? from after reducing in ad is handled here
					addNumber = 1;
					countIncrease+=1;
					bufleng+=addNumber;
				}
				else if(word.matches("\\w+'\\w+[,;:]'{1,2}")){
					addNumber = 3;
					countIncrease+=3;
					bufleng+=addNumber;
				}
				else if(word.matches("\\w+[\\.]*'{3}")){
					addNumber = 2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				//cases ```They from APW19980328.0684.sgm 
				else if(word.matches("`{3}\\w+")){
					addNumber = 2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				else if(word.matches("^\\w+[:\\w+]*[\\.\\?!]*'{1,2}$")){ 				//the following pattern seems to be able to replace the one I commented it. 
					addNumber = 1;
					countIncrease++;
					bufleng+=addNumber;
				}
				//the following pattern is not so good. I need to revise it later.
				//				else if(word.endsWith("'") && (!word.startsWith("'") && !word.startsWith("`")) && (!word.contains(",")&&!word.contains(";")&&!word.contains(":"))){ 
				//					//current token will not change at all.
				//					addNumber = 1;
				//					countIncrease++;
				//					bufleng+=addNumber;
				//
				//				}
				else if(word.startsWith("'") && word.endsWith("'.")){
					addNumber =1;
					countIncrease++;
					bufleng+=addNumber;
				}
				//I know why, I used word.endWith, it is not supposed to be a regular expression, 9803.461.sgm
				//?!. should go tegether
				else if(word.matches("'\\w+'[,;:]")){
					//else if(word.startsWith("'") && word.endsWith("';")){
					addNumber =3;
					countIncrease++;
					bufleng+=addNumber;
				}
				//it seems that we need to separate cases where the whole word is ".\"" and where "w+.\"" since
				//".\"" will become ". '' ". that is, one more byte is added.
				//cases like well-positioned." from 9804.266.sgm, it needs the following pattern
				else if(word.matches("[\\w]*[\\-]*\\w+[.]\"")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;

				}else if(word.matches("\\({2}\\w+")){
					addNumber=10;
					countIncrease+=10;
					bufleng+=addNumber;

				}else if(word.matches("\\w+\\){2}$")){
					addNumber=10;
					countIncrease+=10;
					bufleng+=addNumber;
				}
				else if(word.matches("[\\w+\\-]+\\${1}[\\w+\\-]*\\w+$")){
					addNumber=3;
					countIncrease+=3;
					bufleng+=addNumber;
				}
				else if(word.matches("[\\[\\(]\\w+[.]*[,:;]")){
					//(Eds.: from  NYT19980407.0272.sgm is handled here. 
					//I think that we can add 6. there are some other errors from the first step above. That is why adding 6 was wrong. Now it is correct. 
					addNumber=6;
					countIncrease+=6;
					bufleng+=addNumber;
				}
				//the following pattern catches cases like 9.09% in APW19980401.1133.sgm.
				//patterns like 70-75% from APW19980506.1426.sgm can also be caught here 
				else if(word.matches("\\w+[\\.\\-\\w+]*[,:;%][.]*")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;
				}
				else if(word.matches("\\w+[\\.]\\w+[\\.]\\w+[,;:]")){
					addNumber=2;
					countIncrease++;
					bufleng+=addNumber;
				}
				//cases like Corp.; from 9806.164.sgm
				else if(match.matches("\\w+[.]*[,:;]$")&& !word.contains("'") && !word.contains("/") && !word.startsWith("\"") && !word.startsWith("[") 
						&& !word.endsWith(")")&&!word.endsWith("]")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}
				//http://www.seeingwithc.org/topic7html.html where how to use dash is explained. Like any other special characters, we should use \\- in java
				//else if(word.matches("\\w+\\-\\w+\\-\\w+'\\w+")){
				else if(word.matches("[\\w+\\-]+\\w+'\\w+")){
					//cases like BC-Russia-Kalashnikov'sM from APW19980227.0468.sgm
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;
				}
				else if(word.matches("\\w+[\\-]*\\w+'\\w+")){
					//cases like Saro-Wiwa's from 9804.338.sgm
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;
				}
				else if(match.matches("\\w[,:;]")&& !word.contains("/") && word.startsWith("\"")){
					addNumber=3;
					countIncrease+=3;
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
				//the following one in 9803.62.sgm, the example is "slot,"=> '' slot , '' therefore, we should add 5 rather than 4. 
				else if(word.startsWith("\"") && word.endsWith(",\"")){
					addNumber=5;
					countIncrease+=5;
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

				}
				//the following else if is the same the next one, so it is right
				//else if(match.matches("\"\\w+[\"]*$")){ //come on, I didn't consider cases like "''It's" from NYT19980206.0468.sgm until today
				//unbelievable. June 28, 11.
				else if(word.matches("'{1,2}\\w+'\\w+")){
					System.out.println(word.substring(0,word.length()));
					if(!weirdNameList.contains(word.substring(0,word.length()))){
						addNumber=2;
						countIncrease+=2;
						bufleng+=addNumber;
					}
				}
				//awesome, the patter catches cases like "level.'However," in APW19980401.1133.sgm where the annotator typed wrongly the two words as one. 
				//so, I have to add the word to the specialLsit for simplicity though I have other ways. Then, I use the following pattern to add 2 more bytes.
				//it works well now.
				else if(word.matches("\\w+[\\-]*\\w+[\\.]*'\\w+[,:;]")){
					//System.out.println(word.substring(0,word.length()-1));
					//cases like O'Brean,
					//also cases like Ma'am, in NYT19980313.0434.sgm is handled by the first if.
					if(weirdNameList.contains(word.substring(0,word.length()-1))){
						addNumber=1;
						countIncrease+=1;
						bufleng+=addNumber;
					}else{
						addNumber=2;
						countIncrease+=2;
						bufleng+=addNumber;
					}	
				}
				//cases like Johnson's. from 9802.438.sgm
				else if(word.matches("\\w+[.]*'\\w+[.]$")){	
					//	System.out.println(word.substring(0,word.length()-1));
					if(word.endsWith(".")&&!weirdNameList.contains(word.substring(0,word.length()-1))){
						addNumber=1;
						countIncrease++;
						bufleng+=addNumber;
					}

				}else if(word.matches("\\w+[.]*'\\w+$")){	
					//	System.out.println(word.substring(0,word.length()-1));
					if(!weirdNameList.contains(word.substring(0,word.length()))){
						addNumber=1;
						countIncrease++;
						bufleng+=addNumber;
					}

				}else if(word.matches("\\w+-$")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}else if(word.matches("\\w+%")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}
				//not sure if the following one with period is from which one. Anyway, I will keep it for the moment
				//but $230,000 from NYT19980212.0012.sgm is working. It is interesting to know that we can use double brackets as follows
				else if(word.matches("\\$[\\w+[.,]]*\\w+[\\.]*")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;
				}
				else if(word.matches("\\[\\[\\w+[.]*[\\w]*\\]\\]")){
					addNumber=20;
					countIncrease++;
					bufleng+=addNumber;
				}
				//else if(match.endsWith(".")&&abbreList.contains(word) && Character.isUpperCase(nextWord.charAt(0)) && !properNameList.contains(potentialPN.toString())){
				//it seems that Character.isUpperCase(nextWord.charAt(0) is not reliable anymore. Sunbeam Corp. health from 9806.202.sgm still regards it as the end of a sentence.
				//similar case happens to 9801.395.sgm where GTE Corp. and other ... is regarded to the end of the sentence. But there are conflicting cases in the same article
				//where "AT&T Corp." made is not regarded to the end of the sentence. I don't know how to handle it yet. I will reconsider it.
				else if(match.endsWith(".")&&abbreList.contains(word)  && Character.isUpperCase(nextWord.charAt(0))&& !properNameList.contains(potentialPN.toString())
						&& !properNameList.contains(potentialPN2.toString()) && !properNameList.contains(potentialPN3.toString())){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}else if(match.endsWith(",\"")){
					addNumber=3;
					countIncrease+=3;
					bufleng+=addNumber;

				}else if(word.startsWith("{") && !word.contains("}")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}else if(word.startsWith("}")&& !word.contains("{")){
					addNumber=1;
					countIncrease++;
					bufleng+=addNumber;

				}
				//I think that I need to reduce all similar brackets into one rule. cases like [Greek-Greek from 9806.183.sgm can use more general rules.
				else if(word.contains("[") && !word.contains("]")){
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
				//note: the pattern I added [,\\w+]* aims at finding 69,000), from  APW19980219.0507.sgm
				else if(word.matches(("\\w+[,\\w+]*[\\)\\]][,:;]"))||word.matches(("\\w+[,\\w+]*[,:;][\\)\\]]"))){
					addNumber+=6;
					countIncrease+=6;
					bufleng+=addNumber;
				}
				else if(word.matches("\\w+[\\.]'{1,2}[\\)\\]]")){//pattern like recall.'') from NYT19980313.0434.sgm is handled here 
					addNumber+=6;
					countIncrease+=6;
					bufleng+=addNumber;
				}
				else if(word.matches(("\\w+[\\)\\]][,:;]'{1,2}"))||word.matches(("\\w+[,:;][\\)\\]]'{1,2}"))){
					addNumber+=7;
					countIncrease+=7;
					bufleng+=addNumber;
				}
				//the following second pattern can handle cases like 11,000). from  APW19980626.0364.sgm
				else if(word.matches("[\\w+\\-]*\\w+[.]*[\\]\\)]")||word.matches("[\\w+,]*[\\]\\)][.]*")){
					addNumber=5;
					countIncrease+=5;
					bufleng+=addNumber;
				}
				else if(word.contains("]")&& !word.contains("[")){
					addNumber=5;
					countIncrease+=5;
					bufleng+=addNumber;

				}
				//starting from here::

				else if(word.contains("(") && !word.contains(")")){
					addNumber=5;
					countIncrease+=5;
					bufleng+=addNumber;

				}
				//the following aims at considering the word is ended with ")".But the pattern is not good.
				else if(word.matches("\\w+\\)$")&& !word.contains("(")){
					addNumber=5;
					countIncrease+=5;
					bufleng+=addNumber;
				}else if(word.matches("[\"]\\w+\'\\w+$")){
					addNumber=3;
					countIncrease+=3;
					bufleng+=addNumber;
				}else if(word.matches("\\w+\'[,:;]")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				//weird, it is not until today (July 2, 2011) that I add this pattern to handle cases like grandparents'. from  APW19980526.1320.sgm.
				//sigh! what a smart head!
				else if(word.matches("\\w+\'[.?!]")){
					addNumber=1;
					countIncrease+=1;
					bufleng+=addNumber;

				}
				else if(word.matches("[\\[\\(]\\w+[-]*\\w+[\\]\\)]$")){
					addNumber=10;
					countIncrease+=10;
					bufleng+=addNumber;
				}
				//the second pattern handles cases like (yard)slick from APW19980219.0472.sgm
				else if(word.matches("[\\(\\[]\\w+[-]*\\w+[.]*[\\)\\]][,]$") ||word.matches("[\\(\\[]\\w+[\\)\\]]\\w+")){
					//cases like (R-Fla.), then it becomes -LRB- R-Fla . -RRB- ,
					//we should add 11 since it becomes -LRB- R Fla. -RRB- ,
					//[1989], from 9806.443.sgm
					addNumber=11;
					countIncrease+=11;
					bufleng+=addNumber;

				}else if(word.matches("[\\(\\[]\\w+[-]*\\w+[.][\\)\\]]$")){
					//cases like (R-Mo.) then it becomes -LRB- R-Mo . -RRB-
					///since the period is misregarded to be sentence delimiter, then, only 10 bytes is added. 
					//not sure if every case is like this. 

					addNumber=10;
					countIncrease+=10;
					bufleng+=addNumber;

				}
				//				else if(word.matches("[\\(\\[]\\w+[-]*\\w+[.][\\)\\]][,]$")){
				//					//cases like (R-Fla.), then it becomes -LRB- R-Fla . -RRB- ,
				//					//not sure which one is correct. In 9801.193.sgm. (R-Miss.), becomes -LRB- R-Miss . -RRB-, so, the final number should be 
				//					//11 rather than 13, I will check consistency later.
				//					addNumber=11;
				//					countIncrease+=11;
				//					bufleng+=addNumber;
				//
				//				}
				else if(word.matches("[\\(\\[]\\w+[']\\w+[\\)\\]]$")){ //cases like [Hill's] from 9803.290.sgm
					//cases like (R-Fla.), then it becomes -LRB- R-Fla . -RRB- ,
					//not sure which one is correct. In 9801.193.sgm. (R-Miss.), becomes -LRB- R-Miss . -RRB-, so, the final number should be 
					//11 rather than 13, I will check consistency later.
					addNumber=11;
					countIncrease+=11;
					bufleng+=addNumber;

				}
				//why add 4, it seems incorrect. For example, in " anxiety. . . ." " of 9801.327.sgm, firstly, the elliptical mark becomes anxiety .... '' 
				//that is, nothing changed happens in bytes. for the following, only 2 should be added.
				else if(word.equals(".\"")){
					//addNumber=4;
					//countIncrease+=4;
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;

				}else if(word.matches("\\w+[!?.]\"")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;

				}
				//w+ can represent both words and numbers, so, cases like 5/8, from 9804.341.sgm will match the following type
				else if(word.matches("\\w+/\\w+[,:;]")){
					addNumber=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}
				else if(word.matches("\\w+/\\w+$")){
					addNumber=1;
					countIncrease+=1;
					bufleng+=addNumber;
				}
				else if(word.matches("\\d*[-]*\\d+[/]\\d+[.]*$")){
					addNumber=1;
					countIncrease+=1;
					bufleng+=addNumber;
				}
				//cases like  BEI-102/3 APW19980607.0705.sgm is handled here.
				else if(word.matches("[\\d\\w]+[-]\\d+[/]\\d+[.]*$")){
					addNumber=3;
					countIncrease+=3;
					bufleng+=addNumber;
				}
				//cases like 3/4, in 9803.224.sgm
				else if(word.matches("\\d*[-]*\\d+[/]\\d+[,]$")){
					addNumber=2;
					countIncrease+=2;
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
				}else if(word.matches("[\\(]\\w+[\\)],$")){
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
				else if(word.endsWith("....") && redDotHm.get(i)==true){
					addNumber=3;
					countIncrease-=3;
					bufleng-=addNumber;
				}
				//why I should use this following patter since, in NYT19980206.0461.sgm, the pattern ... is used. I don't know why they use inconsistent pattern.
				//so, in this case, I should exclude it from others. 
				else if(word.endsWith("...") && !word.equals("...") && redDotHm.get(i)==true){
					//what on earth should we reduce three or two? In 9802.67.sgm at least, we should reduce 3. 
					//but in 9802.197.sgm and many others, we should reduce 2. I will recheck this later.
					addNumber=2;
					countIncrease-=2;
					bufleng-=addNumber;
				}
				//the following refers to cases where ACE corpus has words like justify... from APW19980227.0469.sgm. Stanford parse parsed it as justify ...
				//thus, one byte should be added instead of like the above.
				else if(word.endsWith("...") && !word.equals("...") && redDotHm.get(i)==false){
					//what on earth should we reduce three or two? In 9802.67.sgm at least, we should reduce 3. 
					//but in 9802.197.sgm and many others, we should reduce 2. I will recheck this later.
					addNumber=1;
					countIncrease+=1;
					bufleng+=addNumber;
				}
				else if(word.matches("$[\\d]+,[\\d]+")){
					addNumber++;
					countIncrease++;
					bufleng+=addNumber;
				}else if(word.matches("\\w+\\(\\w+\\)")){
					addNumber+=11;
					countIncrease+=11;
					bufleng+=addNumber;
				}else if(word.matches("`\\(\\(\\w+\\)\\)")){
					addNumber+=21;
					countIncrease+=21;
					bufleng+=addNumber;
				}
				//Ld-Writethru,0693 from APW19980213.1305.sgm, or 
				//cases like BC-Sports-Tennis-St.Pete from APW19980213.1325.sgm
				//but I don't quite understand why the following will include 180,000 inside. Probably because I didn't add \\ before "-".
				//in the begining, now, it should be Ok. Also, the following pattern will handle the specialList2 case.
				//if so, it seems that the specialList2 is not useful anymore. I will take more considerations into this. I should remove the outside bracket.
				//else if(word.matches("[[\\w]+-]+\\w+,\\w+")){
				else if(word.matches("[\\w+\\-]+\\w+[,.]\\w+") && !word.matches("\\d+[,.]\\d+")){
					addNumber++;
					countIncrease++;
					bufleng+=addNumber;
				}


				else if(word.endsWith("-")){
					addNumber++;
					countIncrease++;
					bufleng+=addNumber;
				}
				//cool, the pattern in the following reflects a fragment from NYT19980212.0012.sgm
				//BC-BABBITT-INQUIRE-ART-1STLD-423(2TAKES)-NYT
				else if(word.matches("[\\w+-]+\\(\\w+\\)-\\w+")){
					addNumber+=13;
					countIncrease+=13;
					bufleng+=addNumber;
				}
				//note: the following is a little different from above. cases like 
				//(yard)-high from APW19980219.0492.sgm. caret means the start of the string
				else if(word.matches("^\\(\\w+\\)-\\w+")){
					addNumber+=12;
					countIncrease+=12;
					bufleng+=addNumber;
				}
				//the following patter handles two cases from APW19980213.1302.sgm.
				//firstly, "BC-UN-Sierra Leone,0542" should add one, but
				//secondly, "250,000" cannot be split and thus cannot add one.
				else if(word.matches("\\w+,\\w+") && !word.matches("\\d+,\\d+")){
					addNumber++;
					countIncrease++;
					bufleng+=addNumber;
				}else if(word.matches("\\w+\\.{3}\\w+")){
					addNumber+=2;
					countIncrease+=2;
					bufleng+=addNumber;
				}

				//very important point here. bufleng should not add addNumber. Instead, it should only add the same number as the number that addNumber adds here
				//otherwise, it will repeat.
				if(special){
					addNumber+=1;
					countIncrease+=1;
					bufleng++;
				}

				//the following only suitable for cases like word;word
				if(word.contains(";")&&!word.endsWith(";") && !word.contains(";.")){
					addNumber+=2;
					countIncrease+=2;
					bufleng+=2;
				}

				//				if(word.contains(":")&&!word.endsWith(":") && !word.contains(":.")){
				//					addNumber+=2;
				//					countIncrease+=2;
				//					bufleng+=2;
				//				}

				if(word.matches("\\w+:\\w+")){
					addNumber+=2;
					countIncrease+=2;
					bufleng+=2;
				}


				//cases like &LR; . This from NYT19980206.0485.sgm where ; add one while
				//. is regarded to be the end of a sentence and then one space is deleted.
				if(word.equals(".")){
					addNumber-=1;
					countIncrease-=1;
					//the reaons minus 2 is that it seems that the period occupies one more space when we add bufleng+word.length+1;
					bufleng-=2;
				}

				if(special3){
					addNumber+=2;
					countIncrease+=2;
					bufleng+=2;
				}

				if(special4){
					addNumber-=1;
					countIncrease-=1;
					bufleng-=1;
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

					int menStart =Integer.parseInt(mentionSpanPair.getFirst().toString());
					int menEnd =Integer.parseInt(mentionSpanPair.getSecond().toString());
					if(debugBytesMatch){
						System.out.println(filename+" index i: "+i+" "+bufArray[i]+" j: "+j+" before reducing in adjustACEBytesspan: match length: bufleng: "+bufleng+" mention: "+mention.getHeadCoveredText()+" menStart: "+menStart+" menEnd: "+menEnd);					
					}else if(debug){
						if(j==112|| j==113||j==114){
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
						if(menStart>=bufleng-addNumber){
							if((word.endsWith("...")||word.endsWith("....")) && redDotHm.get(i)==true){
								menStart-=addNumber;
								menEnd-=addNumber;
							} 
							else{
								menStart+=addNumber;
								menEnd+=addNumber;		
							}

						}
						//the following case happens when left side of the word involves punctuations such as [word
						else if(menStart>=bufleng-addNumber-word.length()){
							if(word.matches("[`']\\w+'\\w+$") || word.matches("`{1,2}[\\w]+[!.?]*$")){
								menStart+=1;
								menEnd+=1;
							}else if(word.startsWith("'") &&word.startsWith("`") && word.endsWith("'[.]*")){
								menStart+=1;
								menEnd+=1;
							}else if(match.matches("\\w[,:;]")&& !word.contains("/")&&word.startsWith("\"")){
								menStart+=2;
								menEnd+=2;
							}else if(match.matches("\"\\w+$")){
								menStart+=2;
								menEnd+=2;
							}else if(word.startsWith("\"") && word.endsWith("\"")){
								menStart+=2;
								menEnd+=2;
							}else if(match.startsWith("\"") && match.endsWith("\".")){
								menStart+=3;
								menEnd+=3;
							}else if(word.matches("\\$\\w+")){
								menStart++;
								menEnd++;
							}else if(word.startsWith("{") && !word.contains("}")){
								menStart++;
								menEnd++;
							}else if(word.contains("[") && !word.contains("]")){
								menStart+=5;
								menEnd+=5;
							}else if(word.contains("(") && !word.contains(")")){
								menStart+=5;
								menEnd+=5;
							}else if(word.matches("[\"]\\w+\'\\w+$")){
								menStart+=2;
								menEnd+=2;
							}else if(word.matches("[\\[\\(]\\w+[-]*\\w+[\\]\\)]$")){
								menStart+=5;
								menEnd+=5;
							}//I don't quite understand here????????, should add 11. but in the article, it seems that it is not correct to add 11.
							//I will check them later.
							//I know now, in 9801.193, (R-Mo.), the period after Mo. is misregarded to be sentence delimiter. Thus, new line 
							//incur and then only add 10 rather than add 11. But I am not sure if other places are like this.
							else if(word.matches("[\\[\\(]\\w+[-]*\\w+.[\\]\\)]$")){
								//cases like (R-Mo.) it becomes -LRB- R-Mo . -RRB-
								menStart+=5;
								menEnd+=5;
							}else if(word.matches("[\\[\\(]\\w+[-]*\\w+[\\]\\)][,]$")){
								menStart+=5;
								menEnd+=5;
							}else if(word.matches("[\\(]\\w+[-]*\\w+[.][\\)][,]$")){
								//cases like (R-Fla.), it becomes -LRB- R-Fla . -RRB- ,
								//weird thing is that (R-Miss.), in 9801.193.sgm becomes -LRB- R-Fla . -RRB-,  
								menStart+=5;
								menEnd+=5;
							}else if(word.matches("[\\(]\\w+[-]*\\w+[\\)][-]$")){
								//cases like (Tuesday)- becomes -LRB- Tuesday -RRB- - from 9801.219.s	gm,  
								menStart+=5;
								menEnd+=5;
							}else if(word.matches("[\\(\\[]\\w+[-]*\\w+[.]\\w+[.][\\)\\]],")){
								if(menStart>=bufleng){
									menStart+=5;
									menEnd+=5;
								}
							}
						}
						//else if(word.endsWith("...")){
						//menStart-=2;
						//menEnd-=2;
						//}						//note: this is a special case. In sgm file, ... is writeent as . . . . together with the period of previous sentence.
						//while stanford parser convert . . . . to ... Therefore, menStart and menEnd should reduce 3 rather than adding like other punctuations.
						//						else if(word.endsWith("....")){
						//							if(menStart>=bufleng-addNumber){
						//								menStart-=addNumber;
						//								menEnd-=addNumber;
						//							}
						//						}else if(word.endsWith("...")){
						//							if(menStart>=bufleng-addNumber){
						//								menStart-=addNumber;
						//								menEnd-=addNumber;
						//							}
						//						}

						//						if(special){
						//							if(menStart>=bufleng-addNumber){
						//								menStart+=addNumber;
						//								menEnd+=addNumber;
						//							}
						//						}
					}


					//else if(menStart>bufleng && match.) //please consider cases like he's, in stanford parser, it is split as he 's. 
					//Dingchegn Li smart guy though slow guys as well sometimes. 
					//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
					if(debugBytesMatch){

						System.out.println("after reducing in adjustACEBytesspan: shorterBuf length: "+shorterBuf.length()+" bufleng: "+bufleng+" mention: "+mention.getHeadCoveredText()+" "+menStart+" "+menEnd+" countIncrease: "+countIncrease);
					}else if(debug){
						if(j==112||j==113||j==114){
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
