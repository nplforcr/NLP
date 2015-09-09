
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
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerConfigurationException;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;

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

public class PrepareEvaData {
	File inputFile;
	static String MARKABLE = "MARKABLE";
	static String COREF = "COREF";
	static String SENTENCE = "S";
	static String WORD = "W";
	static String PUNC = "PUNCT";
	static String ID = "ID";
	static String SRC ="SRC";
	List<String> markIdList;
	List<String> markableList;
	List<List<String>> listOfCoMarkableList;
	HashMap<String, Integer> markableIndexHM;

	public PrepareEvaData (File inFile) {
		inputFile = inFile;
		markIdList = new ArrayList<String>();
		markableList = new ArrayList<String>();
		listOfCoMarkableList = new ArrayList<List<String>>();
		markableIndexHM = new HashMap<String,Integer>();
	}

	protected Node getFirstChildByTagName(Node parent, String tagName){
		NodeList nodeList = parent.getChildNodes();
		for(int i=0;i<nodeList.getLength();i++){
			Node node = nodeList.item(i);
			if(node.getNodeType()==Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(tagName))
				return node;
		}
		return null;
	}

	public void prepareCorefList() {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(inputFile);
				Node child = document.getFirstChild();
				NodeList markableNodeList = document.getElementsByTagName(MARKABLE);
				int count=0;
				for (int i = 0; i < markableNodeList.getLength(); i++) {
					Node ithNode = markableNodeList.item(i);
					String markableId = ithNode.getAttributes().getNamedItem(ID).getNodeValue();
					markIdList.add(markableId);
					//System.out.println(ithNode.getNodeName()+" : "+ithNode.getAttributes().getNamedItem(ID).getNodeValue());
					Node corefNode=this.getFirstChildByTagName(ithNode, COREF);
					String corefId = "";
					if(corefNode==null){
						List<String> newMarkableList=new ArrayList<String>();
						newMarkableList.add(markableId);
						listOfCoMarkableList.add(newMarkableList);
						//System.out.println(ithNode.getNodeName()+" : "+ithNode.getAttributes().getNamedItem(ID).getNodeValue());
						markableIndexHM.put(markableId, count);
						count++;
						//int corefIdIndex = markIdList.indexOf(corefId);					
					}else{
						corefId = corefNode.getAttributes().getNamedItem(SRC).getNodeValue();						
						if(markableIndexHM.containsKey(corefId)){
							List<String> corefList=new ArrayList<String>();
							int index = markableIndexHM.get(corefId);
							corefList= listOfCoMarkableList.get(index);
							corefList.add(markableId);
							listOfCoMarkableList.set(index, corefList);
						}else{
							//the following refers to this case: markableId=0, it is put into markableIndexHM. 
							//then markableId = 5 and corefId = 0, then, it will be put into corefList which includes
							//id = 0. But later on, another markableId = 10 refers back markableId=5, which is not put into
							//markableIndexHM. This is a bad annotation of the corpus since usually, all coreference goes to the 
							//the first one. Then, we need to use the following code
							for(int j=0;j<listOfCoMarkableList.size();j++){
								List<String> corefList=listOfCoMarkableList.get(j);
								if(corefList.contains(corefId)){
									corefList.add(markableId);
									listOfCoMarkableList.set(j, corefList);
								}
							}
						}
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
	}

	public void printMarkableIndexHM(){
		Iterator iterMarkableIndexHM = markableIndexHM.keySet().iterator();
		while(iterMarkableIndexHM.hasNext()){
			String markable = (String) iterMarkableIndexHM.next();
			Integer token= markableIndexHM.get(markable);
			//System.out.println(markable+" "+token);
		}
	}
	
	public void prepareCorefData(PrintWriter pwDevTestData) {
		pwDevTestData.print("# System response ");
		for(int i=0;i<listOfCoMarkableList.size();i++){
			List<String> ithList = listOfCoMarkableList.get(i);
			for(int j=0;j<ithList.size()-1;j++){
				String jthItem = ithList.get(j);
				String j1thItem = ithList.get(j+1);
				int indexOfjthItem = markIdList.indexOf(jthItem);
				int indexOfj1thItem = markIdList.indexOf(j1thItem);
				//System.out.println("IDENT\t"+indexOfjthItem+"\t"+indexOfj1thItem);
				System.out.println("IDENT\t"+jthItem+"\t"+j1thItem);
				pwDevTestData.println("IDENT\t"+indexOfjthItem+"\t"+indexOfj1thItem);
			}
		}
	}

	public void prepareDevTest(PrintWriter pwDevTestData) {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//File inputFile = new File(inputString);
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
					this.extractWordPunc(ithNode,pwDevTestData);					
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

	public void extractWordPunc( Node rootNode,PrintWriter pwDevTestData) {
		Stack<Node> stack = new Stack<Node>();
		// ignore root -- root acts as a container, here, the rootNode should be a sentNode.
		Node node=rootNode.getFirstChild();
		while (node!=null) {
			String tokenId = "";
			String token = "";
			String markable = "";
			String markableId = "";
			if(node.getNodeName().equals(WORD) || node.getNodeName().equals(PUNC)){
				pwDevTestData.print(node.getTextContent().toLowerCase()+" ");
			}

			if ( node.hasChildNodes()) {
				// store next sibling in the stack. We return to it after all children are processed.
				if (node.getNextSibling()!=null)
					stack.push( node.getNextSibling());
				node = node.getFirstChild();
			}
			else {
				node = node.getNextSibling();
				if (node==null && !stack.isEmpty())
					// return to the parent's level.
					// note that some levels can be skipped if the parent's node was the last one.
					node=(Node) stack.pop();
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		// E:\Dissertation Writing\corpora\corefann\wxml
		//"/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"

		int item = 6;
		long start = System.currentTimeMillis();
		System.out.println("starting time: " + start);
		File inputFile = new File(args[0]);

		File outputDir = new File(args[1]);
		if(!outputDir.exists()){
			outputDir.mkdirs();
		}
		File devDir = null;
		File testDir = null;
		if(args.length>2){
			devDir = new File(args[2]);
			if(!devDir.exists()){
				devDir.mkdirs();
			}

			testDir = new File(args[3]);
			if(!testDir.exists()){
				//System.out.println(testDir.getName());
				testDir.mkdirs();
			}
		}


		if(inputFile.isDirectory()){
			File[] listOfFiles = inputFile.listFiles();
			for(int i=0;i<listOfFiles.length;i++){
				PrepareEvaData prepareEvaData = new PrepareEvaData(listOfFiles[i]);
				PrintWriter pwDevData = null;
				PrintWriter pwTestData = null;
				try {
					if(i>=listOfFiles.length*0.8 && i<listOfFiles.length*0.9){
						System.out.println("file number for dev >>: "+listOfFiles[i].getName());
						////System.out.println("in dev : "+i);
						pwDevData = new PrintWriter(new FileWriter(new File(devDir,listOfFiles[i].getName()+".dev")));
						////System.out.println("where are you? "+devDir.getAbsolutePath());
						prepareEvaData.prepareDevTest(pwDevData);
						pwDevData.close();
					}else if(i>=listOfFiles.length*0.9 && i<listOfFiles.length) {
						System.out.println("file number testing >>>: "+listOfFiles[i].getName());
						pwTestData = new PrintWriter(new FileWriter(new File(testDir,listOfFiles[i].getName()+".test")));
						prepareEvaData.prepareDevTest(pwTestData);
						pwTestData.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}else{
			PrepareEvaData prepareEvaData = new PrepareEvaData(inputFile);
			try {
				PrintWriter pwDevTestData=new PrintWriter(new FileWriter(new File(outputDir,inputFile.getName().substring(0,inputFile.getName().length()-3)+"eva")));
				prepareEvaData.prepareCorefList();
				prepareEvaData.printMarkableIndexHM();
				prepareEvaData.prepareCorefData(pwDevTestData);
				pwDevTestData.close();
			} catch (IOException e) {
				//			// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
