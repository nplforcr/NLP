package aceProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import readers.AceReader;
import readers.SgmReader;
import annotation.Mention;
import utils.Pair;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PrepareACEDevInput extends AceReader{

	// static String startXML =
	// "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
	static String startXML = "<?xml version=\"1.0\"?>";
	static String SOURCE = "source_file";
	int tokenIndex = 0;
	int markableIndex = 0;
	List<String> pronounList;
	List<String> reflexiveList;
	List<String> maPronounsList;
	List<String> fePronounsList;
	List<String> nePronounsList;
	static String MTURN = "<TURN>";
	static String MTIME = "<time";
	static String TEXT = "TEXT";
	int countTags = 0;
	// int countToken = 0;
	boolean isMarkable = false;
	boolean isCoref = false;
	boolean isEmbedded = false;
	List<Pair> mentionSpanList;
	HashMap<Pair,String> bytespanIdM;
	// to store the word token and its id;
	HashMap<String, String> tokenIdHM;
	HashMap<String, String> idTokenHM;
	List<String> tokenList;
	List<String> markIdList;
	HashMap<Integer, List<String>> mentionHM;
	List<List> mentionPairList;
	HashMap<Integer, String> start_TokenHM;
	HashMap<Integer,String> start_TypeHM;
	List<Pair> mentionIdList;

	File inputsgmFile;
	File inputxmlFile;

	public PrepareACEDevInput(File sgmFile, File xmlFile) {
		tokenList = new ArrayList<String>();
		inputsgmFile = sgmFile;
		inputxmlFile = xmlFile;
		// tokenList = new ArrayList<String>();
		tokenIdHM = new HashMap<String, String>();
		markIdList = new ArrayList<String>();
		idTokenHM = new HashMap<String, String>();
		mentionHM = new HashMap<Integer, List<String>>();
		mentionPairList = new ArrayList<List>();
		String[] reflexiveArray = { "himself", "herself", "itself",
		"themselves" };
		String[] pronounArray = { "he", "his", "him", "she", "her", "it",
				"its", "hers", "they", "their", "them", };
		String[] maPronounsArray = { "he", "his", "him", "himself" };
		String[] fePronounsArray = { "she", "her", "hers", "herself" };
		String[] nePronounsArray = { "it", "its", "itself" };
		String[] singDemonArray = { "this", "that" };
		String[] pluralArray = { "they", "their", "them", "these", "those",
		"themselves" };
		reflexiveList = Arrays.asList(reflexiveArray);
		pronounList = Arrays.asList(pronounArray);
		maPronounsList = Arrays.asList(maPronounsArray);
		fePronounsList = Arrays.asList(fePronounsArray);
		nePronounsList = Arrays.asList(nePronounsArray);
		mentionIdList = new ArrayList<Pair>();
		start_TokenHM = new HashMap<Integer, String>();
		start_TypeHM=new HashMap<Integer, String>();
	}

	/**
	 * 
	 */
	public void fillMentionList(){
		bytespanIdM = new HashMap<Pair,String>();
		mentionSpanList = new ArrayList<Pair>();
		//idNeM;
		//idMentionM;
		Iterator<String> menIter = idMentionM.keySet().iterator();
		while(menIter.hasNext()){
			String menId = menIter.next();
			Mention mention = idMentionM.get(menId);
			//int[] bytespan = new int[2];
			//bytespan[0]=mention.getExtentSt();
			//bytespan[1]=mention.getExtentEd();
			Pair bytespan = new Pair(mention.getExtentSt(),mention.getExtentEd());
			mentionSpanList.add(bytespan);
			bytespanIdM.put(bytespan, menId);
			//System.out.println(mention.getType() + " "+menId+" "+mention.getHeadCoveredText()+" "+bytespan.toString());
		}
		//System.out.println("mentionSpanList size above: "+mentionSpanList.size());
	}

	public StringBuffer tokenizeText(String inputXMLString) {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		// List<String> tokenList2=new ArrayList<String>();
		StringBuffer textBuffer = new StringBuffer();
		StringBuffer sbText = new StringBuffer();
		try {
			builder = factory.newDocumentBuilder();
			try {
				// the following will keep mention index
				document = builder.parse(new InputSource(new StringReader(
						inputXMLString)));
				Node child = document.getFirstChild();
				textBuffer.append(child.getTextContent());
				// the following will keep a tokenList for creating the input
				// text
				NodeList textNodeList = document.getElementsByTagName(TEXT);
				for (int i = 0; i < textNodeList.getLength(); i++) {
					Node ithNode = textNodeList.item(i);
					sbText.append(ithNode.getTextContent() + " ");
					// System.out.println(text);
				}
				List<Sentence<? extends HasWord>> sentences = MaxentTagger
				.tokenizeText(new StringReader(sbText.toString()));
				for (Sentence<? extends HasWord> sentence : sentences) {
					// System.out.println("sentence = "+sentence);
					for (int i = 0; i < sentence.size(); i++) {
						String word = sentence.get(i).word();
						// System.out.println(word);
						tokenList.add(word);
					}
				}
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
		return textBuffer;
	}

	public void printTextFile(PrintWriter pwText, List<String> tokList) {
		for (int i = 0; i < tokList.size(); i++) {
			pwText.print(tokList.get(i).toLowerCase() + " ");
		}
	}

	public void printInputFile(PrintWriter pwMention, StringBuffer textBuffer,
			List<String> tokList) {
		HashMap<Integer, String> fullTokIndHM = new HashMap<Integer, String>();
		//iniStart is the start bytespan from sgmfiles
		//start_TokenHM is the start bytespan from xml file
		int iniStart = textBuffer.indexOf(tokList.get(0));
		if (start_TokenHM.containsKey(iniStart)) {
			pwMention.println("Entity " + 0 + " : "
					+ tokList.get(0).toLowerCase() + " = 1.0");
		}
		fullTokIndHM.put(iniStart, tokList.get(0));
		System.out.println("iniStart: " + iniStart);
		// for(int j=0;j<startIndexList.size();j++){
		// int jthStart=startIndexList.get(j);
		// String mentionToken=start_TokenHM.get(jthStart);
		// System.out.println(jthStart+" "+mentionToken);
		// }
		int tempStart = iniStart;
		for (int i = 1; i < tokList.size(); i++) {
			String ithToken = tokList.get(i);
			int ithStart = textBuffer.indexOf(ithToken, tempStart);
			//System.out.println("ithStart: " + ithStart);
			fullTokIndHM.put(ithStart, ithToken);
			if (start_TokenHM.containsKey(ithStart)) {
				//System.out.println("Entity " + i + " : " + ithToken + " = 1.0");
				pwMention.println("Entity " + i + " : "
						+ ithToken.toLowerCase() + " = 1.0");
			}
			tempStart = ithStart;
			// System.out.println(tokList.get(i));
		}
	}

	// now, I need to consider tokenize words as required format

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException  {
		// TODO Auto-generated method stub
		// E:\Dissertation Writing\corpora\corefann\wxml
		// "/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"

		int item = 6;
		long start = System.currentTimeMillis();
		File fileTag = new File(args[0]);
		// File outputFile = new File(args[2]);
		// PrintWriter pwOutput = new PrintWriter(new FileWriter(outputFile));
		if (fileTag.isDirectory()) {
			File dirOutput = new File(fileTag + "_input_entity");
			File dirOuttext = new File(fileTag + "_input_text");
			if (!dirOutput.exists()) {
				dirOutput.mkdir();
			}
			if (!dirOuttext.exists()) {
				dirOuttext.mkdir();
			}
			String text = "";
			// PrintWriter pwTokenNe = new PrintWriter(new FileWriter(new
			// File(dirOutput,fileTag.getName()+".nemodel"),true));
			File[] fileArray = fileTag.listFiles();
			for (int i = 0; i < fileArray.length; i++) {
				System.out.println(fileArray[i]);
				if (fileArray[i].getName().endsWith(".sgm")) {
					File sgmFile = fileArray[i];
					String fileName = sgmFile.getName();
					File xmlFile = new File(sgmFile.getParent(), fileName+".tmx.rdc.xml");
					PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(dirOutput,fileName)));
					PrintWriter pwOuttext = new PrintWriter(new FileWriter(
							new File(dirOuttext, fileName)));
					PrepareACEDevInput processACEDevInput = new PrepareACEDevInput(sgmFile,xmlFile);
					StringBuffer sbDev = SgmReader.readDoc3(fileArray[i]);
					StringBuffer textBuffer = processACEDevInput.tokenizeText(sbDev.toString());
					List<String> tokenList = processACEDevInput.tokenList;
					processACEDevInput.printTextFile(pwOuttext, tokenList);
					System.out.println("tokenList.get(0): " + tokenList.get(0));
					processACEDevInput.printInputFile(pwOutput,textBuffer,tokenList);
					pwOutput.close();
					pwOuttext.close();
				}
			}
		} else {
			PrepareACEDevInput processACEJdom = new PrepareACEDevInput(fileTag, new File(fileTag.getParent(),fileTag.getName()+".tmx.rdc.xml"));
			System.out.println(fileTag);
			PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(
					fileTag.getName() + ".tagged")));
			pwOutput.close();
		}

		// extractDJNML.extractDJNML(brDJFile);
		/*
		 * To get time in milliseconds, use long getTimeInMillis() method of
		 * Java Calendar class. It returns millseconds from Jan 1, 1970.
		 */
		/*
		 * Typical output would beCurrent milliseconds since Jan 1, 1970 are
		 * :1198628984102
		 */

		long stop = System.currentTimeMillis();
		System.out.println("stopping time: " + stop);
		long elapsed = stop - start;
		System.out.println("this is the total running time: " + elapsed);
	}
}
