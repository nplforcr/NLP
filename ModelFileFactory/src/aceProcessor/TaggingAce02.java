package aceProcessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;

public class TaggingAce02 {
	//the following line is the arguments needed by the stanford tagger
	//E:\NLPWorkspace\stanford-postagger-full-2008-09-28\models\bidirectional-wsj-0-18.tagger sample-input.txt
	static String DOC = "DOC";
	static String DOCNO = "DOCNO";
	static String DOCTYPE = "DOCTYPE";
	static String HEADER = "HEADLINE";
	static String TEXT = "TEXT";
	static String CODER = "CODER";
	static String DD = "DD";
	static String AN = "AN";
	static String HL = "HL";
	static String SO ="SO";
	static String IN = "IN";
	static String GV = "GV";
	static String DATELINE = "DATELINE";
	static String TXT = "TXT";
	static String TURN = "TURN";
	static String PARA = "p";
	static String SENT = "s";
	static String MUC = "muc";
	static String SGM = "sgm";
	static String PLAIN = "plain";
	static String TIME = "<time";
	MaxentTagger tagger = null;

	public TaggingAce02(MaxentTagger comeTagger) throws Exception{
		//though tagger here is not directly read. But without it, it will return error. 
		//So, it seems that this way make MaxentTagger wrap the model now.
		//we can also use the object tagger to do tagging.
		//MaxentTagger tagger = new MaxentTagger(args[0]);
		this.tagger = comeTagger;
		//MaxentTagger tagger = new MaxentTagger("models/bidirectional-wsj-0-18.tagger");
	}
	
	public void tagInputText(String ticker, String inputXMLString,PrintWriter pwOutput) {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(new InputSource(new StringReader(inputXMLString)));
				Node child = document.getFirstChild();
				NodeList headLine = null;
				NodeList sentenceNodeList=null;
				if(ticker.equals(MUC)){
					headLine = document.getElementsByTagName(HL);
					for (int i = 0; i < headLine.getLength(); i++) {
						Node sentenceNode = headLine.item(i);
						String sentText=sentenceNode.getTextContent();
						sentText.replace("&", "and");
						System.out.println(sentText);
						List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(sentText));
						for (Sentence<? extends HasWord> sentence : sentences) {
							Sentence<TaggedWord> tSentence = MaxentTagger.tagSentence(sentence);
							//System.out.println(tSentence.toString(false));
							pwOutput.println(tSentence.toString(false));
						}
					}				
					sentenceNodeList = document.getElementsByTagName(SENT);
				}else if (ticker.equals(SGM)){
					headLine = document.getElementsByTagName(HEADER);
					for (int i = 0; i < headLine.getLength(); i++) {
						Node sentenceNode = headLine.item(i);
						String sentText=sentenceNode.getTextContent();
						sentText.replace("&", "and");
						System.out.println(sentText);
						List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(sentText));
						for (Sentence<? extends HasWord> sentence : sentences) {
							Sentence<TaggedWord> tSentence = MaxentTagger.tagSentence(sentence);
							//System.out.println(tSentence.toString(false));
							pwOutput.println(tSentence.toString(false));
						}
					}				
					sentenceNodeList = document.getElementsByTagName(TEXT);
				}
				//probably, the following xml contents other than the headling 
				//read into doesn't have any problematic items, so, we can directly tag them.
				int count=0;
				for (int i = 0; i < sentenceNodeList.getLength(); i++) {
					Node sentenceNode = sentenceNodeList.item(i);
					String sentText=sentenceNode.getTextContent();
					List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(sentText));
					for (Sentence<? extends HasWord> sentence : sentences) {
						Sentence<TaggedWord> tSentence = MaxentTagger.tagSentence(sentence);
						//System.out.println(tSentence.toString(false));
						pwOutput.println(tSentence.toString(false));
					}
				}					

				pwOutput.close();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void tagPlainText(String ticker,ArrayList<String> i2b2SentList,PrintWriter pwOutput) throws Exception{
		if(ticker.equals(PLAIN)){
			for(int i=0;i<i2b2SentList.size();i++){
				String ithSent = i2b2SentList.get(i);
				String tSentence=MaxentTagger.tagString(ithSent);
				pwOutput.println(tSentence);
			}
		}
		pwOutput.close();
	}

	public static void main(String[] args) throws Exception {
		MaxentTagger tagger = new MaxentTagger("models/bidirectional-wsj-0-18.tagger");
		TaggingAce02 taggerDemo=new TaggingAce02(tagger);
		if (args.length != 3) {
			System.err.println("usage: java TaggingAce02 modelFile outputFile fileToTag");
			return;
		}
		//though tagger here is not directly read. But without it, it will return error. 
		//So, it seems that this way make MaxentTagger wrap the model now.
		//MaxentTagger tagger = new MaxentTagger(args[0]);
		File inputFile = new File(args[1]);
		File outputFile = new File(args[2]);


		@SuppressWarnings("unchecked")

		//File fileTag1 = new File("/home/dingcheng/Documents/OSU_corpora/ace_phase2/data/ace2_train/bnews");
		//File fileTag2 = new File("/home/dingcheng/Documents/OSU_corpora/ace_phase2/data/ace2_train/npaper");
		//File fileTag3 = new File("/home/dingcheng/Documents/OSU_corpora/ace_phase2/data/ace2_train/nwire");
		//File fileTag3 = new File("/home/dingcheng/Documents/corpora/ace05/data/English/nw/adj");
		//File fileTag3 = new File("/home/dingcheng/Documents/corpora/ace05/data/English/nw/adj");
		//File fileTag3 = new File("/home/dingcheng/Documents/corpora/ace05/data/English/nw/adj");
		File[] fileTagArray = {inputFile};
		String ticker = "";
		//File outputFile = new File(args[2]);
		//PrintWriter pwOutput = new PrintWriter(new FileWriter(outputFile));

		for(int j=0;j<fileTagArray.length;j++){
			File fileTag = fileTagArray[j];
			System.out.println(fileTag+" "+fileTag.isDirectory());
			if(fileTag.isDirectory()){
				File dirOutput = new File(outputFile, fileTag.getName()+"-tagged");
				//System.out.println(fileTag);
				if(!dirOutput.exists()){
					dirOutput.mkdirs();
				}
				PrintWriter pwOutput = null;
				File[] fileArray = fileTag.listFiles();
				for(int i=0;i<fileArray.length;i++){
					int fileLeng = fileArray[i].getName().length();
					System.out.println("fileArray[i]: "+i+" "+fileArray[i].getName());
					if(fileArray[i].getName().matches("README-WSJ")){
						continue;
					}else if(fileArray[i].getName().contains(".xml")){
						continue;
					}
					else if(fileArray[i].getName().contains(".sgm")){
						System.out.println(fileArray[i].getName());
						ticker=SGM;
						pwOutput = new PrintWriter(new FileWriter(new File(dirOutput, fileArray[i].getName()+".tagged")));
						BufferedReader bfMuc = new BufferedReader(new FileReader(fileArray[i]));
						StringBuffer sbMuc = new StringBuffer();
						String line = "";
						while((line=bfMuc.readLine())!=null){
							line=line.replace("&", "and");
							if(line.startsWith(TIME)){
								line=line.replace(">", "/>");
							}

							sbMuc.append(line+" ");
						}
						//System.out.println(sbMuc);
						taggerDemo.tagInputText(ticker,sbMuc.toString(), pwOutput);
					}else if(fileArray[i].getName().endsWith(".txt")){
						System.out.println(fileArray[i].getName());
						ticker=PLAIN;
						pwOutput = new PrintWriter(new FileWriter(new File(dirOutput, fileArray[i].getName()+".tagged")));
						BufferedReader bfMuc = new BufferedReader(new FileReader(fileTag));
						ArrayList<String> i2b2SentList = new ArrayList<String>();
						StringReader srMuc = null;
						String line = "";
						while((line=bfMuc.readLine())!=null){
							//&AMP leads to trouble. since it becomes andamp. But in reading the sgm file, &amp is splitted into two words 
							//as & and amp. As a result, amp appears in the word model but not in the posWord model. 
							i2b2SentList.add(line);

						}
						taggerDemo.tagPlainText(ticker,i2b2SentList, pwOutput);
					}
				}
			}else{
				File dirOutput = new File(outputFile, fileTag.getParentFile().getName()+"-tagged");
				if(!dirOutput.exists()){
					dirOutput.mkdirs();
				}
				System.out.println(dirOutput.getName());
				PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(dirOutput, fileTag.getName()+".tagged")));

				BufferedReader bfMuc = new BufferedReader(new FileReader(fileTag));
				System.out.println(fileTag.getName());
				StringBuffer sbMuc = new StringBuffer();
				ArrayList<String> i2b2SentList = new ArrayList<String>();
				StringReader srMuc = null;
				String line = "";
				while((line=bfMuc.readLine())!=null){
					//&AMP leads to trouble. since it becomes andamp. But in reading the sgm file, &amp is splitted into two words 
					//as & and amp. As a result, amp appears in the word model but not in the posWord model. 
					if(ticker==SGM || ticker==MUC){
						if(line.contains("&")){
							line=line.replace("&", "and");
							sbMuc.append(line);
						}else{
							sbMuc.append(line);
						}
					}else if(ticker==PLAIN){
						i2b2SentList.add(line);
					}

				}
				//System.out.println(sbMuc);
				if(fileTag.getName().contains(SGM)){
					ticker=SGM;
					taggerDemo.tagInputText(ticker,sbMuc.toString(), pwOutput);
				}else if(fileTag.getName().contains(MUC)){
					ticker=MUC;
					taggerDemo.tagInputText(ticker,sbMuc.toString(), pwOutput);
				}else{
					ticker=PLAIN;
					taggerDemo.tagPlainText(ticker,i2b2SentList, pwOutput);
				}

				//System.out.println(sbMuc);
				//taggerDemo.tagMucText(fileTag, pwOutput);
				//List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader(args[1])));
				//				List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader(fileTag)));
				//				for (Sentence<? extends HasWord> sentence : sentences) {
				//					Sentence<TaggedWord> tSentence = MaxentTagger.tagSentence(sentence);
				//					pwOutput.println(tSentence.toString(false));
				//					//System.out.println(tSentence.toString(false));
				//				}
				pwOutput.close();
			}
		}

	}
}
