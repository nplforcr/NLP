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

public class ProcessCherryPickerMucOutJdom {

	// static String startXML =
	// "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
	static String startXML = "<?xml version=\"1.0\"?>";
	static String DOC = "DOC";
	static String SENTENCE = "s";
	static String COREF = "COREF";
	static String REF = "REF";
	static String ID = "ID";
	List<Pair> wordIdPairList;
	HashMap<String,String> hmIdWord;
	List<String> pronounList;
	// to store the word token and its id;
	HashMap<String, List<String>> coChainHM;
	File inputFile;

	public ProcessCherryPickerMucOutJdom(File inFile) {
		inputFile = inFile;
		hmIdWord = new HashMap<String,String>();
		wordIdPairList = new ArrayList<Pair>();
		coChainHM = new HashMap<String, List<String>>();
		String[] pronounArray = {"he", "his", "him","she", "her","it", "its", "hers","they", "their", "them",};
		pronounList = Arrays.asList(pronounArray);
	}

	public ProcessCherryPickerMucOutJdom() {
		hmIdWord = new HashMap<String,String>();
		wordIdPairList = new ArrayList<Pair>();
		coChainHM = new HashMap<String, List<String>>();
		String[] pronounArray = {"he", "his", "him","she", "her","it", "its", "hers","they", "their", "them",};
		pronounList = Arrays.asList(pronounArray);
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

	public void splitMarkableNode(String inputXMLString) {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(new InputSource(new StringReader(inputXMLString)));
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
		// ignore root -- root acts as a container, here, the rootNode should be
		// a sentNode.<COR EF ID="2" REF="1">
		Node node = rootNode.getFirstChild();
		HashMap<String,List> hmCorefChain = new HashMap<String,List>();
		while (node != null) {
			if (node.getNodeName().equals(COREF)) {
				String markableId = node.getAttributes().getNamedItem(ID).getNodeValue();
				String word = node.getTextContent();
				Pair idWordPair = new Pair(markableId,word);
				wordIdPairList.add(idWordPair);
				hmIdWord.put(markableId, word);
				String refStr = null;
				if(node.getAttributes().getNamedItem(REF)!=null){
					refStr = node.getAttributes().getNamedItem(REF).getNodeValue();
				}
				if(refStr!=null){
					if (!coChainHM.containsKey(refStr)) {
						List<String> corefChainList = new ArrayList<String>();
						corefChainList.add(markableId);
						coChainHM.put(refStr, corefChainList);
					}else{
						List<String> corefChainList = coChainHM.get(refStr);
						corefChainList.add(markableId);
						coChainHM.put(refStr, corefChainList);
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

	public void printCochain(PrintWriter pwCochain){
		Iterator<String> it = coChainHM.keySet().iterator();
		// Iterator<Pair> it = replaceRefIndexHM.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String refWord = hmIdWord.get(key);
			// Pair key = it.next();
			List<String> coChainList = coChainHM.get(key);
			for(int i=0;i<coChainList.size();i++){
				String markId = coChainList.get(i);
				String markWord = hmIdWord.get(markId);
				if(pronounList.contains(markWord)){
					pwCochain.print(key+" "+refWord+" "+markId+" "+hmIdWord.get(markId));
					pwCochain.println();
				}

			}
		}
	}

	public static void main(String[] args) throws Exception {

		// TODO Auto-generated method stub
		// E:\Dissertation Writing\corpora\corefann\wxml
		// "/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"

		int item = 6;
		long start = System.currentTimeMillis();
		File fileTag = new File(args[0]);
		//File outputFile = new File(args[2]);
		//PrintWriter pwOutput = new PrintWriter(new FileWriter(outputFile));
		if(fileTag.isDirectory()){
			File dirOutput = new File(fileTag.getParent()+"\\cherryAnswer");
			System.out.println(fileTag.getParent());
			if(!dirOutput.exists()){
				dirOutput.mkdir();
			}
			File[] fileArray = fileTag.listFiles();
			for(int i=0;i<fileArray.length;i++){
				if(fileArray[i].getName().endsWith("responses")){
					PrintWriter pwCochain = new PrintWriter(new FileWriter(new File(dirOutput, fileArray[i].getName()+".cherryPicker")));
					if (args.length>1 && args[1].equals("-s")) {
						System.setProperty("inputstring", "true");
						ProcessCherryPickerMucOutJdom processCherryPickerMucOutjdom =new ProcessCherryPickerMucOutJdom();
						BufferedReader bfMuc = new BufferedReader(new FileReader(fileArray[i]));
						StringBuffer sbMuc = new StringBuffer();
						String line = "";
						sbMuc.append("<DOC>");
						sbMuc.append("<s>");
						while((line=bfMuc.readLine())!=null){
							sbMuc.append(line);
						}
						sbMuc.append("</s>");
						sbMuc.append("</DOC>");
						processCherryPickerMucOutjdom.splitMarkableNode(sbMuc.toString());
						processCherryPickerMucOutjdom.printCochain(pwCochain);
					}else{
						ProcessCherryPickerMucOutJdom processCherryPickerMucOutjdom =new ProcessCherryPickerMucOutJdom(fileArray[i]);
						processCherryPickerMucOutjdom.splitMarkableNode(); 
						processCherryPickerMucOutjdom.printCochain(pwCochain);
					}
					pwCochain.close();
				}
			}
			
		}else{
			File dirOutput = new File(fileTag.getParent()+"\\cherryAnswer");
			System.out.println(fileTag.getParent());
			if(!dirOutput.exists()){
				dirOutput.mkdir();
			}
			PrintWriter pwCochain = new PrintWriter(new FileWriter(new File(dirOutput, fileTag.getName()+".cherrypicker")));
			if (args.length>1 && args[1].equals("-s")) {
				System.setProperty("inputstring", "true");
				ProcessCherryPickerMucOutJdom processCherryPickerMucOutjdom =new ProcessCherryPickerMucOutJdom();
				BufferedReader bfMuc = new BufferedReader(new FileReader(fileTag));
				StringBuffer sbMuc = new StringBuffer();
				String line = "";
				sbMuc.append("<DOC>");
				sbMuc.append("<s>");
				while((line=bfMuc.readLine())!=null){
					sbMuc.append(line);
				}
				sbMuc.append("</s>");
				sbMuc.append("</DOC>");
				processCherryPickerMucOutjdom.splitMarkableNode(sbMuc.toString());
				processCherryPickerMucOutjdom.printCochain(pwCochain);
			}else{
				ProcessCherryPickerMucOutJdom processCherryPickerMucOutjdom =new ProcessCherryPickerMucOutJdom(fileTag);
				processCherryPickerMucOutjdom.splitMarkableNode(); 
				processCherryPickerMucOutjdom.printCochain(pwCochain);
			}
			pwCochain.close();
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
