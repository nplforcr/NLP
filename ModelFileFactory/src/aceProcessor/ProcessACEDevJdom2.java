package aceProcessor;

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

import annotation.Mention;

import readers.AceReader;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import corpusProcessor.FeaStruct;
import utils.Pair;

public class ProcessACEDevJdom2 {

	// static String startXML =
	// "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
	static String startXML = "<?xml version=\"1.0\"?>";
	static String SOURCE = "source_file";
	static String DOC = "DOCUMENT";
	static String DOCID = "DOCID";
	static String ENTITY = "entity";
	static String ENTTYPE = "entity_type";
	static String ENTMENTION = "entity_mention";
	static String ENTATTRIBUTES = "entity_attributes";
	static String TYPE = "TYPE";
	static String EXTENT = "extent";
	static String TEXT = "TEXT";
	static String CHARESEQ = "charseq";
	static String START = "start";
	static String END = "end";
	static String HEAD = "head";
	static String PARAGRAPH = "p";
	static String SENTENCE = "s";
	static String COREF = "COREF";
	static String ROLE = "ROLE";
	static String NAEM = "name";
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
	static String delim = "_";
	static String REFL = "reflexive";
	static String PERPRO = "personalPron";
	static String PRON = "PRONOUN";
	static String MTURN = "<TURN>";
	static String MTIME = "<time";
	int tokenIndex = 0;
	int markableIndex = 0;
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
	HashMap<Pair, Pair> spanIdHM;
	HashMap<Pair, String> roleIdHM;
	HashMap<Pair, String> typeIdHM;
	HashMap<Pair, String> headIdHM;
	HashMap<Pair, Pair> idSpanHM;
	HashMap<Integer, Integer> indexHM;
	HashMap<Pair, String> entityHM;
	HashMap<Integer, Pair> startIdHM;
	HashMap<Integer, String> start_TokenHM;
	HashMap<Integer,String> start_TypeHM;
	List<Pair> mentionIdList;
	List<Integer> startIndexList;

	File inputFile;
	HashMap<String,Mention> idMentionM;

	//the code is not well written. But it is quite interesting. When I wrote it, I created two 
	//contructors and one is used to handle sgm file and the other is used to handel xml file
	//this is a good use of the functions of constructors.
	public ProcessACEDevJdom2() {
		tokenList = new ArrayList<String>();
	}

	public ProcessACEDevJdom2(File inFile) throws ParserConfigurationException, SAXException, IOException {
		inputFile = inFile;
		replaceRefIndexList = new ArrayList<Pair>();
		// tokenList = new ArrayList<String>();
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
		singDemonList = Arrays.asList(singDemonArray);
		pluralList = Arrays.asList(pluralArray);
		spanIdHM = new HashMap<Pair, Pair>();
		roleIdHM = new HashMap<Pair, String>();
		typeIdHM = new HashMap<Pair, String>();
		headIdHM = new HashMap<Pair, String>();
		entityHM = new HashMap<Pair, String>();
		mentionIdList = new ArrayList<Pair>();
		idSpanHM = new HashMap<Pair, Pair>();
		indexHM = new HashMap<Integer, Integer>();
		entityHM = new HashMap<Pair, String>();
		startIndexList = new ArrayList<Integer>();
		startIdHM = new HashMap<Integer, Pair>();
		start_TokenHM = new HashMap<Integer, String>();
		start_TypeHM=new HashMap<Integer, String>();
	}

	public void extractEntity(org.w3c.dom.Document document) {
		Node child = document.getFirstChild();
		NodeList docChildList = child.getChildNodes();
		NodeList entNodeList = document.getElementsByTagName(ENTITY);
		String type, role = "", head, token;
		int start, end;
		for (int i = 0; i < entNodeList.getLength(); i++) {
			Node ithNode = entNodeList.item(i);
			String entType = this.getFirstChildByTagName(ithNode,
					ENTTYPE).getTextContent();
			// System.out.println("entType: "+entType);
			List<Node> mentionList = this.getChildrenByTagName(ithNode,
					ENTMENTION);
			for (int j = 0; j < mentionList.size(); j++) {
				Node mentionNode = mentionList.get(j);
				type = mentionNode.getAttributes().getNamedItem(TYPE)
				.getNodeValue();
				//System.out.println("type:::::::::::::::::::::: "+type);
				if (mentionNode.getAttributes().getNamedItem(ROLE) != null) {
					role = mentionNode.getAttributes().getNamedItem(
							ROLE).getNodeValue();
					if (role.equals("PER")) {
						role = "PERSON";
					}
					// System.out.println("role: "+role);
				} else {
					role = entType;
				}
				String[] idArray = mentionNode.getAttributes()
				.getNamedItem(ID).getNodeValue().split("-");
				Pair idPair = new Pair(idArray[0], idArray[1]);
				// System.out.println(this.getFirstChildByTagName(mentionNode,
				// EXTENT));
				// System.out.println(mentionNode.getChildNodes().getLength());
				NodeList childNodeList = mentionNode.getChildNodes();
				// for(int k=0;k<childNodeList.getLength();k++){
				// System.out.println(childNodeList.item(k));
				// }
				// Node charseqNode =
				// mentionNode.getFirstChild().getNextSibling().getFirstChild().getNextSibling();
				//Node charseqNode = mentionNode.getLastChild()
				//.getPreviousSibling().getFirstChild()
				//.getNextSibling();

				Node headNode = this.getFirstChildByTagName(mentionNode, HEAD);
				Node charseqNode = this.getFirstChildByTagName(headNode, CHARESEQ);


				//System.out.println("charseqNode: "+charseqNode.getNodeName()+" "+this.getFirstChildByTagName(charseqNode,
				//START));
				token = charseqNode.getFirstChild().getNextSibling()
				.getTextContent();
				System.out.println(token);
				token = token.substring(11, token.length() - 2);
				if (token.contains(" ")) {
					String[] tokenArray = token.split(" ");
					token = tokenArray[tokenArray.length - 1];
				}
				// start=this.getFirstChildByTagName(charseqNode,
				// START).getTextContent();
				end = Integer.valueOf(this.getFirstChildByTagName(
						charseqNode, END).getTextContent());
				//I use end-token.length()+1 so that the token can be unique and no repetition
				start = end - token.length() + 1;
				System.out.println("token: " + token + " start: "+ start + " end: " + end);
				Pair spanPair = new Pair(start, end);
				spanIdHM.put(idPair, spanPair);
				typeIdHM.put(idPair, type);
				roleIdHM.put(idPair, role);
				headIdHM.put(idPair, token.toLowerCase());
				indexHM.put(start, end);
				entityHM.put(idPair, entType);
				//startIdHM.put(start, idPair);
				//start_TokenHM.put(start, token);
				//start_TypeHM.put(start, type);
				startIndexList.add(start);
			}
		}
	}
	
	public void fillStartTokenHM(org.w3c.dom.Document document) {
		DocumentBuilder builder;
		AceReader aceReader = new AceReader();
		aceReader.processEntities(document);
		idMentionM=aceReader.idMentionM;
		Iterator<String> idmenIter = idMentionM.keySet().iterator();
		while(idmenIter.hasNext()){
			String menId = idmenIter.next();
			Mention mention =idMentionM.get(menId);
			String token = mention.getHeadCoveredText();
			int start = mention.getHeadSt();
			int end = mention.getHeadEd();
			System.out.println("fillstartTokenHM: "+start+" "+token);
			start_TokenHM.put(start, token);
			startIndexList.add(start);
		}
	}
	
	

	public void setTokenFeatures(PrintWriter pwOper, PrintWriter pwDist,
			PrintWriter pwGender, PrintWriter pwNumber, PrintWriter pwNe,
			PrintWriter pwRole) {
		Collections.sort(startIndexList);
		for (int i = 0; i < startIndexList.size(); i++) {
			int ithStart = startIndexList.get(i);
		}
	}

	public void setTokenDependFeatures(PrintWriter pwTokenNe) {
		// Set entries = map.entrySet();
		Iterator<Pair> itHm = roleIdHM.keySet().iterator();
		// Iterator it = entries.iterator();
		while (itHm.hasNext()) {
			Pair key = itHm.next();
			String role = roleIdHM.get(key);
			String token = headIdHM.get(key);
			// System.out.println(type+" "+role);
			pwTokenNe.println("Ne_W " + token + " : " + role);
		}
	}

	public StringBuffer readSgmFile(File sgmFile) {
		BufferedReader bfMuc;
		StringBuffer sbMuc = new StringBuffer();
		try {
			bfMuc = new BufferedReader(new FileReader(sgmFile));
			String line = "";
			try {
				while ((line = bfMuc.readLine()) != null) {
					line = line.replace("&", ":");
					if (line.startsWith(MTURN) || line.startsWith(MTIME)) {
						line = line.replace(">", "/>");
					}
					sbMuc.append(line + " ");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sbMuc;
	}

	// public StringBuffer createInputFiles(PrintWriter pwText,String
	// inputXMLString){
	// StringBuffer sbText=new StringBuffer();
	// sbText=createInputFiles(inputXMLString);
	// pwText.println(sbText.toString().toLowerCase().trim());
	// return sbText;
	// }

	public StringBuffer tokenizeInputFiles(String inputXMLString) {
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
				// String[] textArray = sbText.toString().split(" ");
				// tokenList=Arrays.asList(textArray);
				// char[] destination = new char[100];
				// sbText.getChars(65, 108, destination, 0);
				// textBuffer.getChars(65, 108, destination, 0);
				// System.out.println(destination);
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
		Collections.sort(startIndexList);
		int iniStart = textBuffer.indexOf(tokList.get(0));
		if (start_TokenHM.containsKey(iniStart)) {
			pwMention.println("Entity " + 0 + " : "
					+ tokList.get(0).toLowerCase() + " = 1.0");
		}
		fullTokIndHM.put(iniStart, tokList.get(0));
		
		// for(int j=0;j<startIndexList.size();j++){
		// int jthStart=startIndexList.get(j);
		// String mentionToken=start_TokenHM.get(jthStart);
		// System.out.println(jthStart+" "+mentionToken);
		// }
		int tempStart = iniStart;
		for (int i = 1; i < tokList.size(); i++) {
			String ithToken = tokList.get(i);
			int ithStart = textBuffer.indexOf(ithToken, tempStart);
			//the following is specifically designed for one special case, in fact, I think that this is bug of ACE corpus
			//in ABC19981116.1830.0759.sgm, so many times, words like black in fact which plays the role of adjective are annotated
			//as mention, I don't know why. In one place, the full word is all-black, but the annotation is black. So, I missed it.
			//it leads to the result of mismatching index of the key and the reponse. thus lead to the difficulty of evaluation.
			//no way, I try the following way to add it. sign!
			int temIthStart = -1;
			if(ithToken.contains("-")){
				temIthStart = ithStart+ithToken.indexOf("-")+1;
			}
			System.out.println("ithStart: " + ithStart+" ithToken: "+ithToken);
			fullTokIndHM.put(ithStart, ithToken);
			if (start_TokenHM.containsKey(ithStart) || start_TokenHM.containsKey(temIthStart)) {
				System.out.println("ithStart: " + ithStart+" "+ithToken.toLowerCase());
				//System.out.println("Entity " + i + " : " + ithToken + " = 1.0");
				pwMention.println("Entity " + i + " : "
						+ ithToken.toLowerCase() + " = 1.0");
				//very important!!!! tempStart should start from end of ithStart rather than begining.
				//If the latter and if the token has something common to it, the ithtoken will be read again
				//in addition, tempStart must be within the for loop!!!!!!!!!
				tempStart = ithStart+ithToken.length();
			}
			
//			else if(pronounList.contains(ithToken)||reflexiveList.contains(ithToken)){
//				System.out.println("Entity " + i + " : " + ithToken + " = 1.0");
//				pwMention.println("Entity " + i + " : "
//						+ ithToken.toLowerCase() + " = 1.0");
//			}
		//	System.out.println("ithStart: "+ithStart+" ithToken: "+ithToken);
			
			// System.out.println(tokList.get(i));
		}
	}

	public List<String> listMention(StringBuffer textBuffer,List<String> tokList){
		HashMap<Integer,String> fullTokIndHM=new HashMap<Integer,String>();
		Collections.sort(startIndexList);
		List<String> opList = new ArrayList<String>();
		int iniStart=textBuffer.indexOf(tokList.get(0));
		fullTokIndHM.put(iniStart,tokList.get(0));
		//System.out.println("iniStart: "+iniStart);
		if(start_TokenHM.containsKey(iniStart)){
			opList.add(NEW);
			//pwMention.println("Entity "+0+" : "+tokList.get(0).toLowerCase()+" = 1.0");
		}else{
			opList.add(COPY);
		}
		int tempStart=iniStart;
		for(int i=1;i<tokList.size();i++){
			String ithToken=tokList.get(i);
			int ithStart=textBuffer.indexOf(ithToken,tempStart);
			//System.out.println("ithStart: "+ithStart);
			fullTokIndHM.put(ithStart,ithToken);
			//System.out.println("type---------------------------!!!!!!!!!!!!!!!!!!!"+start_TypeHM.get(ithStart));
			//if(start_TokenHM.containsKey(ithStart) && (pronounList.contains(ithToken)||reflexiveList.contains(ithToken))){
			if(start_TokenHM.containsKey(ithStart) && (pronounList.contains(ithToken)||reflexiveList.contains(ithToken))&& (start_TypeHM.get(ithStart).equals(PRON))){
				opList.add(OLD);
			}else if(start_TokenHM.containsKey(ithStart)){
				opList.add(NEW);
			}else{
				opList.add(COPY);
			}
			//System.out.println("ithStart: "+ithStart+" ithToken: "+ithToken);
			tempStart=ithStart;
			//System.out.println(tokList.get(i));
		}
		return opList;
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

	// now, I need to consider tokenize words as required format

	public static void main(String[] args) throws Exception {
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
					String fileName = fileArray[i].getName();
					PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(dirOutput,fileName)));
					PrintWriter pwOuttext = new PrintWriter(new FileWriter(
							new File(dirOuttext, fileName)));
					ProcessACEDevJdom2 processACESgmJdom = new ProcessACEDevJdom2();
					StringBuffer sbDev = processACESgmJdom
					.readSgmFile(fileArray[i]);
					// StringBuffer
					//textBuffer=processACESgmJdom.createInputFiles(pwOuttext,sbDev.toString());
					StringBuffer textBuffer = processACESgmJdom
					.tokenizeInputFiles(sbDev.toString());
					List<String> tokenList = processACESgmJdom.tokenList;
					processACESgmJdom.printTextFile(pwOuttext, tokenList);
					System.out.println("tokenList.get(0): " + tokenList.get(0));
					for(int j=0;j<fileArray.length;j++){
						if(fileArray[j].getName().endsWith(".xml")){
							org.w3c.dom.Document document = null;
							DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
							DocumentBuilder builder;
							builder = factory.newDocumentBuilder();
							document = builder.parse(fileArray[j]);
							String xmlFileName = fileArray[j].getName();
							if(xmlFileName.contains(fileName)){
								ProcessACEDevJdom2 processACEXmlJdom = new
								ProcessACEDevJdom2(fileArray[j]);
								processACEXmlJdom.extractEntity(document);
								processACEXmlJdom.fillStartTokenHM(document);
								processACEXmlJdom.printInputFile(pwOutput,textBuffer,tokenList);
							}
						}
					}
					pwOutput.close();
					pwOuttext.close();
				}
			}
		} else {
			ProcessACEDevJdom2 processACEJdom = new ProcessACEDevJdom2(fileTag);
			System.out.println(fileTag+" "+fileTag.getParentFile().getParent());
			File dirOutput = new File(fileTag.getParentFile().getParent(),fileTag.getParentFile().getName() + "_input_entity");
			File dirOuttext = new File(fileTag.getParentFile().getParent(), fileTag.getParentFile().getName() + "_input_text");
			if (!dirOutput.exists()) {
				dirOutput.mkdir();
			}
			if (!dirOuttext.exists()) {
				dirOuttext.mkdir();
			}			
			String fileName = fileTag.getName();
			PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(dirOutput,fileName)));
			PrintWriter pwOuttext = new PrintWriter(new FileWriter(
					new File(dirOuttext, fileName)));
			ProcessACEDevJdom2 processACESgmJdom = new ProcessACEDevJdom2();
			StringBuffer sbDev = processACESgmJdom.readSgmFile(fileTag);
			// StringBuffer
			//textBuffer=processACESgmJdom.createInputFiles(pwOuttext,sbDev.toString());
			StringBuffer textBuffer = processACESgmJdom
			.tokenizeInputFiles(sbDev.toString());
			List<String> tokenList = processACESgmJdom.tokenList;
			processACESgmJdom.printTextFile(pwOuttext, tokenList);
			System.out.println("tokenList.get(0): " + tokenList.get(0));
			ProcessACEDevJdom2 processACEJdom2 = new ProcessACEDevJdom2(new File(fileTag.getParent(),fileTag.getName()+".tmx.rdc.xml"));
			org.w3c.dom.Document document = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			document = builder.parse(new File(fileTag.getParent(),fileTag.getName()+".tmx.rdc.xml"));
			processACEJdom2.extractEntity(document);
			processACEJdom2.fillStartTokenHM(document);
			processACEJdom2.printInputFile(pwOutput,textBuffer,tokenList);
			pwOutput.close();
			pwOuttext.close();
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
