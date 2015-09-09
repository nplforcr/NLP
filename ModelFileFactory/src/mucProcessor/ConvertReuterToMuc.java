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

import reutersProcessor.ProcessReutersJdom;
import utils.Pair;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import corpusProcessor.FeaStruct;

public class ConvertReuterToMuc {

	// static String startXML =
	// "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
	//﻿<?xml version="1.0" encoding="UTF-8"?
	static String startXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	static String DOC = "DOC";
	static String DOCNO = "DOCNO";
	static String DOCU = "DOCUMENT";
	static String DD = "DD";
	static String HL = "HL";
	static String PARAGRAPH = "P";
	static String SENTENCE = "S";
	static String COREF = "COREF";
	static String PERIOD = ".";
	static String MARKABLE = "MARKABLE";
	static String TYPE = "TYPE";
	static String SRC = "SRC";
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

	public ConvertReuterToMuc(File inFile) throws IOException {
		inputFile = inFile;
	}

	// "/project/nlp/dingcheng/nlplab/corpora/bukavu/381790newsML-done.xml.xml"

	public void splitMarkableNode(PrintWriter pwCorpus) {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder; 
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(inputFile);
				Node child = document.getFirstChild();
				System.out.println(startXML);
				System.out.println("<"+DOC+">");
				System.out.println("<"+DOCNO+">"+"</"+DOCNO+">");
				pwCorpus.println(startXML);
				pwCorpus.println("<"+DOC+">");
				pwCorpus.println("<"+DOCNO+">"+"</"+DOCNO+">");
				NodeList docChildList = child.getChildNodes();
				NodeList senNodeList = document.getElementsByTagName(SENTENCE);
				for (int i = 0; i < senNodeList.getLength(); i++) {
					Node ithNode = senNodeList.item(i);
				}

				for (int i = 0; i < senNodeList.getLength(); i++) {
					Node ithNode = senNodeList.item(i);
					this.muclizeReuters(pwCorpus,ithNode);
				}
				pwCorpus.println("</"+DOC+">");
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
		pwCorpus.close();
	}

	/**
	 * Performs full tree traversal using stack. rootNode here is a sentence
	 * node.
	 */
	public void muclizeReuters(PrintWriter pwCorpus,Node rootNode) {
		Stack<Node> stack = new Stack<Node>();
		// ignore root -- root acts as a container, here, the rootNode should be
		// a sentNode.
		Node node = rootNode.getFirstChild();
		int height = 0;
		int absHeight=0;
		boolean insideMarkable=false;
		while (node != null) {
			//cannot close here since it may miss some markable node.
//			Node prevNode = node.getPreviousSibling();
//			if(prevNode!=null){
//				if(prevNode.getNodeName().equals(MARKABLE)){
//					pwCorpus.print("</COREF> ");
//					System.out.print("</COREF> ");
//				}
//			}
			Node parentNode = node.getParentNode();
			if (node.getNodeName().equals(MARKABLE)) {
				if(node.getAttributes().getNamedItem(ID).getNodeValue().equals("37")){
					System.out.println("start the debugging:");
				}
				if(parentNode.getNodeName().equals(SENTENCE)){
					insideMarkable=true;
					height = this.getHeight(node)-2;
					absHeight=this.getHeight(node)-2;
				}else if(insideMarkable==true && height<absHeight){
					height++;
				}
				Node firstKidNode=node.getFirstChild();
				NamedNodeMap markableMap = node.getAttributes();
				String markableId = markableMap.getNamedItem(ID).getNodeValue();
				if(firstKidNode.getNodeName().equals(COREF)){
					String refId = firstKidNode.getAttributes().getNamedItem(SRC).getNodeValue();
					pwCorpus.print("<"+COREF+" "+ID+"="+"\""+markableId+"\" "+REF+"="+"\""+refId+"\""+">");
					System.out.print("<"+COREF+" "+ID+"="+"\""+markableId+"\" "+REF+"="+"\""+refId+"\""+">");
				}else{
					pwCorpus.print("<"+COREF+" "+ID+"="+"\""+markableId+"\""+">");
					System.out.print("<"+COREF+" "+ID+"="+"\""+markableId+"\""+">");
				}
			}else if (node.getNodeName().equals(WORD)
					|| node.getNodeName().equals(PUNC)) {
				if(node.getTextContent().equals(PERIOD)){
					pwCorpus.println(node.getTextContent());
					System.out.println(node.getTextContent());
				}else{
					pwCorpus.print(node.getTextContent()+" ");
					System.out.print(node.getTextContent()+" ");
				}
				
				Node nextNode = node.getNextSibling();
				Node parNextNode = parentNode.getNextSibling();
				if(nextNode==null){
					if(parentNode.getNodeName().equals(SENTENCE)){
						break;
					}
					else if(!parentNode.getNodeName().equals(SENTENCE)&&parNextNode==null){
						pwCorpus.print("</COREF> ");
						System.out.print("</COREF> ");
						int relHeight=0;
//						Stack<Node> parNodeStack=new Stack<Node>();
//						parNodeStack.push(parentNode);
						int locHeight=this.getHeight(parentNode)-2;
						//ashamed, you should not feel difficult in designing such a basic algorithm
						//but you have spent almost 5 hours in finishing this though I like it.
						//stack is not needed at all. If you really want to use stack, you should
						//loop the stack and pop them out and increase relHeight. 
						while(locHeight<=absHeight){
							parentNode=parentNode.getParentNode();
							parNextNode = parentNode.getNextSibling();
							if (!parentNode.getNodeName().equals(SENTENCE)&&parNextNode==null) {
//								parNodeStack.push(parentNode);
								relHeight++;
							} else if(!parentNode.getNodeName().equals(SENTENCE)&&parNextNode!=null) {
								break;
//								if (!parNodeStack.isEmpty()){
//									parentNode = (Node) parNodeStack.pop();
//									
//								}	
							}
							locHeight++;
						}
						for(int i=relHeight;i>=0;i--){
							pwCorpus.print("</COREF> ");
							System.out.print("</COREF> ");
						}
					}else{
						//now it is the case where either parentNode.getNodeName() is equal to SENTENCE
						//or parNextNode is null
						if(height==0){
							insideMarkable=false;
						}else{
							pwCorpus.print("</COREF> ");
							System.out.print("</COREF> ");
						}
						height--;
					}
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
	
	//getHeight here will return the height from node to the wordNode. For
	//a markable, the height I need should 2 fewer. 
	public int getHeight(Node aNode) {
		int height = 0;
		int interHeight = 0;
		if (aNode != null) {
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

	protected Node getFirstChildByTagName(Node parent, String tagName) {
		NodeList nodeList = parent.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& node.getNodeName().equalsIgnoreCase(tagName))
				return node;
		}
		return null;
	}

	protected List<Node> getChildrenByTagName(Node parent, String tagName) {
		List<Node> eleList = new ArrayList<Node>();
		NodeList nodeList = parent.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& node.getNodeName().equalsIgnoreCase(tagName)) {
				eleList.add(node);
			}

		}
		return eleList;
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// E:\Dissertation Writing\corpora\corefann\wxml
		// "/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"
		long start = System.currentTimeMillis();
		File inputFile = new File(args[0]);
		if (inputFile.isDirectory()) {
			File[] listOfFiles = inputFile.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				ConvertReuterToMuc convertReuterToMuc = new ConvertReuterToMuc(listOfFiles[i]);
				System.out.println("file number: " + listOfFiles[i].getName());
				File outputDir = new File(args[1]);
				if (!outputDir.exists()) {
					outputDir.mkdirs();
				}
				PrintWriter pwCorpus = null; 	
				// //the following is for debugging:
				try {
					pwCorpus=new PrintWriter(new FileWriter(new File(outputDir, listOfFiles[i].getName())));
					convertReuterToMuc.splitMarkableNode(pwCorpus);
				} catch (IOException e) {
					// // TODO Auto-generated catch block
					e.printStackTrace();
				}
				pwCorpus.close();
			}
		} else {
			ConvertReuterToMuc convertReuterToMuc = new ConvertReuterToMuc(inputFile);

			File outputDir = new File(args[1]);
			if (!outputDir.exists()) {
				outputDir.mkdirs();
			}
			try {
				PrintWriter pwCorpus = new PrintWriter(new FileWriter(new File(args[1])));
				convertReuterToMuc.splitMarkableNode(pwCorpus);
				pwCorpus.close();
			} catch (IOException e) {
				// // TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		long stop = System.currentTimeMillis();
		System.out.println("stopping time: " + stop);
		long elapsed = stop - start;
		System.out.println("this is the total running time: " + elapsed);
	}
}
