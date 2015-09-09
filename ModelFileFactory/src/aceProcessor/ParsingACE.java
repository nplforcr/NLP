package aceProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

//import opennlp.tools.lang.english.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;

import org.xml.sax.SAXException;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import readers.SgmReader;
import utils.DpSentence;
import utils.FileProcessor;

public class ParsingACE {
	public StringBuffer docBuf;
	public boolean depend = false;
	public boolean ACE02 = true;
	public boolean EMPRONOUN = false;
	public boolean PARSED = false;
	public boolean BYTES = true;

	HashMap<DpSentence, Integer> sentM;
	List<DpSentence> sentList;
	SentenceDetectorME sdetector;
	//sdetector = new SentenceDetector("/Users/lixxx345/Documents/modelblocks/DataProcessor/models/english/sentdetect/EnglishSD.bin.gz");
	LexicalizedParser lp = null;
	//Oh, I see, the maxLength is here.

	public ParsingACE(LexicalizedParser lparser) throws IOException, ParserConfigurationException, SAXException{
		//lp = new LexicalizedParser("models/englishPCFG.ser.gz");
		this.lp = lparser;
		lp.setOptionFlags(new String[]{"-maxLength", "120", "-retainTmpSubcategories"});
	}

	public ParsingACE(String inputDir, String outDir) throws IOException, ParserConfigurationException, SAXException{
		lp = new LexicalizedParser("models/englishPCFG.ser.gz");//LexicalizedParser.loadModel("models/englishPCFG.ser.gz");
				//new LexicalizedParser("models/englishPCFG.ser.gz");
		lp.setOptionFlags(new String[]{"-maxLength", "120", "-retainTmpSubcategories"});
		this.process(inputDir, outDir);
	}

	public void parsingPlainText(String ticker,ArrayList<String> i2b2SentList,PrintWriter pwOutput){
		for(int i=0;i<i2b2SentList.size();i++){
			String ithSent = i2b2SentList.get(i);
			System.out.println(i+"th sent: "+ithSent);

			Tree parse =lp.apply(ithSent);
			parse.pennPrint();

			TreebankLanguagePack tlp = new PennTreebankLanguagePack();
			GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
			//Collection tdl = gs.typedDependenciesCollapsed();
			Collection tdl = gs.allTypedDependencies();
			System.out.println(tdl);
			System.out.println();
			TreePrint tp = new TreePrint("penn");
			//TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
			tp.printTree(parse);
			if(tdl!=null){
				pwOutput.println(tdl);
				pwOutput.println();
			}
		}
	}

	public void process (String dirName,String outDir) throws IOException, ParserConfigurationException, SAXException{

		//String genre = dirName.substring(dirName.lastIndexOf("/"));
		String[] dirArray = dirName.split("/");
		String genre = "";
		if(ACE02){
			genre = dirArray[dirArray.length-1];
		}else{
			genre = dirArray[dirArray.length-2];
		}

		File dFile =  new File (dirName);
		File outDependFile = new File(outDir,"dependTree/"+genre);
		if(!outDependFile.exists()){
			outDependFile.mkdirs();
		}
		File outParseFile = new File(outDir, "parseTree/"+genre);
		if(!outParseFile.exists()){
			outParseFile.mkdirs();
		}

		File outBytesFile = new File(outDir, "bytesSpan/"+genre);
		if(!outBytesFile.exists()){
			outBytesFile.mkdirs();
		}

		if( dFile.isDirectory()){
			File files[]  =  dFile.listFiles();
			for(int i=0; i< files.length; i++){
				if(files[i].isFile()){
					String filename1 = files[i].getName();
					if(filename1.endsWith(".sgm")){
						System.out.println("sgm file name: "+filename1);
						if(PARSED){
							PrintWriter pwByteSpan = new PrintWriter(new FileWriter(new File(outBytesFile, files[i].getName())));
							printByteSpan(files[i].getAbsolutePath(), pwByteSpan);
						}else if(BYTES){
							PrintWriter pwByteSpan = new PrintWriter(new FileWriter(new File(outBytesFile, files[i].getName())));
							this.printByteSpan(files[i].getAbsolutePath(), pwByteSpan);
						}
						else{
							PrintWriter pwDependTree = new PrintWriter(new FileWriter(new File(outDependFile, files[i].getName())));
							PrintWriter pwParseTree = new PrintWriter(new FileWriter(new File(outParseFile, files[i].getName())));
							PrintWriter pwByteSpan = new PrintWriter(new FileWriter(new File(outBytesFile, files[i].getName())));
							parsingAce(files[i].getAbsolutePath(), pwDependTree,pwParseTree,pwByteSpan);
						}

					}
				} else if(files[i].isDirectory()){
					//I found why it takes so long a time to run the parsing. In fact, under the input dir, say 
					///Users/lixxx345/Documents/corpora/ace05/data/English/cts, genre should be cts
					//but since under cts, there is an adj and other file dir. Then, it goes down to adj, then genre becomes adj. 
					//so, I'd better avoid the recursion here. I should directly use cts/adj, rather than cts. 
					process(files[i].getAbsolutePath(),outDir);
				}
			}
		}else{
			String filename = dFile.getName();
			if(filename.endsWith(".sgm")){
				if(depend){
					System.out.println("sgm file name: "+filename);
					//if we have parsed files,we don't need to repeat it. For example, now we only
					//want to get the bytes span of sentences and mentions for evaluations
					if(PARSED){
						PrintWriter pwByteSpan = new PrintWriter(new FileWriter(new File(outBytesFile, dFile.getName())));
						printByteSpan(dFile.getAbsolutePath(), pwByteSpan);
					}else{
						PrintWriter pwDependTree = new PrintWriter(new FileWriter(new File(outDependFile, dFile.getName())));
						PrintWriter pwParseTree = new PrintWriter(new FileWriter(new File(outParseFile, dFile.getName())));
						PrintWriter pwByteSpan = new PrintWriter(new FileWriter(new File(outBytesFile, dFile.getName())));
						parsingAce(dFile.getAbsolutePath(), pwDependTree,pwParseTree,pwByteSpan);
					}
				}else if(BYTES){
					PrintWriter pwByteSpan = new PrintWriter(new FileWriter(new File(outBytesFile, dFile.getName())));
					this.printByteSpan(dFile.getAbsolutePath(), pwByteSpan);
				}else{
					System.out.println("sgm file name: "+filename);
					if(PARSED){
						PrintWriter pwByteSpan = new PrintWriter(new FileWriter(new File(outBytesFile, dFile.getName())));
						printByteSpan(dFile.getAbsolutePath(), pwByteSpan);
					}else{
						PrintWriter pwDependTree = new PrintWriter(new FileWriter(new File(outDependFile, dFile.getName())));
						PrintWriter pwParseTree = new PrintWriter(new FileWriter(new File(outParseFile, dFile.getName())));
						PrintWriter pwByteSpan = new PrintWriter(new FileWriter(new File(outBytesFile, dFile.getName())));
						parsingAce(dFile.getAbsolutePath(), pwDependTree,pwParseTree,pwByteSpan);
					}
				}
			}
		}
	}



	/**
	 * this methods aims at finding bytes span of each sentence and probably each mention for final evaluations
	 * since only bytes spans plus their mentions are comparable between keys and responses.
	 * @param sgmFname
	 * @param pwByteSpan
	 */
	public void printByteSpan(String sgmFname,PrintWriter pwByteSpan){
		docBuf =  SgmReader.readDoc2(sgmFname);
		//the following words are from NEPairDataFactory.java. 
		//now, we unify both code so that we can assign chunk features extracted by ExtracSynInfor.java to words in 
		//NEPairDataFactory.java. 
		//here, we replace SentenceDector of OpenNLP with MasentTagger of StanfordParser for unification with ParsingACE.java
		//because ParsingACE.java will parse sentences into parsing tree and then we will get chunk features from them. 
		//So, the sentences should be identical in both methods. 

		sentM =  new HashMap<DpSentence, Integer>();
		sentList = new ArrayList<DpSentence>();
		String text = docBuf.toString(); 
		int fromInd = 0;		
		List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(docBuf.toString()));
		StringBuffer docBufNew = new StringBuffer();
		//I finally found solution to the inconsistencies brought by MaxentTagger.tokenizeText between docBuf and sentences
		//the reason is that tokenizeText split punctuation marks and some other symbols or change some symbols, such as from 
		//( to -LRB-. Consequently, the bytes in sentences are much more than docBuf. Now, a simple solution is just to create 
		//a new StringBuffer as docBufNew so that docBufNew fully follow sentences. Then, the bytes span will match.
		//the same solution is made in NEPairDataFactory.java
		for(Sentence<? extends HasWord> stanfordSentence : sentences){
			docBufNew.append(stanfordSentence.toString());
		}

		for (Sentence<? extends HasWord> stanfordSentence : sentences) {

			////////////////////////////////////////////////////////
			////////     obtain bytespan of sentences    ///////////
			////////////////////////////////////////////////////////
			String sentence = stanfordSentence.toString();
			//			if(sentence.contains("-LRB-")){
			//				sentence=sentence.replace("-LRB-", "(");
			//			}
			//			
			//			if(sentence.contains("-RRB-")){
			//				sentence=sentence.replace("-RRB-", ")");
			//			}

			//System.out.println(sentence);
			int[] sentByteSpan = new int[2];
			//String sentStr   =  StringMethods.sentenceString(sentence).replaceAll("\\[ [0-9][0-9]* \\]", ""); // replace all citations [ 45 ] seen in wikipedia --  this messes up the openNLP parser.  
			sentByteSpan=FileProcessor.findByteSpan(docBufNew, sentence, fromInd);
			DpSentence sent = new DpSentence();
			sent.setSent(sentence);
			sent.setStart(sentByteSpan[0]);
			sent.setEnd(sentByteSpan[1]);
			if(!sentList.contains(sent)){
				sentM.put(sent, 1);
				sentList.add(sent);
			}
			fromInd = sentByteSpan[1];
			pwByteSpan.println(sentByteSpan[0]+" "+sentByteSpan[1]+" "+stanfordSentence);
		}
		pwByteSpan.flush();
		pwByteSpan.close();
	}

	/**
	 * 
	 * @param sgmFname
	 * @param pwDependTree
	 * @param pwParseTree
	 * @throws IOException 
	 */
	public void parsingAce(String sgmFname, PrintWriter pwDependTree,PrintWriter pwParseTree,PrintWriter pwByteSpan) throws IOException{
		//in order to use chunklink_2-2-2---_for_conll.pl
		docBuf =  SgmReader.readDoc2(sgmFname);


		System.out.println(docBuf.toString());

		//the following words are from NEPairDataFactory.java. 
		//now, we unify both code so that we can assign chunk features extracted by ExtracSynInfor.java to words in 
		//NEPairDataFactory.java. 
		//here, we replace SentenceDector of OpenNLP with MasentTagger of StanfordParser for unification with ParsingACE.java
		//because ParsingACE.java will parse sentences into parsing tree and then we will get chunk features from them. 
		//So, the sentences should be identical in both methods. 

		sentM =  new HashMap<DpSentence, Integer>();
		sentList = new ArrayList<DpSentence>();
		String text = docBuf.toString(); 
		// break a paragraph into sentences and we get array of sentences and also their types spans
		// they compose Sentence objects and are stored into both sentList and sentM.  
		int fromInd = 0;		
		List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(docBuf.toString()));
		StringBuffer docBufNew = new StringBuffer();

		//I finally found solution to the inconsistencies brought by MaxentTagger.tokenizeText between docBuf and sentences
		//the reason is that tokenizeText split punctuation marks and some other symbols or change some symbols, such as from 
		//( to -LRB-. Consequently, the bytes in sentences are much more than docBuf. Now, a simple solution is just to create 
		//a new StringBuffer as docBufNew so that docBufNew fully follow sentences. Then, the bytes span will match.
		//the same solution is made in NEPairDataFactory.java
		for(Sentence<? extends HasWord> stanfordSentence : sentences){
			docBufNew.append(stanfordSentence.toString());
		}

		for (Sentence<? extends HasWord> stanfordSentence : sentences) {

			////////////////////////////////////////////////////////
			////////     obtain bytespan of sentences    ///////////
			////////////////////////////////////////////////////////
			String sentence = stanfordSentence.toString();
			//			if(sentence.contains("-LRB-")){
			//				sentence=sentence.replace("-LRB-", "(");
			//			}
			//			
			//			if(sentence.contains("-RRB-")){
			//				sentence=sentence.replace("-RRB-", ")");
			//			}

			//System.out.println(sentence);
			int[] sentByteSpan = new int[2];
			//String sentStr   =  StringMethods.sentenceString(sentence).replaceAll("\\[ [0-9][0-9]* \\]", ""); // replace all citations [ 45 ] seen in wikipedia --  this messes up the openNLP parser.  
			sentByteSpan=FileProcessor.findByteSpan(docBufNew, sentence, fromInd);
			DpSentence sent = new DpSentence();
			sent.setSent(sentence);
			sent.setStart(sentByteSpan[0]);
			sent.setEnd(sentByteSpan[1]);
			if(!sentList.contains(sent)){
				sentM.put(sent, 1);
				sentList.add(sent);
			}
			fromInd = sentByteSpan[1];

			///////////////////////////////////////////
			////////     parse sentences    ///////////
			///////////////////////////////////////////
			//System.out.println(sentence);
			if(EMPRONOUN){
				pwByteSpan.println(sentByteSpan[0]+" "+sentByteSpan[1]+" "+stanfordSentence);
			}else{
				pwParseTree.println(sentByteSpan[0]+" "+sentByteSpan[1]+" "+stanfordSentence);
				pwByteSpan.println(sentByteSpan[0]+" "+sentByteSpan[1]+" "+stanfordSentence);
			}
			stanfordSentence.toArray();


			Tree parse = null;
			if(PARSED){
				parse = (Tree) lp.apply(stanfordSentence);
				//System.out.println(stanfordSentence);
				//parse.pennPrint();
				//System.out.println();
				//pwOutput.println();

				TreebankLanguagePack tlp = new PennTreebankLanguagePack();
				GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
				GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
				Collection tdl = gs.typedDependenciesCollapsed();
				//System.out.println(tdl);
				//System.out.println();

				if(pwDependTree!=null){
					pwDependTree.println(stanfordSentence);
					pwDependTree.println(tdl);
					pwDependTree.println();
				}
				//TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
				//in the following method, in order for chunklink_2-2-2000_for_conll.pl to understand, we add
				//( before (ROOT in pennPrint of Tree.java and meanwhile, add ) to the end of the parsing tree
				//which is in the method of pennPrint of Tree.java. 
				TreePrint tp = new TreePrint("penn");
				tp.printTree(parse,pwParseTree);
			}


		}

		if(pwDependTree!=null){
			pwDependTree.flush();
			pwDependTree.close();
		}
		pwParseTree.flush();
		pwParseTree.close();
		pwByteSpan.flush();
		pwByteSpan.close();
	}

	/**
	 * @param args
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		ParsingACE exSynInfo = new ParsingACE(args[0],args[1]);

	}

}
