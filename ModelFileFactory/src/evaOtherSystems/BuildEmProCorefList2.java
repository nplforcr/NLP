package evaOtherSystems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Pair;
import utils.Tree2Sentence;
import utils.TreebankPhraseTags;
import utils.TreebankPosTags;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

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
public class BuildEmProCorefList2 {
	

	HashMap<Integer, CorefChain> corefHash;
	ArrayList<Integer> corefNoList;
	HashMap<String,ArrayList<PhraseSpan>> sentBytespanListHM; //sentence + list of bytespan of mentions
	HashMap<String,Pair> sentBytespanHM; //sentence + bytespan of the sentence;
	ArrayList<String> sentList;
	
	private class CorefChain {
		int corefNo;
		ArrayList<String> mentions;
		ArrayList<String> sentNoList;
		ArrayList<String> tagList;
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
				ArrayList<String> tagList,
				HashMap<String, String> sent2Mens,
				HashMap<String, String> tag2Mens) {
			this.corefNo = corefNo;
			this.mentions = menList;
			this.sentNoList = sentNoList;
			this.tagList = tagList;
			this.sentNo2mens = sent2Mens;
			this.tag2mens = tag2Mens;
		}
	}
	
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
	public BuildEmProCorefList2() {
		corefHash = new HashMap<Integer, CorefChain>();
		corefNoList = new ArrayList<Integer>();
		//sentBytespanListHM = new HashMap<String,ArrayList<Pair>>();
		sentBytespanListHM = new HashMap<String,ArrayList<PhraseSpan>>();
		sentBytespanHM = new HashMap<String,Pair>() ;
		sentList = new ArrayList<String>();
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
			
			while (found) {
				//System.out.println(matcher.group());
				String match = matcher.group();
				//this is the start index of match such as NP#752;
				int intspanStart = line.indexOf(match);
				String subLine = line.substring(line.indexOf(match));
				//int wordStart = intspanStart+match.length();
				ArrayList<String> mentWithinSent = new ArrayList<String>();
				//punkedPhrase refers to what NP#752 or PRP#437 refers to since there is a # inside it. But the extracted phrase should be the phrase.
				String punkedPhrase = matchBracket(match, subLine, sentNo, menCount).trim();
				int firstRRB = subLine.indexOf(")"); 
				String firstRRBStr = subLine.substring(0,firstRRB); //(PRP#437 it
				//it is so troublesome, but I don't know better ways, it aims at finding the start index of the first word of the whole phrase.
				int firstWordStart = firstRRBStr.lastIndexOf(" "); //say, if 8 for (PRP#437 it
				int phraseStart = intspanStart+firstWordStart+1; //now count from the beginning of the whole line.
				if(wordIndexList.contains(phraseStart)){
					int index = wordIndexList.indexOf(phraseStart); //index = 4, it means that "it" is the 5th word.
					int byteStart = 0;
					int byteEnd = 0;
					for(int i=0;i<index;i++){
						String ithWord = wordList.get(i);
						if(i==0){
							byteStart+=ithWord.length();
						}else{
							byteStart+=ithWord.length()+1;
						}
					}
					byteEnd = byteStart+punkedPhrase.length()-1;
					//Pair byteSpan = new Pair(byteStart,byteEnd);
					//byteSpanList.add(byteSpan);
					//PhraseSpan phraseSpan = new PhraseSpan(punkedPhrase,byteStart,byteEnd);
					PhraseSpan phraseSpan = this.computePhraseSpan(sentBytespanHM, sentSb.toString(), punkedPhrase, byteStart, byteEnd);
					phraseSpanList.add(phraseSpan);
				}
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
	public String matchBracket(String match, String line, int sentNo, int menCount)
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
						corChain = new CorefChain(sentNo, mentions, sentNoList,tagList,
								sentNo2mens, tag2mens);
						corefHash.put(corefNo, corChain);
					} else {
						ArrayList<String> mentions = new ArrayList<String>();
						ArrayList<String> sentNoList = new ArrayList<String>();
						ArrayList<String> tagList = new ArrayList<String>();
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
						CorefChain corChain = new CorefChain(sentNo, mentions,sentNoList, tagList,
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

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		File inputFile = new File(args[0]);
		File corpora = new File(args[1]);
		File sentSpanDir = new File(args[2]);
		if(inputFile.isDirectory()){
			File[] files = inputFile.listFiles();
			for(int i=0;i<files.length;i++){
				BuildEmProCorefList2 buildCrList = new BuildEmProCorefList2();
				System.out.println(files[i].getName());
				File byteSpanFile = new File(args[2],files[i].getName());
				buildCrList.readSentBytespanFile(byteSpanFile);
				buildCrList.extractCorefChain(files[i]);
				buildCrList.printBytespanListHM();
				buildCrList.printCorefHash();
				
				File sgmFile = new File(args[1], files[i].getName());
				File xmlFile = new File(args[1],files[i].getName()+".tmx.rdc.xml");
				ExtractACECoref extractACECoref = new ExtractACECoref();
				extractACECoref.obtainGoldCr(args[1]+"/"+files[i].getName()+".tmx.rdc.xml");
				extractACECoref.process(args[1]+"/"+files[i].getName());
				extractACECoref.matchEntity2Sent();
			}			
		}else{
			BuildEmProCorefList2 buildCrList = new BuildEmProCorefList2();
			File byteSpanFile = new File(args[2],inputFile.getName());
			buildCrList.readSentBytespanFile(byteSpanFile);
			buildCrList.extractCorefChain(inputFile);
			buildCrList.printBytespanListHM();
			buildCrList.printCorefHash();
			File xmlFile = new File(inputFile.getName()+".tmx.rdc.xml");
			ExtractACECoref extractACECoref = new ExtractACECoref();
			extractACECoref.obtainGoldCr(args[1]+"/"+inputFile.getName()+".tmx.rdc.xml");
			extractACECoref.process(args[1]+"/"+inputFile.getName());
			extractACECoref.matchEntity2Sent();
		}
	}
}
