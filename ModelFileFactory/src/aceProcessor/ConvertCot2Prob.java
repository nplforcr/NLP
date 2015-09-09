package aceProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import annotation.Mention;

import readers.AceReader;
import utils.CorefUser;
import utils.CorefUsers;
import utils.Pair;
import utils.ReadPosTagFile;

public class ConvertCot2Prob extends AceReader {

	protected static List<Pair> mentionSpanList;
	protected static HashMap<Pair,String> bytespanIdM;
	protected static HashMap<String,List<Pair>> idMentionListM; 
	protected static HashMap<Pair,String> sentByteSpanHm;
	protected static HashMap<String,String> sentParseHm;
	protected static String byteSpanDir = "/Users/m048100/Documents/modelblocks/ModelFileFactory/test/bytesSpan/three2one";
	//protected static String byteSpanDir = "/Users/m048100/Documents/modelblocks/ModelFileFactory/ParsedACEMay11/bytesSpan/three2one";
	protected static String dependDir="/Users/m048100/Documents/modelblocks/ModelFileFactory/ParsedACEMay11/dependTree/three2one";
	protected static String parsedDir="/Users/m048100/Documents/modelblocks/ModelFileFactory/ParsedACEMay11/parseTree/three2one";
	protected static String outputDir="/Users/m048100/Documents/modelblocks/ModelFileFactory/cotTopic/three2one";
	protected boolean debug = false;
	protected boolean debugTest = true;
	boolean nootherpron;
	boolean xmlonly = false;
	protected static int MAXLEN = 6;

	/**
	 * constructor, a few lists of pronouns are constructed
	 */
	public ConvertCot2Prob(boolean debug, boolean nootherpron){
		this.debug = debug;
		this.nootherpron = nootherpron;
		idMentionListM = new HashMap<String,List<Pair>>();
		bytespanIdM = new HashMap<Pair,String>();
		sentParseHm = new HashMap<String,String>();
		mentionSpanList = new ArrayList<Pair>();
	}

	/**
	 * constructor, a few lists of pronouns are constructed
	 */
	public ConvertCot2Prob(){
		this.debug = debug;
		this.nootherpron = nootherpron;
		idMentionListM = new HashMap<String,List<Pair>>();
		bytespanIdM = new HashMap<Pair,String>();
		sentParseHm = new HashMap<String,String>();
		mentionSpanList = new ArrayList<Pair>();
	}


	public void processTest(String sgmFileName, String xmlFileName) throws ParserConfigurationException, SAXException, IOException{
		System.out.println("sgm file name: "+sgmFileName+" xmlFileName: "+xmlFileName);
		processDocument(sgmFileName, xmlFileName);
		CorefUser.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
		CorefUser.fillIdMenListM(idMentionListM, idNeM);
	}

	public void process (String dirName) throws IOException, ParserConfigurationException, SAXException{

		File dFile =  new File (dirName);
		String outStr = dFile.getName();
		String outDirStr = "topicModels";
		System.out.println(outStr);
		//File outDir = null;

		if( dFile.isDirectory()){
			File outDir = new File(outDirStr);
			if(!outDir.exists()){
				outDir.mkdir();
			}
			System.out.println(outDir);
			File files[]  =  dFile.listFiles();
			for(int i=0; i< files.length; i++){
				if(files[i].isFile()){
					String filename = files[i].getName();
					if(xmlonly){
						if(filename.endsWith(".xml")){
							System.out.println("xml file name: "+filename);
							processDocument(null,files[i].getAbsolutePath());
							CorefUser.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
							CorefUser.fillIdMenListM(idMentionListM, idNeM);
							//fillMentionList();
							//this.fillIdMenListM();
						}
					}else{
						if(filename.endsWith(".sgm")){
							System.out.println("sgm file name: "+filename);
							String xmlFileName = files[i].getAbsolutePath()+".tmx.rdc.xml";
							processDocument(files[i].getAbsolutePath(),xmlFileName);

							CorefUser.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
							CorefUser.fillIdMenListM(idMentionListM, idNeM);
							File inputBytesFile = new File(byteSpanDir,filename);
							File inputDependFile = new File(dependDir,filename);
							File inputParsedFile = new File(parsedDir,filename);
							
							File outputFile = new File(outDir,filename);
							if(!outputFile.exists()){
								outputFile.createNewFile();
							}
							File debugDir = new File(outputDir);
							if(!debugDir.exists()){
								debugDir.mkdirs();
							}
							File debugFile = new File(debugDir,inputBytesFile.getName());
							ArrayList<ArrayList<WordDepend>> wordDependSentList=this.assignTopic(inputBytesFile, inputDependFile,inputParsedFile,debugFile);
							this.genCOTConstraints(wordDependSentList, outputFile);
							//we need to vacate all data structures after each article is processed
							
						}
						idMentionM = new HashMap<String,Mention>();
						mentionSpanList = new ArrayList<Pair>();
						bytespanIdM = new HashMap<Pair,String>();
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
					CorefUser.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
					CorefUser.fillIdMenListM(idMentionListM, idNeM);
					//fillMentionList();
					//this.fillIdMenListM();
				}
			}else{
				if(dirName.endsWith(".sgm")){
					System.out.println("sgm file name: "+dirName);
					String xmlFileName = dFile.getAbsolutePath()+".tmx.rdc.xml";
					processDocument(dFile.getAbsolutePath(),xmlFileName);
					CorefUser.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
					CorefUser.fillIdMenListM(idMentionListM, idNeM);
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
							CorefUser.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
							CorefUser.fillIdMenListM(idMentionListM, idNeM);
							//fillMentionList();
							//this.fillIdMenListM();
						}
					}else{
						if(filename.endsWith(".sgm")){
							System.out.println("sgm file name: "+filename);
							String xmlFileName = files[i].getAbsolutePath()+".tmx.rdc.xml";
							processDocument(files[i].getAbsolutePath(),xmlFileName);
							CorefUser.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
							CorefUser.fillIdMenListM(idMentionListM, idNeM);
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
					CorefUser.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
					CorefUser.fillIdMenListM(idMentionListM, idNeM);
					//fillMentionList();
					//fillIdMenListM();
				}
			}else{
				if(dirName.endsWith(".sgm")){
					System.out.println("sgm file name: "+dirName);
					String xmlFileName = dFile.getAbsolutePath()+".tmx.rdc.xml";
					processDocument(dFile.getAbsolutePath(),xmlFileName);
					CorefUser.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
					CorefUser.fillIdMenListM(idMentionListM, idNeM);
				}
			}

		}
		System.out.println("now, you are in process(File)");
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
	 * this method read into bytesspan file where sentences are detected for each article of ACE
	 * the first two tokens are the start bytes and end bytes and after that is the sentence.
	 * so, through this function, we put bytespan pair as the key and the sentence as the value of a 
	 * hashmap called byteSpanHm.
	 * 
	 * @param inputBytesFile
	 * @return
	 * @throws IOException
	 * 
	 */
	public HashMap<Pair,String> readBytespanFile(File inputBytesFile,ArrayList<Pair> bytePairList) throws IOException{
		HashMap<Pair,String> byteSpanHm = new HashMap<Pair,String>();
		BufferedReader bfByteSpan = new BufferedReader(new FileReader(inputBytesFile));
		String line = "";
		while((line=bfByteSpan.readLine())!=null){
			Pattern pattern = Pattern.compile("\\d+");
			Matcher matcher = pattern.matcher(line);
			boolean found = matcher.find();
			String match1 = matcher.group();
			found = matcher.find();
			String match2 = matcher.group();
			Pair bytePair = new Pair(match1,match2);
			int beginIndex=match1.length()+match2.length()+2;
			line = line.substring(beginIndex);
			bytePairList.add(bytePair);
			byteSpanHm.put(bytePair, line);
		}
		return byteSpanHm;
	}


	/**
	 * this method read into dependFile and then create a HashMap 
	 * 
	 * @param inputBytesFile
	 * @return
	 * @throws IOException
	 * 
	 */
	public HashMap<String,String> readDependFile(File inputDependFile,ArrayList<String> sentList) throws IOException{
		HashMap<String,String> sentParseHm = new HashMap<String,String>();
		BufferedReader bfDependFile = new BufferedReader(new FileReader(inputDependFile));
		String line = "";
		String oddLine = "";
		int count = 0;
		while((line=bfDependFile.readLine())!=null){
			//I need to consider cases where the sentence is not parsed successfully. In this case, the oddLine may not 
			//be the sentence and the evenLine may not be the parsed either. 
			//in order to handle this case, we need to make a check with regular expressions. 
			//a simple check is to consider if the first character is "[" and meanwhile, check the second token of evenLine
			//is the same as the first token of oddLine. 
			//then, if this case happens, I still need to add the oddLine to sentParseHm and the value to be null. Meanwhile
			//count add one. Only this way, the oddLine will be still the sentence and the even line will still be parse part.
			//System.out.println("line: "+count+" "+line);
			if(count%3==0){
				oddLine = line;
				//System.out.println(oddLine+" oddLine in readDependFile");
				sentList.add(oddLine);
			}else if(count%3==1){
				//System.out.println(" parsed line: "+line);
				sentParseHm.put(oddLine, line);
				oddLine="";
			}
			count++;
		}
		//		System.out.println(sentParseHm.size()+" "+sentList.size());
		//		for(int i=0;i<sentList.size();i++){
		//			System.out.println(sentList.get(i));
		//			System.out.println(sentParseHm.get(sentList.get(i)));
		//		}
		return sentParseHm;
	}

	private class WordDepend{
		String token;
		String gramRole;
		String targetToken;
		String pos;
		String menId;
		int index; //this index refers to the index of the token in the sentence
		int targetIndex; //targetIndex refers to the index of the target token related to the token in grammatical roles 
		int sentIndex; //the sentence index of the token
		int topic;
		Pair byteSpan;
		public String toString(Writer writer) throws IOException{
			//System.out.println("sentIndex in WordDepend of ConvertCot2Prob.java: "+sentIndex+" index: "+index+" token: "+token+" gramRole: "+gramRole+" targetIndex: "+targetIndex+" targetToken: "
				//	+targetToken+" byteSpan: "+byteSpan+" menId: "+menId+" pos: "+pos);
			writer.write("sentIndex in WordDepend of ConvertCot2Prob.java: "+sentIndex+" index: "+index+" token: "+token+" gramRole: "+gramRole+" targetIndex: "+targetIndex+" targetToken: "
					+targetToken+" byteSpan: "+byteSpan+" menId: "+menId+" pos: "+pos+"\n");
			//writer.flush();
			//writer.close();
			return token;
		}
		
		public String print(PrintStream ps){
			//System.out.println("sentIndex in WordDepend of ConvertCot2Prob.java: "+sentIndex+" index: "+index+" token: "+token+" gramRole: "+gramRole+" targetIndex: "+targetIndex+" targetToken: "
				//	+targetToken+" byteSpan: "+byteSpan+" menId: "+menId+" pos: "+pos);
			ps.println("sentIndex in WordDepend of ConvertCot2Prob.java: "+sentIndex+" index: "+index+" token: "+token+" gramRole: "+gramRole+" targetIndex: "+targetIndex+" targetToken: "
					+targetToken+" byteSpan: "+byteSpan+" menId: "+menId+" pos: "+pos);
			
			return token;
		}
	}

	/**
	 * 
	 * @param sentParseHm
	 * @param sentList
	 * @param byteSpanPairList
	 * @param wordDependSentList
	 * @param inputParsedFile
	 * @return
	 * @throws IOException
	 */
	public ArrayList<WordDepend> createWordDependList(HashMap<String,String> sentParseHm,ArrayList<String> sentList,ArrayList<Pair> byteSpanPairList,ArrayList<ArrayList<WordDepend>> wordDependSentList,File inputParsedFile) throws IOException{
		//in future, I should try to use LinkedHashMap or SortedMap
		//LinkedHashMap linkedSentParseHm = new LinkedHashMap(sentParseHm );
		ArrayList<WordDepend> wordDependList = new ArrayList<WordDepend>();
		ArrayList<ArrayList<Pair>> sentWordPosList=ReadPosTagFile.createPosWordList2(inputParsedFile);
		//idea here is that we want to build a list in which list of mentions in each sentence is the component.
		//In this way, we can track of sentences and thus can match coreferences conveniently
		//ArrayList<ArrayList<WordDepend>> wordDependSentList = new ArrayList<ArrayList<WordDepend>>();
		for(int i=0;i<sentList.size()-1;i++){
			ArrayList<WordDepend> ithWordDependSent = new ArrayList<WordDepend>();
			ArrayList<Pair> ithWordPosSent = sentWordPosList.get(i);
			String sent = sentList.get(i);
			Pair byteSpanPair = byteSpanPairList.get(i);
			int startByte = Integer.valueOf(byteSpanPair.getFirst().toString());
			int endByte = Integer.valueOf(byteSpanPair.getSecond().toString());
			String parsedSent = sentParseHm.get(sent);
			//System.out.println(parsedSent);
			Pattern patLbr = Pattern.compile("\\(");
			Matcher matLbr = patLbr.matcher(parsedSent);
			boolean foundLbr = matLbr.find();
			if(foundLbr==false){
				continue;
			}
			String matLbr1 = matLbr.group();
			Pattern patRbr = Pattern.compile("\\)");
			Matcher matRbr = patRbr.matcher(parsedSent);
			boolean foundRbr = matRbr.find();
			//System.out.println("foundRbr: "+foundRbr);
			String matRbr1 = matRbr.group();
			//System.out.println(matRbr1);
			int indexRb=0;
			int indexLb=0;
			int indexComma=0;
			int count = 0 ;
			int prevIndexRb=0;
			int indexDashDigitStr1=0;
			int indexDashDigitStr2=0;
			int startInd = -1;
			while(foundRbr && foundLbr){
				foundLbr = matLbr.find();
				foundRbr = matRbr.find();
				Pattern patDashDigit1 = Pattern.compile("-[\\d]+[']*,");
				//it is interesting, for the second pattern, we need to consider special cases like
				//conj_but(wants-33, wants-33') where the ending is 33') rather than common 33).
				Pattern patDashDigit2 = Pattern.compile("-[\\d]+[']*\\)");
				indexRb=parsedSent.indexOf(matRbr1,indexRb+1);
				indexLb=parsedSent.indexOf(matLbr1, indexLb+1);
				String twoWordStr = parsedSent.substring(indexLb+1,indexRb+1);
				Matcher matDashDigit1 = patDashDigit1.matcher(twoWordStr);
				boolean foundDashDigit1 = matDashDigit1.find();
				//saynum (SOURCE-4, 9801.139-1), dashDigitStr2="-1)" and dashDigitStr1="-4,";
				String dashDigitStr1="";
				//don't confuse index meanings here. 
				//for example: SOURCE-4, indexTarget = 4;
				Matcher matDashDigit2 = patDashDigit2.matcher(twoWordStr);
				boolean foundDashDigit2 = matDashDigit2.find();				
				String dashDigitStr2= "";
				//if dashDigitStr1 and dashDigitsStr2 are not existent, it implies that parsing doesn't happen
				//in this case, it seems that everthing will not be correct. What I can do is to let it go for the moment
				//Is that right? 
				if(foundDashDigit1 && foundDashDigit2){
					dashDigitStr1=matDashDigit1.group();
					int indexTarget =-1;
					if(dashDigitStr1.endsWith("',")){
						int firstQuote = dashDigitStr1.indexOf("'");
						indexTarget = Integer.valueOf(dashDigitStr1.substring(1,firstQuote));
					}else{
						indexTarget = Integer.valueOf(dashDigitStr1.substring(1,dashDigitStr1.length()-1));
					}
					
					
					dashDigitStr2=	matDashDigit2.group();
					//for example, in num(SOURCE-4, 9801.139-1), indexKeyWord = 1;
					
					int indexKeyWord = -1;
					if(dashDigitStr2.endsWith("')")){
						int firstQuote = dashDigitStr2.indexOf("'");
						indexKeyWord=Integer.valueOf(dashDigitStr2.substring(1,firstQuote));
					}else{
						indexKeyWord=Integer.valueOf(dashDigitStr2.substring(1,dashDigitStr2.length()-1));
					}
					
					//indexComma refers to the actual index of comma after -4. 
					indexComma = parsedSent.indexOf(",",indexLb);
					//when dashDigitStr2 is the same as dashDigitStr1, trouble will arise.
					//A simple solution is to add ")" to dashDigitStr2 and if necessary, 
					//add "," to dashDigitStr1
					//System.out.println("dashDigitStr2:"+dashDigitStr2);
					indexDashDigitStr2 = parsedSent.indexOf(dashDigitStr2,indexDashDigitStr2+1);
					indexDashDigitStr1 = parsedSent.indexOf(dashDigitStr1,indexDashDigitStr1+1);
					//(SOURCE-4, 9801.139-1) is a two word string, therefore, 9801.139 is the key word. dashDigitStr here refers to -1.
					String keyword=parsedSent.substring(indexComma+2,indexDashDigitStr2);
					if(startInd==-1){
						startInd=sent.indexOf(keyword);
					}else{
						//in this case, one special error may arise. In this example: "When he ... ", word he and when share something
						//as a result, the index of -he- in when may be misregarded to the index of the real word he.
						keyword=keyword+" ";
						startInd=sent.indexOf(keyword,startInd+1);
						keyword=keyword.substring(0,keyword.length()-1);
					}
					//System.out.println("keyword: "+keyword);
					String targetWord=parsedSent.substring(indexLb+1,indexDashDigitStr1);
					
					//System.out.println("targetword: "+targetWord);
					String gramRole="";
					if(count==0){
						gramRole=parsedSent.substring(1,indexLb);
						prevIndexRb=indexRb;
					}else if(indexLb>0 && count>0){
						gramRole=parsedSent.substring(prevIndexRb+3,indexLb);
						prevIndexRb=indexRb;
					}

					//System.out.println(gramRole);
					WordDepend wordDepend = new WordDepend();
					//Pair byteSpan = new Pair(startByte+indexComma+2,startByte+indexDashDigitStr2);
					Pair byteSpan = new Pair(startByte+startInd,startByte+startInd+keyword.length()-1);
					wordDepend.sentIndex=i;
					wordDepend.token=keyword;
					wordDepend.byteSpan=byteSpan;
					wordDepend.gramRole=gramRole;
					wordDepend.index=indexKeyWord;
					wordDepend.targetIndex=indexTarget;
					wordDepend.targetToken=targetWord;
					//System.out.println("keyword: "+keyword+" indexKeyWord: "+indexKeyWord);
					wordDepend.pos = ithWordPosSent.get(indexKeyWord-1).getSecond().toString();
					//System.out.println("pos: "+wordDepend.pos);
					//System.out.println(ithWordPosSent.get(indexKeyWord-1) in creatWordDependList of ConvertCot2Prob.java: +" token: "+keyword);
					//wordDepend.toString();
					wordDependList.add(wordDepend);
					ithWordDependSent.add(wordDepend);
					count++;
				}
			}
			wordDependSentList.add(ithWordDependSent);
		}
		return wordDependList;
	}


//	/**
//	 * 
//	 * @param inputBytesFile
//	 * @param inputDependFile
//	 * @throws IOException
//	 */
//	public void assignTopic1(File inputBytesFile,File inputDependFile,File inputParsedFile,File outputFile) throws IOException{
//		PrintWriter pwTopicOutput = new PrintWriter(outputFile);
//		CorefUser.adjustACEBytespan(inputBytesFile.getName(),docBuf, bytespanIdM, idMentionM, idMentionListM, mentionSpanList);
//		//////////////////////////////////
//		///mapping process: /////////////
//		//as next step, read into ParsedACEMay11, including bytesSpan, dependTree. 
//		//so, we call readBytespanFile to get byteSpanHm
//		//for dependTree file, we check if the string is the same as the string in byteSpanHm. 
//		//if so, then, check if the string after the string of files is the depend parsing. 
//		//the method of finding this is to check the format, such as if the first character is
//		//'[', after we check the first character, we can further check if the part after '[' is
//		//a syntactic item such as nsubj. 
//		//we may need to collect grammatical items to make such a judgment possible. 
//		//after we confirm that the line corresponds to the sentence, what we need to do is to
//		//iterate through bytespanIdM, get menId and from menId, we get mentions from annotation side.
//		//from depend-parse side, we need to match words by bytes spans. 
//		//this is possible, we know the start bytes and end bytes, in the parsing, the numbers after each
//		//word like NEWS-2, refers to the order of the word in the sentence, so, it is possible for us to
//		//know the bytes span of each word. 
//		//in my model, we use the head word to represent the whole phrase, so, we also need to further 
//		//make clear the bytespan of the head word. Only if so can we find map the head word of the phrase 
//		//from the annotations to the word (its syntactic role should be subject, object or similar roles) 
//		//of the article.
//
//		ArrayList<Pair> byteSpanPairList = new ArrayList<Pair>();
//		ArrayList<String> sentList = new ArrayList<String>();
//		sentByteSpanHm = this.readBytespanFile(inputBytesFile, byteSpanPairList);
//		sentParseHm = this.readDependFile(inputDependFile, sentList);
//		ArrayList<ArrayList<WordDepend>> wordDependSentList = new ArrayList<ArrayList<WordDepend>>();
//		ArrayList<WordDepend> wordDependList=this.createWordDependList(sentParseHm, sentList,byteSpanPairList,wordDependSentList,inputParsedFile);
//		//////////////////////////////////////
//		///////// topic discovery
//		//for the first sentence, we only need to record the syntactic roles, its bytespan and mention id of
//		//each mention and the mention itself
//		//we may create a new data structure for it. not so sure
//		//for the remaining sentences, we need to record above, besides, we need to check if the mention
//		//is the pronoun, which mention in previous sentence is its antecedent. If its antecedent is the subject
//		//and if the pronoun is a subject as well, then, the mention is topic. 
//		//if not, other conditions should be taken into considerations. Go ahead tomorrow. 
//		for(int i=0;i<wordDependSentList.size();i++){
//			ArrayList<WordDepend> ithWordDependSent = wordDependSentList.get(i);
//
//			for(int j=0;j<ithWordDependSent.size();j++){
//				WordDepend jthWordDepend = ithWordDependSent.get(j);
//			}
//		}
//
//		int menCount = 0;
//		for(int i=0;i<wordDependList.size();i++){
//			WordDepend ithWordDepend=wordDependList.get(i);
//			Pair ithbyteSpan = ithWordDepend.byteSpan;
//			if(mentionSpanList.contains(ithbyteSpan)){
//				String gramRole = ithWordDepend.gramRole;
//				int sentIndex = ithWordDepend.sentIndex;
//				String ithId=bytespanIdM.get(ithbyteSpan);
//				String ithMentionContent = idMentionM.get(ithId).getHeadCoveredText();
//				System.out.println(menCount+"th mention in assignTopic ConvertCot2Prob: "+i+" span "+ithId+" "+ithbyteSpan+" ithId: "+ithId+" sentIndex: "+sentIndex+" gramRole: "+gramRole+" "+ithMentionContent);
//				menCount++;
//			}
//		} 
//
//		//loop through wordDependSentList, finding topics in each sentence. The idea here is that in each sentence, there may be
//		//one or more mentions which refer back to mentions of previous sentences. If the mention is a subject and if the previous mention
//		//is a topic or a subject too, then, the current mention should be a topic as well. 
//		//our goal is the find P(topic_{t}|pos_{t}); P(topic_{t}|ind_{t},topic_{ind}) and P(topic_{t}|syn_{t})
//		for(int i=0;i<wordDependSentList.size()-1;i++){
//			ArrayList<WordDepend> ithWordDependList = wordDependSentList.get(i);
//			ArrayList<WordDepend> i1thWordDependList = wordDependSentList.get(i+1);
//			if(i==0){
//				for(int j=0;j<i1thWordDependList.size();j++){
//					WordDepend jthWordDepend=i1thWordDependList.get(j);
//					Pair jthByteSpan = jthWordDepend.byteSpan; 
//					String jthGramRole = jthWordDepend.gramRole;
//					if(bytespanIdM.containsKey(jthByteSpan)){
//						String jthMenId = bytespanIdM.get(jthByteSpan);
//						Mention jthMention = idMentionM.get(jthMenId);
//						String jthNeId = jthMenId.substring(0,jthMenId.indexOf("-"));
//						List<Pair> jthMentionSpanList = idMentionListM.get(jthNeId);
//						int indexJthByteSpan = jthMentionSpanList.indexOf(jthByteSpan);
//						if(jthMentionSpanList.contains(iithByteSpan)){
//							int indexiithByteSpan = jthMentionSpanList.indexOf(iithByteSpan);
//							int ind = indexiithByteSpan-indexJthByteSpan;
//							String jthPos = jthWordDepend.pos;
//							if(iithGramRole.contains("subj")){
//								pwTopicOutput.println("Topic_Men "+iithGramRole +" : 1");
//								pwTopicOutput.println();
//								System.out.println("Topic_Men "+iithGramRole +" : 1");
//							}else{
//								pwTopicOutput.println("Topic_Men "+iithGramRole +" : 0");
//								pwTopicOutput.println();
//								System.out.println("Topic_Men "+iithGramRole +" : 0");
//							}
//						}
//					}else{
//						pwTopicOutput.println("Topic_Men "+iithGramRole +" : 0");
//						pwTopicOutput.println();
//						System.out.println("Topic_Men "+iithGramRole +" : 0");
//					}
//				}
//			}else{
//				//idea here is that looping through i1thWordDependSent. In i1thWordDependList, we get each WordDepend.
//				//For each WordDepend, we can find the byteSpan. Then invoke bytespanIdM, we can get the mentionId.
//				//with mentionId, find mention, and also, we can find idMentionListM. Note, mentionId is the ID for one mention
//				//ID in idMentionListM is the NamedEntityId. With this infomration, we can find antecedents of current Mention. 
//				//The information about the antecedent includes: token; gramRole;targetToken;index;targetIndex;sentIndex;topic;byteSpan;
//				//if the antecedent is in different sentence and if the antecedent is also a topic, the current one is also a topic,
//				for(int ii=0;ii<ithWordDependList.size();ii++){
//					WordDepend iithWordDepend=i1thWordDependList.get(ii);
//					Pair iithByteSpan = iithWordDepend.byteSpan; 
//					String iithGramRole = iithWordDepend.gramRole;
//					if(bytespanIdM.containsKey(iithByteSpan)){
//						String iithMenId = bytespanIdM.get(iithByteSpan);
//						Mention iithMention = idMentionM.get(iithMenId);
//						String iithNeId = iithMenId.substring(0,iithMenId.indexOf("-"));
//						List<Pair> iithMentionSpanList = idMentionListM.get(iithNeId);
//						int indexJthByteSpan = iithMentionSpanList.indexOf(iithByteSpan);
//					}
//					//i+1 WordDependList: if mention in i+1 sentence, refers back to ith sentence. No matter whether 
//					//mention in ith sentence is subject or not, as long as the mention in i+1th sentence is subject,
//					//we can regard the mention is a topic.
//					for(int j=0;j<i1thWordDependList.size();j++){
//						WordDepend jthWordDepend=i1thWordDependList.get(j);
//						Pair jthByteSpan = jthWordDepend.byteSpan; 
//						String jthGramRole = jthWordDepend.gramRole;
//						//check if bytespanIdM contains jthByteSpan though usually, this should be true. 
//						//The check is more for debugging. Namely, in case, jthByteSpan doens't match corresponding bytespan
//						//from annotated corpus (say ACE).
//						if(bytespanIdM.containsKey(jthByteSpan)){
//							String jthMenId = bytespanIdM.get(jthByteSpan);
//							Mention jthMention = idMentionM.get(jthMenId);
//							String jthNeId = jthMenId.substring(0,jthMenId.indexOf("-"));
//							List<Pair> jthMentionSpanList = idMentionListM.get(jthNeId);
//							int indexJthByteSpan = jthMentionSpanList.indexOf(jthByteSpan);
//							if(jthMentionSpanList.contains(iithByteSpan)){
//								int indexiithByteSpan = jthMentionSpanList.indexOf(iithByteSpan);
//								int ind = indexiithByteSpan-indexJthByteSpan;
//								String jthPos = jthWordDepend.pos;
//								//if i+1th, that is, jthGramRole is subj, because it refers back mentions in the ith sentence
//								//the current mention can be regarded to be topic.
//								if(jthGramRole.contains("subj")){
//									pwTopicOutput.println("Topic_Men "+iithGramRole +" : 1");
//									pwTopicOutput.println();
//									System.out.println("Topic_Men "+iithGramRole +" : 1");
//								}else{
//									//if mention in i+1th is not subj, even if it refers back to mention in the ith sentence,
//									//we may not regard it as topic. But there are some exceptions. If there are not topics in 
//									//i+1th sentence, we can still assign topics to it. But in this case, it is a little bit complex,
//									//we cannot make decision until we reach the end of the sentence.
//									//for the moment, I don't assign topics to it for simplicity. I will return it later.
//									pwTopicOutput.println("Topic_Men "+iithGramRole +" : 0");
//									pwTopicOutput.println();
//									System.out.println("Topic_Men "+iithGramRole +" : 0");
//								}
//								//								if(iithGramRole.contains("subj")){
//								//									pwTopicOutput.println("Topic_Men "+iithGramRole +" : 1");
//								//									pwTopicOutput.println();
//								//									System.out.println("Topic_Men "+iithGramRole +" : 1");
//								//								}else{
//								//									pwTopicOutput.println("Topic_Men "+iithGramRole +" : 0");
//								//									pwTopicOutput.println();
//								//									System.out.println("Topic_Men "+iithGramRole +" : 0");
//								//								}
//							}else{
//								//this means that iithByteSpan is not the one
//								pwTopicOutput.println("Topic_Men "+iithGramRole +" : 0");
//								pwTopicOutput.println();
//								System.out.println("Topic_Men "+iithGramRole +" : 0");
//							}
//						}
//					}
//				}
//			}
//		}
//		pwTopicOutput.flush();
//		pwTopicOutput.close();
//		//		for(int i=0;i<mentionSpanList.size();i++){
//		//			Pair ithbyteSpan = mentionSpanList.get(i);
//		//			String ithId=bytespanIdM.get(ithbyteSpan);
//		//			//String ithMentionContent = idMentionM.get(ithId).getExtentCoveredText();
//		//			String ithMentionContent = idMentionM.get(ithId).getHeadCoveredText();
//		//			System.out.println(i+"th mention span in assignTopic ConvertCot2Prob: "+ithbyteSpan+" ithId: "+ithId+" "+ithMentionContent);
//		//		}		
//	}

	/**
	 * 
	 * @param inputBytesFile
	 * @param inputDependFile
	 * @throws IOException
	 * in this function, we assign topics and set them in the WordDependList. Then, we need another function to print the dependences as other
	 * model files.
	 */
	public ArrayList<ArrayList<WordDepend>> assignTopic(File inputBytesFile,File inputDependFile,File inputParsedFile,File debugOutputFile) throws IOException{
		//call adjustACEBytesSpan to adjust the bytespan of ACE annotations so that they can map to Stanford parser results.
		//that is, bytespanIdM, mentionSpanList will change. 
		CorefUsers.adjustACEBytespan(inputBytesFile.getName(),docBuf, bytespanIdM, idMentionM, idMentionListM, mentionSpanList);
		PrintWriter pwDebugOutput = new PrintWriter(debugOutputFile);
		File debugOutputFile2 = new File(debugOutputFile.getParent(),debugOutputFile.getName()+".pred");
		//System.out.println("debugOutputFile2: "+debugOutputFile2);
		File debugOutputFile3 = new File(debugOutputFile.getParent(),debugOutputFile.getName()+".true");
		PrintWriter pwDebugOutput2 = new PrintWriter(debugOutputFile2);
		PrintWriter pwDebugOutput3 = new PrintWriter(debugOutputFile3);
		
		//////////////////////////////////
		///mapping process: /////////////
		//as next step, read into ParsedACEMay11, including bytesSpan, dependTree. 
		//so, we call readBytespanFile to get byteSpanHm
		//for dependTree file, we check if the string is the same as the string in byteSpanHm. 
		//if so, then, check if the string after the string of files is the depend parsing. 
		//the method of finding this is to check the format, such as if the first character is
		//'[', after we check the first character, we can further check if the part after '[' is
		//a syntactic item such as nsubj. 
		//we may need to collect grammatical items to make such a judgment possible. 
		//after we confirm that the line corresponds to the sentence, what we need to do is to
		//iterate through bytespanIdM, get menId and from menId, we get mentions from annotation side.
		//from depend-parse side, we need to match words by bytes spans. 
		//this is possible, we know the start bytes and end bytes, in the parsing, the numbers after each
		//word like NEWS-2, refers to the order of the word in the sentence, so, it is possible for us to
		//know the bytes span of each word. 
		//in my model, we use the head word to represent the whole phrase, so, we also need to further 
		//make clear the bytespan of the head word. Only if so can we find map the head word of the phrase 
		//from the annotations to the word (its syntactic role should be subject, object or similar roles) 
		//of the article.

		ArrayList<Pair> byteSpanPairList = new ArrayList<Pair>();
		ArrayList<String> sentList = new ArrayList<String>();
		sentByteSpanHm = this.readBytespanFile(inputBytesFile, byteSpanPairList);
		sentParseHm = this.readDependFile(inputDependFile, sentList);
		ArrayList<ArrayList<WordDepend>> wordDependSentList = new ArrayList<ArrayList<WordDepend>>();
		ArrayList<WordDepend> wordDependList=this.createWordDependList(sentParseHm, sentList,byteSpanPairList,wordDependSentList,inputParsedFile);
		
		for(int i=0;i<wordDependList.size();i++){
			WordDepend ithWordDepend = wordDependList.get(i);
			ithWordDepend.toString(pwDebugOutput);
			//ithWordDepend.print(System.out);
		}
		
		//////////////////////////////////////
		///////// topic discovery
		//for the first sentence, we only need to record the syntactic roles, its bytespan and mention id of
		//each mention and the mention itself
		//we may create a new data structure for it. not so sure
		//for the remaining sentences, we need to record above, besides, we need to check if the mention
		//is the pronoun, which mention in previous sentence is its antecedent. If its antecedent is the subject
		//and if the pronoun is a subject as well, then, the mention is topic. 
		//if not, other conditions should be taken into considerations. Go ahead tomorrow. 

		int menCount = 0;
		if(debugTest){
			for(int i=0;i<wordDependList.size();i++){
				WordDepend ithWordDepend=wordDependList.get(i);
				Pair ithbyteSpan = ithWordDepend.byteSpan;
				if(mentionSpanList.contains(ithbyteSpan)){
					String gramRole = ithWordDepend.gramRole;
					int sentIndex = ithWordDepend.sentIndex;
					String ithMenId=bytespanIdM.get(ithbyteSpan);
					ithWordDepend.menId=ithMenId;
					String ithMentionContent = idMentionM.get(ithMenId).getHeadCoveredText();
					//System.out.println(menCount+"th mention in assignTopic ConvertCot2Prob: "+" span "+ithbyteSpan+" "+i+"thId: "+ithMenId+" sentIndex: "+sentIndex+" gramRole: "+gramRole+" "+ithMentionContent);
					//System.out.println(menCount+"th mention in assignTopic ConvertCot2Prob: "+" span "+ithbyteSpan+" "+i+"thId: "+ithMenId+" "+ithMentionContent);
					//pwDebugOutput.println(menCount+"th mention in assignTopic ConvertCot2Prob: "+" span "+ithbyteSpan+" "+i+"thId: "+ithMenId+" sentIndex: "+sentIndex+" gramRole: "+gramRole+" "+ithMentionContent);
					pwDebugOutput2.println(menCount+"th mention in assignTopic ConvertCot2Prob: "+" span "+ithbyteSpan+" "+ithMenId+" "+ithMentionContent);
					menCount++;
				}
//				else{
//					System.out.println("not matching in assignTopic ConvertCot2Prob: "+ithbyteSpan+" "+ithWordDepend.token);
//				}
				//System.out.println(men);
			}
		}


		//loop through wordDependSentList, finding topics in each sentence. The idea here is that in each sentence, there may be
		//one or more mentions which refer back to mentions of previous sentences. If the mention is a subject and if the previous mention
		//is a topic or a subject too, then, the current mention should be a topic as well. 
		//it seems that the above idea is not so correct now. In fact, the assignment of topics is not determined by previous sentences. Instead,
		//it is more determined by its grammatical roles. Usually, we assign subject of a sentence as the topic and if there is not subject, 
		//the direct object should be the topic and if there is not direct object, the indirect object should be. 
		//meanwhile, we assume that each sentence has a topic. This is different from centering theory which suppose that there are not topics for some sentences
		//for example, the first sentence. The practice of COT is that if the sentence has no topic, the constraint is considered to be satisfied vacuouly.
		//our goal is the find P(topic_{t}|pos_{t}); P(topic_{t}|ind_{t},topic_{ind}) and P(topic_{t}|syn_{t})
		for(int i=0;i<wordDependSentList.size()-1;i++){
			ArrayList<WordDepend> ithWordDependList = wordDependSentList.get(i);
			//if i==0, there is no antecedent. In this case, according to COT or centering theory, there is no topic.
			//but it seems that we need a topic in our models. 
			for(int j=0;j<ithWordDependList.size();j++){
				WordDepend jthWordDepend=ithWordDependList.get(j);
				Pair jthByteSpan = jthWordDepend.byteSpan; 
				if(mentionSpanList.contains(jthByteSpan)){
					String jthMenId=bytespanIdM.get(jthByteSpan);
					jthWordDepend.menId=jthMenId;
					String ithMentionContent = idMentionM.get(jthMenId).getHeadCoveredText();
					//System.out.println(menCount+"th mention in assignTopic ConvertCot2Prob: "+i+" span "+ithMenId+" "+ithbyteSpan+" sentIndex: "+sentIndex+" gramRole: "+gramRole+" "+ithMentionContent);
					//menCount++;
				}

				String jthGramRole = jthWordDepend.gramRole;
				if(jthGramRole.contains("subj")){
					jthWordDepend.topic=1;
				}else{
					jthWordDepend.topic=0;
				}
				ithWordDependList.set(j, jthWordDepend);
			}
			wordDependSentList.set(i, ithWordDependList);
		}
		for(int i=0;i<mentionSpanList.size();i++){
			Pair ithbyteSpan = mentionSpanList.get(i);
			String ithId=bytespanIdM.get(ithbyteSpan);
			//String ithMentionContent = idMentionM.get(ithId).getExtentCoveredText();
			String ithMentionContent = idMentionM.get(ithId).getHeadCoveredText();
			//System.out.println(i+"th mention span in assignTopic ConvertCot2Prob: "+ithbyteSpan+" ithId: "+ithId+" "+ithMentionContent);
			if(debug){
				System.out.println(i+"th mention in assignTopic ConvertCot2Prob: "+" span "+ithbyteSpan+" "+i+"thId: "+ithId+" "+ithMentionContent);
			}
			pwDebugOutput3.println(i+"th mention in assignTopic ConvertCot2Prob: "+" span "+ithbyteSpan+" "+ithId+" "+ithMentionContent);
		}	
		System.out.println("true mention size: "+mentionSpanList.size()+" menCount: "+menCount);
		pwDebugOutput.flush();
		pwDebugOutput.close();
		pwDebugOutput2.flush();
		pwDebugOutput2.close();
		pwDebugOutput3.flush();
		pwDebugOutput3.close();
		return wordDependSentList;
	}

	public void genCOTConstraints(ArrayList<ArrayList<WordDepend>> wordDependSentList,File outputFile) throws FileNotFoundException{
		PrintWriter pwOutput = new PrintWriter(outputFile);
		for(int i=0;i<wordDependSentList.size()-1;i++){
			ArrayList<WordDepend> ithWordDependList = wordDependSentList.get(i);
			//if i==0, there is no antecedent. In this case, according to COT or centering theory, there is no topic.
			//But in COT implementation (Donna Byron, et al, 06), they suppose that COT constraints are vacuously realized
			//in my implementation, I suppose that there is a topic. But since it doesn't have antecedent, I suppose that it violates
			//coherence and align constraints. Namely, Topic_COT topic_{i-1},
			//the difference between i==0 and i>0 lies also in when i>0, we need to consider both the ith sentence and i-th sentence
			if(i==0){
				for(int j=0;j<ithWordDependList.size();j++){
					WordDepend jthWordDepend=ithWordDependList.get(j);
					String jthMenId = jthWordDepend.menId;
					if(jthMenId!=null){
						
						String jthNeId = jthMenId.substring(0,jthMenId.indexOf("-"));
						List<Pair> jthMentionSpanList = idMentionListM.get(jthNeId);

						Pair jthByteSpan = jthWordDepend.byteSpan; 
						String jthGramRole = jthWordDepend.gramRole;
						String jthPos = jthWordDepend.pos;
						int jthTopic = jthWordDepend.topic;
						int jthInd = jthWordDepend.index;
						//since it is the first sentence, we cannot loop through its previous sentence
						pwOutput.println("Topic_Pro "+jthPos+" : "+jthTopic);
						pwOutput.println("Topic_Align "+jthGramRole+" : "+jthTopic);
						//although it is the first sentence, there may be some mentions referring mentions before them within the same sentence
						//we still need to get ind value so that we can fill Topic_Ment Ind Topic_{ind} : Topic.
						//in order to get jthMentionSpanList, we need find jthNeId. 
						//it means that the mention has coreferring mentions and if j>0, there should be one which is its antecedent. 
						if(jthMentionSpanList.contains(jthByteSpan) && j>0){
							//we need to loop through all values between j and 0 within the sentence
							for(int k=j-1;k>0;k--){
								WordDepend prevWordDepend = ithWordDependList.get(k);
								Pair prevByteSpan = prevWordDepend.byteSpan;
								if(jthMentionSpanList.contains(prevByteSpan)){
									int preTopic = prevWordDepend.topic;
									String synInd = prevWordDepend.gramRole;
									int indexJthByteSpan = jthMentionSpanList.indexOf(jthByteSpan);
									int indexiithByteSpan = jthMentionSpanList.indexOf(prevByteSpan);
									int ind = Math.abs(indexiithByteSpan-indexJthByteSpan);
									pwOutput.println("Topic_Cohere "+ind+" "+preTopic+" : "+jthTopic);
									pwOutput.println("Binding "+ind+" "+synInd+" : "+jthPos);
									//if i+1th, that is, jthGramRole is subj, because it refers back mentions in the ith sentence
									//the current mention can be regarded to be topic.
								}
							}
						}
					}
				}
			}else{
				//for i>0, we not only take mentions within i, we also need to consider i-1;
				ArrayList<WordDepend> prevWordDependList = wordDependSentList.get(i-1);
				for(int j=0;j<ithWordDependList.size();j++){
					WordDepend jthWordDepend=ithWordDependList.get(j);
					String jthMenId = jthWordDepend.menId;
					
					if(jthMenId!=null){
						Pair jthByteSpan = jthWordDepend.byteSpan; 
						String jthGramRole = jthWordDepend.gramRole;
						String jthPos = jthWordDepend.pos;
						int jthTopic = jthWordDepend.topic;
						int jthInd = jthWordDepend.index;
						//since it is the first sentence, we cannot loop through its previous sentence
						pwOutput.println("Topic_Pro "+jthPos+" : "+jthTopic);
						pwOutput.println("Topic_Align "+jthGramRole+" : "+jthTopic);

						//although it is the first sentence, there may be some mentions referring mentions before them within the same sentence
						//we still need to get ind value so that we can fill Topic_Ment Ind Topic_{ind} : Topic.
						//in order to get jthMentionSpanList, we need find jthNeId. 


						String jthNeId = jthMenId.substring(0,jthMenId.indexOf("-"));
						List<Pair> jthMentionSpanList = idMentionListM.get(jthNeId);

						//it means that the mention has coreferring mentions and if j>0, there should be one which is its antecedent. 
						if(jthMentionSpanList.contains(jthByteSpan) && j>0){
							//we need to loop through all values between j and 0 within the sentence
							int indexJthByteSpan = jthMentionSpanList.indexOf(jthByteSpan);
							for(int k=j-1;k>0;k--){
								WordDepend prevWordDepend = ithWordDependList.get(k);
								Pair prevByteSpan = prevWordDepend.byteSpan;
								if(jthMentionSpanList.contains(prevByteSpan)){
									int preTopic = prevWordDepend.topic;
									String synInd = prevWordDepend.gramRole;
									
									int indexiithByteSpan = jthMentionSpanList.indexOf(prevByteSpan);
									int ind = Math.abs(indexiithByteSpan-indexJthByteSpan);
									pwOutput.println("Topic_Align "+ind+" "+preTopic+" : "+jthTopic);
									pwOutput.println("Binding "+ind+" "+synInd+" : "+jthPos);
									//if i+1th, that is, jthGramRole is subj, because it refers back mentions in the ith sentence
									//the current mention can be regarded to be topic.
								}
							}
							
							for(int k=prevWordDependList.size()-1;k>0;k--){
								WordDepend prevSentWordDepend = prevWordDependList.get(k);
								Pair prevByteSpan = prevSentWordDepend.byteSpan;
								if(jthMentionSpanList.contains(prevByteSpan)){
									int preTopic = prevSentWordDepend.topic;
									String synInd = prevSentWordDepend.gramRole;
									int indexiithByteSpan = jthMentionSpanList.indexOf(prevByteSpan);
									int ind = Math.abs(indexiithByteSpan-indexJthByteSpan);
									//we need to check how long ind is. If ind is larger than MAXLEN, it is meaningless to generate 
									//binding or the first dependence.
									if(ind<MAXLEN){
										pwOutput.println("Topic_Cohere "+ind+" "+preTopic+" : "+jthTopic);
										pwOutput.println("Binding "+ind+" "+synInd+" : "+jthPos);
									}
									//if i+1th, that is, jthGramRole is subj, because it refers back mentions in the ith sentence
									//the current mention can be regarded to be topic.
								}
							}
						}
					}
				}
			}
		}
		pwOutput.flush();
		pwOutput.close();
	}

	/**
	 * @param args
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub

		ConvertCot2Prob conCot2Prob = new ConvertCot2Prob();
		File inputData = new File(args[0]);
		if(inputData.isFile()){
			conCot2Prob.process(args[0]);
			//File inputBytesFile = new File(args[2]);
			//File inputDependFile = new File(args[3]);
			File inputBytesFile = new File(byteSpanDir,inputData.getName());
			File inputDependFile = new File(dependDir,inputData.getName());
			File inputParsedFile = new File(parsedDir,inputData.getName());
			File outputFile = new File(args[1]);
			File debugDir = new File(outputDir);
			System.out.println(debugDir);
			if(!debugDir.exists()){
				debugDir.mkdirs();
			}
			if(outputFile.exists()){
				outputFile.mkdirs();
			}
			
			File debugFile = new File(debugDir,inputBytesFile.getName());
			ArrayList<ArrayList<WordDepend>> wordDependSentList=conCot2Prob.assignTopic(inputBytesFile, inputDependFile,inputParsedFile,debugFile);
			conCot2Prob.genCOTConstraints(wordDependSentList, outputFile);
		}else{
			conCot2Prob.process(args[0]);
		}
	}
}
