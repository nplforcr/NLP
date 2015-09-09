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
import utils.Pair;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import corpusProcessor.FeaStruct;

public class ProcessACEDevJdom {

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
	boolean exOtherPron = false;
	List<String> otherPronounsList;

	int countTags = 0;
	// int countToken = 0;
	boolean isMarkable = false;
	boolean isCoref = false;
	boolean isEmbedded = false;
	static boolean debug = false;
	// to store the word token and its id;
	HashMap<String, String> tokenIdHM;
	HashMap<String, String> idTokenHM;
	public List<String> tokenList;
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
	public ProcessACEDevJdom() {
		String[] otherPronouns = {"you","yourself","youselves","yours","your","i","me","my","mine","we","our","ourselves","us",
				"'s","'s"};
		otherPronounsList=Arrays.asList(otherPronouns);
		tokenList = new ArrayList<String>();
	}

	public ProcessACEDevJdom(File inFile) throws ParserConfigurationException, SAXException, IOException {
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
		otherPronounsList = new ArrayList<String>();
		String[] otherPronouns = {"you","yourself","youselves","yours","your","i","me","my","mine","we","our","ourselves","us"};
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
		otherPronounsList=Arrays.asList(otherPronouns);
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

	public void extractEntity() {
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
						Node charseqNode = mentionNode.getLastChild()
						.getPreviousSibling().getFirstChild()
						.getNextSibling();
						// System.out.println("charseqNode: "+charseqNode.getNodeName()+" "+this.getFirstChildByTagName(charseqNode,
						// START));
	
						
						token = charseqNode.getFirstChild().getNextSibling().getTextContent();
						//the reason the token should start from 11 is due to the annotation looks like:
						// string = "sales ``help''". 
						//note: token here is usually one word since token is a head already. But some head 
						//may inivolve more than one word. 
						token = token.substring(11, token.length() - 2);
						if(token.contains("``")){
							System.out.println(token + " with bad quotes!");
							token=token.substring(2);
						}
						if (token.contains(" ")) {
							String[] tokenArray = token.split(" ");
							token = tokenArray[tokenArray.length - 1];
							//one special case here is some word include quotes. we should remove them in order 
							//extract the real word. If we don't do so, it will lead to troubles so that `` may be 
							//extract as the mention rather than the word.
							//System.out.println("token ================= "+token);
							if(token.contains("``")){
								//System.out.println(token + " with bad quotes!");
								token=token.substring(2);
							}
						}
						
						//the reason I use the first if clause is due to the reason that I think that first pronoun or second pronoun are more
						//in quotes, we may remove them for the present system. 
						//System.out.println(token+"++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
						if((otherPronounsList.contains(token.toLowerCase())||token.equals("'s")) && this.exOtherPron==true){
							//System.out.println(token+"are you here ??????????????????????????????????????????????????????????????????");
							continue;
						}else{
							//System.out.println("token ================= "+token);

							// start=this.getFirstChildByTagName(charseqNode,
							// START).getTextContent();
							//end is the end of bytespan
							end = Integer.valueOf(this.getFirstChildByTagName(
									charseqNode, END).getTextContent());
							//start is the start bytes of the token which is the head word of the phrase rather than the whole phrase.
							start = end - token.length() + 1;
							//						System.out.println("token: " + token + " start: "
							//								+ start + " end: " + end);
							Pair spanPair = new Pair(start, end);
							idSpanHM.put(idPair, spanPair);
							typeIdHM.put(idPair, type);
							roleIdHM.put(idPair, role);
							headIdHM.put(idPair, token.toLowerCase());
							indexHM.put(start, end);
							entityHM.put(idPair, entType);
							startIdHM.put(start, idPair);
							spanIdHM.put(spanPair, idPair);
							start_TokenHM.put(start, token);
							start_TypeHM.put(start, type);
							startIndexList.add(start);
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
		Iterator<Pair> iterIdSpanH = idSpanHM.keySet().iterator();
		if(debug){
			while(iterIdSpanH.hasNext()){
				Pair nextKeyPair = iterIdSpanH.next();
				System.out.println(nextKeyPair+""+idSpanHM.get(nextKeyPair));
			}
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

	public StringBuffer createInputFiles(String inputXMLString) {
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
					//in bnews: VOA19981218.1600.2688.sgm
					//the following case handle words like 1 1/2. It seems that MaxentTagger regards them as one word. 
					//but in HHMMParser, it regards it as two words. So, I need to combine them without space
					//yet, to our interest, MaxentTagger tags words like ma'am as three words as ma ' and am. So,
					//I need to combine ma ' and am as ma'am again. 
					for (int i = 0; i < sentence.size(); i++) {
						String word = sentence.get(i).word();
						if(word.contains(" ")){
							tokenList.add(word.split(" ")[0]);
							tokenList.add(word.split(" ")[1]);
							//System.out.println(word.split(" ")[0]+" : "+word.split(" ")[1]);
						}
						//else if(otherPronounsList.contains(word.toLowerCase())){
							//continue;
						//}
						else{
							tokenList.add(word);
							//System.out.println(word);
						}
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
		
		int tempStart = -1;
		int ithStart = 0;
		for(int i=0;i<tokenList.size();i++){
			String ithToken = tokenList.get(i);
			ithStart = textBuffer.indexOf(ithToken,tempStart);
			if(ithToken.equalsIgnoreCase("ma")){
				ithToken = "MA'am";
				if(ithStart!=-1){
					//in ABC19981104.1830.0516.sgm:
					tokenList.set(i, ithToken);
					tokenList.set(i+1, "[MA'am]");
					tokenList.set(i+2, "[MA'am]");
					i+=2;
				}
			}
			
			tempStart = ithStart+ithToken.length();
		}
		// in bnews ABC19981104.1830.0516.sgm:
		//since I add "[MA'am]" in tokenList only for keeping the operation going on and mark these words, now it is time to remove them.
		if(tokenList.contains("[MA'am]")){
			tokenList.remove("[MA'am]");
			tokenList.remove("[MA'am]");	
		}else if(tokenList.contains("%")){  ////in CNN19981012.2130.0981.sgm, 84% is tokenized as 84 and %. It is rediculous, I have to return two tokens as one, namely 84% again.
			int index = tokenList.indexOf("%");
			String theNum = tokenList.get(index-1);
			tokenList.set(index,theNum+"%");
			tokenList.remove(index-1);
		}

		
		return textBuffer;
	}

	public void printInputFile(PrintWriter pwMention, StringBuffer textBuffer,
			List<String> tokList) {
		HashMap<Integer, String> fullTokIndHM = new HashMap<Integer, String>();
		Collections.sort(startIndexList);
		String token = tokList.get(0);
		int iniStart = textBuffer.indexOf(tokList.get(0));
		if (startIndexList.contains(iniStart)&& !pronounList.contains(token) ) {

			String firstToken = start_TokenHM.get(iniStart);


			if(firstToken.contains("''")){
				firstToken=firstToken.substring(0,firstToken.length()-2);
			}else if(firstToken.contains("\"")){
				firstToken=firstToken.substring(0,firstToken.length()-2);
			}


			pwMention.println("Entity " + 0 + " : "
					+ firstToken.toLowerCase() + " = 1.0");
		}
		fullTokIndHM.put(iniStart, tokList.get(0));

		// for(int j=0;j<startIndexList.size();j++){
		// int jthStart=startIndexList.get(j);
		// String mentionToken=start_TokenHM.get(jthStart);
		// System.out.println(jthStart+" "+mentionToken);
		// }
		int tempStart = iniStart;
		//it is used to track how many entities have been printed out in order to avoid the first one 
		//is an entity
		int count = 0;
		int temIthStart = -1;
		int ithStart = -1;
		for (int i = 1; i < tokList.size(); i++) {
			String ithToken = tokList.get(i);
			
				
			if(ithToken.contains("\\")){
				int index = ithToken.indexOf("\\");
				ithToken = ithToken.substring(0,index)+ithToken.substring(index+1);
				//ithToken.replace("\\", "");
				ithStart = textBuffer.indexOf(ithToken, tempStart);
			}else if(ithToken.equalsIgnoreCase("-LRB-")){
				ithToken = "(";
				ithStart = textBuffer.indexOf(ithToken, tempStart);
			}else if(ithToken.equalsIgnoreCase("-RRB-")){
				ithToken=")";
				ithStart = textBuffer.indexOf(ithToken, tempStart);
			}
			else{
				ithStart = textBuffer.indexOf(ithToken, tempStart);
			}
			
			//the following is specifically designed for one special case, in fact, I think that this is bug of ACE corpus
			//in bnews ABC19981116.1830.0759.sgm, so many times, words like black in fact which plays the role of adjective are annotated
			//as mention, I don't know why. In one place, the full word is all-black, but the annotation is black. So, I missed it.
			//it leads to the result of mismatching index of the key and the response. thus lead to the difficulty of evaluation.
			//no way, I try the following way to add it. sign!
			
			if(ithToken.contains("-")){
				temIthStart = ithStart+ithToken.indexOf("-")+1;
			}
			if(debug){
				System.out.println(i+"thStart: " + ithStart+" ithToken: "+ithToken);
			}
			
			fullTokIndHM.put(ithStart, ithToken);
			//temIthStart in most cases is not useful since usually the annotation will annotate the whole phrase such as teng-hui is one name
			//so, the above condition may not lead to troubles. But now, the problem is if the token is - or --, troubles may arise. 
			//the entity file counts it as a word, but it seems that the text file does not. why? I know why since in printing text file, I 
			//add a condition to check if the token contains a -, then the token may be changed. If the whole word is -, it will not be prointed out. 
			//fix it right away. Nov. 30, 2010. 
			if (start_TokenHM.containsKey(ithStart)||start_TokenHM.containsKey(temIthStart)) {
				//System.out.println("ithStart: " + ithStart+" "+ithToken.toLowerCase());
				//System.out.println("Entity " + i + " : " + ithToken + " = 1.0");
				//I add the following lines is due to the fact that ithToken from xml file may be 
				//different from sgm file. For example, in xml file, New York-based is annotated as New York 
				//without based. But in sgm file, through Stanford Maxent tagger, New York-based is tokenzied 
				//as New and York-based. As a result, neWord.model will only include york as mention while the input file 
				//includes york-based as mention
				if(start_TokenHM.containsKey(ithStart)){
					ithToken=start_TokenHM.get(ithStart);
					if(ithToken.contains("''")){
						ithToken=ithToken.substring(0,ithToken.length()-2);
					}else if(ithToken.contains("\"")){
						ithToken=ithToken.substring(0,ithToken.length()-2);
					}
				}else if(start_TokenHM.containsKey(temIthStart)){
					ithToken=start_TokenHM.get(temIthStart);
					if(ithToken.contains("''")){
						ithToken=ithToken.substring(0,ithToken.length()-2);
					}else if(ithToken.contains("\"")){
						ithToken=ithToken.substring(0,ithToken.length()-2);
					}
				}
				
				//I know where the problem is since i, i+1, i+2 share the same content, we should remove i+1 and i+2
				if(count ==0  && pronounList.contains(ithToken)){
					continue;
				}else{
					if(debug){
						System.out.println("Entity " + i + " : "+ ithToken.toLowerCase());
					}
					
					pwMention.println("Entity " + i + " : "+ ithToken.toLowerCase() + " = 1.0");

				}

				//very important!!!! tempStart should start from end of ithStart rather than begining.
				//If the latter and if the token has something common to it, the ithtoken will be read again
				//in addition, tempStart must be within the for loop!!!!!!!!!
				
				count++;
			}

			//			else if(pronounList.contains(ithToken)||reflexiveList.contains(ithToken)){
			//				System.out.println("Entity " + i + " : " + ithToken + " = 1.0");
			//				pwMention.println("Entity " + i + " : "
			//						+ ithToken.toLowerCase() + " = 1.0");
			//			}
			//	System.out.println("ithStart: "+ithStart+" ithToken: "+ithToken);

			// System.out.println(tokList.get(i));
			//tempStart must be outside the if condtions, I changed it this before. But I don't why it went back to if conditions. 
			//temIthStart is only used for cases like all-black and in annotation, only black is annotated. 
			temIthStart = -1;
			tempStart = ithStart+ithToken.length();
		}
	}
	
	public void printTextFile(PrintWriter pwText, List<String> tokList) {
		int countWeirdQuotes = 0;
		while(tokenList.contains("``")){
			int index = tokenList.indexOf("``");
			tokenList.set(index, "\"");
			countWeirdQuotes++;
		}
		
		int count = 0;
		while(tokenList.contains("''")){
			int index = tokenList.indexOf("''");
			tokenList.set(index, "\"");
			count++;
		}
		
		for (int i = 0; i < tokList.size(); i++) {
			if(tokList.get(i).equals("[MA'am]")){
				continue;
			}
			//for the moment, I decide to comment this else if
			/**
			else if(tokList.get(i).contains("-")){ //we cannot always do this way. names like teng-hui, both parts are annoated. so, the text file include them. 
				String reducedToken=tokenList.get(i);
				reducedToken = reducedToken.substring(0,reducedToken.indexOf("-"));
				pwText.print(reducedToken.toLowerCase()+" ");
			}
			*/
			else{
				pwText.print(tokList.get(i).toLowerCase() + " ");
			}
			
		}
	}

	public List<String> debugIndex(StringBuffer textBuffer,	List<String> tokList) {
		if(debug){
			System.out.println("In debug: if you want to close debug and do evalutions, \n" +
			" please change the value of the global variable 'debug' in ProcessACEDevJdom.java to false");
		}
		
		
		List<String> bsTokenList = new ArrayList<String>();
		HashMap<Integer, String> fullTokIndHM = new HashMap<Integer, String>();
		Collections.sort(startIndexList);
		String token = tokList.get(0);
		int iniStart = textBuffer.indexOf(tokList.get(0));
		if (startIndexList.contains(iniStart)&& !pronounList.contains(token) ) {
			//System.out.println("Entity "+ tokList.get(0).toLowerCase());
			//System.out.println(startIdHM.get(iniStart)+" "+idSpanHM.get(startIdHM.get(iniStart)));
			bsTokenList.add(idSpanHM.get(startIdHM.get(iniStart))+" "+tokList.get(0).toLowerCase());
		}
		fullTokIndHM.put(iniStart, tokList.get(0));

		// for(int j=0;j<startIndexList.size();j++){
		// int jthStart=startIndexList.get(j);
		// String mentionToken=start_TokenHM.get(jthStart);
		// System.out.println(jthStart+" "+mentionToken);
		// }
		int tempStart = iniStart;
		//it is used to track how many entities have been printed out in order to avoid the first one 
		//is an entity
		int count = 0;
		int temIthStart = -1;
		int ithStart = -1;
		for (int i = 1; i < tokList.size(); i++) {
			String ithToken = tokList.get(i);
			if(ithToken.equalsIgnoreCase("ma")){
				ithToken = "MA'am";
				ithStart = textBuffer.indexOf(ithToken, tempStart);
				if(ithStart!=-1){
					tokList.set(i, ithToken);
					tokList.set(i+1, "[MA'am]");
					tokList.set(i+2, "[MA'am]");
					i+=2;
				}
			}else if(ithToken.contains("\\")){
				int index = ithToken.indexOf("\\");
				ithToken = ithToken.substring(0,index)+ithToken.substring(index+1);
				//ithToken.replace("\\", "");
				ithStart = textBuffer.indexOf(ithToken, tempStart);
			}else{
				ithStart = textBuffer.indexOf(ithToken, tempStart);
			}
			
			//the following is specifically designed for one special case, in fact, I think that this is bug of ACE corpus
			//in ABC19981116.1830.0759.sgm, so many times, words like black in fact which plays the role of adjective are annotated
			//as mention, I don't know why. In one place, the full word is all-black, but the annotation is black. So, I missed it.
			//it leads to the result of mismatching index of the key and the reponse. thus lead to the difficulty of evaluation.
			//no way, I try the following way to add it. sign!
			
			if(ithToken.contains("-")){
				temIthStart = ithStart+ithToken.indexOf("-")+1;
			}
			//System.out.println("ithStart: " + ithStart+" ithToken: "+ithToken);
			fullTokIndHM.put(ithStart, ithToken);
			if (start_TokenHM.containsKey(ithStart) || start_TokenHM.containsKey(temIthStart)) {
				//System.out.println("ithStart: " + ithStart+" "+ithToken.toLowerCase());
				//System.out.println("Entity " + i + " : " + ithToken + " = 1.0");

				if(count ==0  && pronounList.contains(ithToken)){
					continue;
				}else{
					//System.out.println(startIdHM.get(ithStart)+" "+idSpanHM.get(startIdHM.get(ithStart)));
					if(debug){
						System.out.println("Entity "+" ithStart:  "+ithStart+" "+startIdHM.get(ithStart)+" "+ithToken);						
					}
					
					bsTokenList.add(idSpanHM.get(startIdHM.get(ithStart))+" "+ithToken.toLowerCase());
				}

				//very important!!!! tempStart should start from end of ithStart rather than begining.
				//If the latter and if the token has something common to it, the ithtoken will be read again
				//in addition, tempStart must be within the for loop!!!!!!!!!
				
				count++;
			}
			
			temIthStart = -1;
			tempStart = ithStart+ithToken.length();
		}
		return bsTokenList;
	}
	
	/**
	 * 
	 * @param textBuffer
	 * @param tokList
	 * @return
	 * we get opList from the following function based on the entity annotations. We plan to remove copy in the resolver model.
	 * But it seems Ok to keep copy in opList. Maybe it is useful for keeping copy for some future development. 
	 */
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
			if(start_TokenHM.containsKey(ithStart) && (pronounList.contains(ithToken)||reflexiveList.contains(ithToken))){//&& (start_TypeHM.get(ithStart).equals(PRON))){
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
		File outputFile = new File(args[1]);
		//PrintWriter pwOutput = new PrintWriter(new FileWriter(outputFile));
		if (fileTag.isDirectory()) {
			File dirOutput = new File(outputFile,fileTag.getName() + "_input_entity");
			File dirOuttext = new File(outputFile, fileTag.getName() + "_input_text");
			if (!dirOutput.exists()) {
				dirOutput.mkdirs();
			}
			if (!dirOuttext.exists()) {
				dirOuttext.mkdirs();
			}
			String text = "";
			// PrintWriter pwTokenNe = new PrintWriter(new FileWriter(new
			// File(dirOutput,fileTag.getName()+".nemodel"),true));
			File[] fileArray = fileTag.listFiles();
			for (int i = 0; i < fileArray.length; i++) {
				//System.out.println(fileArray[i]);
				if (fileArray[i].getName().endsWith(".sgm")) {
					String fileName = fileArray[i].getName();
					PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(dirOutput,fileName)));
					PrintWriter pwOuttext = new PrintWriter(new FileWriter(
							new File(dirOuttext, fileName)));
					ProcessACEDevJdom processACESgmJdom = new ProcessACEDevJdom();
					StringBuffer sbDev = processACESgmJdom
					.readSgmFile(fileArray[i]);
					// StringBuffer
					//textBuffer=processACESgmJdom.createInputFiles(pwOuttext,sbDev.toString());
					StringBuffer textBuffer = processACESgmJdom
					.createInputFiles(sbDev.toString());
					List<String> tokenList = processACESgmJdom.tokenList;
					
					for(int j=0;j<fileArray.length;j++){
						if(fileArray[j].getName().endsWith(".xml")){
							String xmlFileName = fileArray[j].getName();
							if(xmlFileName.contains(fileName)){
								ProcessACEDevJdom processACEXmlJdom = new
								ProcessACEDevJdom(fileArray[j]);
								processACEXmlJdom.extractEntity();
								processACEXmlJdom.printInputFile(pwOutput,textBuffer,tokenList);
							}
						}
					}
					processACESgmJdom.printTextFile(pwOuttext, tokenList);
					//System.out.println("tokenList.get(0): " + tokenList.get(0));
					pwOutput.close();
					pwOuttext.close();
				}
			}
		} 
		else {
			ProcessACEDevJdom processACEJdom = new ProcessACEDevJdom(fileTag);
			//System.out.println(fileTag+" "+fileTag.getParentFile().getParent());
			File dirOutput = null;
			File dirOuttext = null;
			if(!debug){
				dirOutput = new File(outputFile,fileTag.getParentFile().getName() + "_input_entity");
				dirOuttext = new File(outputFile, fileTag.getParentFile().getName() + "_input_text");
				//dirOutput = new File(fileTag.getParentFile().getParent(),fileTag.getParentFile().getName() + "_input_entity");
				//dirOuttext = new File(fileTag.getParentFile().getParent(), fileTag.getParentFile().getName() + "_input_text");
				if (!dirOutput.exists()) {
					dirOutput.mkdir();
				}
				if (!dirOuttext.exists()) {
					dirOuttext.mkdir();
				}
			}
						
			//System.out.println(dirOuttext.getAbsolutePath());
			String fileName = fileTag.getName();
			PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(dirOutput,fileName)));
			PrintWriter pwOuttext = new PrintWriter(new FileWriter(
					new File(dirOuttext, fileName)));
			ProcessACEDevJdom processACESgmJdom = new ProcessACEDevJdom();
			StringBuffer sbDev = processACESgmJdom.readSgmFile(fileTag);
			// StringBuffer
			//textBuffer=processACESgmJdom.createInputFiles(pwOuttext,sbDev.toString());
			StringBuffer textBuffer = processACESgmJdom
			.createInputFiles(sbDev.toString());
			List<String> tokenList = processACESgmJdom.tokenList;
			
			//System.out.println("tokenList.get(0): " + tokenList.get(0));
			ProcessACEDevJdom processACEJdom2 = new ProcessACEDevJdom(new File(fileTag.getParent(),fileTag.getName()+".tmx.rdc.xml"));
			processACEJdom2.extractEntity();
			if(!debug){
				processACEJdom2.printInputFile(pwOutput,textBuffer,tokenList);
				processACESgmJdom.printTextFile(pwOuttext, tokenList);
				pwOutput.close();
				pwOuttext.close();
			}else{
				processACEJdom2.debugIndex(textBuffer, tokenList);
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
