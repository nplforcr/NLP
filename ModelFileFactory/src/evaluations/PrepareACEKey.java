package evaluations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import readers.AceReader;
import utils.CorefClusters;
import utils.CorefFunctions;
import utils.Pair;
import annotation.Mention;

public class PrepareACEKey extends AceReader implements PrepareKey{
	
	protected static List<Pair> mentionSpanList;
	protected static HashMap<Pair,String> bytespanIdM;
	static HashMap<String,List<Pair>> idMentionListM; 
	
	List<String> pronounList;
	List<String> thirdRefList;
	List<String> maPronounsList;
	List<String> fePronounsList;
	List<String> nePronounsList;
	List<String> singPronounList;
	List<String> pluralList;
	List<String> otherPronounsList;
	boolean debug;
	boolean nootherpron;
	boolean xmlonly = true;

	/**
	 * constructor, a few lists of pronouns are constructed
	 */
	public PrepareACEKey(boolean debug, boolean nootherpron){
		this.debug = debug;
		this.nootherpron = nootherpron;
		idMentionListM = new HashMap<String,List<Pair>>();
		bytespanIdM = new HashMap<Pair,String>();
		mentionSpanList = new ArrayList<Pair>();
		String[] thirdRefArray = {"himself","herself","itself","themselves"};
		String[] thirdPronArray = {"he", "his", "him","she", "her","it", "its", "hers","they", "their", "them",};
		String[] maPronounsArray = { "he", "his", "him","himself"};
		String[] fePronounsArray = { "she", "her", "hers","herself" };
		String[] nePronounsArray = { "it", "its","itself", "they", "their", "them", "these", "those","themselves" };
		String[] singPronounArray = { "this", "that", "he", "his", "him","himself", "she", "her", "hers","herself", "it", "its","itself" };
		String[] pluralArray = { "they", "their", "them", "these", "those","themselves"};
		String[] otherPronouns = {"you","yourself","youselves","yours","your","i","me","my","mine","we","our","ourselves","us","'s"};
		otherPronounsList=Arrays.asList(otherPronouns);
		thirdRefList = Arrays.asList(thirdRefArray);
		pronounList = Arrays.asList(thirdPronArray);
		maPronounsList = Arrays.asList(maPronounsArray);
		//System.out.println("true or false:::::: "+maPronounsList.contains("he"));
		fePronounsList = Arrays.asList(fePronounsArray);
		nePronounsList = Arrays.asList(nePronounsArray);
		singPronounList = Arrays.asList(singPronounArray);
		pluralList = Arrays.asList(pluralArray);
	}
	
//	public PrepareACEKey(boolean nootherpron){
//		this.nootherpron = nootherpron;
//		idMentionListM = new HashMap<String,List<Pair>>();
//		bytespanIdM = new HashMap<Pair,String>();
//		mentionSpanList = new ArrayList<Pair>();
//		String[] thirdRefArray = {"himself","herself","itself","themselves"};
//		String[] thirdPronArray = {"he", "his", "him","she", "her","it", "its", "hers","they", "their", "them",};
//		String[] maPronounsArray = { "he", "his", "him","himself"};
//		String[] fePronounsArray = { "she", "her", "hers","herself" };
//		String[] nePronounsArray = { "it", "its","itself", "they", "their", "them", "these", "those","themselves" };
//		String[] singPronounArray = { "this", "that", "he", "his", "him","himself", "she", "her", "hers","herself", "it", "its","itself" };
//		String[] pluralArray = { "they", "their", "them", "these", "those","themselves"};
//		String[] otherPronouns = {"you","yourself","youselves","yours","your","i","me","my","mine","we","our","ourselves","us","'s"};
//		otherPronounsList=Arrays.asList(otherPronouns);
//		thirdRefList = Arrays.asList(thirdRefArray);
//		pronounList = Arrays.asList(thirdPronArray);
//		maPronounsList = Arrays.asList(maPronounsArray);
//		//System.out.println("true or false:::::: "+maPronounsList.contains("he"));
//		fePronounsList = Arrays.asList(fePronounsArray);
//		nePronounsList = Arrays.asList(nePronounsArray);
//		singPronounList = Arrays.asList(singPronounArray);
//		pluralList = Arrays.asList(pluralArray);
//	}
	
	public PrepareACEKey(boolean debug){
		this.debug = debug;
		idMentionListM = new HashMap<String,List<Pair>>();
		bytespanIdM = new HashMap<Pair,String>();
		mentionSpanList = new ArrayList<Pair>();
		String[] thirdRefArray = {"himself","herself","itself","themselves"};
		String[] thirdPronArray = {"he", "his", "him","she", "her","it", "its", "hers","they", "their", "them",};
		String[] maPronounsArray = { "he", "his", "him","himself"};
		String[] fePronounsArray = { "she", "her", "hers","herself" };
		String[] nePronounsArray = { "it", "its","itself", "they", "their", "them", "these", "those","themselves" };
		String[] singPronounArray = { "this", "that", "he", "his", "him","himself", "she", "her", "hers","herself", "it", "its","itself" };
		String[] pluralArray = { "they", "their", "them", "these", "those","themselves"};
		String[] otherPronouns = {"you","yourself","youselves","yours","your","i","me","my","mine","we","our","ourselves","us","'s"};
		otherPronounsList=Arrays.asList(otherPronouns);
		thirdRefList = Arrays.asList(thirdRefArray);
		pronounList = Arrays.asList(thirdPronArray);
		maPronounsList = Arrays.asList(maPronounsArray);
		//System.out.println("true or false:::::: "+maPronounsList.contains("he"));
		fePronounsList = Arrays.asList(fePronounsArray);
		nePronounsList = Arrays.asList(nePronounsArray);
		singPronounList = Arrays.asList(singPronounArray);
		pluralList = Arrays.asList(pluralArray);
	}
	
	public PrepareACEKey(){
		idMentionListM = new HashMap<String,List<Pair>>();
		bytespanIdM = new HashMap<Pair,String>();
		mentionSpanList = new ArrayList<Pair>();
		String[] thirdRefArray = {"himself","herself","itself","themselves"};
		String[] thirdPronArray = {"he", "his", "him","she", "her","it", "its", "hers","they", "their", "them",};
		String[] maPronounsArray = { "he", "his", "him","himself"};
		String[] fePronounsArray = { "she", "her", "hers","herself" };
		String[] nePronounsArray = { "it", "its","itself", "they", "their", "them", "these", "those","themselves" };
		String[] singPronounArray = { "this", "that", "he", "his", "him","himself", "she", "her", "hers","herself", "it", "its","itself" };
		String[] pluralArray = { "they", "their", "them", "these", "those","themselves"};
		String[] otherPronouns = {"you","yourself","youselves","yours","your","i","me","my","mine","we","our","ourselves","us","'s"};
		otherPronounsList=Arrays.asList(otherPronouns);
		thirdRefList = Arrays.asList(thirdRefArray);
		pronounList = Arrays.asList(thirdPronArray);
		maPronounsList = Arrays.asList(maPronounsArray);
		//System.out.println("true or false:::::: "+maPronounsList.contains("he"));
		fePronounsList = Arrays.asList(fePronounsArray);
		nePronounsList = Arrays.asList(nePronounsArray);
		singPronounList = Arrays.asList(singPronounArray);
		pluralList = Arrays.asList(pluralArray);
	}
	
	public void processTest(String sgmFileName, String xmlFileName) throws ParserConfigurationException, SAXException, IOException{
		System.out.println("sgm file name: "+sgmFileName+" xmlFileName: "+xmlFileName);
		processDocument(sgmFileName, xmlFileName);
		CorefFunctions.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
		CorefFunctions.fillIdMenListM(idMentionListM, idNeM);
	}
	
	public void process (String dirName) throws IOException, ParserConfigurationException, SAXException{

		File dFile =  new File (dirName);
		String outStr = dFile.getName();
		System.out.println(outStr);
		File outDir = null;

		if( dFile.isDirectory()){
			outDir = new File(dFile.getParentFile(),outStr+"Model");
			if(!outDir.exists()){
				outDir.mkdir();
			}
			File files[]  =  dFile.listFiles();
			for(int i=0; i< files.length; i++){
				if(files[i].isFile()){
					String filename = files[i].getName();
					if(xmlonly){
						if(filename.endsWith(".xml")){
							System.out.println("xml file name: "+filename);
							processDocument(null,files[i].getAbsolutePath());
							CorefFunctions.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
							CorefFunctions.fillIdMenListM(idMentionListM, idNeM);
							//fillMentionList();
							//this.fillIdMenListM();
						}
					}else{
						if(filename.endsWith(".sgm")){
							System.out.println("sgm file name: "+filename);
							String xmlFileName = files[i].getAbsolutePath()+".tmx.rdc.xml";
							processDocument(files[i].getAbsolutePath(),xmlFileName);
							
							CorefFunctions.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
							CorefFunctions.fillIdMenListM(idMentionListM, idNeM);
						}
					}
				} else if(files[i].isDirectory()){
					process(files[i].getAbsolutePath());
				}
			}
		}else{
//			outDir = new File(dFile.getParentFile().getParent(),dFile.getParentFile().getName()+"Model");
//			if(!outDir.exists()){
//				outDir.mkdir();
//			}
			
			if(xmlonly){
				if(dirName.endsWith(".xml")){
					System.out.println("xml file name: "+dirName);
					processDocument(null,dFile.getAbsolutePath());
					CorefFunctions.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
					CorefFunctions.fillIdMenListM(idMentionListM, idNeM);
					//fillMentionList();
					//this.fillIdMenListM();
				}
			}else{
				if(dirName.endsWith(".sgm")){
					System.out.println("sgm file name: "+dirName);
					String xmlFileName = dFile.getAbsolutePath()+".tmx.rdc.xml";
					processDocument(dFile.getAbsolutePath(),xmlFileName);
					CorefFunctions.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
					CorefFunctions.fillIdMenListM(idMentionListM, idNeM);
				}
			}
		}
	}
	
	public void process (File dFile) throws IOException, ParserConfigurationException, SAXException{
		String outStr = dFile.getName();
		System.out.println(outStr);
		File outDir = null;

		if( dFile.isDirectory()){
			outDir = new File(dFile.getParentFile(),outStr+"Model");
			if(!outDir.exists()){
				outDir.mkdir();
			}
			File files[]  =  dFile.listFiles();
			for(int i=0; i< files.length; i++){
				if(files[i].isFile()){
					String filename = files[i].getName();
					if(xmlonly){
						if(filename.endsWith(".xml")){
							System.out.println("xml file name: "+filename);
							processDocument(null,files[i].getAbsolutePath());
							CorefFunctions.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
							CorefFunctions.fillIdMenListM(idMentionListM, idNeM);
							//fillMentionList();
							//this.fillIdMenListM();
						}
					}else{
						if(filename.endsWith(".sgm")){
							System.out.println("sgm file name: "+filename);
							String xmlFileName = files[i].getAbsolutePath()+".tmx.rdc.xml";
							processDocument(files[i].getAbsolutePath(),xmlFileName);
							CorefFunctions.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
							CorefFunctions.fillIdMenListM(idMentionListM, idNeM);
						}
					}
					
				} else if(files[i].isDirectory()){
					process(files[i].getAbsolutePath());
				}
			}
		}else{
			outDir = new File(dFile.getParentFile().getParent(),dFile.getParentFile().getName()+"Model");
			if(!outDir.exists()){
				outDir.mkdir();
			}
			String dirName = dFile.getName();
			if(xmlonly){
				if(dirName.endsWith(".xml")){
					processDocument(null,dFile.getAbsolutePath());
					CorefFunctions.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
					CorefFunctions.fillIdMenListM(idMentionListM, idNeM);
					//fillMentionList();
					//fillIdMenListM();
				}
			}else{
				if(dirName.endsWith(".sgm")){
					System.out.println("sgm file name: "+dirName);
					String xmlFileName = dFile.getAbsolutePath()+".tmx.rdc.xml";
					processDocument(dFile.getAbsolutePath(),xmlFileName);
					CorefFunctions.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
					CorefFunctions.fillIdMenListM(idMentionListM, idNeM);
				}
			}
			
		}
	}
	
	public void process (String dirName,String outputFileName) throws IOException, ParserConfigurationException, SAXException{

		File dFile =  new File (dirName);
		if( dFile.isDirectory()){
			File files[]  =  dFile.listFiles();
			for(int i=0; i< files.length; i++){
				if(files[i].isFile()){
					String filename1 = files[i].getName();
					if(xmlonly){
						if(filename1.endsWith(".xml")){
							System.out.println("xml file name: "+filename1);
							processDocument(null,files[i].getAbsolutePath());
							File indModelFile = new File(outputFileName);
							if(!indModelFile.exists()){
								indModelFile.mkdir();
							}
							PrintWriter pwIndModelInput = new PrintWriter(new FileWriter(new File(indModelFile,dFile.getName())));
						}
					}else{
						if(filename1.endsWith(".sgm")){
							System.out.println("sgm file name: "+filename1);
							String xmlFileName = files[i].getAbsolutePath()+".tmx.rdc.xml";
							processDocument(files[i].getAbsolutePath(),xmlFileName);
							File indModelFile = new File(outputFileName);
							if(!indModelFile.exists()){
								indModelFile.mkdir();
							}
							PrintWriter pwIndModelInput = new PrintWriter(new FileWriter(new File(indModelFile,dFile.getName())));
						}
					}
				} else if(files[i].isDirectory()){
					process(files[i].getAbsolutePath());
				}
			}
		}
	}
	
	/**
	 * The logic here is that if the space between two words are larger than 1, we need to shorten it to 1 since Stanford MaxenTagger tokenze the text
	 * and the word in each sentence is only separated by one byte.
	 * Now, we need to split punctuation marks for the same reason, Stanford MaxentTagger also split punctuation marks as well.      
	 */
	public void adjustACEBytespan(){
		//Pattern pattern = Pattern.compile("[\\w!\\"\\#\\$\\%\\&\\'\\(\\)*+\\,\\.\/\:;<=>?@\^_`{|}~-]\\s+");
		Collections.sort(mentionSpanList);
		Pattern pattern = Pattern.compile("[-\\w!,?:;.\"\'%]\\s+");
		System.out.println(docBuf.toString());
		//Matcher matcher = pattern.matcher(docBuf.toString());
		String tempDoc = docBuf.toString();
		StringBuffer shorterBuf = new StringBuffer();
		Matcher matcher = pattern.matcher(tempDoc);
		boolean found = matcher.find();
		int countFound = 0;
		while(found){
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
				Pattern firstL = Pattern.compile("[-\\$\\w!,?:;.\"\'%]");
				//Pattern firstL = Pattern.compile("w");
				//Pattern firstL = Pattern.compile("\\w!,.\"\'%.*");
				//Matcher matchFirstL = firstL.matcher("word");
				Matcher matchFirstL = firstL.matcher(tempDoc.substring(1));
				boolean foundLetter = matchFirstL.find();
				if(foundLetter){
					String matchedFl = matchFirstL.group();
					//System.out.println("matchedF1 in foundLetter : "+matchedFl+" the first index: "+tempDoc.substring(1).indexOf(matchedFl)+" spaceStart: "+spaceStart+" "+tempDoc);
					//firstPart.append(tempDoc.substring(tempDoc.substring(1).indexOf(matchedFl)-1));
					String appendToken = tempDoc.substring(tempDoc.substring(1).indexOf(matchedFl),spaceStart+1);
					//System.out.println("appendToken: "+appendToken);
					firstPart.append(appendToken);
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

			if(matchLength>2){
				for(int i=0;i<mentionSpanList.size();i++){
					Pair mentionSpanPair = mentionSpanList.get(i);
					String menId = bytespanIdM.get(mentionSpanPair);
					String neId = menId.substring(0,menId.indexOf("-"));
					Mention mention = idMentionM.get(menId);
					String refphrase = mention.getExtentCoveredText();
					//System.out.println(refphrase);
					List<Pair> menList = idMentionListM.get(neId);
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
		
		System.out.println("shorterBuf leng: "+shorterBuf.length());
		System.out.println(shorterBuf);
		String[] bufArray = shorterBuf.toString().split("\\s");
		String cutShortBuf = shorterBuf.toString();
		StringBuffer matchBuf2Stanform = new StringBuffer(); 
		int bufleng = 0;
		int preIndPunc = 0;
		for(int i=0;i<bufArray.length;i++){
			String word = bufArray[i].replace("\\s+", "");
			//bufleng needs to add the byte of white space
			bufleng+=word.length()+1;
			Pattern puncP = Pattern.compile("[\\w.,?:;][!,?:;.\"\'%]+$");
			Matcher matchPunc = puncP.matcher(word);
			boolean foundPunc = matchPunc.find();
			Pattern puncP2 = Pattern.compile("\"\\w+[\"]*$");
			Matcher matchPunc2 = puncP2.matcher(word);
			boolean foundPunc2 = matchPunc2.find();
			
			Pattern puncP3 = Pattern.compile("\\w+\'\\w+$");
			Matcher matchPunc3 = puncP3.matcher(word);
			boolean foundPunc3 = matchPunc3.find();
			
			Pattern puncP4 = Pattern.compile("\\$\\w+");
			Matcher matchPunc4 = puncP4.matcher(word);
			boolean foundPunc4 = matchPunc4.find();
			int countSent = 0;
			if(foundPunc || foundPunc2 ||foundPunc3 || foundPunc4){
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
				}else{
					match=matchPunc4.group();
				}
				
				//it seems that 
				//the following conditions now only works for outputing sentences, I need add more code to change the bytes span.
				//match starts with a letter and then follows a punctuation mark. like "l.", but I should change it to include numbers and another 
				//punctuation marks
				//I have added. It is good.
				if(match.matches("\\w[.?!]")){
					int indexPeriod = shorterBuf.indexOf(match,preIndPunc)+1;
					String sent = shorterBuf.substring(preIndPunc+1,indexPeriod+1);
					//cutShortBuf = cutShortBuf.substring(indexPeriod);
					System.out.println(preIndPunc+" "+indexPeriod+" "+sent);
					//keep preIndPunc for outputing next sentence which should be a string between preIndPunc and indexPeriod
					preIndPunc = indexPeriod+1;
					//"\"" means ". Don't confuse \. If we want to print out \, we need \\.
					//bufleng++ is due to that when we split a punc like ",", we need to add one white space. 
					//bufleng++; we should add bufleng here since the byte number is not adding. 
					//in this case, word is like "extravagant.". then, we split it as "extravagant" and "." and connected by a white space.
					//but note: in this case, bytes number are the same. Only add one to the byte index of "."
					matchBuf2Stanform.append(word.substring(0, word.length()-1)+" "+word.charAt(word.length()-1));
				}
				//the second case would consider cases as "working ahead."
				else if(match.endsWith("\"") && match.charAt(match.length()-2)=='.'){
					//the match is d.". therefore, indexQuote should be match index + 2;
					int indexQuote = shorterBuf.indexOf(match,preIndPunc)+2;
					String sent = shorterBuf.substring(preIndPunc+1,indexQuote+1);
					//cutShortBuf = cutShortBuf.substring(indexPeriod);
					//System.out.println("word : "+word);
					//System.out.println(preIndPunc+" "+indexQuote+" "+sent);
					preIndPunc = indexQuote+1;
					bufleng+=2; //since " becomes '', don't know why
					//matchBuf2Stanform.append(word.substring(0, word.length()-2)+" "+word.charAt(word.length()-2)+" "+word.charAt(word.length()-1));
					matchBuf2Stanform.append(word.substring(0, word.length()-2)+" "+word.charAt(word.length()-2)+" "+"\'\'");
				}else if(match.matches("\\w[,:;]")){
					int indexComma = shorterBuf.indexOf(match,preIndPunc)+1;
					String sent = shorterBuf.substring(preIndPunc+1,indexComma+1);
					//cutShortBuf = cutShortBuf.substring(indexPeriod);
					//System.out.println(preIndPunc+" "+indexComma+" "+sent);
					preIndPunc = indexComma+1;
					bufleng++;
					matchBuf2Stanform.append(word.substring(0, word.length()-1)+" "+word.charAt(word.length()-1)+" ");
				}else if(match.matches("\"\\w+[\"]*$")){
					int indexLq = shorterBuf.indexOf(match,preIndPunc)+1;
					String sent = shorterBuf.substring(preIndPunc+1,indexLq+1);
					//System.out.println(preIndPunc+" "+indexLq+" "+sent);
					preIndPunc = indexLq+1;
					bufleng+=2;
					//matchBuf2Stanform.append(word.charAt(0)+" "+word.substring(1)+" ");
					matchBuf2Stanform.append("\'\'"+" "+word.substring(1)+" ");
				}
				//not done yet for this case. 
				else if(match.startsWith("\"") && match.endsWith("\"")){
					int indexLb = shorterBuf.indexOf(match,preIndPunc)+2;
					int indexRb = shorterBuf.indexOf(match,indexLb)+2;
					bufleng+=3;
					matchBuf2Stanform.append("\'\'"+" "+word.substring(1,word.length()-2)+"\'\'");
				}
				//in the following, we didn't handle the case where doesn't correctly since it is split as doesn and 't not the does and n't.
				else if(match.matches("\\w+\'\\w+$")){
					int indMatch = shorterBuf.indexOf(match,preIndPunc);
					int indSq = word.indexOf("\'");
					int indexSq = shorterBuf.indexOf("\'",preIndPunc);
					String sent = shorterBuf.substring(preIndPunc+1,indexSq+1);
					//System.out.println(preIndPunc+" "+indexSq+" "+sent);
					preIndPunc = indexSq+1;
					bufleng++;
					//matchBuf2Stanform.append(word.charAt(0)+" "+word.substring(1)+" ");
					matchBuf2Stanform.append(word.substring(0,indSq)+" "+word.substring(indSq)+" ");
				}else if(match.matches("\\w+%")){
					//84% is split into 84 and %. This is in fact not correct. But the stanford output looks like this.
					//so, I just follow it for consistency.
					bufleng++;
					matchBuf2Stanform.append(word.substring(0,word.length()-1)+" "+word.charAt(word.length()-1)+" ");
				}else if(match.matches("\\$\\w+")){
					//similarly, $3,000 is split as $ and 3,000. This is not correct. But for consistency.
					bufleng++;
					matchBuf2Stanform.append(word.charAt(0)+" "+word.substring(1)+" ");
				}
				
				for(int j=0;j<mentionSpanList.size();j++){
					//I think that I have found the true bug here. In this step, menBytePair may be called more than once. so, 
					//some items are added more bytes
					Pair mentionSpanPair = mentionSpanList.get(j);
					String menId = bytespanIdM.get(mentionSpanPair);
					String neId = menId.substring(0,menId.indexOf("-"));
					Mention mention = idMentionM.get(menId);
					String refword = mention.getExtentCoveredText();
					//System.out.println(refword);
					List<Pair> menList = idMentionListM.get(neId);
					//find the position in the menList
					int posInmenList = -1;
					for(int k=0;k<menList.size();k++){
						Pair menBytePair = menList.get(k);
						if(menBytePair.equals(mentionSpanPair)){
							posInmenList = k;
							break;
						}
					}
					bytespanIdM.remove(mentionSpanPair);
					int menStart =Integer.parseInt(mentionSpanPair.o1.toString());
					int menEnd =Integer.parseInt(mentionSpanPair.o2.toString());
					//System.out.println("before reducing in adjustACEBytesspan: match length: bufleng: "+bufleng+" menStart: "+menStart+" menEnd: "+menEnd);
					//it seems that the following line should not be needed since the ending punctuation mark will not change the byte number.
//					if(menStart>bufleng && !match.matches("\\w[!?.]$")){
//						menStart = menStart+1;
//						menEnd = menEnd+1;
//					}else 
					//therefore, menStart should only add 2 bytes, cases like "ahead."" should change as "ahead . "". Namely, bytes number changes from 7 to 9. 	
					if(menStart>bufleng){
						//the first if is unnecessary
						//if(match.matches("\\w[.?!]")){
							//continue;
						//}else 
						if(match.endsWith("\"") && match.charAt(match.length()-2)=='.'){
							menStart+=2;
							menEnd+=2;	
						}else if(match.matches("\\w[,:;]")){
							menStart++;
							menEnd++;
						}
						//the following else if is the same the next one, so it is right
						//else if(match.matches("\"\\w+[\"]*$")){
						else if(match.matches("\"\\w+$")){
							menStart+=2;
							menEnd+=2;
						}else if(match.startsWith("\"") && match.endsWith("\"")){
							//should only increase by 3 since "what" becomes '' what ''. The right quotes only add one. 
							menStart+=3;
							menEnd+=3;
						}else if(match.matches("\\w+\'\\w+$")){
							menStart++;
							menEnd++;
						}else if(match.matches("\\w+%")){
							menStart++;
							menEnd++;
						}else if(match.matches("\\$\\w+")){
							menStart++;
							menEnd++;
						}
					}
						
					//else if(menStart>bufleng && match.) //please consider cases like he's, in stanford parser, it is split as he 's. 
						//Dingchegn Li smart guy though slow guys as well sometimes. 
						//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
					//System.out.println("after reducing in adjustACEBytesspan: shorterBuf length: "+shorterBuf.length()+" bufleng: "+bufleng+" mention: "+mention.getExtentCoveredText()+" "+menStart+" "+menEnd);
					mentionSpanPair = new Pair(menStart,menEnd);
					mentionSpanList.set(j, mentionSpanPair);
					bytespanIdM.put(mentionSpanPair, menId);
					if(posInmenList>=0){
						menList.set(posInmenList, mentionSpanPair);
						idMentionListM.put(neId, menList);
					}
				}
			}else{
				matchBuf2Stanform.append(word+" ");
			}
			
			int testleng = bufleng;
			testleng = 0;
			testleng ++;
		}
		System.out.println("bufleng: "+bufleng+" matchBuf2Stanform length: "+matchBuf2Stanform.length());
		System.out.println(matchBuf2Stanform.toString());
		
		
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
	
	public ArrayList<CorefClusters> getKeyData(List<String> bsTokenList){
		for(int i=0;i<mentionSpanList.size();i++){
			Pair mentionSpanPair = mentionSpanList.get(i);
			String menId = bytespanIdM.get(mentionSpanPair);
			Mention mention = idMentionM.get(menId);
			//System.out.println(mentionSpanPair+" "+mention.getExtentCoveredText());
			String neId = menId.substring(0,menId.indexOf("-"));
		}
		ArrayList<CorefClusters> keyDataList = CorefFunctions.getKeyData(bsTokenList, bytespanIdM, idMentionM, idMentionListM, mentionSpanList);
		return keyDataList;
	}
	
//	/**
//	 * idMentionM is the list of mentions which belong to the same named entity. Namely, they are coreferring
//	 */
//	public void fillMentionList(){
//
//		//idNeM;
//		//idMentionM;
//		Iterator<String> menIter = idMentionM.keySet().iterator();
//		while(menIter.hasNext()){
//			String menId = menIter.next();
//			Mention mention = idMentionM.get(menId);
//			//int[] bytespan = new int[2];
//			//bytespan[0]=mention.getExtentSt();
//			//bytespan[1]=mention.getExtentEd();
//			//Pair bytespan = new Pair(mention.getExtentSt(),mention.getExtentEd());
//			int startInd = mention.getHeadCoveredText().indexOf("\"")+1;
//			int endInd = mention.getHeadCoveredText().lastIndexOf("\"");
//			String word = mention.getHeadCoveredText().substring(startInd,endInd).toLowerCase();
//			if(nootherpron){
//				if(otherPronounsList.contains(word)||word.equals("'s")){
//					continue;
//				}else{
//					if (word.contains(" ")) {
//						String[] tokenArray = word.split(" ");
//						word = tokenArray[tokenArray.length - 1];
//					}
//					int startByte = mention.getHeadEd()-word.length()+1;
//					Pair bytespan = new Pair(startByte,mention.getHeadEd());
//					if(!mentionSpanList.contains(bytespan)){
//						mentionSpanList.add(bytespan);
//					}
//					
//					bytespanIdM.put(bytespan, menId);
//					//System.out.println(mention.getType() + " "+menId+" "+mention.getHeadCoveredText()+" "+bytespan.toString());
//				}
//			}else{
//				if (word.contains(" ")) {
//					String[] tokenArray = word.split(" ");
//					word = tokenArray[tokenArray.length - 1];
//				}
//				int startByte = mention.getHeadEd()-word.length()+1;
//				Pair bytespan = new Pair(startByte,mention.getHeadEd());
//				if(!mentionSpanList.contains(bytespan)){
//					mentionSpanList.add(bytespan);
//				}
//				bytespanIdM.put(bytespan, menId);
//				//System.out.println(mention.getType() + " "+menId+" "+mention.getHeadCoveredText()+" "+bytespan.toString());
//			}
//		}
//		//System.out.println("mentionSpanList size above: "+mentionSpanList.size());
//	}
//	
//	/**
//	 * Id is named entity ID. Each coreferring mention is under each named entity. So, if we have idNeM and idMenM
//	 * we can find which one is coreferring with which one. 
//	 */
//	public void fillIdMenListM(){
//		Iterator<String> iterIdNeM = idNeM.keySet().iterator();
//		while(iterIdNeM.hasNext()){
//			String id = iterIdNeM.next();
//			NamedEntity ne = idNeM.get(id);
//			List<Mention> menList = ne.getMentions();
//			List<Pair> menByteList = new ArrayList<Pair>();
//			for(Mention mention:menList){
//				int startInd = mention.getHeadCoveredText().indexOf("\"")+1;
//				int endInd = mention.getHeadCoveredText().lastIndexOf("\"");
//				String word = mention.getHeadCoveredText().substring(startInd,endInd).toLowerCase();
//				if (word.contains(" ")) {
//					String[] tokenArray = word.split(" ");
//					word = tokenArray[tokenArray.length - 1];
//				}
//				
//				if(otherPronounsList.contains(word)||word.equals("'s")){
//					continue;
//				}else{
//					int startByte = mention.getHeadEd()-word.length()+1;
//					Pair bytespan = new Pair(startByte,mention.getHeadEd());
//					menByteList.add(bytespan);
//				}
//			}
//			Collections.sort(menByteList);
//			String numId = id.substring(id.indexOf("-E")+2);
//			if(debug){
//				System.out.println("id in fillIdMenListM: "+id+" numId: "+numId);
//			}
//			idMentionListM.put(numId, menByteList);
//		}
//	}

	/*public List<CorefClusters> getKeyData(List<String> bsTokenList){
		boolean firstCata = false;
		List<CorefClusters> keyDataList = new ArrayList<CorefClusters>();  
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
			
			if(debug){
				System.out.println("bytespan: "+bytespan+" menId: "+menId);
			}
			

			Mention mention = idMentionM.get(menId);
			
			int startInd = mention.getHeadCoveredText().indexOf("\"")+1;
			int endInd = mention.getHeadCoveredText().lastIndexOf("\"");
			String word = mention.getHeadCoveredText().substring(startInd,endInd).toLowerCase();
			if (word.contains(" ")) {
				String[] tokenArray = word.split(" ");
				word = tokenArray[tokenArray.length - 1];
			}
			
			if(sortedIndex==0 && pronounList.contains(word)){
				firstCata = true;
				continue;
			}
			
			//System.out.println(menId+" "+word+" "+ThirdPersonPron.thirdPerPronList2.contains(word));
			if(debug){
				System.out.println("Entity "+sortedIndex+" "+menId+" "+word+" ");
				bsTokenList.add(bytespan+" "+word);
			}
			if(ThirdPersonPron.thirdPerPronList2.contains(word)||ThirdReflexives.thirdRefList2.contains(word)){
				
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
					String corefMenId = bytespanIdM.get(corefByteSpan);
					Mention corefMention = idMentionM.get(corefMenId);
					//System.out.println("corefMention: "+corefMention.getHeadCoveredText());
					//since the word looks like the following: string = "he", we need to find the index of the left quote ". 
					int corefStInd = corefMention.getHeadCoveredText().indexOf("\"")+1;
					//since the word looks like the following: string = "he", we need to find the index of the right quote ". 
					int corefEndInd =corefMention.getHeadCoveredText().lastIndexOf("\"");
					String corefWord = corefMention.getHeadCoveredText().substring(corefStInd,corefEndInd);
					if (corefWord.contains(" ")) {
						String[] tokenArray = corefWord.split(" ");
						corefWord = tokenArray[tokenArray.length - 1].toLowerCase();
					}
					sortedCorefIndex = sortedMentionSpanList.indexOf(corefByteSpan);
					//System.out.println(mentionSpanList.get(index));
					
					if(firstCata==true){
						sortedCorefIndex -= 1;
					}
					System.out.println("IDENT "+sortedCorefIndex+" "+corefWord+" "+sortedIndex+" "+word);
					CorefClusters keyData = new CorefClusters();
					keyData.setCorefIndex(sortedCorefIndex);
					keyData.setCorefWord(corefWord);
					keyData.setAnaIndex(sortedIndex);
					keyData.setAnaphor(word);
					keyDataList.add(keyData);
				}
			}
			sortedIndex ++ ;
		}
		return keyDataList;
	}
*/	/**
	 * @param args
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub

		boolean debug = false;
		boolean nootherpron = false;
		boolean singleTest = true;
		
		if(singleTest){
			PrepareACEKey prepareACEKey = new PrepareACEKey();
			if(args.length==1){
				prepareACEKey = new PrepareACEKey();
				prepareACEKey.process(args[0]);
				//prepareACEKey.adjustACEBytespan();
				List<String> bsTokenList = new ArrayList<String>();
				prepareACEKey.getKeyData(bsTokenList);
			}else if(args.length==2){
				prepareACEKey.processTest(args[0], args[1]);
				prepareACEKey.adjustACEBytespan();
				List<String> bsTokenList = new ArrayList<String>();
				prepareACEKey.getKeyData(bsTokenList);
			}

		}else{
			String dir = "";
			PrepareACEKey prepareACEKey = null;
			if(args.length>=3){
				if(args[0].equals("true")){
					debug = true;
				}
				
				if(args[1].equals("true")){
					nootherpron = true;
				}
				
				prepareACEKey=new PrepareACEKey(debug,nootherpron);
				dir= args[2]; //"/home/dingcheng/Documents/OSU_corpora/ace_phase2/data/ace2_train/bnews";
			}else if(args.length==2){
				if(args[0].equals("true")){
					debug = true;
				}
				prepareACEKey=new PrepareACEKey(debug);
				dir = args[1];
			}
			else{
				dir = args[0];
			}
		 	prepareACEKey.process(dir);
			//it is called in process(dir);
			//prepareACEKey.fillMentionList();
			List<String> bsTokenList = new ArrayList<String>();
			//CorefFunctions.getKeyData(bsTokenList, bytespanIdM, idMentionM, idMentionListM, mentionSpanList);
			//CorefFunctions.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
			//CorefFunctions.fillIdMenListM(idMentionListM, idNeM);
			prepareACEKey.getKeyData(bsTokenList);
			prepareACEKey.adjustACEBytespan();
		}	
	}

public List<CorefClusters> getKeyData(File conFile, File chainFile) {
	// TODO Auto-generated method stub
	return null;
}

public List<CorefClusters> getKeyData(File conFile, File chainFile, File docFile)
		throws IOException {
	// TODO Auto-generated method stub
	return null;
}
}
