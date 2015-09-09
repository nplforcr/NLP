package reutersProcessor;

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
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import corpusProcessor.FeaStruct;
import utils.Pair;

public class ProcessReutersJdomPron {

	// static String startXML =
	// "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
	static String startXML = "<?xml version=\"1.0\"?>";

	static String DOCU = "DOCUMENT";

	static String PARAGRAPH = "P";

	static String SENTENCE = "S";

	static String ELEMARK = "[Element: <MARKABLE/>]";

	static String ELEWORD = "[Element: <W/>]";

	static String ELECOREF = "[Element: <COREF/>]";

	static String ELEPUNC = "[Element: <PUNC/>]";

	static String MARKABLE = "MARKABLE";

	static String COREF = "COREF";

	static String TOKEN = "TOKEN";

	static String WORD = "W";

	static String DEP = "DEP";

	static String TOKTYPE = "token_type";

	static String PUNC = "PUNCT";

	static String SYMBOL = "SYMBOL";

	static String FUNC = "FUNC";

	static String SUBJ = "SUBJ";

	static String OBJ = "OBJ";

	static String P = "P";

	static String LEXEM = "TokenType.LEXEME";

	static String COPY = "copy";

	static String OLD = "old";

	static String NEW = "NEW";

	static String ELEMENT = "Element";

	static String TID = "id";

	static String CID = "ID";

	static String ID = "ID";

	static String REF = "REF";

	static String SRC = "SRC";

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
	List<Pair> wordPosPairList2;

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

	HashMap<String, List<String>> srcMarkableHM;

	List<List> mentionPairList;

	List<String> singDemonList = new ArrayList<String>();

	List<String> pluralList = new ArrayList<String>();

	File inputFile;

	public ProcessReutersJdomPron(File inFile) {
		inputFile = inFile;
		indStructList = new ArrayList<FeaStruct>();
		genStructList = new ArrayList<FeaStruct>();
		numStructList = new ArrayList<FeaStruct>();
		synStructList = new ArrayList<FeaStruct>();
		funcStructList = new ArrayList<Pair>();
		replaceRefIndexList = new ArrayList<Pair>();
		wordPosPairList = new ArrayList<Pair>();
		wordPosPairList2 = new ArrayList<Pair>();
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
		srcMarkableHM = new HashMap<String, List<String>>();
		String[] reflexiveArray = { "himself", "herself", "itself", "themselves" };
		String[] pronounArray = { "he", "his", "him", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "their", "them", "themselves", };
		String[] maPronounsArray = { "he", "his", "him", "himself" };
		String[] fePronounsArray = { "she", "her", "hers", "herself" };
		String[] nePronounsArray = { "it", "its", "itself" };
		String[] singDemonArray = { "this", "that" };
		String[] pluralArray = { "they", "their", "them", "these", "those", "themselves" };
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

				// //System.out.println(node.getTextContent()+" "+tokenId+"
				// "+tokenIndex);
			} else if (node.getNodeName().equals(PUNC)) {
				// I will consider the case where wordNode.getPreviousSibling is
				// null, later whereas, this case is rare.
				// haha, not rare, S7 starts with _
				token = node.getTextContent();
				tokenId = this.getPuncTokenId(node);
				tokenIdHM.put(tokenId, token);
				// Pair tokenIdPair = new Pair(tokenId,tokenIndex);
				tokenList.add(tokenId);
				tokenIndex++;
				// //System.out.println(node.getTextContent()+" "+tokenId+"
				// "+tokenIndex);
			} else if (node.getNodeName().equals(MARKABLE)) {
				markableId = node.getAttributes().getNamedItem(ID).getNodeValue();
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
					// //System.out.println(i+" ithNode
					// "+ithNode.getNodeName());
					this.assignTokenIndex(ithNode);
				}
				List[] listArray = new List[8];
				List<Pair> tempList = new ArrayList<Pair>();
				List<Pair> trackList = new ArrayList<Pair>();
				List<String> funcList = new ArrayList<String>();
				List<String> posList = new ArrayList<String>();
				List<String> wordList = new ArrayList<String>();
				List<String> tempSrcIdList = new ArrayList<String>();
				List<Node> tempNodeList = new ArrayList<Node>();
				List<String> tempMarkIdList = new ArrayList<String>();

				for (int i = 0; i < senNodeList.getLength(); i++) {

					Node ithNode = senNodeList.item(i);
					// this.preorderTraverse(ithNode,replaceRefIndexHM,markableHeadWordHM);
					listArray = this.preorderTraverse(ithNode, tempList, trackList, funcList, posList, wordList, tempNodeList, tempSrcIdList, tempMarkIdList);
					tempList = listArray[0];
					trackList = listArray[1];
					funcList = listArray[2];
					posList = listArray[3];
					wordList = listArray[4];
					tempNodeList = listArray[5];
					tempSrcIdList = listArray[6];
					tempMarkIdList = listArray[7];
					// System.out.println("tempList size in splitMarkableNode:
					// "+listArray.length);

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

	public List[] processTempNodeList(List<Node> tempNodeList, List<String> tempSrcIdList, List<String> tempMarkIdList, List<Pair> tempList, List<Pair> trackList, List<String> funcList, List<String> posList, List<String> wordList) {
		// System.out.println("tempSrcIdList size in processTempNodeList:
		// "+tempSrcIdList.size());

		List[] listArray = new List[8];

		for (int i = 0; i < tempMarkIdList.size(); i++) {
			// System.out.println("tempMarkIdList items in processTempNodeList:
			// "+tempMarkIdList.get(i));
		}

		for (int i = 0; i < tempNodeList.size(); i++) {
			Node tempNode = tempNodeList.get(i);
			if (tempNode.getNodeName().equals(MARKABLE)) {
				Node corefNode = this.getFirstChildByTagName(tempNode, COREF);
				String tokenMarkId = tempNode.getAttributes().getNamedItem(ID).getNodeValue();
				int tokenMarkIdIndex = markIdList.indexOf(tokenMarkId);
				String oper = "";
				String refId = "";
				// System.out.println("tokenID in processTempNodeList:
				// "+tokenId);
				if (corefNode != null) {
					String corefId = corefNode.getAttributes().getNamedItem(SRC).getNodeValue();
					// this case, the cataphora refer forward to future mention
					if (tempSrcIdList.contains(corefId)) {
						// System.out.println("tokenID in processTempNodeList:
						// "+tokenMarkId);
						int targetIndex = tempMarkIdList.indexOf(corefId);
						// System.out.println("targetCorefNode index:
						// "+targetIndex);
						Node targetNode = tempNodeList.get(targetIndex);
						// System.out.println("targetCorefNode:
						// "+targetNode.getNodeName());
						Node targetCorefNode = this.getFirstChildByTagName(targetNode, COREF);
						// if targetCorefNode is null, it implies that it
						// doesn't have previous coreferring mentions
						// then the cataphora should be regarded as new.
						// System.out.println("targetCorefNode:
						// "+targetCorefNode);

						int targetMarkIdIndex = markIdList.indexOf(targetNode.getAttributes().getNamedItem(ID).getNodeValue());

						if (targetCorefNode == null) {
							// oper is new, so refId is empty so far
							// System.out.println("Is inside processMarkableNode
							// : processTempNodeList ");
							oper = NEW;
							// in this case, the opposite is true. the present
							// markableNode is the SCR Id in
							// their catophoric mention
							// processMarkableNode where inputOper is not empty
							// while inputRefId is empty
							if (tokenMarkIdIndex < targetMarkIdIndex) {
								Pair markSrcPair = new Pair(corefId, tokenMarkId);
								markableSrcList.add(markSrcPair);
								// that is, corefId is new and tokenMarkId is
								// old
								markableSrcHM.put(corefId, tokenMarkId);
								// it seems that the following code is not
								// functional at all
								markableSrcHM.remove(tokenMarkId);
							}
							listArray = this.processMarkableNode(tempNode, tempList, trackList, funcList, posList, wordList, oper, refId);
							tempList = listArray[0];
							trackList = listArray[1];
							funcList = listArray[2];
							posList = listArray[3];
							wordList = listArray[4];
						}
						// if targetCorefNode is not null, it implies that it
						// does have previous coreferring mentions
						// then the cataphora should still be regarded as OLD.
						else {
							// oper is still OLD, so no neccesary to state it
							// again
							// but we need new refId which is from the later
							// reference which refer back to some previous
							// mentions
							refId = targetCorefNode.getAttributes().getNamedItem(SRC).getNodeValue();

							// the markableSrcHM should set key-value pair which
							// is usually done in preorderTraveal
							// when node is coref.
							// processMarkableNode where inputOper is empty
							// while inputRefId is not
							// order here is important since corefId is corefId
							// is after tokenMarkId, so it should be
							// behind tokenMarkId
							Pair markSrcPair = new Pair(tokenMarkId, refId);
							markableSrcList.add(markSrcPair);
							markableSrcHM.put(tokenMarkId, refId);

							Pair corefSrcPair = new Pair(corefId, refId);
							markableSrcList.add(corefSrcPair);
							markableSrcHM.put(corefId, refId);

							// System.out.println(targetCorefNode.getAttributes().getNamedItem(SRC).getNodeValue());
							listArray = this.processMarkableNode(tempNode, tempList, trackList, funcList, posList, wordList, oper, refId);
							tempList = listArray[0];
							trackList = listArray[1];
							funcList = listArray[2];
							posList = listArray[3];
							wordList = listArray[4];

						}
					}
					// in this case, the markableNode is still normal which is
					// included into the tempNodeList due to
					// previous cataphora and the future mention is after this
					// markableNode
					// processMarkableNode where both inputOper and inputRefId
					// are empty
					else {
						listArray = this.processMarkableNode(tempNode, tempList, trackList, funcList, posList, wordList, oper, refId);
						tempList = listArray[0];
						trackList = listArray[1];
						funcList = listArray[2];
						posList = listArray[3];
						wordList = listArray[4];
					}

				} else {
					// System.out.println("tokenID in processTempNodeList when
					// corefNode is null: "+tokenMarkId);
					// there are two cases, 1. it is normal case, then, we only
					// need to call processMarkableNode
					// where both oper and refId are empty
					// 2. it is not the normal case where its markableId is in
					// the tempSrcIdList but it does not refer
					// back to previous mention
					// when processing such a markable node, both inputOper and
					// intpuRefId are not empty
					if (tempSrcIdList.contains(tokenMarkId)) {
						oper = OLD;
						refId = markableSrcHM.get(tokenMarkId);
						// System.out.println("refId in processTempNodeList 4th
						// case: "+refId);
						listArray = this.processMarkableNode(tempNode, tempList, trackList, funcList, posList, wordList, oper, refId);
						tempList = listArray[0];
						trackList = listArray[1];
						funcList = listArray[2];
						posList = listArray[3];
						wordList = listArray[4];
					}
					// tempSrcIdList doesn't contain tokenId, so, it means that
					// this is a normal case.
					else {
						listArray = this.processMarkableNode(tempNode, tempList, trackList, funcList, posList, wordList, oper, refId);
						tempList = listArray[0];
						trackList = listArray[1];
						funcList = listArray[2];
						posList = listArray[3];
						wordList = listArray[4];
					}
				}
			} else if (tempNode.getNodeName().equals(WORD) || tempNode.getNodeName().equals(PUNC)) {
				if (tempNode.getNodeName().equals(WORD)) {
					String tokenId = tempNodeList.get(i).getAttributes().getNamedItem(ID).getNodeValue();
					// System.out.println("tokenID in processTempNodeList:
					// "+tokenId);
				} else {
					// System.out.println("tokenID in processTempNodeList:
					// "+tempNodeList.get(i).getTextContent());
				}
				Node parentNode = tempNode.getParentNode();
				String refId = "";
				String oper = COPY;
				String markableId = "";
				if (parentNode.getNodeName().equals(SENTENCE)) {
					String tokenId = "";
					if (tempNode.getNodeName().equals(WORD)) {
						tokenId = tempNode.getAttributes().getNamedItem(ID).getNodeValue();
					} else {
						tokenId = tempNode.getTextContent();
					}
					listArray = this.switchCountToken(tempNode, tempList, trackList, funcList, posList, wordList, oper, markableId, refId);
					tempList = listArray[0];
					trackList = listArray[1];
					funcList = listArray[2];
					posList = listArray[3];
					wordList = listArray[4];
					if (tempList != null && tempList.size() > 0) {
						// System.out.println("tempList size in processTempNode:
						// "+tempList.get(0)+" the content of the tempNode:
						// "+tempNode.getTextContent());
					}
				}
				// else implies that the parent should be MARKABLE, it seems
				// that I don't need to do anything
				// in this case since the word will be handled in markable node.
				// else{};
				// String nodeName = this.extractInfor(tempNode);
			}

		}
		listArray[5] = new ArrayList<Node>();
		listArray[6] = new ArrayList<String>();
		listArray[7] = new ArrayList<String>();
		return listArray;
	}

	/**
	 * Performs full tree traversal using stack. rootNode here is a sentence
	 * node.
	 */
	public List[] preorderTraverse(Node rootNode, List<Pair> tempList, List<Pair> trackList, List<String> funcList, List<String> posList, List<String> wordList, List<Node> tempNodeList, List<String> tempSrcIdList, List<String> tempMarkIdList) {
		List[] listArray = new List[8];
		Stack<Node> stack = new Stack<Node>();

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
			// print node information

			
			//System.out.println(node.getNodeName()+" node Id: "+"="+node.getNodeValue()+node.getTextContent());
			int markableHeight = this.getHeight(node) - 2;
			if (node.getNodeName().equals(MARKABLE)) {
				List<Node> eleWordList = new ArrayList<Node>();
				String markableID = node.getAttributes().getNamedItem(ID).getNodeValue();
				
				//for 365634, I know why markable 54 doesn't have a head since it involves two ands, as a result,
				//it cannot a head word for it with my findHeadWord method. I will consider this later
				//In addition, I am not sure why extractInfor fail to get a word here, probably for the same reason
				//I will consider them later as well.
				if (markableID.equals("54")) {
					System.out.println("start the debugging");
				}
				//if a srcId of a markableId is larger than the markableId, then contents between srcId and markableId
				//are stored. That is how in tempSrcId works. 
				int markIdIndex = markIdList.indexOf(markableID);
				Node corefNode = this.getFirstChildByTagName(node, COREF);
				String corefId = "";
				String inputOper = "";
				String inputRefId = "";
				// int corefIdInt = 0;
				// for case the markable has a corefNode
				String parentNodeId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue();
				String parentNodeName = node.getParentNode().getNodeName();
				Node markKidNode = this.getFirstChildByTagName(node, MARKABLE);
				// System.out.println("in preorderTraverse: "+parentNodeId+"
				// "+parentNodeName);
				if (corefNode != null) {
					// System.out.println("what is the corefNode now in
					// preorderTraverse -&&&&&&&&&&&&&&&&&&&&&&&&&&&&:
					// "+corefNode.getNodeName()+" ID: "+
					// corefNode.getAttributes().getNamedItem(ID).getNodeValue()+"
					// SRC
					// ID:"+corefNode.getAttributes().getNamedItem(SRC).getNodeValue());
					corefId = corefNode.getAttributes().getNamedItem(SRC).getNodeValue();
					// corefIdInt=Integer.valueOf(corefNode.getAttributes().getNamedItem(SRC).getNodeValue());
					// if(corefIdInt>Integer.valueOf(markableID)){
					// this case is the start of cataphora, that is the corefId
					// cannot be found in markabelHeadWordHM
					// if(!markableHeadWordHM.containsKey(corefId)){
					int corefIdIndex = markIdList.indexOf(corefId);
					// int markIdIndex = markIdList.indexOf(markIdIndex);
					if (corefIdIndex > markIdIndex) {
						if (tempSrcIdList == null) {
							tempSrcIdList = new ArrayList<String>();
							tempNodeList = new ArrayList<Node>();
							tempMarkIdList = new ArrayList<String>();
							tempSrcIdList.add(corefId);
							tempNodeList.add(node);
							tempMarkIdList.add(markableID);
						} else {
							tempSrcIdList.add(corefId);
							tempNodeList.add(node);
							tempMarkIdList.add(markableID);
						}

					}
					// if corefId is in markableHeadWordHM, it implies that this
					// is normal anophora
					// but we need to compare it with corefId in the corefIdList
					// if its markabeId is smaller than corefId in that list,
					// this markabelNode will be added
					// to the tempNodeList as well.
					// for the following case, the node added to the
					// tempNodeList due to the reason that they are
					// subnode of markableNode which has been added to the
					// tempNodeList
					else if (tempMarkIdList != null && tempMarkIdList.contains(parentNodeId) && parentNodeName.equals(MARKABLE)) {
						// System.out.println("any extra node added here in
						// preorderTraverse?");
						tempNodeList.add(node);
						tempMarkIdList.add(markableID);
					}
					// now this case refers to cases where the parentNode should
					// be Sentence
					else {
						if (tempSrcIdList != null && !tempSrcIdList.isEmpty()) {
							// System.out.println("tempSrcIdList size in
							// preorderTraverse: "+tempSrcIdList.size());
							// that means that we find the SRC of catophora
							if (tempSrcIdList.contains(markableID)) {
								tempNodeList.add(node);
								tempMarkIdList.add(markableID);
								if (markKidNode == null) {
									listArray = this.processTempNodeList(tempNodeList, tempSrcIdList, tempMarkIdList, tempList, trackList, funcList, posList, wordList);
									tempList = listArray[0];
									trackList = listArray[1];
									funcList = listArray[2];
									posList = listArray[3];
									wordList = listArray[4];
									tempNodeList = listArray[5];
									tempSrcIdList = listArray[6];
									tempMarkIdList = listArray[7];
									// listArray[3]=new ArrayList<Node>();
									// listArray[4]=new ArrayList<String>();
									// listArray[5]=new ArrayList<String>();

								}
							}
							// that means the the markableNode is in between the
							// refering and refered mentions,
							// so we add them to tempNodeList too.
							else {

								for (int i = 0; i < tempSrcIdList.size(); i++) {
									String ithSrcId = tempSrcIdList.get(i);
									int ithSrcIdIndex = markIdList.indexOf(ithSrcId);
									if (markIdIndex < ithSrcIdIndex) {
										tempNodeList.add(node);
										tempMarkIdList.add(markableID);
										break;
									} else if (i == tempSrcIdList.size() - 1) {
										listArray = this.processTempNodeList(tempNodeList, tempSrcIdList, tempMarkIdList, tempList, trackList, funcList, posList, wordList);
										tempList = listArray[0];
										trackList = listArray[1];
										funcList = listArray[2];
										posList = listArray[3];
										wordList = listArray[4];
										tempNodeList = listArray[5];
										tempSrcIdList = listArray[6];
										tempMarkIdList = listArray[7];
										// listArray[3]=new ArrayList<Node>();
										// listArray[4]=new ArrayList<String>();
										// listArray[5]=new ArrayList<String>();
										listArray = this.processMarkableNode(node, tempList, trackList, funcList, posList, wordList, inputOper, inputRefId);
										tempList = listArray[0];
										trackList = listArray[1];
										funcList = listArray[2];
										posList = listArray[3];
										wordList = listArray[4];
									}

								}

							}
						}
						// now it means that there is not catophora at all.
						else {
							listArray = this.processMarkableNode(node, tempList, trackList, funcList, posList, wordList, inputOper, inputRefId);
							// a lesson, remember how to pass parameters from
							// one method to another method!
							// it takes me almost two days to debug it.
							tempList = listArray[0];
							trackList = listArray[1];
							funcList = listArray[2];
							posList = listArray[3];
							wordList = listArray[4];
						}
					}
				}
				// for markalble which doesn't have a corefNode. In this case,
				// if the tempSrcIdList
				// is not empty, we should progress tempNodeList firstly and
				// then go to the markable node
				// if the marklabeId is larger than
				// else, do the common thing for a common node.
				else {

					if (tempSrcIdList != null && !tempSrcIdList.isEmpty()) {
						// System.out.println("tempSrcIdList size in
						// preorderTraverse: "+tempSrcIdList.size());
						if (tempSrcIdList.contains(markableID)) {
							int index = tempSrcIdList.indexOf(markableID);
							if (index < tempSrcIdList.size() - 1) {
								tempNodeList.add(node);
								tempMarkIdList.add(markableID);
							} else {
								tempNodeList.add(node);
								tempMarkIdList.add(markableID);
								if (markKidNode == null) {
									listArray = this.processTempNodeList(tempNodeList, tempSrcIdList, tempMarkIdList, tempList, trackList, funcList, posList, wordList);
									tempList = listArray[0];
									trackList = listArray[1];
									funcList = listArray[2];
									posList = listArray[3];
									wordList = listArray[4];
									tempNodeList = listArray[5];
									tempSrcIdList = listArray[6];
									tempMarkIdList = listArray[7];
								}
							}
						} else if (tempMarkIdList.contains(parentNodeId) && parentNodeName.equals(MARKABLE)) {
							// System.out.println("any extra node added here in
							// preorderTraverse?");
							tempNodeList.add(node);
							tempMarkIdList.add(markableID);
						} else {
							tempNodeList.add(node);
							tempMarkIdList.add(markableID);
						}
					}
					// that is tempSrcIdList is empty, implying that this
					// markable node is a normal one.
					else {
						listArray = this.processMarkableNode(node, tempList, trackList, funcList, posList, wordList, inputOper, inputRefId);
						// a lesson, remember how to pass parameters from one
						// method to another method!
						// it takes me almost two days to debug it.
						tempList = listArray[0];
						trackList = listArray[1];
						funcList = listArray[2];
						posList = listArray[3];
						wordList = listArray[4];
						tempNodeList = listArray[5];
						tempSrcIdList = listArray[6];
						tempMarkIdList = listArray[7];
					}
				}
			} else if (node.getNodeName().equals(COREF)) {
				// this implies that its parent must be a MARKABLE we need to
				// record its SRC and its Parent ID
				// but it seems that we don't need to do anything here since it
				// will be handled in markable node as well
				Node parentNode = node.getParentNode();
				String markableId = parentNode.getAttributes().getNamedItem(ID).getNodeValue();
				String srcStr = node.getAttributes().getNamedItem(SRC).getNodeValue();
				if (!markableSrcHM.containsKey(markableId)) {
					Pair markSrcPair = new Pair(markableId, srcStr);
					markableSrcList.add(markSrcPair);
					markableSrcHM.put(markableId, srcStr);
				}
			} else if (node.getNodeName().equals(WORD) || node.getNodeName().equals(PUNC)) {
				Node parentNode = node.getParentNode();
				String refId = "";
				String oper = COPY;
				String markableId = "";

				// the logic is that if the present node is a word or punc and
				// if they are within some catophora range,
				// the ID of its closest markable should be smaller or equal to
				// the markable index, so, I need to create
				// a markableIndexHM for it.

				if (parentNode.getNodeName().equals(SENTENCE)) {
					String tokenId = "";
					if (node.getNodeName().equals(WORD)) {
						tokenId = node.getAttributes().getNamedItem(ID).getNodeValue();
					} else {
						tokenId = parentNode.getAttributes().getNamedItem(ID).getNodeValue() + node.getTextContent();
					}
					//loop through words rather than markable. but tempSrcIdList needs to be checked
					//I really admire what hard work and good designing had been done by you though big troubles were brought when
					//you are recreating this work with variations. A good lesson here is that you must write very clear
					//comments. Otherwise, a stupid man like you will be confused by your own smart ideas. 
					//nothing fancy here, since here involving cataphora, then, you found error will be aroused if processing xml files
					//one tag after another tag, then, you use temSrcIdList to store srcIds which are larger than their  you encounter on the way
					//anphors. Then, loop through tempSrcIdList, during the loop, add tempNodeList items which are between temSrcId and preMarkId
					//That is what if clause in the for looop is doing. Then, in the "else if" clause, all items are added, call processTempNodeList
					//to process the tempNodeList. after processTempNodeList is done, temSrcIdList, tempNodeList and tempMarkIdList will be emptied. 
					//but other lists will be passed to other functions one by one. That is the difference between first five lists and the last three lists.
					//Dingcheng Mar 27, 10
					if (tempSrcIdList != null && !tempSrcIdList.isEmpty()) {
						// the following code handle cases where node is a word
						// or a punc and we don't know if they have
						// been out of the range of cataphora
						if (node.getPreviousSibling() != null && node.getPreviousSibling().getNodeName().equals(MARKABLE)) {
							String preMarkId = node.getPreviousSibling().getAttributes().getNamedItem(ID).getNodeValue();
							int preMarkIndex = markIdList.indexOf(preMarkId);
//							System.out.println(preMarkId + " " + tempSrcIdList.get(0));
//							if (preMarkId.equals("26")) {
//								System.out.println("start the debugging");
//							}
							for (int i = 0; i < tempSrcIdList.size(); i++) {
								String ithSrcId = tempSrcIdList.get(i);
								int ithSrcIdIndex = markIdList.indexOf(ithSrcId);
								if (preMarkIndex < ithSrcIdIndex) {
									tempNodeList.add(node);
									tempMarkIdList.add(tokenId);
									break;
								} else if (i == tempSrcIdList.size() - 1) {
									listArray = this.processTempNodeList(tempNodeList, tempSrcIdList, tempMarkIdList, tempList, trackList, funcList, posList, wordList);
									tempList = listArray[0];
									trackList = listArray[1];
									funcList = listArray[2];
									posList = listArray[3];
									wordList = listArray[4];
									//you are really stupid, after a long time, you totally forgot what you are doing. Therefore,
									//you totatlly don't know what the following three lines are doing here. in fact, the following line aims at emptying these lists.
									//that is, tempSrcIdList after running processTempNodeList should be emptied. Otherwise, the for loop will be never ending or some itme will
									//be out of bounds. 
									tempNodeList = listArray[5];
									tempSrcIdList = listArray[6];
									tempMarkIdList = listArray[7];
									listArray = this.switchCountToken(node, tempList, trackList, funcList, posList, wordList, oper, markableId, refId);
									tempList = listArray[0];
									trackList = listArray[1];
									funcList = listArray[2];
									posList = listArray[3];
									wordList = listArray[4];
								}
							}
						}
						// here, else implies that this is the second word or
						// punc when tempSrcIdList is not empty yet
						// that is the word or the punt is in the range of the
						// cataphora. thus, tempNodeList needs them
						else {
							tempNodeList.add(node);
							tempMarkIdList.add(tokenId);
						}
					} else {
						listArray = this.switchCountToken(node, tempList, trackList, funcList, posList, wordList, oper, markableId, refId);
						tempList = listArray[0];
						trackList = listArray[1];
						funcList = listArray[2];
						posList = listArray[3];
						wordList = listArray[4];
						tempNodeList = listArray[5];
						tempSrcIdList = listArray[6];
						tempMarkIdList = listArray[7];
						// if(tempList.size()>0){
						// System.out.println("tempList size in
						// preorderTraverse: "+tempList.get(0)+" the content of
						// the node: "+node.getTextContent());
						// }
					}
					// the following understanding is correct. But now, I must
					// use different methods to pass
					// tempList and trackList to upper methods. Nov.1,09
					// cool! what I have learned here is that we must empty
					// tempList and trackList here
					// not any other places!! I cannot delete the two parameters
					// from processMarkalbeNode as well
					// a good lesson here. Go ahead, victory is nearby!
					// tempList = new ArrayList<Pair>();
					// trackList = new ArrayList<Pair>();
					// countToken++;
				}
				// else implies that the parent should be MARKABLE, it seems
				// that I don't need to do anything
				// in this case since the word will be handled in markable node.
				// else{};
				// loop will go through each node. so it seems that after
				// markable node is processed, word nodes will be handed
				// one by one here. and word and pos will be added to
				// wordPosPairList one by one then.
				String nodeName = this.extractInfor(node);
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

		// for(int i=0;i<tempMarkIdList.size();i++){
		// System.out.println("tempMarkIdList items in preOrderTraverse:
		// "+tempMarkIdList.get(i));
		// }
		listArray[5] = tempNodeList;
		listArray[6] = tempSrcIdList;
		listArray[7] = tempMarkIdList;
		return listArray;
	}

	public List[] switchCountToken(Node node, List<Pair> tempList, List<Pair> trackList, List<String> funcList, List<String> posList, List<String> wordList, String oper, String markableId, String refId) {
		List[] listArray = new List[8];
		int countToken = 0;
		String tokenId = "";
		String pos = "";
		String word = "";
		int tempToken = -1;
		String kidNodeName = "";
		if (node.getNodeName().equals(WORD)) {
			tokenId = node.getAttributes().getNamedItem(ID).getNodeValue();
			pos = node.getAttributes().getNamedItem(POS).getNodeValue();
			word = node.getTextContent();
			countToken = tokenList.indexOf(tokenId);
			// System.out.println("countToken in switchCountToken:
			// "+countToken);
		} else {
			// I will consider the case where wordNode.getPreviousSibling is
			// null, later whereas, this case is rare.
			tokenId = this.getPuncTokenId(node);
			pos = node.getTextContent();
			word = node.getTextContent();
			countToken = tokenList.indexOf(tokenId);
		}

		// how to start again the method after smaller countToken is done is a
		// problem.
		if (countToken > indStructList.size()) {
			// String kidNodeName = this.extractInfor(wordNode);

			if (tempList != null && tempList.size() > 0) {
				listArray = this.processTempList(tempList, trackList, funcList, posList, wordList, markableId);
				this.operIsCopy(countToken,pos,word);
				// tempList = listArray[0];
				// trackList = listArray[1];
				// funcList = listArray[2];
			}
		} else if (countToken == indStructList.size()) {
			// the following code leads to redundant call.
			// kidNodeName = this.extractInfor(node);
			countToken = this.operIsCopy(countToken,pos,word);
		}
		// listArray[0]=tempList;
		// listArray[1]=trackList;
		// listArray[2]=funcList;
		// System.out.println("countToken in swithcountToken, op=copy:
		// "+countToken+" word: "+node.getTextContent()+" tokenId: "+tokenId);
		return listArray;
	}

	//makeDumHead should return value, but it is weird. it doesn't. 
	public void makeDumHead(String oper, Node markableNode, String refId) {
		NamedNodeMap markableMap = markableNode.getAttributes();
		String markableId = markableMap.getNamedItem(ID).getNodeValue();
		Node firstKidMarkable = this.getFirstChildByTagName(markableNode, MARKABLE);
		// List<Node> grandKidNodeList =
		// this.getChildrenByTagName(firstKidMarkable, MARKABLE);
		List<Node> wordKidList = this.getChildrenByTagName(firstKidMarkable, WORD);
		Node kidHeadWordNode = this.findHeadword(wordKidList);
		int indexOfHeadNode = wordKidList.indexOf(kidHeadWordNode);
		String kidHeadWord = "";
		String kidHeadWordId = "";
		String kidHeadWordFunc = "";
		String kidHeadWordPos = "";

		// String kidHeadWordGen = "";
		// String kidHeadWordNum = "";
		String kidNodeName = "";
		if (kidHeadWordNode != null) {
			kidHeadWord = ((String) kidHeadWordNode.getTextContent()).toLowerCase();
			kidHeadWordId = (String) kidHeadWordNode.getAttributes().getNamedItem(ID).getNodeValue();
			kidHeadWordFunc = (String) kidHeadWordNode.getAttributes().getNamedItem(FUNC).getNodeValue();
			kidHeadWordPos = (String) kidHeadWordNode.getAttributes().getNamedItem(POS).getNodeValue();
			kidNodeName = this.extractInfor(kidHeadWordNode);
			// kidHeadWordGen = this.extractGen(kidHeadWordNode);
			// kidHeadWordNum = this.extractNum(kidHeadWordNode);
		}else{
			Node firstGrandKidMarkable = this.getFirstChildByTagName(firstKidMarkable, MARKABLE);
			List<Node> wordGrandKidList = this.getChildrenByTagName(firstGrandKidMarkable, WORD);
			Node grandKidHeadWordNode = this.findHeadword(wordGrandKidList);
			int indexOfKidHeadNode = wordGrandKidList.indexOf(grandKidHeadWordNode);
			if(grandKidHeadWordNode!=null){
				kidHeadWord = ((String) grandKidHeadWordNode.getTextContent()).toLowerCase();
				kidHeadWordId = (String) grandKidHeadWordNode.getAttributes().getNamedItem(ID).getNodeValue();
				kidHeadWordFunc = (String) grandKidHeadWordNode.getAttributes().getNamedItem(FUNC).getNodeValue();
				kidHeadWordPos = (String) grandKidHeadWordNode.getAttributes().getNamedItem(POS).getNodeValue();
				kidNodeName = this.extractInfor(grandKidHeadWordNode);
			}
		}
		// tokenId=wordNode.getAttributes().getNamedItem(ID).getNodeValue();
		// tokenId=firstKidMarkable.getAttributes().getNamedItem(ID).getNodeValue();
		// int countToken = tokenIdHM.get(kidHeadWordId);
		int countToken = tokenList.indexOf(kidHeadWordId);
		// System.out.println("countToken in the dumHead: "+countToken);

		// countToken for the dum head should be the value of the countToken of
		// the headword of the kidnode substract the index
		// of the countToken in the wordKidList.
		int dumCountToken = countToken - indexOfHeadNode;
		if (dumCountToken != indStructList.size()) {
			// this is always correct since I regulate that the dum head of a
			// markable is
			// the first one of its kids
			dumCountToken = indStructList.size();
		}
		// String dumHeadWord =
		// ((String)wordPosPairList.get(dumCountToken).getFirst()).toLowerCase();
		// String dumHeadPos =
		// ((String)wordPosPairList.get(dumCountToken).getSecond()).toLowerCase();
		// System.out.println("dumHeadWord and dumHeadPos: "+dumHeadWord+"
		// "+dumHeadPos+" "+kidHeadWordNode.getTextContent());

		if (oper == OLD && pronounList.contains(kidHeadWord)) {
			if (reflexiveList.contains(kidHeadWord)) {
				kidHeadWordPos = REFL;
			} else if (pronounList.contains(kidHeadWord)) {
				kidHeadWordPos = PERPRO;
			}
			if (!srcMarkableHM.containsKey(refId)) {
				List<String> mentionList = new ArrayList<String>();
				mentionList.add(markableId);
				srcMarkableHM.put(refId, mentionList);
				this.operIsOld(dumCountToken, refId, kidHeadWordFunc, kidHeadWordPos, kidHeadWord);
			} else {
				List<String> mentionList = srcMarkableHM.get(refId);
				String contiguousMarkId = mentionList.get(mentionList.size() - 1);
				mentionList.add(markableId);
				srcMarkableHM.put(refId, mentionList);
				this.operIsOld(dumCountToken, contiguousMarkId, kidHeadWordFunc, kidHeadWordPos, kidHeadWord);
			}
			// srcMarkableHM.put(refId, arg1);
		} else if (oper == OLD && !pronounList.contains(kidHeadWord)) {
			if (!srcMarkableHM.containsKey(refId)) {
				List<String> mentionList = new ArrayList<String>();
				mentionList.add(markableId);
				srcMarkableHM.put(refId, mentionList);
			} else {
				List<String> mentionList = srcMarkableHM.get(refId);
				mentionList.add(markableId);
				srcMarkableHM.put(refId, mentionList);
			}
			this.operIsNew(dumCountToken, markableId, kidHeadWordFunc, kidHeadWordPos, kidHeadWord);
		} else if (oper == NEW) {
			this.operIsNew(dumCountToken, markableId, kidHeadWordFunc, kidHeadWordPos, kidHeadWord);
		}
		// System.out.println("from makeDumHead: "+kidHeadWord);
		String dumId = "dum-" + kidHeadWordId;
		tokenList.add(dumCountToken, dumId);
		markableHeadWordHM.put(markableId, String.valueOf(dumCountToken));
		headWordMarkableIdHM.put(String.valueOf(dumCountToken), markableId);
	}

	public List[] loopRemList(NodeList kidList, String refId, String headWord, String oper, Node wordNode, String markableId, int k, List<Pair> tempList, List<Pair> trackList, List<String> funcList, List<String> posList, List<String> wordList) {
		List[] listArray = new List[8];
		for (int j = k + 1; j < kidList.getLength(); j++) {
			Node remWordNode = kidList.item(j);
			String remWord = remWordNode.getTextContent().toLowerCase();
			String remPos = "";
			// System.out.println("loopRemList remWord: "+remWord);
			String remTokenId = "";
			// System.out.println("word: "+word+" tokenId in loop methods:
			// "+tokenId);
			String remOper = COPY;
			String remFunc = "";
			String remGen = "";
			String remNum = "";
			int remCountToken = 0;
			if (remWordNode.getNodeName().equals(WORD) || remWordNode.getNodeName().equals(PUNC)) {
				if (remWordNode.getNodeName().equals(WORD)) {
					remTokenId = remWordNode.getAttributes().getNamedItem(ID).getNodeValue();
					if (remWordNode.getAttributes().getNamedItem(FUNC) != null) {
						remFunc = remWordNode.getAttributes().getNamedItem(FUNC).getNodeValue();
					} else {
						remFunc = SYMBOL;
					}

					if (remWordNode.getAttributes().getNamedItem(POS) != null) {
						remPos = remWordNode.getAttributes().getNamedItem(POS).getNodeValue();
					} else {
						remPos = SYMBOL;
					}

				} else {
					remTokenId = this.getPuncTokenId(remWordNode);
					remFunc = remWord;
					remPos = remWord;
				}
				// System.out.println("word: "+word+" tokenId in loop methods:
				// "+tokenId);
				// String remOper = COPY;

				remCountToken = tokenList.indexOf(remTokenId);
				Pair remIndexPair = null;
				Pair remTrackPair = null;
				if (remWord.equals(headWord)) {
					remIndexPair = new Pair(remCountToken, oper);
					remTrackPair = new Pair(remCountToken, new Pair(refId, wordNode));
					markableHeadWordHM.put(markableId, String.valueOf(remCountToken));
					headWordMarkableIdHM.put(String.valueOf(remCountToken), markableId);
				} else {
					remIndexPair = new Pair(remCountToken, remOper);
					remTrackPair = new Pair(remCountToken, remWordNode);
				}
				tempList.add(remIndexPair);
				trackList.add(remTrackPair);
				funcList.add(remFunc);
				posList.add(remPos);
				wordList.add(remWord);
			}
		}
		listArray[0] = tempList;
		listArray[1] = trackList;
		listArray[2] = funcList;
		listArray[3] = posList;
		listArray[4] = wordList;
		return listArray;
		// for(int i=0;i<tempList.size();i++){
		// System.out.println("loopRemList tempList:
		// "+tempList.get(i).toString());
		// System.out.println("loopRemList trackList:
		// "+trackList.get(i).toString());
		// }
	}

	// loopMarkableKidNode will not consider lower markableNode since lower
	// markableNode will be
	// looped in preorderTraverse
	public List[] loopMarkableKidNode(Node markableNode, String markableId, String refId, String headWord, List<Pair> tempList, List<Pair> trackList, List<String> funcList, List<String> posList, List<String> wordList, Node corefNode, String inputOper) {
		// System.out.println(markableNode.getAttributes().getNamedItem(ID).getNodeValue());
		// if(markableNode.getAttributes().getNamedItem(ID).getNodeValue().equals("265")){
		// System.out.println("start the debugging: ");
		// }
		NodeList kidList = markableNode.getChildNodes();
		// the following line is not used at all in this function
		// List<Node> markableKidList = this.getChildrenByTagName(markableNode,
		// MARKABLE);
		// if(markableKidList.size()>0){
		// int curMarkIndex=markIdList.indexOf(markableId);
		// int newIndexCurMark = curMarkIndex+markableKidList.size();
		// markIdList.remove(markableId);
		// markIdList.add(newIndexCurMark,markableId);
		// }
		List[] listArray = new List[8];
		String oper = "";
		for (int k = 0; k < kidList.getLength(); k++) {
			// we can know the order information based the inex k, but how to
			// incorperate it into
			// countoken, need to have a good design.
			Node wordNode = kidList.item(k);
			String word = wordNode.getTextContent().toLowerCase();
			String tokenId = "";
			String func = "";
			String pos = "";
			Node funcNode = null;
			Node posNode = null;
			// word equals headword, then the word should be the head of the
			// markable
			if (word.equals(headWord) && !word.isEmpty()) {
				tokenId = wordNode.getAttributes().getNamedItem(ID).getNodeValue();
				funcNode = wordNode.getAttributes().getNamedItem(FUNC);
				// System.out.println(funcNode.getNodeValue());
				if (funcNode != null) {
					func = funcNode.getNodeValue();
				}
				posNode = wordNode.getAttributes().getNamedItem(POS);
				// System.out.println(posNode.getNodeValue());
				if (posNode != null) {
					pos = posNode.getNodeValue();
				}

				// System.out.println("word: "+word+" tokenId in loop methods:
				// "+tokenId);
				int countToken = 0;
				countToken = tokenList.indexOf(tokenId);
				markableHeadWordHM.put(markableId, String.valueOf(countToken));
				headWordMarkableIdHM.put(String.valueOf(countToken), markableId);
				// if countToken is larger than indStructList size, it implies
				// that there are other words or
				// other markable nodes between the head word and the markable
				// node
				// or there are other nodes which are not processed yet.
				if (countToken > indStructList.size()) {
					// String kidNodeName = this.extractInfor(wordNode);
					// System.out.println(countToken+" oper: "+OP+" countToken
					// when size is larger than indStructlist");
					// tempList.size> implies that there are items which are not
					// procesed left from upper markable node, now,
					// it should be processed and meanwhile must be emptied
					// before the tempList can be used to add others!!
					if (tempList != null && tempList.size() > 0) {
						// System.out.println("countToken and oper in
						// loopMakralbeKidNode when word is the head before:
						// "+countToken+" "+oper+" tokenId: "+tokenId+" tempList
						// size and content:"+tempList.size()+"
						// "+tempList.get(0));
						// processTempList will handle all tempList
						// System.out.println("tempList in headword:
						// "+tempList.size()+" "+tempList.get(0));
						// System.out.println("trackList in headword:
						// "+trackList.size()+" "+trackList.get(0));
						List[] temListArray = this.processTempList(tempList, trackList, funcList, posList, wordList, markableId);
						tempList = temListArray[0];
						trackList = temListArray[1];
						funcList = temListArray[2];
						posList = temListArray[3];
						wordList = temListArray[4];
						// System.out.println(tempList.size()+"
						// "+tempList.get(0));

						// //after tempList is done, then go back to the present
						// word node.
						// tempList = new ArrayList<Pair>();
						// trackList = new ArrayList<Pair>();

						if (countToken > indStructList.size()) {
							// I see, if countToken is larger than
							// indStructList.size
							// this markable node will be added to tempList and
							// will be processed later.
							if (inputOper.isEmpty()) {
								if (corefNode != null) {
									oper = OLD;
								} else {
									oper = NEW;
								}
							} else {
								oper = inputOper;
							}

							Pair indexOpPair = new Pair(countToken, oper);
							// System.out.println("in loopMakralbeKidNode when
							// word is the head: "+countToken+oper);
							// Pair trackPair = new Pair(refId,wordNode);
							Pair trackPair = new Pair(countToken, new Pair(refId, wordNode));
							tempList.add(indexOpPair);
							trackList.add(trackPair);
							funcList.add(func);
							posList.add(pos);
							wordList.add(word);

							listArray = this.loopRemList(kidList, refId, headWord, oper, wordNode, markableId, k, tempList, trackList, funcList, posList, wordList);
							tempList = temListArray[0];
							trackList = temListArray[1];
							funcList = temListArray[2];
							posList = temListArray[3];
							wordList = temListArray[4];
							// System.out.println("countToken and oper in
							// loopMakralbeKidNode when word is the head after:
							// "+countToken+" countToken larger than indStrList
							// size: "+indStructList.size()+oper+" tokenId:
							// "+tokenId+" tempList size and
							// content:"+tempList.size()+" "+tempList.get(0));
							break;
						} else {
							// countToken may be equal to the size of
							// indStructList or smaller.
							if (inputOper.isEmpty()) {
								// String tokenWord =(String)
								// wordPosPairList.get(countToken-1).getFirst();
								// String tokenPos =(String)
								// wordPosPairList.get(countToken-1).getSecond();
								// System.out.println("tokenWord and tokenPos in
								// loopMarkableKidNodeList inputOper is empty:
								// "+tokenWord+" "+tokenPos+" "+headWord+"
								// "+word);
								if (corefNode != null && pronounList.contains(headWord)) {
									if (reflexiveList.contains(headWord)) {
										pos = REFL;
									} else if (pronounList.contains(headWord)) {
										pos = PERPRO;
									}
									if (!srcMarkableHM.containsKey(refId)) {
										List<String> mentionList = new ArrayList<String>();
										mentionList.add(markableId);
										srcMarkableHM.put(refId, mentionList);
										this.operIsOld(countToken, refId, func, pos, headWord);
									} else {
										List<String> mentionList = srcMarkableHM.get(refId);
										String contiguousMarkId = mentionList.get(mentionList.size() - 1);
										mentionList.add(markableId);
										srcMarkableHM.put(refId, mentionList);
										this.operIsOld(countToken, contiguousMarkId, func, pos, headWord);
									}
								} else if (corefNode != null && !pronounList.contains(headWord)) {
									if (!srcMarkableHM.containsKey(refId)) {
										List<String> mentionList = new ArrayList<String>();
										mentionList.add(markableId);
										srcMarkableHM.put(refId, mentionList);
									} else {
										List<String> mentionList = srcMarkableHM.get(refId);
										mentionList.add(markableId);
										srcMarkableHM.put(refId, mentionList);
									}
									this.operIsNew(countToken, refId, func, pos, headWord);
								} else {
									this.operIsNew(countToken, markableId, func, pos, headWord);
								}
							} else {
								if (inputOper == NEW) {
									this.operIsNew(countToken, markableId, func, pos, headWord);
								} else if (inputOper == OLD && pronounList.contains(headWord)) {
									if (reflexiveList.contains(headWord)) {
										pos = REFL;
									} else if (pronounList.contains(headWord)) {
										pos = PERPRO;
									}
									if (!srcMarkableHM.containsKey(refId)) {
										List<String> mentionList = new ArrayList<String>();
										mentionList.add(markableId);
										srcMarkableHM.put(refId, mentionList);
										this.operIsOld(countToken, refId, func, pos, headWord);
									} else {
										List<String> mentionList = srcMarkableHM.get(refId);
										String contiguousMarkId = mentionList.get(mentionList.size() - 1);
										mentionList.add(markableId);
										srcMarkableHM.put(contiguousMarkId, mentionList);
									}
								} else if (inputOper == OLD && !pronounList.contains(headWord)) {
									if (!srcMarkableHM.containsKey(refId)) {
										List<String> mentionList = new ArrayList<String>();
										mentionList.add(markableId);
										srcMarkableHM.put(refId, mentionList);
									} else {
										List<String> mentionList = srcMarkableHM.get(refId);
										mentionList.add(markableId);
										srcMarkableHM.put(refId, mentionList);
									}
									this.operIsNew(countToken, markableId, func, pos, headWord);
								}
							}

						}

						// System.out.println("value of K: "+k+" countToken,
						// markable high 1, coref=true and op=old:
						// "+countToken+" word: "+wordNode.getTextContent()+"
						// markableID "+markableId);
					} else {
						// since the word is the headword, we should know if the
						// oper is new or old based on corefNode
						// System.out.println("countToken and oper in
						// loopMakralbeKidNode when word is the head:
						// "+countToken+oper);
						if (inputOper.isEmpty()) {
							if (corefNode != null) {
								oper = OLD;
							} else {
								oper = NEW;
							}
						} else {
							oper = inputOper;
						}
						Pair indexOpPair = new Pair(countToken, oper);
						// System.out.println("in loopMakralbeKidNode:
						// "+countToken+oper);
						// Pair trackPair = new Pair(refId,wordNode);
						Pair trackPair = new Pair(countToken, new Pair(refId, wordNode));
						if (tempList == null) {
							tempList = new ArrayList<Pair>();
							trackList = new ArrayList<Pair>();
							funcList = new ArrayList<String>();
							posList = new ArrayList<String>();
							wordList = new ArrayList<String>();
							tempList.add(indexOpPair);
							trackList.add(trackPair);
							funcList.add(func);
							posList.add(pos);
							wordList.add(word);
						} else {
							tempList.add(indexOpPair);
							trackList.add(trackPair);
							funcList.add(func);
							posList.add(pos);
							wordList.add(word);
						}

						// this.loopRemList(kidList, k, tempList, trackList);
						// this.loopRemList(kidList, refId, headWord,
						// markableId, k, tempList, trackList);
						listArray = this.loopRemList(kidList, refId, headWord, oper, wordNode, markableId, k, tempList, trackList, funcList, posList, wordList);
						tempList = listArray[0];
						trackList = listArray[1];
						funcList = listArray[2];
						posList = listArray[3];
						wordList = listArray[4];
						// System.out.println("countToken and oper in
						// loopMakralbeKidNode when word is the head and the
						// tempList is nulll: "+countToken+oper+" tokenId:
						// "+tokenId+" tempList size and
						// content:"+tempList.size()+" "+tempList.get(0));
						break;
					}
					// System.out.println("value of K in loopMarkableKidNode
					// 1st: "+k+" countToken: "+countToken+" word:
					// "+wordNode.getTextContent()+" markableID "+markableId);
				} else {
					// System.out.println("node value infor in
					// loopMakralbleKidNode: "+wordNode.getTextContent()+" what
					// is it?? "+"
					// "+wordNode.getAttributes().getNamedItem(ID).getNodeValue());
					// String kidNodeName = this.extractInfor(wordNode);
					// countToken = this.operIsCopy(countToken,indStructList);
					// markableHeadWordHM.put(markableId,String.valueOf(countToken));
					// countToken = this.operIsNew(countToken,
					// markableId,replaceRefIndexHM,indStructList);
					if (inputOper.isEmpty()) {
						if (corefNode != null && pronounList.contains(headWord)) {
							if (reflexiveList.contains(headWord)) {
								pos = REFL;
							} else if (pronounList.contains(headWord)) {
								pos = PERPRO;
							}
							if (!srcMarkableHM.containsKey(refId)) {
								List<String> mentionList = new ArrayList<String>();
								mentionList.add(markableId);
								srcMarkableHM.put(refId, mentionList);
								this.operIsOld(countToken, refId, func, pos, headWord);
							} else {
								List<String> mentionList = srcMarkableHM.get(refId);
								String contiguousMarkId = mentionList.get(mentionList.size() - 1);
								mentionList.add(markableId);
								srcMarkableHM.put(refId, mentionList);
								this.operIsOld(countToken, contiguousMarkId, func, pos, headWord);
							}
						}
						// this line seems to be unnecessary, but not bad to
						// have a double check
						else if (corefNode != null && !pronounList.contains(headWord)) {
							if (!srcMarkableHM.containsKey(refId)) {
								List<String> mentionList = new ArrayList<String>();
								mentionList.add(markableId);
								srcMarkableHM.put(refId, mentionList);
							} else {
								List<String> mentionList = srcMarkableHM.get(refId);
								mentionList.add(markableId);
								srcMarkableHM.put(refId, mentionList);
							}
							this.operIsNew(countToken, markableId, func, pos, headWord);
						} else {
							this.operIsNew(countToken, markableId, func, pos, headWord);
						}
					} else {
						if (inputOper == NEW) {
							this.operIsNew(countToken, markableId, func, pos, headWord);
						} else if (inputOper == OLD && pronounList.contains(headWord)) {
							if (reflexiveList.contains(headWord)) {
								pos = REFL;
							} else if (pronounList.contains(headWord)) {
								pos = PERPRO;
							}
							if (!srcMarkableHM.containsKey(refId)) {
								List<String> mentionList = new ArrayList<String>();
								mentionList.add(markableId);
								srcMarkableHM.put(refId, mentionList);
								this.operIsOld(countToken, refId, func, pos, headWord);
							} else {
								List<String> mentionList = srcMarkableHM.get(refId);
								String contiguousMarkId = mentionList.get(mentionList.size() - 1);
								mentionList.add(markableId);
								srcMarkableHM.put(contiguousMarkId, mentionList);
							}
						} else if (inputOper == OLD && !pronounList.contains(headWord)) {
							if (!srcMarkableHM.containsKey(refId)) {
								List<String> mentionList = new ArrayList<String>();
								mentionList.add(markableId);
								srcMarkableHM.put(refId, mentionList);
							} else {
								List<String> mentionList = srcMarkableHM.get(refId);
								mentionList.add(markableId);
								srcMarkableHM.put(refId, mentionList);
							}
							this.operIsNew(countToken, markableId, func, pos, headWord);
						}
					}
					// System.out.println("value of K: "+k+" countToken,
					// markable high 1, coref=true and op=old: "+countToken+"
					// word: "+wordNode.getTextContent()+" markableID
					// "+markableId);
				}
				// markableHeadWordHM.put(markableId,String.valueOf(countToken));
				// //System.out.println("value of K: "+k+" countToken, markable
				// high 1, coref=true and op=old: "+countToken+" word:
				// "+wordNode.getTextContent()+" markableID "+markableId);
				// countToken = this.operIsOld(countToken,
				// refId,markableHeadWordHM,replaceRefIndexHM,indStructList);
				// System.out.println("value of K in loopMarkableKidNode 2nd:
				// "+k+" countToken: "+countToken+" word:
				// "+wordNode.getTextContent()+" markableID "+markableId);
				// System.out.println("wordNode ID in loopMarkableKidNode:
				// "+tokenId+" countToken of this wordNode:"+countToken);
			} else if (wordNode.getNodeName().equals(WORD) || wordNode.getNodeName().equals(PUNC)) {
				oper = COPY;
				// System.out.println("wordNode:"+wordNode.getTextContent());
				if (wordNode.getNodeName().equals(WORD)) {
					tokenId = wordNode.getAttributes().getNamedItem(ID).getNodeValue();
					funcNode = wordNode.getAttributes().getNamedItem(FUNC);
					if (funcNode != null) {
						func = wordNode.getAttributes().getNamedItem(FUNC).getNodeValue();
					} else {
						func = wordNode.getTextContent();
					}
					posNode = wordNode.getAttributes().getNamedItem(POS);
					if (posNode != null) {
						pos = wordNode.getAttributes().getNamedItem(POS).getNodeValue();
					} else {
						pos = wordNode.getTextContent();
					}

				} else if (wordNode.getNodeName().equals(PUNC)) {
					tokenId = this.getPuncTokenId(wordNode);
					func = word;
					pos = word;
				}

				// System.out.println("word: "+word+" tokenId in loop methods:
				// "+tokenId);
				int countToken = 0;
				countToken = tokenList.indexOf(tokenId);
				// markableHeadWordHM.put(markableId,String.valueOf(countToken));
				if (countToken > indStructList.size()) {
					// String kidNodeName = this.extractInfor(wordNode);
					// System.out.println(countToken+" oper: "+OP+" countToken
					// when size is larger than indStructlist");
					if (tempList != null && tempList.size() > 0) {
						// System.out.println("countToken and oper in
						// loopMakralbeKidNode when word is not the head and
						// tempList.size>0 before: "+countToken+" countToken
						// larger than indStrList size:
						// "+indStructList.size()+"indstrList size: "+oper+"
						// tokenId: "+tokenId+" tempList size and
						// content:"+tempList.size()+" "+tempList.get(0));
						// processTempList will handle all tempList
						List[] temListArray = this.processTempList(tempList, trackList, funcList, posList, wordList, markableId);
						tempList = temListArray[0];
						trackList = temListArray[1];
						funcList = temListArray[2];
						posList = temListArray[3];
						wordList = temListArray[4];
						// System.out.println(tempList.size()+"
						// "+tempList.get(0));

						// after tempList is done, then go back to the present
						// word node. should not empty the two lists here now
						// since processTemplist has done that for us.
						// tempList = new ArrayList<Pair>();
						// trackList = new ArrayList<Pair>();
						if (countToken > indStructList.size()) {
							oper = COPY;
							Pair indexOpPair = new Pair(countToken, oper);
							// System.out.println("in loopMakralbeKidNode:
							// "+countToken+"-----------------------"+oper);
							Pair trackPair = new Pair(countToken, new Pair(refId, wordNode));
							// Pair trackPair = new Pair(countToken,new
							// Pair(refId,wordNode));
							tempList.add(indexOpPair);
							trackList.add(trackPair);
							funcList.add(func);
							posList.add(pos);
							wordList.add(word);
							if (inputOper.isEmpty()) {
								if (corefNode != null) {
									oper = OLD;
								} else {
									oper = NEW;
								}
							} else {
								oper = inputOper;
							}
							listArray = this.loopRemList(kidList, refId, headWord, oper, wordNode, markableId, k, tempList, trackList, funcList, posList, wordList);
							tempList = listArray[0];
							trackList = listArray[1];
							funcList = listArray[2];
							posList = listArray[3];
							wordList = listArray[4];
							// System.out.println("countToken and oper in
							// loopMakralbeKidNode when word is not the head and
							// the tempList is not nulll: "+countToken+oper+"
							// tokenId: "+tokenId+" tempList size and
							// content:"+tempList.size()+" "+tempList.get(0));
							break;

						} else {
							this.operIsCopy(countToken,pos,word);
						}
						// System.out.println("value of K: "+k+" countToken,
						// markable high 1, coref=true and op=old:
						// "+countToken+" word: "+wordNode.getTextContent()+"
						// markableID "+markableId);
						// System.out.println("wordNode ID in
						// loopMarkableKidNode:
						// "+wordNode.getAttributes().getNamedItem(ID).getNodeValue()+"
						// wordNode:"+wordNode.getNodeName());
					} else {

						oper = COPY;
						Pair indexOpPair = new Pair(countToken, oper);
						// System.out.println("in loopMakralbeKidNode:
						// "+countToken+" "+oper+" the word not the head:
						// "+wordNode.getTextContent());
						Pair trackPair = new Pair(countToken, new Pair(countToken, wordNode));
						// Pair trackPair = new Pair(countToken,new
						// Pair(refId,wordNode));
						// System.out.println("indexOpPair string in
						// loopMarkableNode:"+indexOpPair.toString());
						if (tempList == null) {
							tempList = new ArrayList<Pair>();
							trackList = new ArrayList<Pair>();
							funcList = new ArrayList<String>();
							posList = new ArrayList<String>();
							wordList = new ArrayList<String>();
							tempList.add(indexOpPair);
							trackList.add(trackPair);
							funcList.add(func);
							posList.add(pos);
							wordList.add(word);
						} else {
							// System.out.println("func: "+func+" pos: "+ pos+"
							// word: "+word);
							tempList.add(indexOpPair);
							trackList.add(trackPair);
							funcList.add(func);
							posList.add(pos);
							wordList.add(word);
						}

						if (inputOper.isEmpty()) {
							if (corefNode != null) {
								oper = OLD;
							} else {
								oper = NEW;
							}
						} else {
							oper = inputOper;
						}
						listArray = this.loopRemList(kidList, refId, headWord, oper, wordNode, markableId, k, tempList, trackList, funcList, posList, wordList);
						tempList = listArray[0];
						trackList = listArray[1];
						funcList = listArray[2];
						posList = listArray[3];
						wordList = listArray[4];
						// System.out.println("countToken and oper in
						// loopMakralbeKidNode when word is not the head and the
						// tempList is nulll: "+countToken+oper+" tokenId:
						// "+tokenId+" tempList size and
						// content:"+tempList.size()+" "+tempList.get(0));

						break;
					}
					// System.out.println("value of K in loopMarkableKidNode
					// 3rd: "+k+" in word not the head: "+countToken+" word:
					// "+wordNode.getTextContent()+" markableID "+markableId);
				} else {
					this.operIsCopy(countToken,pos,word);
				}
				// markableHeadWordHM.put(markableId,String.valueOf(countToken));
				// //System.out.println("value of K: "+k+" countToken, markable
				// high 1, coref=true and op=old: "+countToken+" word:
				// "+wordNode.getTextContent()+" markableID "+markableId);
				// countToken = this.operIsOld(countToken,
				// refId,markableHeadWordHM,replaceRefIndexHM,indStructList);
				// System.out.println("value of K: "+k+" countToken, markable
				// high 1, coref=true and op=old: "+countToken+" word:
				// "+wordNode.getTextContent()+" markableID "+markableId);
				// this.switchCountToken(wordNode, tempList, trackList, oper,
				// markableId, refId);
				// System.out.println("wordNode ID in loopMarkableKidNode:
				// "+tokenId+" countToken:"+countToken);
			}
			// System.out.println("word: "+word+" tokenId in loop methods:
			// "+tokenId);
		}
		listArray[0] = tempList;
		listArray[1] = trackList;
		listArray[2] = funcList;
		listArray[3] = posList;
		listArray[4] = wordList;
		return listArray;

	}

	public List[] processMarkableNode(Node markableNode, List<Pair> tempList, List<Pair> trackList, List<String> funcList, List<String> posList, List<String> wordList, String inputOper, String inputRefId) {
		Node corefNode = this.getFirstChildByTagName(markableNode, COREF);
		List<Node> eleWordList = this.getChildrenByTagName(markableNode, WORD);
//		if(eleWordList.isEmpty()){
//			
//		}
		List<Node> puncList = this.getChildrenByTagName(markableNode, PUNC);
		NamedNodeMap markableMap = markableNode.getAttributes();
		String markableId = markableMap.getNamedItem(ID).getNodeValue();
//		if(markableId.equals("22")){
//			System.out.println("start debugging in loopmarkableNode");
//		}
		String headWord = "";
		String headNode = null;
		String oper = "";
		String refId = "";
		List[] listArray = new List[8];
		Node firstKidMarkable = this.getFirstChildByTagName(markableNode, MARKABLE);
		Node headWordNode = this.findHeadword(eleWordList);
		// I see, the following if called when findHeadWord returns null,
		// that is, it doesn't find the head word, then it uses the first child
		// with WORD tag
		// of the markable node as its head
		if (firstKidMarkable == null && headWordNode == null) {
			headWordNode = this.getFirstChildByTagName(markableNode, WORD);
		}
		if (tempList != null) {
			for (int i = 0; i < tempList.size(); i++) {
				// System.out.println("tempList in
				// processMarkableNode:+_+_+_+_+_+_+_+_ "+tempList.get(i));
			}
		}

		if (!eleWordList.isEmpty()) {
			if (headWordNode != null) {
				headWord = (String) headWordNode.getTextContent().toLowerCase();
			}
		}

		// System.out.println("processMarkableNode inputOper: "+
		// inputOper+" inputRefId: "+inputRefId);
		// System.out.println("node:
		// "+markableNode.getAttributes().getNamedItem(ID)+" headword:
		// "+headWord);
		// NamedNodeMap markableMap = markableNode.getAttributes();
		// String markableId = markableMap.getNamedItem(ID).getNodeValue();
		// List<Pair> tempList=new ArrayList<Pair>();
		// that is what I should start from here, COREF helps us to find
		// previous MARKABLE tag
		// and find its headword.
		if (corefNode != null) {
			NamedNodeMap corefMap = corefNode.getAttributes();
			refId = corefMap.getNamedItem("SRC").getNodeValue();
			oper = OLD;
		} else {
			oper = NEW;
		}

		if (inputOper.isEmpty() && inputRefId.isEmpty()) {
			// find the head word, in this case, a new markable comes up,
			// but if the markable is composed of a few words.we should find the
			// head
			// word as the representation of the markable. In Reuters, we can
			// find the head word by
			// the attribute DEP. what I need here is the countIndex of the head
			// word.
			// System.out.println("inputRefId and inputOper in the first case
			// processMarkableNode: inputRefId "+inputRefId+" inputOper
			// "+inputOper);
			// NamedNodeMap corefMap = corefNode.getAttributes();
			// refId = corefMap.getNamedItem("SRC").getNodeValue();
			// System.out.println("value of SRC:
			// "+corefMap.getNamedItem("SRC").getNodeValue());
			// String headWord = this.findHeadword(eleWordList);
			// for (int k = 0; k < eleWordList.size(); k++) {
			if (headWord == "") {
				// oper = OLD;
				if (tempList != null && tempList.size() > 0) {
					// there is a case where items in tempList are larger than
					// this markableNode
					// if this.processTempList will be waste of time.
					List[] temListArray = this.processTempList(tempList, trackList, funcList, posList, wordList, markableId);
					tempList = temListArray[0];
					trackList = temListArray[1];
					funcList = temListArray[2];
					posList = temListArray[3];
					wordList = temListArray[4];
					// tempList=new ArrayList<Pair>();
					// trackList=new ArrayList<Pair>();
				}
				//dingcheng notes Mar 27, 10
				// if the head word is not found yet, then uses the head word of
				// its markable kid as the head word
				//I know why, since my function makeDumHead has done everything needed, including
				//operIsNew or operIsOld. So, don't need to return anything for further processing
				//so, what I need to do for markableId fail to find a head from its first kid Makrable is
				//I should go lower level of its kidMarkable and find the grandKid head word in makeDumHead
				this.makeDumHead(oper, markableNode, refId);
				if (tempList != null && tempList.size() > 0) {
					for (int i = 0; i < tempList.size(); i++) {
						Pair ithTempPair = tempList.get(i);
						Pair ithTrackPair = trackList.get(i);
						int index = (Integer) ithTempPair.getFirst() + 1;
						String ithOper = (String) ithTempPair.getSecond();
						Object ithTrack = ithTrackPair.getSecond();
						Pair ithNewPair = new Pair(index, ithOper);
						Pair ithTrackNewPair = new Pair(index, ithTrack);
						tempList.set(i, ithNewPair);
						trackList.set(i, ithTrackNewPair);
						// tempList.add(i,ithNewPair);
						// trackList.add(i,ithTrackNewPair);
					}
				}
			}
			listArray = this.loopMarkableKidNode(markableNode, markableId, refId, headWord, tempList, trackList, funcList, posList, wordList, corefNode, inputOper);
			// tempList=listArray[0];
			// trackList=listArray[1];
			// funcList=listArray[2];
		} else if (inputOper.isEmpty() && !inputRefId.isEmpty()) {
			// this case is from processTempNodeList
			// in this case, inputOper is empty, implying that it is OLD while
			// inputRefId is not empty, implying that
			// System.out.println("inputRefId and inputOper in the second case
			// processMarkableNode: inputRefId "+inputRefId+" inputOper
			// "+inputOper);
			if (headWord == "") {
				// oper = OLD;
				if (tempList.size() > 0) {
					List[] temListArray = this.processTempList(tempList, trackList, funcList, posList, wordList, markableId);
					tempList = temListArray[0];
					trackList = temListArray[1];
					funcList = temListArray[2];
					posList = temListArray[3];
					wordList = temListArray[4];
					// tempList=new ArrayList<Pair>();
					// trackList=new ArrayList<Pair>();
				}
				this.makeDumHead(oper, markableNode, inputRefId);
			}
			listArray = this.loopMarkableKidNode(markableNode, markableId, inputRefId, headWord, tempList, trackList, funcList, posList, wordList, corefNode, inputOper);
		} else if (!inputOper.isEmpty() && (inputRefId == null || inputRefId.isEmpty())) {
			// in this case, inputOper should be new, but inputRefId is empty,
			// it is the case of catophora that
			// the present mention refer to future mention while future mention
			// doesn't refer back to some previous mention
			// so, I should store it into markableSrcHM. But the direction
			// should be the opposition since when processing
			// the future mention, it will regard the present mention as
			// coreferring mention, it needs to know what Id the present mention
			// is
			// System.out.println("inputRefId and inputOper in the third case
			// processMarkableNode: inputRefId "+inputRefId+" inputOper
			// "+inputOper);
			NamedNodeMap corefMap = null;
			if (corefNode != null) {
				refId = corefNode.getAttributes().getNamedItem("SRC").getNodeValue();
			}
			// System.out.println("value of SRC:
			// "+corefMap.getNamedItem("SRC").getNodeValue());
			// String headWord = this.findHeadword(eleWordList);
			// for (int k = 0; k < eleWordList.size(); k++) {
			if (headWord == "") {
				// oper = OLD;
				if (tempList.size() > 0) {
					List[] temListArray = this.processTempList(tempList, trackList, funcList, posList, wordList, markableId);
					tempList = temListArray[0];
					trackList = temListArray[1];
					funcList = temListArray[2];
					posList = temListArray[3];
					wordList = temListArray[4];
					// tempList=new ArrayList<Pair>();
					// trackList=new ArrayList<Pair>();
				}
				this.makeDumHead(oper, markableNode, refId);
			}
			listArray = this.loopMarkableKidNode(markableNode, markableId, refId, headWord, tempList, trackList, funcList, posList, wordList, corefNode, inputOper);
		} else if (!inputOper.isEmpty() && !inputRefId.isEmpty()) {
			// in this case, it is the counterpart of the above case, inputOper
			// should be OLD now, inputRefId should be
			// the markableId of the above case as well.
			// System.out.println("inputRefId and inputOper in the fourth case
			// processMarkableNode: inputRefId "+inputRefId+" inputOper
			// "+inputOper);
			if (headWord == "") {
				// oper = OLD;
				if (tempList != null && tempList.size() > 0) {
					List[] temListArray = this.processTempList(tempList, trackList, funcList, posList, wordList, markableId);
					tempList = temListArray[0];
					trackList = temListArray[1];
					funcList = temListArray[2];
					posList = temListArray[3];
					wordList = temListArray[4];
					// tempList=new ArrayList<Pair>();
					// trackList=new ArrayList<Pair>();
				}
				this.makeDumHead(oper, markableNode, inputRefId);
			}
			listArray = this.loopMarkableKidNode(markableNode, markableId, inputRefId, headWord, tempList, trackList, funcList, posList, wordList, corefNode, inputOper);
		}

		// } else {
		// if(headWord==""){
		// oper = NEW;
		// if(tempList.size()>0){
		// List[] temListArray=this.processTempList(tempList,
		// trackList,funcList, markableId);
		// tempList=temListArray[0];
		// trackList=temListArray[1];
		// funcList=temListArray[2];
		// }
		// this.makeDumHead(oper, markableNode, refId);
		// }
		// System.out.println("what is the headword from else of
		// processMarkableNode: "+headWord+" node:
		// "+markableNode.getTextContent());
		// listArray=this.loopMarkableKidNode(markableNode,markableId, refId,
		// headWord, tempList, trackList,funcList,corefNode,inputOper);
		// }
		// tempList=listArray[0];
		// trackList=listArray[1];
		// funcList=listArray[2];
		//		
		// if(markableNode.getNextSibling()!=null &&
		// markableNode.getParentNode().getNodeName().equals(SENTENCE)){
		// listArray = new List[6];
		// }
		return listArray;
	}

	public void printIndStructList() {
		int subtractor = 0;
		int countOld = 0;
		int countNew = 0;
		for (int i = 0; i < indStructList.size(); i++) {
			FeaStruct indStruct = indStructList.get(i);
			//wordPosPairList and wordPosPairList2 have the same result though I build the two lists from different apporaches 
			//this can be used as a test to see if there are some bugs in the code. Dingcheng notes, Mar 27, 10
			System.out.println(i + " " + indStruct.getFea() + " " + indStruct.getFeaP() + " " + indStruct.getOper() + " " + wordPosPairList.get(i)+" "+wordPosPairList2.get(i));
			if (indStruct.getOper().equals(OLD)) {
				countOld++;
			}
			if (indStruct.getOper().equals(NEW)) {
				countNew++;
			}

		}
		System.out.println("countOld: " + countOld + " countNew: " + countNew + " sum: " + (countOld + countNew));
	}

	public void printSynStructList(PrintWriter pwSynStruct) {
		for (int i = 0; i < synStructList.size(); i++) {
			FeaStruct synStruct = synStructList.get(i);
			String feaP = synStruct.getFeaP();
			String curFea = synStruct.getFea();
			String feaPVal = "";
			String curFeaVal = "";
			if (feaP.contains(delim)) {
				feaPVal = feaP.substring(feaP.lastIndexOf(delim) + 1);
			} else {
				feaPVal = feaP;
			}
			if (curFea.contains(delim)) {
				// System.out.println("curFeaVal: "+curFea);
				curFeaVal = curFea.substring(curFea.lastIndexOf(delim) + 1);
			} else {
				curFeaVal = curFea;
			}

			String oper = synStruct.getOper();

			if (!oper.equals(COPY)) {
				pwSynStruct.println("Syn " + synStruct.getOper() + " : " + curFeaVal);
			}
		}
	}

	public void printGenStructList(PrintWriter pwGenStruct) {
		for (int i = 0; i < genStructList.size(); i++) {
			FeaStruct genStruct = genStructList.get(i);
			if (genStruct.getOper().equals(NEW)) {
				int last_ul = genStruct.getFea().lastIndexOf("_");
				if (last_ul > 0) {
					pwGenStruct.println("Gen " + genStruct.getOper() + " : " + genStruct.getFea().substring(last_ul + 1));
				} else {
					pwGenStruct.println("Gen " + genStruct.getOper() + " : " + genStruct.getFea());
				}

			}
			// pwGenStruct.println("gen: "+genStruct.getOper()+"
			// "+genStruct.getFeaP()+" : "+genStruct.getFea());
		}
	}

	public void printNumStructList(PrintWriter pwNumStruct) {
		for (int i = 0; i < numStructList.size(); i++) {
			FeaStruct numStruct = numStructList.get(i);
			if (numStruct.getOper().equals(NEW)) {
				int last_ul = numStruct.getFea().lastIndexOf("_");
				if (last_ul > 0) {
					pwNumStruct.println("Num " + numStruct.getOper() + " : " + numStruct.getFea().substring(last_ul + 1));
				} else {
					pwNumStruct.println("Num " + numStruct.getOper() + " : " + numStruct.getFea());
				}

			}
			// pwNumStruct.println("num: "+numStruct.getOper()+"
			// "+numStruct.getFeaP()+" : "+numStruct.getFea());
		}
	}

	public int operIsNew(int count, String markableId, String func, String pos, String word) {
		// System.out.println(" func in operIsNew: "+func+" pos: "+pos+" word:
		// "+word);
		wordPosPairList2.add(new Pair(word,pos));
		String oper = NEW;
		String ind = "-";
		String indP = "-";
		String[] indPArray = null;
		// Pair markableCountPair = new Pair(count,markableId);
		List<String> indPArrayList = new ArrayList<String>();
		// syntactic features:
		String syn = "-";
		String synP = "-";
		String[] synPArray = null;
		// Pair markableCountPair = new Pair(count,markableId);
		List<String> synPArrayList = new ArrayList<String>();
		// this is the first state when indStructList is empty, then, curInd is
		// the present count
		// and the indP should be "-"
		if (indStructList.isEmpty()) {
			// ind = tokenId;
			ind = String.valueOf(count);
			FeaStruct indStruct = new FeaStruct(NEW, indP, ind);
			// System.out.println("the first mention count: "+count+" "+oper+"
			// "+ind+" "+indP);
			indStructList.add(indStruct);

			// syntactic features
			FeaStruct synStruct = new FeaStruct(NEW, synP, syn);
			synStructList.add(synStruct);
			// System.out.println("the first mention count: "+count+" "+oper+"
			// "+synP+" "+syn);
			// this is first, ind = count and it is in the first position
			// Whereas, if the begining of the news story is not a new entity,
			// this if will be skipped
			replaceRefIndexHM.put(ind, 0);
			Pair indePair = new Pair(count, 0);
			replaceRefIndexList.add(0, indePair);
			// replaceRefIndexHM.put(markableCountPair, 0);
			// replaceRefIndexHM.put(tokenId, 0);
			// ////System.out.println("new state: "+OP+" "+tokenId+" "+indP+"
			// "+ind+" indStruct Size: "+
			// indStructList.size());
		} else {
			// //////System.out.println("new state: "+
			// "coref is "+coref+" indStructList size: "+indStructList.size());
			// indP = indStrList.get(Integer.valueOf(tokenId)-1).getFea();
			indP = indStructList.get(count - 1).getFea();
			indPArray = indP.split("_");
			// since Arrays.aslist(array) cannot change size, not so good.
			// so, I changed it to an ArrayList.
			// indPArrayList = Arrays.asList(indPArray);
			for (String indPArrayStr : indPArray) {
				indPArrayList.add(indPArrayStr);
			}

			// syntactic features:
			synP = synStructList.get(count - 1).getFea();
			synPArray = synP.split("_");
			for (String synPArrayStr : synPArray) {
				synPArrayList.add(synPArrayStr);
			}
			// //////System.out.println("indPArrayList in new state:
			// "+indPArrayList.size());

			// if size of indPArrayList is equal to MAXNUM, cut the first item
			// off.
			if (indPArrayList.size() == MAXNUM) {
				// now, I need to change the related values of the
				// replaceRefIndexHM
				// it should be straightforward since for each cutoff tokenId, I
				// will change its
				// value, i.e. replaceIndex to 0+MAXNUM and the old one which
				// has value MAXNUM will be changed
				// MAXNUM+1 and all of those which values are larger than MAXNUM
				// will be added one
				// sequentially, meanwhile, those with values which are smaller
				// than MAXNUM will be deducted
				// one sequentially
				// replaceRefIndexHM.get(firstStr);
				Iterator<String> it = replaceRefIndexHM.keySet().iterator();
				// Iterator<Pair> it = replaceRefIndexHM.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					// Pair key = it.next();
					int val = replaceRefIndexHM.get(key);
					if (val == 0) {
						val = val + MAXNUM;
						Pair valKey = new Pair(Integer.valueOf(key), val);
						replaceRefIndexList.add(valKey);
						replaceRefIndexHM.put(key, val);

					} else if (val > 0 && val < MAXNUM) {
						val = val - 1;
						replaceRefIndexHM.put(key, val);
						Pair valKey = new Pair(Integer.valueOf(key), val);
						replaceRefIndexList.add(valKey);
					} else {
						val = val + 1;
						replaceRefIndexHM.put(key, val);
						Pair valKey = new Pair(Integer.valueOf(key), val);
						replaceRefIndexList.add(valKey);
					}
				}

				int indexFirstUndline = indP.indexOf("_");
				// the following substring method, +1 is very important. If not,
				// split will
				// not regard the first "_" as splitter, thus, the size will be
				// 9, then, it will
				// enter into else, the above check is meaningless then.
				String indPSub = indP.substring(indexFirstUndline + 1);
				// ind = indPSub +"_"+ tokenId;
				ind = indPSub + "_" + count;
				FeaStruct indStruct = new FeaStruct(NEW, indP, ind);
				indStructList.add(indStruct);
				// System.out.println("new state more than 8 count: "+count+"
				// "+oper+" "+indP+" "+ind+" indStruct Size: "+
				// indStructList.size());

				// syntactic features
				int synFirstUndline = synP.indexOf("_");
				String synPSub = synP.substring(synFirstUndline + 1);
				// ind = indPSub +"_"+ tokenId;
				syn = synPSub + "_" + func;
				FeaStruct synStruct = new FeaStruct(NEW, synP, syn);
				synStructList.add(synStruct);
				// System.out.println("new state more than 8 count: "+count+"
				// "+oper+" "+synP+" "+syn+" synStruct Size: "+
				// synStructList.size());
				// put the new added tokenId to MAXNUM-1, that is, it is in the
				// last position of ind
				// replaceRefIndexHM.put(tokenId, MAXNUM-1);
				replaceRefIndexHM.put(String.valueOf(count), MAXNUM - 1);
				Pair valKey = new Pair(count, MAXNUM - 1);
				replaceRefIndexList.add(valKey);
				Collections.sort(replaceRefIndexList);
				// replaceRefIndexHM.put(markableCountPair, MAXNUM-1);
				// System.out.println("new state more than 8: "+oper+" "+indP+"
				// "+ind+" indStruct Size: "+
				// indStrList.size());

			}
			// size is fewer than 8
			else {
				// //////System.out.println("how do you escape the check in the
				// first if????");
				// ind = indP +"_"+ tokenId;
				if (indP == "-") {
					ind = String.valueOf(count);
				} else {
					ind = indP + "_" + count;
				}

				FeaStruct indStruct = new FeaStruct(NEW, indP, ind);
				indStructList.add(indStruct);
				// System.out.println("new state fewer than 8 count: "+count+"
				// "+oper+" "+indP+" "+ind+" indStruct Size: "+
				// indStructList.size());

				// syntactic features;
				if (synP == "-") {
					syn = func;
				} else {
					syn = synP + "_" + func;
				}

				FeaStruct synStruct = new FeaStruct(NEW, synP, syn);
				synStructList.add(synStruct);

				// System.out.println("new state fewer than 8 count: "+count+"
				// "+oper+" "+synP+" "+syn+" synStruct Size: "+
				// synStructList.size());
				// replaceRefIndexHM.put(tokenId,indPArrayList.size());
				if (indP == start) {
					replaceRefIndexHM.put(String.valueOf(count), indPArrayList.size() - 1);
					Pair valKey = new Pair(count, indPArrayList.size() - 1);
					replaceRefIndexList.add(valKey);
				} else {
					replaceRefIndexHM.put(String.valueOf(count), indPArrayList.size());
					Pair valKey = new Pair(count, indPArrayList.size() - 1);
					replaceRefIndexList.add(valKey);
				}
				// replaceRefIndexHM.put(markableCountPair,
				// indPArrayList.size());
				// System.out.println("new state fewer than 8: "+" indPArrayList
				// size: "+indPArrayList.size()+" "+indP+" "+ind+" indStruct
				// Size: "+
				// indStrList.size());
			}
		}
		// count++;
		return count;
	}

	public int operIsOld(int count, String refId, String func, String pos, String word) {
		String oper = "";
		// //////System.out.println(Integer.valueOf(value));
		// indP = indStructList.get(Integer.valueOf(tokenId)-1).getFea();
		// for old, I should go back from SCR given by the present COREF tag to
		// that MARKABLE tag.
		// then I need to find that headword and its index. So, it is a little
		// bit complex here.
		// System.out.println("in operIsOld func: "+func+" pos: "+pos+" word:
		// "+word);
		wordPosPairList2.add(new Pair(word,pos));

		String ind = "";
		String indP = "";
		String[] indPArray = null;

		String syn = "";
		String synP = "";
		String[] synPArray = null;

		List<String> indPArrayList = new ArrayList<String>();
		List<String> synPArrayList = new ArrayList<String>();
		List<String> genPArrayList = new ArrayList<String>();
		List<String> numPArrayList = new ArrayList<String>();

		// System.out.println("count in operIsOld: "+count);
		indP = indStructList.get(count - 1).getFea();
		indPArray = indP.split("_");

		synP = synStructList.get(count - 1).getFea();
		synPArray = synP.split("_");

		// indPArrayList = Arrays.asList(indPArray);
		// since Arrays.aslist(array) cannot change size, not so good.
		// so, I changed it to an ArrayList.
		// indPArrayList = Arrays.asList(indPArray);
		for (String indPArrayStr : indPArray) {
			indPArrayList.add(indPArrayStr);
		}

		for (String synPArrayStr : synPArray) {
			synPArrayList.add(synPArrayStr);
		}

		String replaceStr = markableHeadWordHM.get(refId);

		// if(replaceStr==null){
		// markableHeadWordHM.put(refId, "incoming");
		// }

		// System.out.println("refID: "+refId+" replaceStr: "+replaceStr);
		int replaceIndex = -1;
		if (indPArrayList.contains(replaceStr)) {
			// the following two lines should be the same thing since
			// replaceRefIndexHM always keep track of indPArrayList
			replaceIndex = indPArrayList.indexOf(replaceStr);
			// replaceIndex = replaceRefIndexHM.get(replaceStr);
			// replaceIndexList.add(replaceIndex);
			// indPArrayList.set(replaceIndex,tokenId);
			// replaceRefIndexHM.put(replaceStr, replaceIndex);
			Iterator<String> refIndexHMIter = replaceRefIndexHM.keySet().iterator();
			while (refIndexHMIter.hasNext()) {
				String strRefindex = refIndexHMIter.next();
				Integer valueRefIndex = replaceRefIndexHM.get(strRefindex);
				if (valueRefIndex > replaceIndex && valueRefIndex < indPArrayList.size()) {
					valueRefIndex--;
				} else if (valueRefIndex == replaceIndex) {
					// when valueRefIndex = replaceIndex, i.e. the one will be
					// replaced
					// then, it is equal to indPArrayList.size-1;
					valueRefIndex = indPArrayList.size() - 1;
				}
				// else,i.e when valueRefIndex<replaceIndex or
				// valueRefIndex>=MaxNum,
				// no change at all
				replaceRefIndexHM.put(strRefindex, valueRefIndex);
				Pair valKey = new Pair(Integer.valueOf(strRefindex), valueRefIndex);
				replaceRefIndexList.add(valKey);
				Collections.sort(replaceRefIndexList);
			}
			// replaceRefIndexHM.put(replaceStr, indPArrayList.size()-1);
		}
		// if replaceStr is not in the indPArrayList, it implies that it has
		// been
		// replaced by other index. But we can still find it, I use a
		// replaceIndexList to store
		// the position of the index.
		else {
			if (replaceRefIndexHM.containsKey(replaceStr)) {
				// System.out.println("replaceStr in operIsOld: "+replaceStr);

				replaceIndex = replaceRefIndexHM.get(replaceStr);
				// System.out.println("replaceIndex in operIsOld 1st case:
				// "+replaceIndex);

			} else {
				String oldRefId = markableSrcHM.get(refId);
				String oldReplaceStr = "";
				if (markableSrcHM.containsKey(oldRefId)) {
					String evenOldRefId = markableSrcHM.get(oldRefId);
					// oldReplaceStr = markableHeadWordHM.get(evenOldRefId);
					replaceStr = markableHeadWordHM.get(evenOldRefId);
				} else {
					// oldReplaceStr = markableHeadWordHM.get(oldRefId);
					replaceStr = markableHeadWordHM.get(oldRefId);
				}
				System.out.println("oldRefId  in operIsOld: " + oldRefId + " new repalceStr: " + replaceStr);

				// replaceIndex = replaceRefIndexHM.get(oldReplaceStr);
				replaceIndex = replaceRefIndexHM.get(replaceStr);

				// System.out.println("oldRefId in operIsOld: "+oldRefId+"
				// oldReplacestr: "+oldReplaceStr);
				// System.out.println("replaceIndex in operIsOld 2nd case:
				// "+replaceIndex);

			}
		}
		// //System.out.println("replaceIndex: "+replaceIndex);
		if (replaceIndex < MAXNUM) {
			oper = OLD;
			// //////System.out.println("replaceIndex: "+replaceIndex+"size:
			// "+indPArrayList.size());

			indPArrayList.remove(replaceIndex);
			// indPArrayList.add(tokenId);
			indPArrayList.add(String.valueOf(count));
			// indPArrayList.set(replaceIndex,tokenId);

			// indPArrayList.set(replaceIndex,tokenId);
			StringBuffer strBuffer = new StringBuffer();
			strBuffer.append(indPArrayList.get(0));
			for (int ii = 1; ii < indPArrayList.size(); ii++) {
				strBuffer.append("_" + indPArrayList.get(ii));
			}
			ind = strBuffer.toString();
			FeaStruct indStruct = new FeaStruct(OLD, indP, ind);
			indStructList.add(indStruct);

			// System.out.println("old state count: "+count+" "+oper+" "+indP+"
			// "+ind+" indStruct Size: "+
			// indStructList.size());

			StringBuffer synStrBuffer = new StringBuffer();

			synPArrayList.remove(replaceIndex);
			synPArrayList.add(func);
			synStrBuffer.append(synPArrayList.get(0));
			for (int ii = 1; ii < synPArrayList.size(); ii++) {
				synStrBuffer.append("_" + synPArrayList.get(ii));
			}
			syn = synStrBuffer.toString();
			FeaStruct synStruct = new FeaStruct(OLD, synP, syn);
			synStructList.add(synStruct);
			// System.out.println("old stat: indP "+oper+" "+indP+" ind "+ind );
			// System.out.println("old state count: "+count+" "+oper+" "+synP+"
			// "+syn+" synStruct Size: "+
			// synStructList.size());
		}
		// when replaceIndex is larger than or equal to MAXNUM, it implies
		// that the item has been cut off and put into those with index out of
		// range from
		// 0 to MAXNUM, in this case, the item is regarded to be new, but
		// different from
		// brand new one, I put the index which refer to its ref back to the
		// range from
		// 0 to MAXNUM
		else {
			// System.out.println("replaceIndex bigger than MAXNUM:
			// "+replaceIndex);
			oper = NEW;
			// indP = indStrList.get(Integer.valueOf(tokenId)-1).getFea();
			indP = indStructList.get(count - 1).getFea();
			indPArray = indP.split("_");
			indPArrayList = Arrays.asList(indPArray);

			synP = synStructList.get(count - 1).getFea();
			synPArray = synP.split("_");
			synPArrayList = Arrays.asList(synPArray);
			// //////System.out.println("indPArrayList in new state:
			// "+indPArrayList.size());

			// if size of indPArrayList is equal to MAXNUM, cut the first item
			// off.
			// if(indPArrayList.size()==MAXNUM)
			// now, I need to change the related values of the replaceRefIndexHM
			// it should be straightforward since for each cutoff tokenId, I
			// will change its
			// value, i.e. replaceIndex to 0+MAXNUM and the old one which has
			// value MAXNUM will be changed
			// MAXNUM+1 and all of those which values are larger than MAXNUM
			// will be added one
			// sequentially, meanwhile, those with values which are smaller than
			// MAXNUM will be deducted
			// one sequentially
			// replaceRefIndexHM.get(firstStr);
			// Iterator<String> it = replaceRefIndexHM.keySet().iterator();
			// while(it.hasNext()) {
			// String key = it.next();
			// int val = replaceRefIndexHM.get(key);
			// if(val==0){
			// val=val+MAXNUM;
			// replaceRefIndexHM.put(key, val);
			// }else if(val>0 & val<MAXNUM){
			// val=val-1;
			// replaceRefIndexHM.put(key, val);
			// }else if(val>MAXNUM & val<replaceIndex){
			// val=val+1;
			// replaceRefIndexHM.put(key, val);
			// }else if(val == replaceIndex){
			// val = MAXNUM-1;
			// replaceRefIndexHM.put(key, val);
			// }else{
			// replaceRefIndexHM.put(key, val);
			// }
			// }

			int indexFirstUndline = indP.indexOf("_");
			// the following substring method, +1 is very important. If not,
			// split will
			// not regard the first "_" as splitter, thus, the size will be 9,
			// then, it will
			// enter into else, the above check is meaningless then.
			String indPSub = indP.substring(indexFirstUndline + 1);
			// ind = indPSub +"_"+ tokenId;
			ind = indPSub + "_" + String.valueOf(count);
			// FeaStruct indStruct = new FeaStruct(NEW, indP, ind);
			// indStrList.add(indStruct);
			// put the new added tokenId to MAXNUM-1, that is, it is in the last
			// position of ind
			// replaceRefIndexHM.put(tokenId, MAXNUM-1);
			replaceRefIndexHM.put(replaceStr, MAXNUM - 1);
			Pair valKey = new Pair(Integer.valueOf(replaceStr), MAXNUM - 1);
			replaceRefIndexList.add(valKey);
			Collections.sort(replaceRefIndexList);
			// replaceRefIndexHM.put(String.valueOf(count), MAXNUM-1);
			// indPArrayList.set(MAXNUM-1,tokenId);
			FeaStruct indStruct = new FeaStruct(oper, indP, ind);
			indStructList.add(indStruct);
			// System.out.println("new state from old state count: "+count+"
			// "+oper+" "+indP+" "+ind+" indStruct Size: "+
			// indStructList.size());
			// add syntactic features to synStructList
			int synFirstUndline = synP.indexOf("_");
			String synPSub = synP.substring(synFirstUndline + 1);
			syn = synPSub + "_" + func;
			FeaStruct synStruct = new FeaStruct(oper, synP, syn);
			synStructList.add(synStruct);

			// System.out.println("new state from old state count: "+count+"
			// "+oper+" "+synP+" "+syn+" synStruct Size: "+
			// synStructList.size());
			// System.out.println("new state from Old State: "+oper+" "+indP+"
			// "+ind+" indStruct Size: "+
			// indStrList.size());
			// }
		}
		// count++;
		return count;

	}

	public int operIsCopy(int count,String pos, String word) {
		wordPosPairList2.add(new Pair(word,pos));
		String oper = COPY;
		String ind = "-";
		String indP = "-";
		String syn = "-";
		String synP = "-";
		if (count == 0) {
			// //System.out.println("count in count ==0: "+count);
			// ind = String.valueOf(count);
			// ////System.out.println("the first ind: "+ind);
			FeaStruct indStr = new FeaStruct(oper, ind, indP);
			// System.out.println("count in count==0: "+count+" "+oper+" "+ind+"
			// "+indP);
			indStructList.add(indStr);
			// syntactic features
			FeaStruct synStr = new FeaStruct(oper, syn, synP);
			// System.out.println("count in count==0: "+count+" "+oper+" "+syn+"
			// "+synP);
			synStructList.add(synStr);
		} else {
			// //System.out.println("count in count !=0: "+count);
			// indP =
			// indStructList.get(Integer.valueOf(tokenId)-1).getFea();
			indP = indStructList.get(count - 1).getFea();
			// indP = indStructList.get(count).getFea();
			// count++;
			ind = indP;
			FeaStruct indStr = new FeaStruct(oper, indP, ind);
			indStructList.add(indStr);
			// System.out.println("count: "+count+" "+oper+" "+indP+" "+ind+"
			// indStruct Size: "+
			// indStructList.size());

			// syntactic features
			synP = synStructList.get(count - 1).getFea();
			syn = synP;
			FeaStruct synStr = new FeaStruct(oper, synP, syn);
			synStructList.add(synStr);
			// System.out.println("count: "+count+" "+oper+" "+synP+" "+syn+"
			// synStruct Size: "+
			// synStructList.size());
		}
		// //System.out.println("count: "+count);
		// count++;
		return count;
	}

	// public int extractInfor(Node node, String opValue, int
	// count,HashMap<String,Integer> replaceRefIndexHM) {
	// the following method aims at extracting word and pos pairs.
	public String extractInfor(Node node) {
		String nodeName = node.getNodeName();
		// I cannot only extract WORD node since PUNC node is needed as well.
		// List<Node> wordNodeList = this.getChildrenByTagName(ithNode, WORD);
		// return count;
		NamedNodeMap tokenAttributes = node.getAttributes();
		// String word = node.getTextContent().trim().replaceAll("\t", "");
		String word = this.removeStrSpaces(node.getTextContent().toLowerCase());
		if (word.equals("\"_") || word.equals("_\"")) {
			word = "\"";
		}
		if (word.equals("\'_") || word.equals("_\'")) {
			word = "\'";
		}

		String pos = "";
		String function = "";
		if (nodeName.equals(PUNC)) {
			pos = word;
			function = word;
		} else {
			pos = tokenAttributes.getNamedItem(POS).getNodeValue();
			// function = tokenAttributes.getNamedItem(FUNC).getNodeValue();
			// //System.out.println(tokenAttributes.getNamedItem(ID)+"
			// "+tokenAttributes.getNamedItem(POS).getNodeValue());
		}

		if (reflexiveList.contains(word)) {
			pos = REFL;
		} else if (pronounList.contains(word)) {
			pos = PERPRO;
		}

		Pair wordPosPair = new Pair(word, pos);
		wordPosPairList.add(wordPosPair);
		Pair wordFuncPair = new Pair(word, function);
		funcStructList.add(wordFuncPair);
		return nodeName;
	}

	public void printTokenIdHM() {
		Iterator iterTokenIdHM = tokenIdHM.keySet().iterator();
		while (iterTokenIdHM.hasNext()) {
			String tokenId = (String) iterTokenIdHM.next();
			String token = tokenIdHM.get(tokenId);
			System.out.println(tokenId + " " + token);
		}
	}

	public void printTokenIdList() {
		for (int i = 0; i < tokenList.size(); i++) {
			System.out.println(" words from wordPostList " + wordPosPairList.get(i).toString() + " " + indStructList.get(i).getOper());
			System.out.println("tokenList: " + i + " " + tokenList.get(i) + " token: " + tokenIdHM.get(tokenList.get(i)));
		}
	}

	public boolean isPronoun(String mention) {
		boolean pronoun = false;
		if (maPronounsList.contains(mention) || fePronounsList.contains(mention) || nePronounsList.contains(mention) || pluralList.contains(mention)) {
			pronoun = true;
		}
		return pronoun;
	}

	public void fillMentionHM() {
		Iterator iterMarkSrcHM = markableSrcHM.keySet().iterator();
		List<String> refList = new ArrayList<String>();
		while (iterMarkSrcHM.hasNext()) {
			String markableId = (String) iterMarkSrcHM.next();
			String refId = markableSrcHM.get(markableId);
			List<String> sameEntList = new ArrayList<String>();
			// the first if add the refId and the first markableId refers back
			// to it
			if (!refList.contains(refId)) {
				refList.add(refId);
				int refIndex = refList.size() - 1;
				sameEntList.add(refId);
				sameEntList.add(markableId);
				// I may change the order to mentionHM for speed
				mentionHM.put(refIndex, sameEntList);
			} else {
				// else adds all other markableId to sameEntList
				int refIndex = refList.indexOf(refId);
				sameEntList = mentionHM.get(refIndex);
				sameEntList.add(markableId);
				mentionHM.put(refIndex, sameEntList);
			}
		}
		// System.out.println("size of mentionHM: "+mentionHM.size()+"size of
		// markableSrcHM: "+markableSrcHM.size());
	}

	public void buildPronounRefKey(PrintWriter pwKeyfile) {
		Iterator iterMentionHM = mentionHM.keySet().iterator();
		while (iterMentionHM.hasNext()) {
			// mentionList include all mentions which refer to the same entity
			List<String> mentionList = mentionHM.get(iterMentionHM.next());
			System.out.println("mentionList: " + mentionList.size());
			for (int j = 0; j < mentionList.size(); j++) {
				String corefMarkId = mentionList.get(j);
				String headTokenCount = markableHeadWordHM.get(corefMarkId);
				String headId = tokenList.get(Integer.valueOf(headTokenCount));
				String headToken = tokenIdHM.get(headId);
				// System.out.println("mentionList contents: corefMarkId
				// "+corefMarkId+" headTokenId "+headTokenCount+" headId
				// "+headId+" headToken "+headToken);
				if (isPronoun(headToken)) {
					// System.out.println("is there any
					// masculine?-------------------------------------------------");
					System.out.println(j + " " + headToken);
					if (j > 0) {
						String anteMarkId = mentionList.get(j - 1);
						String anteTokenCount = markableHeadWordHM.get(anteMarkId);
						String anteId = tokenList.get(Integer.valueOf(anteTokenCount));
						String anteToken = tokenIdHM.get(anteId);
						pwKeyfile.println("IDENT " + headId + " " + headToken + " " + anteId + " " + anteToken);

					}
				}
			}
		}
	}

	public void fillMentionPairList() {
		List<String> refList = new ArrayList<String>();
		System.out.println("markableSrcList size: " + markableSrcList.size() + "markableSrcHM size: " + markableSrcHM.size() + "markable size: " + markIdList.size());
		for (int i = 0; i < markableSrcList.size(); i++) {
			Pair markSrcPair = markableSrcList.get(i);
			// refId should be a noun and markableId may be a pronoun
			String markableId = (String) markSrcPair.getFirst();
			String refId = (String) markSrcPair.getSecond();
			List<String> sameEntList = new ArrayList<String>();
			// the first if add the refId and the first markableId refers back
			// to it
			if (!refList.contains(refId)) {
				refList.add(refId);
				int refIndex = refList.size() - 1;

				sameEntList.add(refId);
				sameEntList.add(markableId);
				// Pair refEntlistPair = new Pair(refIndex,sameEntList);
				mentionPairList.add(sameEntList);
			} else {
				// else adds all other markableId to sameEntList
				int refIndex = refList.indexOf(refId);
				sameEntList = mentionPairList.get(refIndex);
				sameEntList.add(markableId);
				mentionPairList.set(refIndex, sameEntList);
			}
		}
		System.out.println("size of mentionHM: " + mentionHM.size() + "size of markableSrcHM: " + markableSrcHM.size());
	}

	public void buildPronRefKey(PrintWriter pwKeyfile) {
		int count = 0;
		for (int i = 0; i < mentionPairList.size(); i++) {
			// mentionList include all mentions which refer to the same entity
			List<String> mentionList = mentionPairList.get(i);
			// System.out.println("mentionList: "+mentionList.size());
			count += mentionList.size();
			for (int j = 0; j < mentionList.size(); j++) {

				String corefMarkId = mentionList.get(j);

				String headTokenCount = markableHeadWordHM.get(corefMarkId);
				// tokenList should include redundant words added by some
				// markableNode which has not head word
				// markIdList seems to be consistent since markIdList doesn't
				// have redundant ones.

				String headId = tokenList.get(Integer.valueOf(headTokenCount));
				String headToken = "";
				if (tokenIdHM.get(headId) != null) {
					headToken = tokenIdHM.get(headId).toLowerCase();
				}

				int markIndex = markIdList.indexOf(corefMarkId);
				System.out.println(j + " markIndex: " + markIdList.indexOf(corefMarkId) + " " + headToken + " corefMarkId: " + corefMarkId);
				// System.out.println("mentionList contents: corefMarkId
				// "+corefMarkId+" headTokenId "+headTokenCount+" headId
				// "+headId+" headToken "+headToken);
				if (isPronoun(headToken)) {
					// System.out.println(j+" markIndex: "
					// + markIdList.indexOf(corefMarkId) + " " + headToken
					// + " corefMarkId: " + corefMarkId);
					// System.out.println("is there any
					// masculine?-------------------------------------------------");
					// System.out.println(j+" "+headToken);
					// a little error is that Fujimori and son of japanese
					// immigrant
					// one more error is that why him's id is 0 and one more
					// error is one pronoun seems to missing
					// another task is to find their entity id.
					if (j > 0) {
						String anteMarkId = mentionList.get(j - 1);
						// System.out.println("anteMarkId: "+anteMarkId);
						String anteTokenCount = markableHeadWordHM.get(anteMarkId);
						// System.out.println("anteTokenCount:
						// "+anteTokenCount);
						String anteId = tokenList.get(Integer.valueOf(anteTokenCount));
						String anteToken = tokenIdHM.get(anteId);
						int anteIndex = markIdList.indexOf(anteMarkId);
						// pwKeyfile.println("IDENT\t" + anteIndex+"\t" +
						// markIndex);
						pwKeyfile.println("IDENT " + anteIndex + " " + anteId + " " + anteToken + " " + markIndex + " " + headId + " " + headToken);
					} else {
						String anteMarkId = mentionList.get(j + 1);
						// System.out.println("anteMarkId: "+anteMarkId);
						String anteTokenCount = markableHeadWordHM.get(anteMarkId);
						// System.out.println("anteTokenCount:
						// "+anteTokenCount);
						String anteId = tokenList.get(Integer.valueOf(anteTokenCount));
						String anteToken = tokenIdHM.get(anteId);
						int anteIndex = markIdList.indexOf(anteMarkId);
						pwKeyfile.println("IDENT\t" + markIndex + "\t" + anteIndex);
						// pwKeyfile.println("IDENT " + markIndex + " "
						// + headId + " " + anteIndex + " " + anteId
						// + " " + anteToken + " " + headToken);
					}
				}
			}
		}
		System.out.println("mentionList items count: " + count);
	}

	public void fillGenderHM() {
		Iterator iterMentionHM = mentionHM.keySet().iterator();
		while (iterMentionHM.hasNext()) {
			List mentionList = mentionHM.get(iterMentionHM.next());
			System.out.println("mentionList: " + mentionList.size());
			for (int j = 0; j < mentionList.size(); j++) {
				String corefMarkId = (String) mentionList.get(j);
				String headTokenCount = markableHeadWordHM.get(corefMarkId);
				String headId = tokenList.get(Integer.valueOf(headTokenCount));
				String headToken = tokenIdHM.get(headId);
				// System.out.println("mentionList contents: corefMarkId
				// "+corefMarkId+" headTokenId "+headTokenCount+" headId
				// "+headId+" headToken "+headToken);
				if (maPronounsList.contains(headToken)) {
					// System.out.println("is there any
					// masculine?-------------------------------------------------");
					for (int k = 0; k < mentionList.size(); k++) {
						String markId = (String) mentionList.get(k);
						String tokenCount = markableHeadWordHM.get(markId);
						String tokenId = tokenList.get(Integer.valueOf(tokenCount));
						String token = tokenIdHM.get(tokenId);
						tokenGenderHM.put(tokenId, MASCULINE);
					}
					break;
				} else if (fePronounsList.contains(headToken)) {
					// System.out.println("is there any
					// faminine?-------------------------------------------------");
					for (int k = 0; k < mentionList.size(); k++) {
						String markId = (String) mentionList.get(k);
						String tokenCount = markableHeadWordHM.get(markId);
						String tokenId = tokenList.get(Integer.valueOf(tokenCount));
						String token = tokenIdHM.get(tokenId);
						tokenGenderHM.put(tokenId, FAMININE);
					}
					break;
				} else if (nePronounsList.contains(headToken)) {
					// System.out.println("is there any
					// neuter?-------------------------------------------------");
					for (int k = 0; k < mentionList.size(); k++) {
						String markId = (String) mentionList.get(k);
						String tokenCount = markableHeadWordHM.get(markId);
						String tokenId = tokenList.get(Integer.valueOf(tokenCount));
						String token = tokenIdHM.get(tokenId);
						tokenGenderHM.put(tokenId, NEUTER);
					}
					break;
				}
				// in case nothing above, I may consider checking wordnet on
				// line
				// check morpholgical features and so on.
				else if (j == mentionList.size() - 1) {
					for (int k = 0; k < mentionList.size(); k++) {
						String markId = (String) mentionList.get(k);
						String tokenCount = markableHeadWordHM.get(markId);
						String tokenId = tokenList.get(Integer.valueOf(tokenCount));
						String token = tokenIdHM.get(tokenId);
						tokenGenderHM.put(tokenId, NEUTER);
					}
					break;
					// ???????????????????????????????? waiting for to
					// do;;;;;;;;
				}

			}
		}

		Iterator<String> iterMarkableHeadWordHM = markableHeadWordHM.keySet().iterator();
		while (iterMarkableHeadWordHM.hasNext()) {
			String markableId = iterMarkableHeadWordHM.next();
			String tokenCount = markableHeadWordHM.get(markableId);
			String tokenId = tokenList.get(Integer.valueOf(tokenCount));
			String token = tokenIdHM.get(tokenId);
			if (!tokenGenderHM.containsKey(tokenId)) {
				if (maPronounsList.contains(token)) {
					tokenGenderHM.put(tokenId, MASCULINE);
				} else if (fePronounsList.contains(token)) {
					tokenGenderHM.put(tokenId, FAMININE);
				} else if (nePronounsList.contains(token)) {
					tokenGenderHM.put(tokenId, NEUTER);
				}

				else if (pluralList.contains(token)) {
					tokenGenderHM.put(tokenId, NEUTER);
				} else {
					tokenGenderHM.put(tokenId, NEUTER);
				}
			}
		}

		System.out.println(tokenGenderHM.size());
		Iterator iterTokenGenderHM = tokenGenderHM.keySet().iterator();

		System.out.println("tokenGenderHM size: " + tokenGenderHM.size());

		while (iterTokenGenderHM.hasNext()) {
			String key = (String) iterTokenGenderHM.next();
			System.out.println(key + " " + tokenGenderHM.get(key));
		}
	}

	public void fillNumberHM() {
		Iterator iterMentionHM = mentionHM.keySet().iterator();
		while (iterMentionHM.hasNext()) {
			List mentionList = mentionHM.get(iterMentionHM.next());
			System.out.println("mentionList: " + mentionList.size());
			for (int j = 0; j < mentionList.size(); j++) {
				String corefMarkId = (String) mentionList.get(j);
				String headTokenCount = markableHeadWordHM.get(corefMarkId);
				String headId = tokenList.get(Integer.valueOf(headTokenCount));
				String headToken = tokenIdHM.get(headId);
				// System.out.println("mentionList contents: corefMarkId
				// "+corefMarkId+" headTokenId "+headTokenCount+" headId
				// "+headId+" headToken "+headToken);
				if (maPronounsList.contains(headToken) || fePronounsList.contains(headToken) || nePronounsList.contains(headToken)) {
					// System.out.println("is there any
					// masculine?-------------------------------------------------");
					for (int k = 0; k < mentionList.size(); k++) {
						String markId = (String) mentionList.get(k);
						String tokenCount = markableHeadWordHM.get(markId);
						String tokenId = tokenList.get(Integer.valueOf(tokenCount));
						String token = tokenIdHM.get(tokenId);
						tokenNumberHM.put(tokenId, SINGULAR);
					}
					break;
				} else if (pluralList.contains(headToken)) {
					for (int k = 0; k < mentionList.size(); k++) {
						String markId = (String) mentionList.get(k);
						String tokenCount = markableHeadWordHM.get(markId);
						String tokenId = tokenList.get(Integer.valueOf(tokenCount));
						String token = tokenIdHM.get(tokenId);
						// not accurate for the following, I should consider
						// alternatives to get the information
						// about the gender of the plural forms.
						tokenNumberHM.put(tokenId, PLURAL);
					}
					break;
				}
				// in case nothing above, I may consider checking wordnet on
				// line
				// check morpholgical features and so on.
				else if (j == mentionList.size() - 1) {
					for (int k = 0; k < mentionList.size(); k++) {
						String markId = (String) mentionList.get(k);
						String tokenCount = markableHeadWordHM.get(markId);
						String tokenId = tokenList.get(Integer.valueOf(tokenCount));
						String token = tokenIdHM.get(tokenId);
						tokenNumberHM.put(tokenId, SINGULAR);
					}
					break;
					// ???????????????????????????????? waiting for to
					// do;;;;;;;;
				}

			}
		}

		Iterator<String> iterMarkableHeadWordHM = markableHeadWordHM.keySet().iterator();
		while (iterMarkableHeadWordHM.hasNext()) {
			String markableId = iterMarkableHeadWordHM.next();
			String tokenCount = markableHeadWordHM.get(markableId);
			String tokenId = tokenList.get(Integer.valueOf(tokenCount));
			String token = tokenIdHM.get(tokenId);
			if (!tokenNumberHM.containsKey(tokenId)) {
				if (maPronounsList.contains(token) || fePronounsList.contains(token) || nePronounsList.contains(token)) {
					tokenNumberHM.put(tokenId, SINGULAR);
				} else if (pluralList.contains(token)) {
					tokenNumberHM.put(tokenId, PLURAL);
				} else {
					tokenNumberHM.put(tokenId, SINGULAR);
				}
			}
		}

		System.out.println("size of tokenNumberHM" + tokenNumberHM.size());
		Iterator iterTokenNumberHM = tokenNumberHM.keySet().iterator();

		System.out.println("tokenNumberHM size: " + tokenNumberHM.size());

		while (iterTokenNumberHM.hasNext()) {
			String key = (String) iterTokenNumberHM.next();
			System.out.println(key + " " + tokenNumberHM.get(key));
		}
	}

	public void buildGenderDep() {
		int countNewOld = 0;
		for (int i = 0; i < indStructList.size(); i++) {
			// pwOpDepend.print("OP ");
			String op = indStructList.get(i).getOper();
			String gen = "";
			String genP = "";
			String tokenId;
			String token;
			String gender;
			String[] genPArray = null;
			FeaStruct genStruct = null;
			List<String> genPArrayList = new ArrayList<String>();
			if (i == 0) {
				if (op == COPY) {
					genStruct = new FeaStruct(op, "-", "-");
					genStructList.add(genStruct);
				} else if (op == NEW) {
					countNewOld++;
					tokenId = tokenList.get(i);
					token = tokenIdHM.get(tokenId);
					gender = tokenGenderHM.get(tokenId);
					genStruct = new FeaStruct(op, "-", gender);
					genStructList.add(genStruct);
				}
			} else {
				if (op == COPY) {
					genP = genStructList.get(i - 1).getFea();
					gen = genP;
					genStruct = new FeaStruct(op, genP, gen);
					genStructList.add(genStruct);
					// System.out.println("count: "+i+" "+op+" "+genP+" "+gen+"
					// genStruct Size: "+
					// genStructList.size());
				} else if (op == OLD) {
					countNewOld++;
					tokenId = tokenList.get(i);
					token = tokenIdHM.get(tokenId);
					gender = tokenGenderHM.get(tokenId);
					genP = genStructList.get(i - 1).getFea();
					genPArray = genP.split("_");
					for (String genPArrayStr : genPArray) {
						genPArrayList.add(genPArrayStr);
					}
					String indP = indStructList.get(i).getFeaP();
					String ind = indStructList.get(i).getFea();
					String[] indPArray = indP.split("_");
					String[] indArray = ind.split("_");
					for (int j = 0; j < indPArray.length; j++) {
						int jthInd = Integer.valueOf(indArray[j]);
						int jthIndP = Integer.valueOf(indPArray[j]);
						if (jthInd != jthIndP) {
							if (j == indPArray.length - 1) {
								gen = gen + "_" + gender;
							} else {
								if (gen.isEmpty()) {
									gen = genPArray[j + 1];
								} else {
									gen = gen + "_" + genPArray[j + 1];
								}
							}
						} else {
							if (j == 0) {
								gen = genPArray[0];
							} else {
								gen = gen + "_" + genPArray[j];
							}
						}
					}

					genStruct = new FeaStruct(op, genP, gen);
					genStructList.add(genStruct);
				}
				// else if op == NEW
				else {
					countNewOld++;
					genP = genStructList.get(i - 1).getFea();
					genPArray = genP.split("_");
					tokenId = tokenList.get(i);
					token = tokenIdHM.get(tokenId);
					gender = tokenGenderHM.get(tokenId);
					int length = genPArray.length;
					int theFirstUnderline = genP.indexOf("_");
					// System.out.println("tokenId in new of buildGenderDep:
					// "+tokenId);
					if (length < MAXNUM) {
						if (genP.equals("-")) {
							gen = gender;
						} else {
							gen = genP + "_" + gender;
						}
						genStruct = new FeaStruct(op, genP, gen);
					}
					// in fact, else only means length==MAXNUM since after that,
					// they are cut.
					else {
						gen = genP.substring(theFirstUnderline + 1) + "_" + gender;
						genStruct = new FeaStruct(op, genP, gen);
					}
					genStructList.add(genStruct);
				}
			}
		}
		System.out.println("countNewOld: " + countNewOld);
	}

	public void buildNumberDep() {
		int countNewOld = 0;
		for (int i = 0; i < indStructList.size(); i++) {
			// pwOpDepend.print("OP ");
			String op = indStructList.get(i).getOper();
			String num = "";
			String numP = "";
			String tokenId;
			String token;
			String number;
			String[] numPArray = null;
			FeaStruct numStruct = null;
			List<String> numPArrayList = new ArrayList<String>();
			if (i == 0) {
				if (op == COPY) {
					numStruct = new FeaStruct(op, "-", "-");
					numStructList.add(numStruct);
				} else if (op == NEW) {
					countNewOld++;
					tokenId = tokenList.get(i);
					token = tokenIdHM.get(tokenId);
					number = tokenNumberHM.get(tokenId);
					numStruct = new FeaStruct(op, "-", number);
					numStructList.add(numStruct);
				}
			} else {
				if (op == COPY) {
					numP = numStructList.get(i - 1).getFea();
					num = numP;
					numStruct = new FeaStruct(op, numP, num);
					numStructList.add(numStruct);
					// System.out.println("count: "+i+" "+op+" "+numP+" "+num+"
					// numStruct Size: "+
					// numStructList.size());
				} else if (op == OLD) {
					countNewOld++;
					tokenId = tokenList.get(i);
					token = tokenIdHM.get(tokenId);
					number = tokenNumberHM.get(tokenId);
					numP = numStructList.get(i - 1).getFea();
					numPArray = numP.split("_");
					for (String numPArrayStr : numPArray) {
						numPArrayList.add(numPArrayStr);
					}
					String indP = indStructList.get(i).getFeaP();
					String ind = indStructList.get(i).getFea();
					String[] indPArray = indP.split("_");
					String[] indArray = ind.split("_");
					for (int j = 0; j < indPArray.length; j++) {
						int jthInd = Integer.valueOf(indArray[j]);
						int jthIndP = Integer.valueOf(indPArray[j]);
						if (jthInd != jthIndP) {
							if (j == indPArray.length - 1) {
								num = num + "_" + number;
							} else {
								if (num.isEmpty()) {
									num = numPArray[j + 1];
								} else {
									num = num + "_" + numPArray[j + 1];
								}
							}
						} else {
							if (j == 0) {
								num = numPArray[0];
							} else {
								num = num + "_" + numPArray[j];
							}
						}
					}

					numStruct = new FeaStruct(op, numP, num);
					numStructList.add(numStruct);
				}
				// else if op == NEW
				else {
					countNewOld++;
					numP = numStructList.get(i - 1).getFea();
					numPArray = numP.split("_");
					tokenId = tokenList.get(i);
					token = tokenIdHM.get(tokenId);
					number = tokenNumberHM.get(tokenId);
					int length = numPArray.length;
					int theFirstUnderline = numP.indexOf("_");
					// System.out.println("tokenId in new of buildGenderDep:
					// "+tokenId);
					if (length < MAXNUM) {
						if (numP.equals("-")) {
							num = number;
						} else {
							num = numP + "_" + number;
						}
						numStruct = new FeaStruct(op, numP, num);
					}
					// in fact, else only means length==MAXNUM since after that,
					// they are cut.
					else {
						num = numP.substring(theFirstUnderline + 1) + "_" + number;
						numStruct = new FeaStruct(op, numP, num);
					}
					numStructList.add(numStruct);
				}
			}
		}
		System.out.println("countNewOld: " + countNewOld);
	}

	// sometimes, we cannot find the dependence among a Markable, in this case,
	// let us use the last word
	// as the head word, e.g.
	// <MARKABLE COMMENT="" ID="26">
	// <COREF CERTAIN="TRUE" COMMENT="" ID="230" SRC="14" TYPE_REF="NP"
	// TYPE_REL="IDENT">
	// </COREF>
	// <W DEP="W201" FUNC="DN" ID="W198" LEMMA="the" POS="DET">
	// the
	// </W>
	// <W DEP="W200" FUNC="A" ID="W199" LEMMA="guerrilla" POS="N">
	// guerrillas
	// '
	// </W>
	// </MARKABLE>
	public Node findHeadword(List<Node> eleWordList) {
		String headWord = "";
		// String wordId="";
		// Pair headWordPair = null;
		int preDepID = 0;
		int curWordID = 0;
		int curDepID = 0;
		Node headNode = null;
		List<String> wordIDList = new ArrayList<String>();
		for (int j = 0; j < eleWordList.size(); j++) {
			Node wordNode = eleWordList.get(j);
			NamedNodeMap wordAttributes = wordNode.getAttributes();
			Node wordIdNode = wordNode.getAttributes().getNamedItem(ID);
			String wordIdStr = "";
			if (wordIdNode == null) {
				wordIdStr = nullString;
			} else {
				wordIdStr = wordIdNode.getNodeValue();
			}
			// String wordIDStr =
			// wordAttributes.getNamedItem(ID).getNodeValue();
			wordIDList.add(wordIdStr);
		}
		for (int j = 0; j < eleWordList.size(); j++) {
			Node wordNode = eleWordList.get(j);
			Node wordIdNode = wordNode.getAttributes().getNamedItem(ID);
			NamedNodeMap wordAttributes = wordNode.getAttributes();
			Node depNode = wordAttributes.getNamedItem(DEP);
			String depStr = "";
			if (depNode != null) {
				depStr = wordAttributes.getNamedItem(DEP).getNodeValue();
			}
			Node funcNode = wordAttributes.getNamedItem(FUNC);
			String wordFunc = "";
			if (funcNode != null) {
				wordFunc = wordAttributes.getNamedItem(FUNC).getNodeValue();
			}

			String word = wordNode.getTextContent().trim().replaceAll("\t", "");
			// for the moment, it seems that I can safely use the last word as
			// the head word,
			// since I only consider flat structure rather than embedded
			// Makkable. I know that this is not
			// complete or correct at all. for example, "The girl of pink skin"
			// in this corpus, is an embedded Markable structure,
			// as my simple handling, only pink skin will be processed. In my
			// code, I need consider this case though
			List<String> dependList = new ArrayList<String>();
			String wordIDStr = "";
			wordIDStr = wordIDList.get(j);
			curWordID = Integer.valueOf(wordIDStr.substring(1));
			if (wordFunc.equals(SUBJ) || wordFunc.equals(OBJ)) {
				headNode = wordNode;
				headWord = word;
				if (!depStr.isEmpty() && !depStr.equalsIgnoreCase("none")) {
					curDepID = Integer.valueOf(depStr.substring(1));
				}

				// System.out.println("headWord is the subject or object:
				// "+headWord);
				break;
				// //System.out.println("head word from subj or obj indicator::
				// "+"curDepID: "+curDepID+" curWordID: "+curWordID+" headWord:
				// "+headWord);
			} else {
				if (!depStr.isEmpty() && !depStr.equalsIgnoreCase("none")) {
					curDepID = Integer.valueOf(depStr.substring(1));
				} else {
					curDepID = -1;
				}

				// not so sure how I can handle the above case, I will think
				// about it later.
				if (wordIDList.contains(depStr)) {
					preDepID = curDepID;
				} else if (!wordIDList.contains(depStr) && !stopWordList.contains(wordFunc)) {
					headWord = word;
					headNode = wordNode;
					// System.out.println("headWord is the first one which
					// wordIDList doesn't contain: "+headWord);
					break;
				} else if (j == eleWordList.size() - 1 && !wordFunc.equals("CC") && !wordFunc.equals("-FMAINV") && !wordFunc.equals("+FAUXV")) {
					headWord = word;
					headNode = wordNode;
					// System.out.println("headWord is the last word:
					// "+headWord);
					break;
					// //System.out.println("preDepID: "+preDepID+" curDepID: "
					// + curDepID + " curWordID: "
					// + curWordID + " headWord: " + headWord);
				}
			}
			// //System.out.println(wordNode.getTextContent().trim()+"
			// "+depStr);
			// //System.out.println(wordNode.getTextContent().trim().replaceAll("\\b\\s{2,}\\b",
			// " ")+" "+depStr);
			// //System.out.println(wordNode.getTextContent().trim().replaceAll("\t","")+"
			// "+depStr);
			// //System.out.println(this.removeExtraSpaces(word)+" "+depStr+"
			// "+wordIDStr);

		}
		return headNode;
	}

	public int getHeight(Node aNode) {
		int height = 0;
		int interHeight = 0;
		if (aNode != null) {
			aNode.getNextSibling();
			NodeList childNodeList = aNode.getChildNodes();
			for (int i = 0; i < childNodeList.getLength(); i++) {
				Node ithNode = childNodeList.item(i);
				int ithHeight = getHeight(ithNode);
				if (interHeight < ithHeight) {
					interHeight = ithHeight;
				}
			}
		}
		height = 1 + interHeight;
		return height;
	}

	// public List<Pair> sortPairList(List<Pair> tempList,List<Pair> trackList){
	// for(int i=0;i<tempList.size();i++){
	// Pair ithTemPair = tempList.get(i);
	// Pair ithTrackPair = trackList.get(i);
	//			
	//			
	// }
	// }

	// the purpose of processTempList is that preorderTraverse traverse the XML
	// tree from higher node to lower node and from
	// left to right. But I need to extract information from each Markalbe node
	// which involves multiple embedded Markable node.
	// as a result, outer markable node may put words which are behind some
	// inner
	// Markalbe node in the front of inner Markable node.
	// This will lead to the trouble when in the process of building
	// indStructList since the index of some word belonging to outside
	// Makralbe node have larger index than words which are in the inner
	// Markable node are processed firstly. out of bound error
	// will be caused. This is a complex way to build indStructList. In fact, I
	// can use clearer way. That is, rather than
	// building the list on the fly, I can use ArrayList or HashMap to store all
	// the values and then use those lists to
	// build indStructList. But I have made much effort on this. It seems to be
	// a beautiful work. So, I still keep this. Further,
	// there may be unexpected difficulties for the alternate approach.
	// I need to do more for the following method, that is, I need to sort the
	// tempList, do that tonight.

	public List[] processTempList(List<Pair> tempList, List<Pair> trackList, List<String> funcList, List<String> posList, List<String> wordList, String markableId) {
		List[] listArray = new List[8];
		List<Pair> newTempList = new ArrayList<Pair>();
		List<Pair> newTrackList = new ArrayList<Pair>();
		List<String> newFuncList = new ArrayList<String>();
		List<String> newPosList = new ArrayList<String>();
		List<String> newWordList = new ArrayList<String>();

		// note: we cannot directly assign tempList to another list, this way,
		// unsortedTempList will change followng the change of tempList,
		// interesting
		// List<Pair> unsortedTempList = tempList;

		List<Pair> unsortedTempList = new ArrayList<Pair>();
		for (int i = 0; i < tempList.size(); i++) {
			// System.out.println("what is inside tempList of processTempList:
			// "+tempList.get(i));
			unsortedTempList.add(tempList.get(i));
		}

		Collections.sort(tempList);
		// note: the second item of trackList is not unified, sometimes, it is a
		// node and sometimes it is a pair
		// the comparable cannot be created for the second item. But since
		// countoken is unique, we can ignore this.
		// System.out.println("trackList size in the beginning of
		// processTempList: "+trackList.size());
		for (int i = 0; i < trackList.size(); i++) {
			// System.out.println("trackList items in the begining:
			// "+trackList.get(i).toString()+" tempList items:
			// "+tempList.get(i).toString());
		}
		Collections.sort(trackList);
		HashMap<Pair, Integer> sortedIndexTokenidHM = new HashMap<Pair, Integer>();
		for (int i = 0; i < tempList.size(); i++) {
			sortedIndexTokenidHM.put(tempList.get(i), i);
		}

		String[] sortedFuncArray = new String[funcList.size()];
		String[] sortedPosArray = new String[posList.size()];
		String[] sortedWordArray = new String[wordList.size()];
		// String[] sortedGenArray = new String[funcList.size()];
		// String[] sortedNumArray = new String[funcList.size()];
		for (int i = 0; i < funcList.size(); i++) {
			Pair ithPair = unsortedTempList.get(i);
			int sortedIndex = sortedIndexTokenidHM.get(ithPair);
			sortedFuncArray[i] = funcList.get(sortedIndex);
			sortedPosArray[i] = posList.get(sortedIndex);
			sortedWordArray[i] = wordList.get(sortedIndex);
		}

		List<String> sortedFuncList = new ArrayList<String>();
		List<String> sortedPosList = new ArrayList<String>();
		List<String> sortedWordList = new ArrayList<String>();
		for (int i = 0; i < sortedFuncArray.length; i++) {
			sortedFuncList.add(sortedFuncArray[i]);
			sortedPosList.add(sortedPosArray[i]);
			sortedWordList.add(sortedWordArray[i]);
		}

		List<Integer> processedList = new ArrayList<Integer>();
		int countOper = 0;
		for (int i = 0; i < tempList.size(); i++) {
			// System.out.println("how large is the tempList:
			// "+tempList.size()+" "+tempList.get(0)+" "+markableId);
			Pair ithPair = tempList.get(i);
			int index = (Integer) ithPair.getFirst();
			String oper = (String) ithPair.getSecond();
			String ithFunc = sortedFuncList.get(i);
			String ithPos = sortedPosList.get(i);
			String ithWord = sortedWordList.get(i);
			// System.out.println("wordPosPair in processTempList:
			// "+wordPosPairList.get(index));
			if (index == indStructList.size()) {
				if (oper == COPY) {
					// System.out.println("index and content from
					// processTemplist: "+index+"
					// "+trackList.get(i).getSecond());
					index = this.operIsCopy(index,ithPos,ithWord);
				} else if (oper == NEW) {
					this.operIsNew(index, markableId, ithFunc, ithPos, ithWord);
					countOper++;
					// System.out.println(index+"
					// "+trackList.get(i).getSecond());
				} else {
					// refId is not correct here.
					// String refId = (String) trackList.get(i).getFirst();
					String refId = (String) ((Pair) trackList.get(i).getSecond()).getFirst();
					// System.out.println("what is refID from processTempLIst:
					// "+refId);
					if (!refId.isEmpty() && pronounList.contains(ithWord)) {
						if (reflexiveList.contains(ithWord)) {
							ithPos = REFL;
						} else if (pronounList.contains(ithWord)) {
							ithPos = PERPRO;
						}
						this.operIsOld(index, refId, ithFunc, ithPos, ithWord);
					} else {
						this.operIsNew(index, refId, ithFunc, ithPos, ithWord);
					}
					// System.out.println(index+"
					// "+trackList.get(i).getSecond());
					countOper++;
				}
				processedList.add(i);
			}
		}

		// int markableIndex = markIdList.indexOf(markableId);
		// if (countOper > 0 && !markableId.isEmpty()) {
		// System.out
		// .println("in
		// tempList&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&: "
		// + markableId);
		// markIdList.remove(markableId);
		// markIdList.add((markableIndex + countOper), markableId);
		// }

		for (int i = 0; i < tempList.size(); i++) {
			if (!processedList.contains(i)) {
				newTempList.add(tempList.get(i));
				newTrackList.add(trackList.get(i));
				newFuncList.add(sortedFuncList.get(i));
				newPosList.add(sortedPosList.get(i));
				newWordList.add(sortedWordList.get(i));
			}
		}
		// System.out.println("newTempList size:
		// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% "+newTempList.size()+"
		// tempList size: "+tempList.size());
		// System.out.println("newTrackList size:
		// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% "+newTrackList.size()+"
		// trackList size: "+trackList.size());
		listArray[0] = newTempList;
		listArray[1] = newTrackList;
		listArray[2] = newFuncList;
		listArray[3] = newPosList;
		listArray[4] = newWordList;
		return listArray;
	}

	public String getPuncTokenId(Node node) {
		String tokenId = "";
		if (node.getPreviousSibling() != null && node.getNextSibling() != null) {
			if (!node.getPreviousSibling().getNodeName().equals(PUNC) && node.getNextSibling().getNodeName().equals(PUNC)) {
				String preNodeValue = node.getPreviousSibling().getAttributes().getNamedItem(ID).getNodeValue();
				String preNodeText = node.getPreviousSibling().getAttributes().getNamedItem(ID).getTextContent();
				if (preNodeValue == null) {
					tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue() + preNodeText + node.getTextContent();
				} else {
					tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue() + preNodeValue + node.getTextContent();
				}
			} else if (node.getPreviousSibling().getNodeName().equals(PUNC) && !node.getNextSibling().getNodeName().equals(PUNC)) {
				tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue() + node.getNextSibling().getAttributes().getNamedItem(ID).getNodeValue() + node.getTextContent();
			} else if (!node.getPreviousSibling().getNodeName().equals(PUNC) && !node.getNextSibling().getNodeName().equals(PUNC)) {
				tokenId = node.getPreviousSibling().getAttributes().getNamedItem(ID).getNodeValue() + node.getNextSibling().getAttributes().getNamedItem(ID).getNodeValue() + node.getTextContent();
			}
		} else if (node.getPreviousSibling() != null && node.getNextSibling() == null) {
			if (!node.getPreviousSibling().getNodeName().equals(PUNC)) {
				String preNodeValue = node.getPreviousSibling().getAttributes().getNamedItem(ID).getNodeValue() + node.getTextContent();
				String preNodeText = node.getPreviousSibling().getAttributes().getNamedItem(ID).getTextContent() + node.getTextContent();
				if (preNodeValue == null) {
					tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue() + preNodeText + node.getTextContent();
				} else {
					tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue() + preNodeValue + node.getTextContent();
				}
			} else if (node.getPreviousSibling().getNodeName().equals(PUNC)) {
				tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue() + node.getPreviousSibling().getTextContent();
			}
		} else if (node.getPreviousSibling() == null && node.getNextSibling() != null) {
			if (!node.getNextSibling().getNodeName().equals(PUNC)) {
				String nextNodeValue = node.getNextSibling().getAttributes().getNamedItem(ID).getNodeValue();
				String nextNodeText = node.getNextSibling().getAttributes().getNamedItem(ID).getNodeValue();
				if (nextNodeValue == null) {
					tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue() + nextNodeText + node.getTextContent();
				} else {
					tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue() + nextNodeValue + node.getTextContent();
				}
			} else {
				tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue() + node.getNextSibling().getTextContent() + node.getTextContent();
			}

		}
		return tokenId;
	}

	/**
	 * Performs full tree traversal using stack. rootNode here is a sentence
	 * node.
	 */

	public void inorderTraverse(Node rootNode, Stack<Node> markableNodeStack) {
		Node curNode = rootNode.getFirstChild();
		while (!markableNodeStack.isEmpty() || curNode != null) {
			// NodeList curNodeKidList=null;
			// int countKid = 0;
			// if(curNode!=null){
			// ///curNodeKidList = curNode.getChildNodes();
			// }
			// fine leftmost node with no left child by calling getFirstChild();
			while (curNode != null) {
				markableNodeStack.push(curNode);
				// System.out.println(
				// curNode.getNodeName()+"="+curNode.getNodeValue());
				curNode = curNode.getFirstChild();

			}// end while

			// visit leftmost node, then traverse all its right subtrees.
			if (!markableNodeStack.isEmpty()) {
				Node nextNode = markableNodeStack.pop();
				curNode = nextNode.getNextSibling();
				// curNodeKidList = nextNode.getChildNodes();
				assert nextNode != null; // since markalbeNodeStack was not
				// empty before the pop
				// temNode = curNode;
				// temNode = nextNode;
				// //System.out.println("what is curNode here: "
				// +temNode.getNodeName());
				// //System.out.println("countKid: "+countKid+" nextNode
				// "+nextNode.getNodeName()+" "+nextNode.getFirstChild());
				// if(curNodeKidList.getLength()!=0){
				// curNode = curNodeKidList.item(countKid+1);
				// ////System.out.println(curNode.getNodeName());
				// }

				// cool!!!!!!!!!!! Afte long term thinking and step by step
				// derivation, I finally figure out the
				// way for multinary inorder traversal. countKid is a key,
				// especailly it should be put into the following
				// if clause. But unfortunatelly, this is still wrong. The
				// reason is that I can only keep one countKid.
				// this is obviously wrong. As a result, countKid will be
				// growing continuouslly. So, some items will be
				// skipped since I cannot find a way to start from 0. Luckily,
				// org.w3c.dom has implemented the method
				// getNextSibling, which can solve this problem quite easily
				// now.
				// if(curNode!=null){
				// markableNodeStack.push(temNode);
				// //countKid++;
				// }
				// //System.out.println("what is temNode here: "
				// +temNode.getNodeName()+" "+temNode.getTextContent()+" ");
			}
		}
	}

	protected Node getFirstChildByTagName(Node parent, String tagName) {
		NodeList nodeList = parent.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(tagName))
				return node;
		}
		return null;
	}

	protected List<Node> getChildrenByTagName(Node parent, String tagName) {
		List<Node> eleList = new ArrayList<Node>();
		NodeList nodeList = parent.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(tagName)) {
				eleList.add(node);
			}

		}
		return eleList;
	}

	public String removeSpaces(String s) {
		StringTokenizer st = new StringTokenizer(s, "\t\n", false);
		String t = "";
		while (st.hasMoreElements())
			t += st.nextElement() + " ";
		return t;
	}

	public String removeStrSpaces(String s) {
		String newStr = s.trim().replaceAll("\n", "");
		return newStr;
	}

	private String removeExtraSpaces(String s) {
		if (!s.contains(" "))
			return s;
		return removeExtraSpaces(s.replace(" ", ""));
	}

	public void convertIndStruct(PrintWriter pwIndStruct, PrintWriter pwOldModel) {
		// ////System.out.println(indStructList.size());

		for (int i = 0; i < indStructList.size(); i++) {

			FeaStruct indStruct = indStructList.get(i);
			pwIndStruct.print("Ind ");
			String oper = indStruct.getOper();
			String indPrev = indStruct.getFeaP();
			String indCur = indStruct.getFea();
			// HashMap<String, Integer> indHm = new HashMap<String,Integer>();
			HashMap<Integer, Integer> indHm = new HashMap<Integer, Integer>();
			String[] indPArray = indPrev.split("_");
			String[] indArray = indCur.split("_");
			// List<String> indPList = Arrays.asList(indPArray);
			// List<String> indPList = new ArrayList<String>();

			List<Integer> indPList = new ArrayList<Integer>();

			for (int ii = 0; ii < indPArray.length; ii++) {
				if (indPArray[ii].equals(start)) {
					indPList.add(-1);
				} else {
					indPList.add(Integer.valueOf(indPArray[ii]));
				}
			}

			if (oper.equals(COPY)) {
				pwIndStruct.print(COPY + " ");
				Collections.sort(indPList);
				for (int j = 0; j < indPList.size(); j++) {
					indHm.put(indPList.get(j), j);
				}
				for (int j = 0; j < indPArray.length - 1; j++) {
					pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[j])) + "_");
				}

				if (indPArray[indPArray.length - 1].equals(start)) {
					pwIndStruct.print(start + " : ");
				} else {
					pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[indPArray.length - 1])) + " : ");
				}

				for (int j = 0; j < indArray.length - 1; j++) {
					pwIndStruct.print(indHm.get(Integer.valueOf(indArray[j])) + "_");
				}
				if (indArray[indArray.length - 1].equals(start)) {
					pwIndStruct.print(start);
				} else {
					pwIndStruct.print(indHm.get(Integer.valueOf(indArray[indArray.length - 1])));
				}
				pwIndStruct.println();
			}

			else if (oper.equals(NEW)) {
				pwIndStruct.print(NEW + " ");

				if (indPList.size() == 1 && indPList.contains(-1)) {
					pwIndStruct.println(start + " : " + 1);
				} else if (indPList.size() == 1 && indPList.get(0) == 0) {
					pwIndStruct.println(start + " : " + indArray[0] + "_" + indArray[1]);
				} else {
					indPList.add(Integer.valueOf(indArray[indArray.length - 1]));
					Collections.sort(indPList);
					for (int j = 0; j < indPList.size(); j++) {
						indHm.put(indPList.get(j), j);
						// //////System.out.print(indPList.get(j)+" ");
					}
					// //////System.out.println();
					for (int j = 0; j < indPArray.length - 1; j++) {
						pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[j])) + "_");
					}
					pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[indPArray.length - 1])) + " : ");
					for (int j = 0; j < indArray.length - 1; j++) {
						pwIndStruct.print(indHm.get(Integer.valueOf(indArray[j])) + "_");
					}
					pwIndStruct.print(indHm.get(Integer.valueOf(indArray[indArray.length - 1])) + " ");
					pwIndStruct.println();
				}
			} else if (oper.equals(OLD)) {
				pwIndStruct.print(OLD + " ");
				pwOldModel.print("Ind " + OLD + " ");

				if (indPList.size() == 1 && indPList.contains(-1)) {
					pwIndStruct.println("-" + " " + indArray[0]);
					pwOldModel.println("1" + " " + indArray[0]);
				} else if (indPList.size() == 1 && indPList.get(0) == (0)) {
					pwIndStruct.println("0" + " " + indArray[0] + "_" + indArray[1]);
					pwOldModel.println("0" + " " + indArray[1]);
				} else {
					indPList.add(Integer.valueOf(indArray[indArray.length - 1]));
					Collections.sort(indPList);
					for (int j = 0; j < indPList.size(); j++) {
						indHm.put(indPList.get(j), j);
						// //////System.out.print(indPList.get(j)+" ");
					}
					// //////System.out.println();
					for (int j = 0; j < indPArray.length - 1; j++) {
						pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[j])) + "_");
					}
					pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[indPArray.length - 1])) + " : ");
					pwOldModel.print(indPArray.length + " : ");
					for (int j = 0; j < indArray.length - 1; j++) {
						pwIndStruct.print(indHm.get(Integer.valueOf(indArray[j])));
					}
					pwIndStruct.print(indHm.get(Integer.valueOf(indArray[indArray.length - 1])) + " ");

					for (int j = 0; j < indPArray.length; j++) {
						// //////System.out.println(indPArray.length+" the
						// length of them : "+indArray.length);
						// //////System.out.println()
						if (!Integer.valueOf(indPArray[j]).equals(Integer.valueOf(indArray[j]))) {
							pwOldModel.print(indHm.get(Integer.valueOf(indPArray[j])) + " ");
							// //////System.out.println("what is
							// J::::::::::::"+j);
							break;
						}
						// //if(all items from both arrays are equal
						// else
						// if(Integer.valueOf(indPArray[j])==Integer.valueOf(indArray[j])
						// && (j==indPArray.length-2)){
						// pwOldModel.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+"
						// ");
						// break;
						// }
					}
					pwIndStruct.println();
					pwOldModel.println();
				}
			}

		}
	}

	public void buildPosDependence(PrintWriter pwPosDepend) {
		System.out.println(" in buildPosDependence size of wordPosPairList: " + wordPosPairList.size() + " size of indStructList: " + indStructList.size());
		for (int i = 0; i < wordPosPairList.size(); i++) {
			pwPosDepend.print("Pos ");
			if (i == 0) {
				String posP = "-";
				String pos = (String) wordPosPairList.get(i).getSecond();
				String OP = indStructList.get(i).getOper();
				String ind = indStructList.get(i).getFea();
				pwPosDepend.println(OP + " " + posP + " : " + pos);
				pwPosDepend.println(NEW + " " + posP + " : " + pos);
				pwPosDepend.println(NEW + " " + posP + " : " + pos);
			} else {
				String posP = (String) wordPosPairList.get(i - 1).getSecond();
				String pos = (String) wordPosPairList.get(i).getSecond();
				String oper = indStructList.get(i).getOper();
				String indP = indStructList.get(i).getFeaP();
				String indCur = indStructList.get(i).getFea();
				String[] indPArray = indP.split("_");
				HashMap<Integer, Integer> indHm = new HashMap<Integer, Integer>();
				String[] indArray = indCur.split("_");
				// List<String> indPList = Arrays.asList(indPArray);
				// List<String> indPList = new ArrayList<String>();
				List<Integer> indPList = new ArrayList<Integer>();

				for (int ii = 0; ii < indPArray.length; ii++) {
					if (indPArray[ii].equals(start)) {
						indPList.add(-1);
					} else {
						indPList.add(Integer.valueOf(indPArray[ii]));
					}
				}

				if (oper.equals(COPY)) {
					pwPosDepend.print(COPY + " ");
					// Collections.sort(indPList);
					for (int j = 0; j < indPList.size(); j++) {
						indHm.put(indPList.get(j), j);
					}
					// for(int j=0;j<indPArray.length-1;j++){
					// pwPosDepend.print(indHm.get(indPList.get(j))+"_");
					// }
					// if(indPArray[indPArray.length-1].equals(start)){
					// pwPosDepend.print(start + " ");
					// }else{
					// pwPosDepend.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+"
					// ");
					// }
					// pwPosDepend.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+"
					// ");
					pwPosDepend.print(posP + " : " + pos);
					pwPosDepend.println();
				} else if (oper.equals(NEW)) {
					pwPosDepend.print(NEW + " ");
					indPList.add(Integer.valueOf(indArray[indArray.length - 1]));
					// Collections.sort(indPList);
					for (int j = 0; j < indPList.size(); j++) {
						indHm.put(indPList.get(j), j);
						// //////System.out.print(indPList.get(j)+" ");
					}
					// for(int j=0;j<indArray.length-1;j++){
					// pwPosDepend.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
					// }
					//
					// if(indArray[indArray.length-1].equals(start)){
					// pwPosDepend.print(start + " ");
					// }else{
					// pwPosDepend.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+"
					// ");
					// }

					pwPosDepend.print(posP + " : " + pos);
					pwPosDepend.println();
				} else if (oper.equals(OLD)) {
					pwPosDepend.print(OLD + " ");
					indPList.add(Integer.valueOf(indArray[indArray.length - 1]));
					// Collections.sort(indPList);
					for (int j = 0; j < indPList.size(); j++) {
						indHm.put(indPList.get(j), j);
						// //////System.out.print(indPList.get(j)+" ");
					}
					// for(int j=0;j<indArray.length-1;j++){
					// pwPosDepend.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
					// }
					// pwPosDepend.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+"
					// ");
					pwPosDepend.print(posP + " : " + pos);
					pwPosDepend.println();
				}
			}
		}
		pwPosDepend.println("Pos_W eos : -");
		pwPosDepend.println("Pos - " + wordPosPairList.get(wordPosPairList.size() - 1).getSecond() + " -");

	}

	public void buildOpDependence(PrintWriter pwOpDepend) {
		for (int i = 0; i < wordPosPairList.size(); i++) {
			pwOpDepend.print("Op ");
			if (i == 0) {
				// this corpus doesn't have - - : NEW since it always annotates
				// the beginging as copy
				String posP = "-";
				String opP = "-";
				String Op = synStructList.get(i).getOper();
				// String synP = "-";
				// String [] synPArray = synP.split("_");
				// for(int j=0;j<synPArray.length-1;j++){
				// pwOpDepend.print(synPArray[j]+"_");
				// }
				// pwOpDepend.print(synPArray[synPArray.length-1]+" : ");
				pwOpDepend.print(opP + " " + posP + " : " + Op);
			} else {
				String posP = (String) wordPosPairList.get(i - 1).getSecond();
				// String pos = (String)wordPosPairList.get(i).getSecond();
				String Op = synStructList.get(i).getOper();
				// String synP = synStructList.get(i-1).getFea();
				String opP = synStructList.get(i - 1).getOper();
				// String [] synPArray = synP.split("_");
				// for(int j=0;j<synPArray.length-1;j++){
				// pwOpDepend.print(synPArray[j]+"_");
				// }
				// pwOpDepend.print(synPArray[synPArray.length-1]+" ");
				pwOpDepend.print(opP + " " + posP + " : " + Op);
			}
			pwOpDepend.println();
		}
		// this line is the end state
		pwOpDepend.println("Op " + synStructList.get(synStructList.size() - 1).getOper() + " " + wordPosPairList.get(wordPosPairList.size() - 1).getSecond() + " : -");
		pwOpDepend.println("Op - - : NEW");
		pwOpDepend.println("Op - - : NEW");
	}

	public void printTokenPosPair(PrintWriter pwPosTokenPair) {
		for (int i = 0; i < wordPosPairList.size(); i++) {
			pwPosTokenPair.println(wordPosPairList.get(i));
		}
	}

	public void buildWdDependence(PrintWriter pwPosWdDepend, PrintWriter pwWdDepend, PrintWriter pwWd) {
		for (int i = 0; i < wordPosPairList.size(); i++) {
			String word = (String) wordPosPairList.get(i).getFirst();
			String pos = (String) wordPosPairList.get(i).getSecond();
			String oper = indStructList.get(i).getOper();
			String ind = indStructList.get(i).getFea();
			String gen = genStructList.get(i).getFea();
			String num = numStructList.get(i).getFea();
			String syn = synStructList.get(i).getFea();

			String curind = "";
			String curgen = "";
			String curnum = "";
			String cursyn = "";

			if (ind.contains(delim)) {
				curind = ind.substring(ind.lastIndexOf(delim) + 1);
				curgen = gen.substring(gen.lastIndexOf(delim) + 1);
				curnum = num.substring(num.lastIndexOf(delim) + 1);
				cursyn = syn.substring(syn.lastIndexOf(delim) + 1);
			} else {
				curind = ind;
				curgen = gen;
				curnum = num;
				cursyn = syn;
			}

			if (!curgen.equals(start)) {

				pwWdDepend.println("Gen_W " + word.toLowerCase() + " : " + curgen);
				pwWd.println("Gender : " + curgen);
			}

			if (!curnum.equals(start)) {

				pwWdDepend.println("Num_W " + word.toLowerCase() + " : " + curnum);
				pwWd.println("Number : " + curnum);
			}

			if (!cursyn.equals(start)) {
				pwWdDepend.println("Syn_W " + word.toLowerCase() + " : " + cursyn);
				pwWd.println("Syntax : " + cursyn);
			}
			pwPosWdDepend.println("Pos_W " + word.toLowerCase() + " : " + pos);
			pwWd.println("Pos : " + pos);
			pwWd.println("Word : " + word.toLowerCase());

		}
		pwWdDepend.println("Num_W eos : -");
		pwWdDepend.println("Gen_W eos : -");
		pwWdDepend.println("Syn_W eos : -");
		pwPosWdDepend.println("Pos_W eos : -");
		pwWd.println("Number : -");
		pwWd.println("Gender : -");
		pwWd.println("Syntax : -");
		pwWd.println("Pos : -");
		pwWd.println("Word : eos");
	}

	public void buildEntity(PrintWriter pwEntity, PrintWriter pwText) {
		int countMention = 0;
		int subtract = 0;
		for (int i = 0; i < wordPosPairList.size(); i++) {
			String word = (String) wordPosPairList.get(i).getFirst();
			FeaStruct indFeaStruct = indStructList.get(i);
			String oper = indFeaStruct.getOper();
			if (oper.equals(NEW) || oper.equals(OLD)) {
				countMention++;
				if (countMention == 1) {
					subtract = i;
				}
				if (i - subtract >= 0) {
					pwEntity.println("Entity " + (i - subtract) + " : " + word.toLowerCase() + " = 1.0");
				}
			}
		}
		for (int i = subtract; i < wordPosPairList.size(); i++) {
			String word = (String) wordPosPairList.get(i).getFirst();
			pwText.print(word.toLowerCase() + " ");
		}
	}

	public void buildBindingFeat(PrintWriter pwBinding) {
		for (int i = 0; i < wordPosPairList.size(); i++) {
			Pair wordPos = wordPosPairList.get(i);
			String word = (String) wordPos.getFirst();
			String pos = (String) wordPos.getSecond();
			FeaStruct indFeaStruct = indStructList.get(i);
			FeaStruct synFeaStruct = synStructList.get(i);
			String[] indPArray = indFeaStruct.getFeaP().split("_");
			String[] indArray = indFeaStruct.getFea().split("_");
			String[] synPArray = synFeaStruct.getFeaP().split("_");
			String[] synArray = synFeaStruct.getFea().split("_");
			String oper = indFeaStruct.getOper();
			HashMap<Integer, Integer> indHm = new HashMap<Integer, Integer>();
			List<Integer> indPList = new ArrayList<Integer>();

			for (int ii = 0; ii < indPArray.length; ii++) {
				if (indPArray[ii].equals(start)) {
					indPList.add(-1);
				} else {
					indPList.add(Integer.valueOf(indPArray[ii]));
				}
			}
			if (oper.equals(OLD)) {
				if (indPList.size() == 1 && indPList.get(0) == (0)) {
					pwBinding.println("Bind " + indArray.length + indArray[0] + " " + synPArray[0] + " " + synArray[0] + " : " + pos);
					pwBinding.println("Bind " + indArray[0] + " " + synPArray[0] + " " + synArray[0] + " : " + pos);
					pwBinding.println("Bind " + synPArray[0] + " " + synArray[0] + " : " + pos);
					pwBinding.println("Bind " + synArray[0] + " : " + pos);
					pwBinding.println("Bind : " + pos);

				} else {
					indPList.add(Integer.valueOf(indArray[indArray.length - 1]));
					Collections.sort(indPList);
					for (int j = 0; j < indPList.size(); j++) {
						indHm.put(indPList.get(j), j);
						// //////System.out.print(indPList.get(j)+" ");
					}
					for (int j = 0; j < indPArray.length; j++) {
						// //////System.out.println(indPArray.length+" the
						// length of them : "+indArray.length);
						// //////System.out.println()
						if (!Integer.valueOf(indPArray[j]).equals(Integer.valueOf(indArray[j]))) {
							pwBinding.println("Bind " + indArray.length + " " + indHm.get(Integer.valueOf(indPArray[j])) + " " + synPArray[j] + " " + synArray[j] + " : " + pos);
							pwBinding.println("Bind " + indHm.get(Integer.valueOf(indPArray[j])) + " " + synPArray[j] + " " + synArray[j] + " : " + pos);
							pwBinding.println("Bind " + synPArray[j] + " " + synArray[j] + " : " + pos);
							pwBinding.println("Bind " + synArray[j] + " : " + pos);
							pwBinding.println("Bind : " + pos);
							// //////System.out.println("what is
							// J::::::::::::"+j);
							break;
						}
						// //if(all items from both arrays are equal
						// else
						// if(Integer.valueOf(indPArray[j])==Integer.valueOf(indArray[j])
						// && (j==indPArray.length-2)){
						// pwOldModel.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+"
						// ");
						// break;
						// }
					}
				}
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		// E:\Dissertation Writing\corpora\corefann\wxml
		// "/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"

		int item = 6;
		long start = System.currentTimeMillis();
		System.out.println("starting time: " + start);

		// ProcessReutersJdom processReutersCorpus = new ProcessReutersJdom();
		File input = new File(args[0]);
		// BufferedReader brXMLCorpus = new BufferedReader(new
		// FileReader(input));
		// BufferedReader brXMLCorpus2 = new BufferedReader(new
		// FileReader(input));
		File inputFile = new File(args[0]);
		if (inputFile.isDirectory()) {
			File[] listOfFiles = inputFile.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				ProcessReutersJdomPron processReutersCorpus = new ProcessReutersJdomPron(listOfFiles[i]);
				System.out.println("file number: " + listOfFiles[i].getName());
				processReutersCorpus.splitMarkableNode();
				File outputDir = new File(args[1]);
				if (!outputDir.exists()) {
					outputDir.mkdirs();
				}
				PrintWriter pwIndFormat = null;
				PrintWriter pwOldModel = null;
				PrintWriter pwPosModel = null;
				PrintWriter pwOpModel = null;
				PrintWriter pwWdModel = null;
				PrintWriter pwWd = null;
				PrintWriter pwSynModel = null;
				PrintWriter pwGenModel = null;
				PrintWriter pwNumModel = null;
				PrintWriter pwPosWdModel = null;
				// //the following is for debugging:
				PrintWriter posTokenPw = null;
				try {
					pwIndFormat = new PrintWriter(new FileWriter(new File(outputDir, input.getName() + ".model"), true));
					pwOldModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName() + "input.model"), true));
					pwPosModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName() + "pos.model"), true));
					pwOpModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName() + "op.model"), true));
					pwWdModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName() + "wdDep.model"), true));
					pwPosWdModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName() + "posWdDep.model"), true));
					pwWd = new PrintWriter(new FileWriter(new File(outputDir, input.getName() + "wd.model"), true));
					pwSynModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName() + "syn.model"), true));
					pwGenModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName() + "gen.model"), true));
					pwNumModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName() + "num.model"), true));
					posTokenPw = new PrintWriter(new FileWriter(new File(outputDir, input.getName() + ".postoken"), true));
					processReutersCorpus.printIndStructList();
					processReutersCorpus.fillGenderHM();
					processReutersCorpus.fillNumberHM();
					processReutersCorpus.buildGenderDep();
					processReutersCorpus.buildNumberDep();
					// processReutersCorpus.printTokenIdHM();
					processReutersCorpus.convertIndStruct(pwIndFormat, pwOldModel);
					processReutersCorpus.buildPosDependence(pwPosModel);
					processReutersCorpus.buildOpDependence(pwOpModel);
					processReutersCorpus.buildWdDependence(pwPosWdModel, pwWdModel, pwWd);
					processReutersCorpus.printTokenPosPair(posTokenPw);
					processReutersCorpus.printSynStructList(pwSynModel);
					processReutersCorpus.printGenStructList(pwGenModel);
					processReutersCorpus.printNumStructList(pwNumModel);
				} catch (IOException e) {
					// // TODO Auto-generated catch block
					e.printStackTrace();
				}

				pwIndFormat.close();
				pwOldModel.close();
				pwPosModel.close();
				pwOpModel.close();
				pwWdModel.close();
				pwPosWdModel.close();
				pwWd.close();
				posTokenPw.close();
				pwSynModel.close();
				pwGenModel.close();
				pwNumModel.close();

			}
		} else {
			ProcessReutersJdomPron processReutersCorpus = new ProcessReutersJdomPron(inputFile);
			processReutersCorpus.splitMarkableNode();
			File outputDir = new File(args[1]);
			if (!outputDir.exists()) {
				outputDir.mkdirs();
			}

			PrintWriter pwIndFormat = null;
			PrintWriter pwOldModel = null;
			PrintWriter pwPosModel = null;
			PrintWriter pwOpModel = null;
			PrintWriter pwWdModel = null;
			PrintWriter pwWd = null;
			PrintWriter pwSynModel = null;
			PrintWriter pwGenModel = null;
			PrintWriter pwNumModel = null;
			PrintWriter pwPosWdModel = null;
			// //the following is for debugging:
			PrintWriter posTokenPw = null;
			try {
				pwIndFormat = new PrintWriter(new FileWriter(new File(outputDir, input.getName().substring(0, input.getName().length() - 3) + "model"), true));
				pwOldModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName().substring(0, input.getName().length() - 3) + "input.model"), true));
				pwPosModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName().substring(0, input.getName().length() - 6) + "posmodel"), true));
				pwOpModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName().substring(0, input.getName().length() - 6) + "opmodel"), true));
				pwWdModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName().substring(0, input.getName().length() - 6) + "wdDepModel"), true));
				pwPosWdModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName().substring(0, input.getName().length() - 6) + "posWdDepModel"), true));
				pwWd = new PrintWriter(new FileWriter(new File(outputDir, input.getName().substring(0, input.getName().length() - 6) + "wdModel"), true));
				pwSynModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName().substring(0, input.getName().length() - 6) + "synModel"), true));
				pwGenModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName().substring(0, input.getName().length() - 6) + "genModel"), true));
				pwNumModel = new PrintWriter(new FileWriter(new File(outputDir, input.getName().substring(0, input.getName().length() - 6) + "numModel"), true));
				posTokenPw = new PrintWriter(new FileWriter(new File(outputDir, input.getName().substring(0, input.getName().length() - 6) + "postoken"), true));
				processReutersCorpus.printIndStructList();
				processReutersCorpus.fillGenderHM();
				processReutersCorpus.fillNumberHM();
				processReutersCorpus.buildGenderDep();
				processReutersCorpus.buildNumberDep();
				// processReutersCorpus.printTokenIdHM();
				processReutersCorpus.convertIndStruct(pwIndFormat, pwOldModel);
				processReutersCorpus.buildPosDependence(pwPosModel);
				processReutersCorpus.buildOpDependence(pwOpModel);
				processReutersCorpus.buildWdDependence(pwPosWdModel, pwWdModel, pwWd);
				processReutersCorpus.printTokenPosPair(posTokenPw);
				processReutersCorpus.printSynStructList(pwSynModel);
				processReutersCorpus.printGenStructList(pwGenModel);
				processReutersCorpus.printNumStructList(pwNumModel);
			} catch (IOException e) {
				// // TODO Auto-generated catch block
				e.printStackTrace();
			}

			pwIndFormat.close();
			pwOldModel.close();
			pwPosModel.close();
			pwOpModel.close();
			pwWdModel.close();
			pwWd.close();
			posTokenPw.close();
			pwSynModel.close();
			pwGenModel.close();
			pwNumModel.close();
			pwPosWdModel.close();
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
