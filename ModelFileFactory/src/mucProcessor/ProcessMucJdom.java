package mucProcessor;

import java.io.*;


/*
 * @ author Dingcheng Li
 * @ date Oct. 15, 09
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//import org.jdom.Content;
//import org.jdom.Document;
//import org.jdom.Element;
//import org.jdom.Attribute;
//import org.jdom.JDOMException;
//import org.jdom.input.SAXBuilder;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import utils.Pair;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import corpusProcessor.FeaStruct;

public class ProcessMucJdom {

	// static String startXML =
	// "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
	static String startXML = "<?xml version=\"1.0\"?>";
	static String DOC = "DOC";
	static String DOCNO = "DOCNO";
	static String DD = "DD";
	static String HL = "HL";
	static String PARAGRAPH = "p";
	static String SENTENCE = "s";
	static String COREF = "COREF";
	static String TYPE = "TYPE";
	static String REF = "REF";
	static String MIN = "MIN";
	static String WORD = "W";
	static String DEP = "DEP";
	static String ID = "ID";
	static String PUNC = "PUNCT";
	static String FUNC = "FUNC";
	static String SUBJ = "SUBJ";
	static String OBJ = "OBJ";
	static String COPY = "copy";
	static String OLD = "old";
	static String NEW = "NEW";
	static String nullString = "ZERO";
	static String POS = "POS";
	static String MASCULINE = "MAS";
	static String FAMININE = "FAM";
	static String NEUTER = "NEU";
	static String SINGULAR = "SING";
	static String PLURAL = "PLUR";
	static int MAXNUM = 6;
	static String start = "-";
	static String delim = "_";
	static String REFL = "reflexive";
	static String PERPRO = "personalPron";
	int tokenIndex = 0;
	int markableIndex = 0;
	List<FeaStruct> indStructList;
	List<Pair> wordPosPairList;
	List<Pair> funcStructList;
	List<String> stopWordList;
	List<FeaStruct> genStructList;
	List<FeaStruct> numStructList;
	List<FeaStruct> synStructList;
	List<String> pronounList;
	List<String> reflexiveList;
	List<String> maPronounsList;
	List<String> fePronounsList;
	List<String> nePronounsList;

	int countTags = 0;
	// int countToken = 0;
	boolean isMarkable = false;
	boolean isCoref = false;
	boolean isEmbedded = false;
	// to store the word token and its id;
	HashMap<String, String> tokenIdHM;
	HashMap<String, String> idTokenHM;
	List<String> tokenList;
	List<String> markIdList;
	HashMap<String, Integer> replaceRefIndexHM;
	List<Pair> replaceRefIndexList;
	HashMap<String, String> markableHeadWordHM;
	HashMap<String, String> headWordMarkableIdHM;
	HashMap<String, String> markableSrcHM;
	List<Pair> markableSrcList;
	HashMap<String, String> tokenGenderHM;
	HashMap<String, String> tokenNumberHM;
	HashMap<Integer, List<String>> mentionHM;
	List<List> mentionPairList;
	List<String> singDemonList = new ArrayList<String>();
	List<String> pluralList = new ArrayList<String>();
	File inputFile;

	public ProcessMucJdom(File inFile) {
		inputFile = inFile;
		indStructList = new ArrayList<FeaStruct>();
		genStructList = new ArrayList<FeaStruct>();
		numStructList = new ArrayList<FeaStruct>();
		synStructList = new ArrayList<FeaStruct>();
		funcStructList = new ArrayList<Pair>();
		replaceRefIndexList = new ArrayList<Pair>();
		wordPosPairList = new ArrayList<Pair>();
		tokenList = new ArrayList<String>();
		tokenIdHM = new HashMap<String, String>();
		markIdList = new ArrayList<String>();
		idTokenHM = new HashMap<String, String>();
		replaceRefIndexHM = new HashMap<String, Integer>();
		markableHeadWordHM = new HashMap<String, String>();
		headWordMarkableIdHM = new HashMap<String, String>();
		markableSrcHM = new HashMap<String, String>();
		markableSrcList = new ArrayList<Pair>();
		tokenGenderHM = new HashMap<String, String>();
		tokenNumberHM = new HashMap<String, String>();
		mentionHM = new HashMap<Integer, List<String>>();
		mentionPairList = new ArrayList<List>();
		String[] reflexiveArray = {"himself","herself","itself","themselves"};
		String[] pronounArray = {"he", "his", "him","she", "her","it", "its", "hers","they", "their", "them",};
		String[] maPronounsArray = { "he", "his", "him","himself"};
		String[] fePronounsArray = { "she", "her", "hers","herself" };
		String[] nePronounsArray = { "it", "its","itself" };
		String[] singDemonArray = { "this", "that" };
		String[] pluralArray = { "they", "their", "them", "these", "those","themselves"};
		stopWordList = new ArrayList<String>();
		stopWordList.add("CC");
		stopWordList.add("DN");
		stopWordList.add("FAUXV");
		stopWordList.add("-FMAINV");
		reflexiveList = Arrays.asList(reflexiveArray);
		pronounList = Arrays.asList(pronounArray);
		maPronounsList = Arrays.asList(maPronounsArray);
		fePronounsList = Arrays.asList(fePronounsArray);
		nePronounsList = Arrays.asList(nePronounsArray);
		singDemonList = Arrays.asList(singDemonArray);
		pluralList = Arrays.asList(pluralArray);
	}

	public void assignTokenIndex(Node rootNode) {
		Stack<Node> stack = new Stack<Node>();
		// ignore root -- root acts as a container, here, the rootNode should be
		// a sentNode.
		Node node = rootNode.getFirstChild();
		while (node != null) {
			String tokenId = "";
			String token = "";
			String markable = "";
			String markableId = "";
			if (node.getNodeName().equals(WORD)) {
				tokenId = node.getAttributes().getNamedItem(ID).getNodeValue();
				token = node.getTextContent();
				tokenIdHM.put(tokenId, token);
				// idTokenHM.put(token, tokenId);
				// Pair tokenIdPair = new Pair(tokenId,tokenIndex);
				tokenList.add(tokenId);
				tokenIndex++;

				// //System.out.println(node.getTextContent()+" "+tokenId+" "+tokenIndex);
			} else if (node.getNodeName().equals(PUNC)) {
				// I will consider the case where wordNode.getPreviousSibling is
				// null, later whereas, this case is rare.
				// haha, not rare, S7 starts with _
				token = node.getTextContent();
				tokenIdHM.put(tokenId, token);
				// Pair tokenIdPair = new Pair(tokenId,tokenIndex);
				tokenList.add(tokenId);
				tokenIndex++;
				// //System.out.println(node.getTextContent()+" "+tokenId+" "+tokenIndex);
			} else if (node.getNodeName().equals(COREF)) {
				markableId = node.getAttributes().getNamedItem(ID)
						.getNodeValue();
				markIdList.add(markableId);
			}
			if (node.hasChildNodes()) {
				// store next sibling in the stack. We return to it after all
				// children are processed.
				if (node.getNextSibling() != null)
					stack.push(node.getNextSibling());
				node = node.getFirstChild();
			} else {
				node = node.getNextSibling();
				if (node == null && !stack.isEmpty())
					// return to the parent's level.
					// note that some levels can be skipped if the parent's node
					// was the last one.
					node = (Node) stack.pop();
			}
		}
		// for(int i=0;i<tokenList.size();i++){
		// System.out.println(i+" thmarkId "+tokenList.get(i));
		// }
	}

	// "/project/nlp/dingcheng/nlplab/corpora/bukavu/381790newsML-done.xml.xml"

	public void splitMarkableNode() {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(inputFile);
				Node child = document.getFirstChild();
				NodeList docChildList = child.getChildNodes();
				NodeList senNodeList = document.getElementsByTagName(SENTENCE);
				for (int i = 0; i < senNodeList.getLength(); i++) {
					Node ithNode = senNodeList.item(i);
					//System.out.println(i+" ithNode "+ithNode.getNodeName()+" "+ithNode.getTextContent());
					//this.assignTokenIndex(ithNode);
				}

				for (int i = 0; i < senNodeList.getLength(); i++) {

					Node ithNode = senNodeList.item(i);
					// this.preorderTraverse(ithNode,replaceRefIndexHM,markableHeadWordHM);
					this.preorderTraverse(ithNode);
					// System.out.println("tempList size in splitMarkableNode: "+listArray.length);

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
	}

	/**
	 * Performs full tree traversal using stack. rootNode here is a sentence
	 * node.
	 */
	public void preorderTraverse(Node rootNode) {
		Stack<Node> stack = new Stack<Node>();
		List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(rootNode.getTextContent()));
		for (Sentence<? extends HasWord> sentence : sentences) {
			System.out.println("sentence: "+sentence);
		}

		// ignore root -- root acts as a container, here, the rootNode should be
		// a sentNode.
		Node node = rootNode.getFirstChild();

		// List<Pair> tempList = new ArrayList<Pair>();
		// List<Pair> trackList = new ArrayList<Pair>();
		// List<String> funcList = new ArrayList<String>();
		// List<String> tempSrcIdList = new ArrayList<String>();
		// List<Node> tempNodeList = new ArrayList<Node>();
		// List<String> tempMarkIdList=new ArrayList<String>();
		while (node != null) {
			if (node.getNodeName().equals(COREF)) {
				// this implies that its parent must be a MARKABLE we need to
				// record its SRC and its Parent ID
				// but it seems that we don't need to do anything here since it
				// will be handled in markable node as well
				Node parentNode = node.getParentNode();
				String markableId = parentNode.getAttributes().getNamedItem(ID)
						.getNodeValue();
				String typeStr = node.getAttributes().getNamedItem(TYPE)
						.getNodeValue();
				String refStr = node.getAttributes().getNamedItem(REF)
						.getNodeValue();
				String minStr = node.getAttributes().getNamedItem(MIN)
						.getNodeValue();
				if (!markableSrcHM.containsKey(markableId)) {
					//Pair markSrcPair = new Pair(markableId, srcStr);
					//markableSrcList.add(markSrcPair);
					//markableSrcHM.put(markableId, srcStr);
				}
			} 
			if (node.hasChildNodes()) {
				// store next sibling in the stack. We return to it after all
				// children are processed.
				if (node.getNextSibling() != null)
					stack.push(node.getNextSibling());
				node = node.getFirstChild();
			} else {
				node = node.getNextSibling();
				if (node == null && !stack.isEmpty())
					// return to the parent's level.
					// note that some levels can be skipped if the parent's node
					// was the last one.
					node = (Node) stack.pop();
			}
		}
	}
	
	//now, I need to consider tokenize words as required format.
	public void indexMucText(String inputXMLString,PrintWriter pwOutput) {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(new InputSource(new StringReader(inputXMLString)));				
				NodeList sentenceNodeList = document.getElementsByTagName(SENTENCE);
				int count=0;
				for (int i = 0; i < sentenceNodeList.getLength(); i++) {
					Node sentenceNode = sentenceNodeList.item(i);
					String sentText=sentenceNode.getTextContent();
					List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(sentText));
					for (Sentence<? extends HasWord> sentence : sentences) {
						//Sentence<TaggedWord> tSentence = MaxentTagger.tagSentence(sentence);
						//System.out.println(tSentence.toString(false));
						//pwOutput.println(tSentence.toString(false));
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
	

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// E:\Dissertation Writing\corpora\corefann\wxml
		// "/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"

		int item = 6;
		long start = System.currentTimeMillis();
		File fileTag = new File(args[0]);
		File fileTag2 = new File(args[1]);
		//File outputFile = new File(args[2]);
		//PrintWriter pwOutput = new PrintWriter(new FileWriter(outputFile));
		ProcessMucJdom processMucJdom = new ProcessMucJdom(fileTag2);
		processMucJdom.splitMarkableNode();
		if(fileTag.isDirectory()){
			File dirOutput = new File(fileTag.getParent()+"\\tagged");
			System.out.println(fileTag.getParent());
			if(!dirOutput.exists()){
				dirOutput.mkdir();
			}
			PrintWriter pwOutput = null;
			File[] fileArray = fileTag.listFiles();

			for(int i=0;i<fileArray.length;i++){
				int fileLeng = fileArray[i].getName().length();
				if(fileArray[i].getName().matches("README-WSJ")){
					continue;
				}else{
					pwOutput = new PrintWriter(new FileWriter(new File(dirOutput, fileArray[i].getName()+".tagged")));
					BufferedReader bfMuc = new BufferedReader(new FileReader(fileArray[i]));
					StringBuffer sbMuc = new StringBuffer();
					String line = "";
					while((line=bfMuc.readLine())!=null){
						line=line.replace("&", "and");
						sbMuc.append(line);
					}
					//System.out.println(sbMuc);
				}
				
			}
		}else{
			PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(fileTag.getName()+".tagged")));
			BufferedReader bfMuc = new BufferedReader(new FileReader(fileTag));
			StringBuffer sbMuc = new StringBuffer();
			StringReader srMuc = null;
			String line = "";
			while((line=bfMuc.readLine())!=null){
				if(line.contains("&")){
					line=line.replace("&", "and");
					sbMuc.append(line);
				}else{
					sbMuc.append(line);
				}
			}
			srMuc = new StringReader(sbMuc.toString());
			//System.out.println(sbMuc);
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
