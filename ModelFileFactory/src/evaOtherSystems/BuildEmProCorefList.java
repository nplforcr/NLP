package evaOtherSystems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import annotation.Mention;

import readers.AceReader;

import utils.CorefClusters;
import utils.CorefUser;
import utils.IntArray;
import utils.Pair;
import utils.ThirdPersonPron;
import utils.ThirdReflexives;
import utils.Tree2Sentence;
import utils.TreebankPhraseTags;
import utils.TreebankPosTags;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import evaluations.PrepareACEKey;

/**
 * 
 * @author lixxx345 Dingcheng Li
 * The idea here is that three classes will be created. this class is responsible for reading emPronoun outputs 
 *and we can obtain a list of coreferring information in which, coreferring number, sentence number, tags and 
 *the mention will be extracted.
 *Then, ExtractACECoref.java is responsible for extracting gold annotation information. 
 *The third class EvaEmPronouns will call both of them and make comparisons and output the accuracies. So, the third class will 
 *process the directory.  
 *
 */
public class BuildEmProCorefList extends PrepareACEKey {
	
	//static List<Pair> mentionSpanList;
	//static HashMap<Pair,String> bytespanIdM;
	//static HashMap<String,List<Pair>> idMentionListM; 
	ArrayList<CorefClusters> responseDataList;
	
	HashMap<Integer, CorefChain> corefHash;
	ArrayList<Integer> corefNoList;
	ArrayList<PhraseSpan> wholePhraseSpanList; //a global list to store all phrases and their spans
	HashMap<String,ArrayList<PhraseSpan>> sentBytespanListHM; //sentence + list of bytespan of mentions
	HashMap<String,Pair> sentBytespanHM; //sentence + bytespan of the sentence;
	ArrayList<String> sentList;
	
	/**
	 * CorefChain inclues all information about a coreferring chain. It is cool class
	 * @author lixxx345
	 *
	 */
	private class CorefChain {
		int corefNo;
		ArrayList<String> mentions; //all coreferring mentions 
		ArrayList<String> sentNoList; //the sentence that the mention belongs to
		//ArrayList<Pair> bytespanList;
		ArrayList<PhraseSpan> phraseSpanList;
		ArrayList<String> tagList; //the phrase or part-of-speech types
		//ArrayList<String> sentList;
		HashMap<String, String> sentNo2mens; // Note: in order to avoid
												// repeatition, we need to add
												// another number to sentNo.
		HashMap<String, String> tag2mens; // the type of tag such as NP or PRP
											// corresponding to the metion,
											// similar to tag2mens

		// seems unnecessary,but it doesn't hurt.
		public CorefChain(int corefNo, ArrayList<String> menList,
				ArrayList<String> sentNoList,
				ArrayList<PhraseSpan> phraseSpanList,
				ArrayList<String> tagList,
				HashMap<String, String> sent2Mens,
				HashMap<String, String> tag2Mens) {
			this.corefNo = corefNo;
			this.mentions = menList;
			this.phraseSpanList=phraseSpanList;
			this.sentNoList = sentNoList;
			this.tagList = tagList;
			this.sentNo2mens = sent2Mens;
			this.tag2mens = tag2Mens;
		}
	}
	
	/**
	 * Now, it seems that the class is useless since I have put bytespan into CorefCahin
	 * but it seems that we still need it for the moment since I don't have funtion to get bytespan
	 * instead, I have a function called computePhraseSpan
	 * @author lixxx345
	 *
	 */
	private class PhraseSpan{
		String phrase;
		int start;
		int end;
		public PhraseSpan(String phrase, int phraseStart,int phraseEnd){
			this.phrase = phrase;
			this.start = phraseStart;
			this.end = phraseEnd;
		}
	}

	/**
	 * 
	 */
	public BuildEmProCorefList() {
		super();
		//idMentionListM = new HashMap<String,List<Pair>>();
		//bytespanIdM = new HashMap<Pair,String>();
		//mentionSpanList = new ArrayList<Pair>();
		
		corefHash = new HashMap<Integer, CorefChain>();
		corefNoList = new ArrayList<Integer>();
		//sentBytespanListHM = new HashMap<String,ArrayList<Pair>>();
		sentBytespanListHM = new HashMap<String,ArrayList<PhraseSpan>>();
		sentBytespanHM = new HashMap<String,Pair>() ;
		sentList = new ArrayList<String>();
		wholePhraseSpanList = new ArrayList<PhraseSpan>();
		responseDataList = new ArrayList<CorefClusters>();
	}
	
	
	public void readSentBytespanFile(File sentBytespanFile) throws IOException{
		BufferedReader bfInput = new BufferedReader(new FileReader(sentBytespanFile));
		String line = "";
		int sentNo = 0;
		Pair bytespanPair = null;
		while((line=bfInput.readLine())!=null){
			//  6 6 2.59207 3.94638 -1.67039
			 // (\s+\d+){2}(\s+-?\d+\.\d+){3}
			//Pattern closeBraP = Pattern.compile("(\\s+\\d+){2}");
			Pattern closeBraP = Pattern.compile("(\\d+\\s+){2}");
			Matcher matcher = closeBraP.matcher(line);
			boolean found = matcher.find();
			String subLine = "";
			if(found){
				String match = matcher.group();
				String[] bytespanStr = match.split("\\s+");
				bytespanPair = new Pair(bytespanStr[0],bytespanStr[1]);
				//bytespanPair.o1 = bytespanStr[0];
				//bytespanPair.o2 = bytespanStr[1];
				subLine = line.substring(match.length());
				String[] subLineArray = subLine.split("\\s+");
				StringBuffer sbSubLine = new StringBuffer();
				for(int i=0;i<subLineArray.length;i++){	
					sbSubLine.append(subLineArray[i]+" ");
				}
				sentBytespanHM.put(sbSubLine.toString(), bytespanPair);
			}
		}
	}

	/**
	 * this function matches # and the coreference phrase hence with matchBracket function. 
	 * It seems that we cannot see anything is done in this function, in fact, data structure like
	 * corefHash and corefNoList which are global ones are filed in matchBracket. So, I can add 
	 * code after matchBracket in this function to fill bytespans list and sentence list.
	 * @param inputFile
	 * @throws Exception
	 */
	public void extractCorefChain(File inputFile) throws Exception {
		BufferedReader bfInput = new BufferedReader(new FileReader(inputFile));
		String line = "";
		int sentNo = 0;
		int incLeng = 0;
		int startLen = 0;
		while ((line = bfInput.readLine()) != null) {
			StringBuffer sentSb = new StringBuffer();
			//ArrayList<Pair> byteSpanList = new ArrayList<Pair>();
			ArrayList<PhraseSpan> phraseSpanList = new ArrayList<PhraseSpan>();
			ArrayList<Integer> wordIndexList = new ArrayList<Integer>();
			
			Pattern pattern = Pattern.compile("\\([A-Z]+#\\d+");
			Matcher matcher = pattern.matcher(line);
			boolean found = matcher.find();
			int menCount = 0;
			//(ROOT (S (PP (IN In) (NP (DT some) (NNS cases))) (, ,) (NP (PRP#437 it)) (VP (MD can) (VP (VB be) (NP (DT the) (JJS best) (NN deal)))) (. .)))
			//wordIndexList = [17, 29, 40, 52, 68, 81, 94, 106, 116, 126, 138]
			//sentSb = In some cases , it can be the best deal .
			ArrayList<String> wordList = Tree2Sentence.sentFinder(sentSb,line,wordIndexList);
			//System.out.println(line);
			//System.out.println(sentSb);
			
		
			incLeng+=sentSb.toString().trim().length();
			System.out.println(startLen+" "+(incLeng-1)+" "+sentSb.toString().trim());
			startLen = incLeng;
			//System.out.println("incLeng from extractCorefChain: "+incLeng+" "+sentSb.toString().trim());
			//System.out.print(sentSb.toString().trim());
			int preIntSpanStart = 0;
			while (found) {
				//System.out.println(matcher.group());
				String match = matcher.group();
				//this is the start index of match such as NP#752;
				int intspanStart = line.indexOf(match,preIntSpanStart);
				preIntSpanStart = intspanStart+match.length();
				String subLine = line.substring(line.indexOf(match));
				//int wordStart = intspanStart+match.length();
				ArrayList<String> mentWithinSent = new ArrayList<String>();
				int firstRRB = subLine.indexOf(")"); 
				String firstRRBStr = subLine.substring(0,firstRRB); //(PRP#437 it
				//it is so troublesome, but I don't know better ways, it aims at finding the start index of the first word of the whole phrase.
				int firstWordStart = firstRRBStr.lastIndexOf(" "); //say, if 8 for (PRP#437 it
				int phraseStart = intspanStart+firstWordStart+1; //now count from the beginning of the whole line.
				int byteStart = 0;
				if(wordIndexList.contains(phraseStart)){
					int index = wordIndexList.indexOf(phraseStart); //index = 4, it means that "it" is the 5th word.
					//int byteStart = 0;
					//int byteEnd = 0;
					for(int i=0;i<index;i++){
						String ithWord = wordList.get(i);
						if(i==0){
							byteStart+=ithWord.length();
						}else{
							byteStart+=ithWord.length()+1;
						}
					}
				}
				
				//punkedPhrase refers to what NP#752 or PRP#437 refers to since there is a # inside it. But the extracted phrase should be the phrase.
				
				String punkedPhrase = matchBracket(match, subLine, sentNo, menCount,sentSb.toString(),byteStart).trim();
				
//				int firstRRB = subLine.indexOf(")"); 
//				String firstRRBStr = subLine.substring(0,firstRRB); //(PRP#437 it
//				//it is so troublesome, but I don't know better ways, it aims at finding the start index of the first word of the whole phrase.
//				int firstWordStart = firstRRBStr.lastIndexOf(" "); //say, if 8 for (PRP#437 it
//				int phraseStart = intspanStart+firstWordStart+1; //now count from the beginning of the whole line.
//				if(wordIndexList.contains(phraseStart)){
//					int index = wordIndexList.indexOf(phraseStart); //index = 4, it means that "it" is the 5th word.
//					int byteStart = 0;
//					int byteEnd = 0;
//					for(int i=0;i<index;i++){
//						String ithWord = wordList.get(i);
//						if(i==0){
//							byteStart+=ithWord.length();
//						}else{
//							byteStart+=ithWord.length()+1;
//						}
//					}
//					byteEnd = byteStart+punkedPhrase.length()-1;
//					//Pair byteSpan = new Pair(byteStart,byteEnd);
//					//byteSpanList.add(byteSpan);
//					//PhraseSpan phraseSpan = new PhraseSpan(punkedPhrase,byteStart,byteEnd);
//					PhraseSpan phraseSpan = this.computePhraseSpan(sentBytespanHM, sentSb.toString(), punkedPhrase, byteStart, byteEnd);
//					phraseSpanList.add(phraseSpan);
//					wholePhraseSpanList.add(phraseSpan);
//					
//				}
				found = matcher.find();
				menCount++;
				//Pair sentBytes =  sentBytespanHM.get(sentence);
			}
			
//			if(!byteSpanList.isEmpty()){
//				sentBytespanListHM.put(sentSb.toString(), byteSpanList);
//			}
			if(!phraseSpanList.isEmpty()){
				sentBytespanListHM.put(sentSb.toString(), phraseSpanList);
				sentList.add(sentSb.toString());
			}
			//use the follwoing code to test if the length of sents is equal to the one output from PrepareACEKey.java
			sentNo++;
		}
	}
	
	
	public void printBytespanListHM(){
		for(int i=0;i<sentList.size();i++){
			String sent = sentList.get(i);
			System.out.println(sent);
			ArrayList<PhraseSpan> menSpanList = sentBytespanListHM.get(sent);
			for(int j=0;j<menSpanList.size();j++){
				PhraseSpan phraseSpan=menSpanList.get(j);
				System.out.println(phraseSpan.phrase+" "+phraseSpan.start+" "+phraseSpan.end);
			}
		}
	}

	
	public PhraseSpan computePhraseSpan(HashMap<String,Pair> sentSpanHM, String sent, String punkedPhrase,int byteStart,int byteEnd){
		Pair sentSpan = sentSpanHM.get(sent);
		int sentSpanStart = Integer.valueOf(sentSpan.o1.toString());
		int sentSpanEnd = Integer.valueOf(sentSpan.o2.toString());
		int phraseSpanStart = sentSpanStart+byteStart;
		int phraseSpanEnd = sentSpanStart+byteEnd;
		if(phraseSpanEnd>sentSpanEnd || phraseSpanStart>sentSpanEnd){
			System.err.println("something is wrong!");
			return null;
		}
		PhraseSpan phraseSpan = new PhraseSpan(punkedPhrase,phraseSpanStart,phraseSpanEnd);
		return phraseSpan;
	}

	/**
	 * matchBracket aims at building a corefChain crossing sentences, but it itself only handle one sentence. extractCorefChain
	 * iteratively calls matchBracket to keep adding coreferring mentions to CorefChain. Now, what I want to do in the meanwhile is 
	 * to get the bytespan of each mention. So, it seems that I need matchBracket returns a list of mention each time so that
	 * we can combine other methods to get bytespan for each mention  
	 * @param match
	 * @param line
	 * @param sentNo
	 * @param menCount
	 * @throws Exception
	 */
	public String matchBracket(String match, String line, int sentNo, int menCount,String sent,int byteStart)
			throws Exception {
		//this is wrong since matchBracket only return on token rather than a list;
		//ArrayList<String> mentsWithinSent = new ArrayList<String>();
		MaxentTagger tagger = new MaxentTagger(
				"models/bidirectional-wsj-0-18.tagger");
		int corefNo = Integer.valueOf(match.substring(match.indexOf("#") + 1));
		if(!corefNoList.contains(corefNo)){
			corefNoList.add(corefNo);
		}
		String tag = match.substring(1, match.indexOf("#"));
		// here is a stupid choice since I have not found a better tokenizer.
		// The problem here is that
		// though line in fact is a sentence, the MaxentTagger will split it
		// into two sentences since
		// there are a few brackets after the period. then, brackets after the
		// period is regarded to be one sentence.
		// So, I need to combine them into one sentence. I will look for or
		// implment a good tokenizer in future.
		List<Sentence<? extends HasWord>> sentences = MaxentTagger
				.tokenizeText(new StringReader(line));
		Stack<String> braStack = new Stack<String>();
		StringBuffer sbMention = new StringBuffer();
		// int menCount = 0;
		String preToken = "";
		//the following step is important since if we don't combine the wrong splitting, the condition of stack.size==0 will 
		//be implemented twice so that sbMention and corefNo will be added to CorefHash twice as well though we can avoid it
		//other way. But the real mistake is not fixed.
		//now I use a wordList to concatenate words in the split sentences which in fact belongs to one sentence
		ArrayList<String> wordList = new ArrayList<String>();
		for (int i = 0; i < sentences.size(); i++) {
			Object[] sentence = sentences.get(i).toArray();
			for (int j = 0; j < sentence.length; j++) {
				wordList.add(sentence[j].toString());
			}
		}
		// System.out.println(sentences.get(i));
		for (int j = 0; j < wordList.size(); j++) {
			String token = wordList.get(j);
			if (token.equals("-LRB-")) {
				braStack.push(token);
			} else if (token.equals("-RRB-")) {
				if (braStack.size() > 0) {
					braStack.pop();
				}

				if (braStack.size() == 0) {
					//mentsWithSent adds all mentions in a sentence into the list, don't care whether they are coreferring or not
					//mentsWithinSent.add(sbMention.toString());
					if (corefHash.containsKey(corefNo)) {
						CorefChain corChain = corefHash.get(corefNo);
						ArrayList<String> mentions = corChain.mentions;
						ArrayList<String> sentNoList = corChain.sentNoList;
						ArrayList<String> tagList = corChain.tagList;
						ArrayList<PhraseSpan> phraseSpanList = corChain.phraseSpanList;
						//mentions add all coreferring mentions in a document and they are saved into corefHash globally
						mentions.add(sbMention.toString());
						HashMap<String, String> sentNo2mens = corChain.sentNo2mens;
						StringBuffer sentMenNoSb = new StringBuffer();
						sentMenNoSb.append(sentNo);
						sentMenNoSb.append("-");
						sentMenNoSb.append(menCount);
						sentNoList.add(sentMenNoSb.toString());
						sentNo2mens.put(sentMenNoSb.toString(),
								sbMention.toString());
						
						HashMap<String, String> tag2mens = corChain.tag2mens;
						StringBuffer tagMenSb = new StringBuffer();
						tagMenSb.append(tag);
						tagMenSb.append("-");
						tagMenSb.append(menCount);
						tagList.add(tagMenSb.toString());
						tag2mens.put(tagMenSb.toString(), sbMention.toString());
						int byteEnd = byteStart+sbMention.length()-1;
						PhraseSpan phraseSpan = this.computePhraseSpan(sentBytespanHM, sent, sbMention.toString(), byteStart,byteEnd);
						phraseSpanList.add(phraseSpan);
						corChain = new CorefChain(sentNo, mentions, sentNoList,phraseSpanList,tagList,
								sentNo2mens, tag2mens);
						corefHash.put(corefNo, corChain);
					} else {
						ArrayList<String> mentions = new ArrayList<String>();
						ArrayList<String> sentNoList = new ArrayList<String>();
						ArrayList<String> tagList = new ArrayList<String>();
						ArrayList<PhraseSpan> phraseSpanList = new ArrayList<PhraseSpan>();
						mentions.add(sbMention.toString());
						StringBuffer sentMenNoSb = new StringBuffer();
						sentMenNoSb.append(sentNo);
						sentMenNoSb.append("-");
						sentMenNoSb.append(menCount);
						HashMap<String, String> sentNo2mens = new HashMap<String, String>();
						sentNoList.add(sentMenNoSb.toString());
						sentNo2mens.put(sentMenNoSb.toString(),
								sbMention.toString());
						StringBuffer tagMenSb = new StringBuffer();
						tagMenSb.append(tag);
						tagMenSb.append("-");
						tagMenSb.append(menCount);
						tagList.add(tagMenSb.toString());
						HashMap<String, String> tag2mens = new HashMap<String, String>();
						tag2mens.put(tagMenSb.toString(), sbMention.toString());
						int byteEnd = byteStart+sbMention.length()-1;
						PhraseSpan phraseSpan = this.computePhraseSpan(sentBytespanHM, sent, sbMention.toString(), byteStart,byteEnd);
						phraseSpanList.add(phraseSpan);
						CorefChain corChain= new CorefChain(sentNo, mentions, sentNoList,phraseSpanList,tagList,
								sentNo2mens, tag2mens);
						corefHash.put(corefNo, corChain);
					}
					menCount++;
					break;
				}
			} else if (TreebankPosTags.tbPosList.contains(token)
					|| TreebankPhraseTags.tbPhraseList.contains(token)) {
				// System.out.println(token);
				continue;
			} else if (token.equals("#")) {
				preToken = token;
				continue;
			} else if (token.matches("\\d+") && preToken.equals("#")) {
				preToken = "";
				continue;
			} else {
				sbMention.append(token + " ");
			}
		}

		// the following line does't work. Stanford MaxentTagger split a
		// document into sentences and meanwhile
		// tag sentence into words, including splitting punctuation marks. It is
		// cool.
		// String taggedString = MaxentTagger.tagString(line);
		// /String[] lineArray = taggedString.split(" ");
		// System.out.println(taggedString);
		//return mentsWithinSent;
		
		
		
		return sbMention.toString();
	}

	/**
	 * print out CorefChains based on corefHash
	 */
	public void printCorefHash() {
		for(int i=0;i<corefNoList.size();i++){
			int corefNo = corefNoList.get(i);
			CorefChain corefChain = corefHash.get(corefNo);
			ArrayList<String> mentions = corefChain.mentions;
			HashMap<String, String> sentNo2mens = corefChain.sentNo2mens;
			HashMap<String, String> tag2mens = corefChain.tag2mens;
			Iterator<String> iterSentNo2Mens = sentNo2mens.keySet().iterator();
			ArrayList<String> senNoList = corefChain.sentNoList;
			ArrayList<String> tagNoList = corefChain.tagList;
			
			for(int j=0;j<mentions.size();j++){
				String mention = mentions.get(j);
				System.out.println(corefNo+" sentNo: "+senNoList.get(j)+" tag: "+tagNoList.get(j)+" mention: "+mention);
			}
		}
	}
	
	
	public ArrayList<CorefClusters> legalCorefPair(){
		for(int i=0;i<corefNoList.size();i++){
			int corefNo = corefNoList.get(i);
			CorefChain corefChain = corefHash.get(corefNo);
			ArrayList<String> mentions = corefChain.mentions;
			ArrayList<String> sentNoList = corefChain.sentNoList;
			ArrayList<PhraseSpan> phraseSpanList = corefChain.phraseSpanList;
			boolean preCompare = false;
			IntArray preIntArray = null;
			for(int j=1;j<mentions.size();j++){
				String preJMenPhrase = mentions.get(j-1).trim();
				PhraseSpan preJPhraseSpan = phraseSpanList.get(j-1);
				IntArray preJthIntArray = new IntArray(preJPhraseSpan.start,preJPhraseSpan.end);
				String jthMenPhrase = mentions.get(j).trim();
				if(ThirdPersonPron.thirdPerPronList2.contains(jthMenPhrase.toLowerCase())||ThirdReflexives.thirdRefList2.contains(jthMenPhrase.toLowerCase())){
					PhraseSpan jthPhraseSpan = phraseSpanList.get(j);
					IntArray jthIntArray = new IntArray(jthPhraseSpan.start,jthPhraseSpan.end);
					System.out.println("preJMenPhrase: "+preJMenPhrase+" preJMenStart: "+preJPhraseSpan.start+" pre end: "+preJPhraseSpan.end+" jthMenPhrase: "+jthMenPhrase+" jthPhraseSpan start: "+jthPhraseSpan.start+" jthPhraseSpan end: "+jthPhraseSpan.end);
					int legalInd = -1;
					int preInd = -1;
					Mention preMention = null;
					for(int k=0;k<mentionSpanList.size();k++){
						Pair kthMenSpan = mentionSpanList.get(k);
						String kthMenId = bytespanIdM.get(kthMenSpan);
						Mention kthMention = idMentionM.get(kthMenId);
						IntArray kthIntArray = new IntArray(Integer.valueOf(kthMenSpan.o1.toString()),Integer.valueOf(kthMenSpan.o2.toString()));
						if(preJthIntArray.spanCompare(kthIntArray)!=0){
							if(preCompare==true){ 
								int differ1 = preJthIntArray.differ(kthIntArray);
								int differ2 = preJthIntArray.differ(preIntArray);
								System.out.println("differ1: "+differ1+" differ2: "+differ2);
								if(differ1<differ2){
									preInd = k;
									preMention = kthMention;
									System.out.println("coreferring ones when preCompare is true: "+preJthIntArray+" "+preInd+" "+kthIntArray.toString());
									preCompare = true;
								}
								
							}else if(preCompare==false){
								preInd = k;
								preMention = kthMention;
								System.out.println("coreferring ones when preCompare is false: "+preJthIntArray+" "+preInd+" "+kthIntArray.toString());
								preCompare = true;
								preIntArray = kthIntArray;
							}
							
						}
						
						if(jthIntArray.spanCompare(kthIntArray)!=0 && preCompare==true && preInd!=k){
							legalInd = k;
							System.out.println("kthIntArray start: "+jthPhraseSpan.start+" kthIntArray end: "+jthPhraseSpan.end);
							//System.out.println(preInd+" "+preJMenPhrase+" "+preMention.getExtentCoveredText()+" " +legalInd+" "+jthMenPhrase+" "+kthMention.getExtentCoveredText());
							System.out.println(preInd+" "+preJMenPhrase+" "+preMention.getExtentCoveredText()+" " +legalInd+" "+jthMenPhrase+" "+kthMention.getExtentCoveredText());
							CorefClusters responseData = new CorefClusters();
							responseData.setCorefIndex(preInd);
							responseData.setCorefWord(preJMenPhrase);
							responseData.setCorefIntArray(preIntArray);
							responseData.setAnaIndex(legalInd);
							responseData.setAnaphor(jthMenPhrase);
							responseData.setAnaIntArray(jthIntArray);
							responseDataList.add(responseData);
							preCompare = false;
							break;
						}
					}
				}
			}
		}
		return responseDataList;
	}
	
	
	/**
	 * 
	 * @param keyDataList
	 * @param responseDataList
	 * @return
	 */
	public double calculateAccuracy(List<CorefClusters> keyDataList,List<CorefClusters> responseDataList){
		int totalPair = keyDataList.size();
		int correctPair = 0;
		double finalAccuracy = 0;
		for(int i=0;i<keyDataList.size();i++){
			CorefClusters keyData = keyDataList.get(i);
			if(responseDataList.size()==0){
				System.out.println("there is a bug in the parsing process and I will fix it later!");
				return 0;
			}
			for(int j=0;j<responseDataList.size();j++){
				//System.out.println(keyData.toString()+" "+responseData.toString());	
				CorefClusters responseData = responseDataList.get(j);
				if(keyData.getAnaIndex()==responseData.getAnaIndex() &&keyData.getAnaWord().equals(responseData.getAnaWord()) ){
					if(keyData.getCorefIndex()==responseData.getCorefIndex() && keyData.getCorefWord().equals(responseData.getCorefWord())){
						correctPair++;
					}else if(keyData.getCorefIntArray().spanCompare(responseData.getCorefIntArray())!=0 && keyData.getAnaIntArray().spanCompare(responseData.getAnaIntArray())!=0){
						correctPair++;
					}
					break;
				}
			}
		}
		finalAccuracy = (double)correctPair/totalPair;
		System.out.println("totalPair: "+totalPair+" totalCorre: "+correctPair+" finalAccuracy: "+finalAccuracy);
		return finalAccuracy;
	}
	

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		File inputFile = new File("input");
		File corpora = new File("input");
		File sentSpanDir = new File("output");
		if(inputFile.isDirectory()){
			File[] files = inputFile.listFiles();
			for(int i=0;i<files.length;i++){
				BuildEmProCorefList buildCrList = new BuildEmProCorefList();
				File sgmFile = new File("input", files[i].getName());
				File xmlFile = new File("input",files[i].getName()+".tmx.rdc.xml");
				System.out.println(files[i].getName());
				buildCrList.processTest(sgmFile.getAbsolutePath(), xmlFile.getAbsolutePath());
				buildCrList.adjustACEBytespan();
				List<String> bsTokenList = new ArrayList<String>();
				ArrayList<CorefClusters> keyDataList = buildCrList.getKeyData(bsTokenList);
				
				buildCrList.printBytespanListHM();
				File byteSpanFile = new File("output",files[i].getName());
				buildCrList.readSentBytespanFile(byteSpanFile);
				try {
					buildCrList.extractCorefChain(files[i]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buildCrList.printCorefHash();
				ArrayList<CorefClusters> responseDataList=buildCrList.legalCorefPair();
				buildCrList.calculateAccuracy(keyDataList, responseDataList);
			}			
		}else{
			
			BuildEmProCorefList buildCrList = new BuildEmProCorefList();
			File sgmFile = new File(corpora,inputFile.getName());
			File xmlFile = new File(corpora,inputFile.getName()+".tmx.rdc.xml");
			buildCrList.processTest(sgmFile.getAbsolutePath(), xmlFile.getAbsolutePath());
			buildCrList.adjustACEBytespan();
			List<String> bsTokenList = new ArrayList<String>();
			ArrayList<CorefClusters> keyDataList = buildCrList.getKeyData(bsTokenList);
			
			buildCrList.printBytespanListHM();

			File byteSpanFile = new File("output",inputFile.getName());
			buildCrList.readSentBytespanFile(byteSpanFile);
			try {
				buildCrList.extractCorefChain(inputFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buildCrList.printCorefHash();
			ArrayList<CorefClusters> responseDataList=buildCrList.legalCorefPair();
			buildCrList.calculateAccuracy(keyDataList, responseDataList);
		}
	}
}
