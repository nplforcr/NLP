package reutersProcessor;
import java.io.*;


/*
 * @ author Dingcheng Li
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

public class ProcessReutersOct27 {

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
	static String FUNC ="FUNC";
	static String SUBJ = "SUBJ";
	static String OBJ = "OBJ";
	static String P = "P";
	static String LEXEM = "TokenType.LEXEME";
	static String COPY = "copy";
	static String OLD = "old";
	static String NEW = "new";
	static String ELEMENT = "Element"; 
	static String TID = "id";
	static String CID = "ID";
	static String ID = "ID";
	static String REF = "REF";
	static String SRC ="SRC";
	static String nullString = "ZERO";
	static String POS = "POS";
	static int MAXNUM = 8;
	static String start = "-";	
	int tokenIndex = 0;
	List<FeaStruct> indStructList;
	List<Pair> wordPosPairList;
	List<String> stopWordList;
	
	int countTags = 0;
	//int countToken = 0;
	boolean isMarkable = false;
	boolean isCoref = false;
	boolean isEmbedded = false;
	//HashMap<String, Integer> tokenIdHM;
	List<String> tokenList;
	HashMap<String, Integer> replaceRefIndexHM;
	HashMap<String, String> markableHeadWordHM;
	HashMap<String,String> markableSrcHM;
	
	
	public ProcessReutersOct27() {
		indStructList = new ArrayList<FeaStruct>();
		wordPosPairList = new ArrayList<Pair>();
		tokenList = new ArrayList<String>();
		//tokenIdHM = new HashMap<String,Integer>();
		replaceRefIndexHM = new HashMap<String, Integer>();
		markableHeadWordHM = new HashMap<String, String>();
		markableSrcHM = new HashMap<String, String>();
		stopWordList=new ArrayList<String>();
		stopWordList.add("CC");
		stopWordList.add("DN");
	}
	
	public void assignTokenIndex( Node rootNode) {
		Stack<Node> stack = new Stack<Node>();
		// ignore root -- root acts as a container, here, the rootNode should be a sentNode.
		Node node=rootNode.getFirstChild();
		while (node!=null) {
			String tokenId = "";
			if(node.getNodeName().equals(WORD)){
				tokenId = node.getAttributes().getNamedItem(ID).getNodeValue();
				//tokenIdHM.put(tokenId, tokenIndex);
				//Pair tokenIdPair = new Pair(tokenId,tokenIndex);
				tokenList.add(tokenId);
				tokenIndex++;
				
				////System.out.println(node.getTextContent()+" "+tokenId+" "+tokenIndex);
			}else if(node.getNodeName().equals(PUNC)){
				//I will consider the case where wordNode.getPreviousSibling is null, later whereas, this case is rare.
				//haha, not rare, S7 starts with _
				tokenId = this.getPuncTokenId(node);
				//tokenIdHM.put(tokenId, tokenIndex);
				//Pair tokenIdPair = new Pair(tokenId,tokenIndex);
				tokenList.add(tokenId);
				tokenIndex++;
				
				////System.out.println(node.getTextContent()+" "+tokenId+" "+tokenIndex);
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
	
//"/project/nlp/dingcheng/nlplab/corpora/bukavu/381790newsML-done.xml.xml"
	
	public void splitMarkableNode(String inputFile) {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		File f = new File(inputFile);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(f);
				Node child = document.getFirstChild();
				NodeList docChildList = child.getChildNodes();
				NodeList senNodeList = document.getElementsByTagName(SENTENCE);
				for (int i = 0; i < senNodeList.getLength(); i++) {
					Node ithNode = senNodeList.item(i);
					////System.out.println(i+" ithNode "+ithNode.getNodeName());
					this.assignTokenIndex(ithNode);
				}
				for (int i = 0; i < senNodeList.getLength(); i++) {
					Node ithNode = senNodeList.item(i);
					//this.preorderTraverse(ithNode,replaceRefIndexHM,markableHeadWordHM);
					this.preorderTraverse(ithNode);
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
	 * Performs full tree traversal using stack. rootNode here is a sentence node.
	 */
	public void preorderTraverse(Node rootNode) {
		Stack<Node> stack = new Stack<Node>();
		// ignore root -- root acts as a container, here, the rootNode should be a sentNode.
		Node node=rootNode.getFirstChild();
		List<Pair> tempList = new ArrayList<Pair>();
		List<Pair> trackList = new ArrayList<Pair>();
		while (node!=null) {
			// print node information
			////System.out.println( node.getNodeName()+"="+node.getNodeValue());
			if(node.getNodeName().equals(MARKABLE)){
				List<Node> eleWordList = new ArrayList<Node>();
				String markableID = node.getAttributes().getNamedItem(ID).getNodeValue();
				int markableHeight = this.getHeight(node)-2;				
				this.processMarkableNode(node,tempList,trackList);
			}else if(node.getNodeName().equals(COREF)){
				//this implies that its parent must be a MARKABLE we need to record its SRC and its Parent ID
				//but it seems that we don't need to do anything here since it will be handled in markable node as well
				Node parentNode = node.getParentNode();
				String markableId = parentNode.getAttributes().getNamedItem(ID).getNodeValue();
				String srcStr = node.getAttributes().getNamedItem(SRC).getNodeValue();
				if(!markableSrcHM.containsKey(markableId)){
					markableSrcHM.put(markableId, srcStr);  
				}
			}else if(node.getNodeName().equals(WORD) || node.getNodeName().equals(PUNC)){
				Node parentNode = node.getParentNode();
				String refId = "";
				String oper = COPY;
				String markableId = "";
				if(parentNode.getNodeName().equals(SENTENCE)){	
					this.switchCountToken(node, tempList, trackList, oper, markableId,refId);
					//cool! what I have learned here is that we must empty tempList and trackList here
					//not any other places!! I cannot delete the two parameters from processMarkalbeNode as well
					//a good lesson here. Go ahead, victory is nearby!
					tempList = new ArrayList<Pair>();
					trackList = new ArrayList<Pair>();
					//countToken++;
					//System.out.println("countToken, op=copy: "+countToken+" word: "+node.getTextContent());
				}
				//else implies that the parent should be MARKABLE, it seems that I don't need to do anything
				//in this case since the word will be handled in markable node.
//				else{};
				String nodeName = this.extractInfor(node);
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

	public void switchCountToken(Node node,List<Pair> tempList, List<Pair> trackList,String oper,String markableId,String refId){
		int countToken = 0;
		String tokenId = "";
		int tempToken = -1;
		String kidNodeName="";
		if(node.getNodeName().equals(WORD)){
			tokenId=node.getAttributes().getNamedItem(ID).getNodeValue();
			countToken=tokenList.indexOf(tokenId);
		}else{
			//I will consider the case where wordNode.getPreviousSibling is null, later whereas, this case is rare.
			tokenId = this.getPuncTokenId(node);
			countToken=tokenList.indexOf(tokenId);
		}
		
		//how to start again the method after smaller countToken is done is a problem.
		if(countToken>indStructList.size()){
			//String kidNodeName = this.extractInfor(wordNode);
			
			if(tempList.size()>0){
				this.processTempList(tempList, trackList, markableId);
				if(oper==COPY){
					this.operIsCopy(countToken);
				}else if(oper==OLD){
					this.operIsOld(countToken, refId);
				}else{
					this.operIsNew(countToken, markableId);
				}			
				//System.out.println("value of K: "+k+" countToken, markable high 1, coref=true and op=old: "+countToken+" word: "+wordNode.getTextContent()+" markableID "+markableId);
			}else{
				System.out.println(countToken+" oper:  "+oper+"is the word going here:"+node.getTextContent());
				Pair indexOpPair = new Pair(countToken,oper);
				//System.out.println("the first case that countToken is larger than size:"+indexOpPair.toString());
				Pair trackPair = new Pair(countToken,node);
				//it seems that tempList always has one member. If so, I can use a String to replace it. 
				tempList.add(indexOpPair);
				trackList.add(trackPair);
			}
			//tempToken = countToken;
		}else if(countToken==indStructList.size()){
			kidNodeName = this.extractInfor(node);
			countToken = this.operIsCopy(countToken);
		//	System.out.println("the first case that countToken is larger than size:");
		}else{
			if(oper==COPY){
				this.operIsCopy(tempToken);
			}else if(oper==OLD){
				this.operIsOld(tempToken, refId);
			}else{
				this.operIsNew(tempToken, markableId);
			}
			tempToken = countToken;
		}	
		System.out.println("countToken, op=copy: "+countToken+" word: "+node.getTextContent());
	}
	
	public void makeDumHead(String oper,Node markableNode,String refId){
		NamedNodeMap markableMap = markableNode.getAttributes();
		String markableId = markableMap.getNamedItem(ID).getNodeValue();
		Node firstKidMarkable=this.getFirstChildByTagName(markableNode, MARKABLE);
		List<Node> wordKidList=this.getChildrenByTagName(firstKidMarkable, WORD);
		Node kidHeadWordNode = this.findHeadword(wordKidList);
		String kidHeadWord = "";
		String kidHeadWordId = "";
		if(kidHeadWordNode!=null){
			kidHeadWord = (String) kidHeadWordNode.getTextContent();
			kidHeadWordId = (String)kidHeadWordNode.getAttributes().getNamedItem(ID).getNodeValue();				
		}
		//tokenId=wordNode.getAttributes().getNamedItem(ID).getNodeValue();					
		//tokenId=firstKidMarkable.getAttributes().getNamedItem(ID).getNodeValue();
		//int countToken = tokenIdHM.get(kidHeadWordId);
		int countToken = tokenList.indexOf(kidHeadWordId);
		System.out.println("countToken in the dumHead: "+countToken);
		String kidNodeName = this.extractInfor(kidHeadWordNode);
		//countToken = this.operIsCopy(countToken,indStructList);
		//markableHeadWordHM.put(markableId,String.valueOf(countToken));
		//countToken = this.operIsNew(countToken, markableId,replaceRefIndexHM,indStructList);
		
		if(oper==OLD){
			this.operIsOld(countToken,refId);
		}else if(oper==NEW){
			this.operIsNew(countToken, markableId);
		}
		System.out.println("from makeDumHead: "+kidHeadWord);
		String dumId = "dum-"+kidHeadWordId;
		tokenList.add(countToken, dumId);
		markableHeadWordHM.put(markableId, String.valueOf(countToken));
	}
	
	
	public void loopMarkableKidNode(Node markableNode,String refId,String headWord,List<Pair> tempList,List<Pair> trackList,Node corefNode){
		NamedNodeMap markableMap = markableNode.getAttributes();
		String markableId = markableMap.getNamedItem(ID).getNodeValue();
		NodeList kidList = markableNode.getChildNodes();
		String oper = "";
		for (int k = 0; k < kidList.getLength(); k++) {
			//we can know the order information based the inex k, but how to incorperate it into 
			//countoken, need to have a good design. 

			Node wordNode = kidList.item(k);
			String word = wordNode.getTextContent();
			String tokenId = "";
			//System.out.println("kidList size in loopMarkableKidNode: "+kidList.getLength()+" wordNode:"+wordNode.getNodeName());
			if (word.equals(headWord) && !word.isEmpty()) {				
				tokenId=wordNode.getAttributes().getNamedItem(ID).getNodeValue();
				//System.out.println("word: "+word+" tokenId in loop methods: "+tokenId);
				int countToken = 0;
				countToken=tokenList.indexOf(tokenId);
				markableHeadWordHM.put(markableId,String.valueOf(countToken));
				
				if(countToken>indStructList.size()){
					//String kidNodeName = this.extractInfor(wordNode);
					//System.out.println(countToken+" oper:  "+OP+" countToken when size is larger than indStructlist");
					if(tempList.size()>0){
						System.out.println("countToken and oper in loopMakralbeKidNode when word is the head: "+countToken+oper+" tokenId: "+tokenId+" tempList size and content:"+tempList.size()+" "+tempList.get(0));
						//processTempList will handle all tempList
						this.processTempList(tempList, trackList, markableId);
						//System.out.println(tempList.size()+" "+tempList.get(0));
						
						//after tempList is done, then go back to the present word node.
//						tempList = new ArrayList<Pair>();
//						trackList = new ArrayList<Pair>();
						if(countToken>indStructList.size()){
							Pair indexOpPair = new Pair(countToken,oper);
							System.out.println("in loopMakralbeKidNode: "+countToken+oper);
							//Pair trackPair = new Pair(refId,wordNode);
							Pair trackPair = new Pair(countToken,new Pair(refId,wordNode));
							tempList.add(indexOpPair);
							trackList.add(trackPair);
							if(corefNode!=null){
								oper=OLD;
							}else{
								oper=NEW;
							}
							for(int j=k+1;j<kidList.getLength();j++){
								Node remWordNode = kidList.item(j);
								String remTokenId=remWordNode.getAttributes().getNamedItem(ID).getNodeValue();
								//System.out.println("word: "+word+" tokenId in loop methods: "+tokenId);
								String remOper = COPY;
								int remCountToken = 0;
								remCountToken=tokenList.indexOf(remTokenId);
								Pair remIndexPair = new Pair(remCountToken,remOper);
								Pair remTrackPair = new Pair(countToken,remWordNode);
								tempList.add(remIndexPair);
								trackList.add(remTrackPair);
							}
						}else{
							if(corefNode!=null){
								this.operIsOld(countToken,refId);
							}else{
								
								this.operIsNew(countToken, markableId);
							}
							
						}
						
						//System.out.println("value of K: "+k+" countToken, markable high 1, coref=true and op=old: "+countToken+" word: "+wordNode.getTextContent()+" markableID "+markableId);
					}else{
						//since the word is the headword, we should know if the oper is new or old based on corefNode
						System.out.println("countToken and oper in loopMakralbeKidNode when word is the head: "+countToken+oper);
						if(corefNode!=null){
							oper=OLD;
						}else{
							oper=NEW;
						}
						Pair indexOpPair = new Pair(countToken,oper);
						System.out.println("in loopMakralbeKidNode: "+countToken+oper);
						//Pair trackPair = new Pair(refId,wordNode);
						Pair trackPair = new Pair(countToken,new Pair(refId,wordNode));
						tempList.add(indexOpPair);
						trackList.add(trackPair);
						for(int j=k+1;j<kidList.getLength();j++){
							Node remWordNode = kidList.item(j);
							String remTokenId=remWordNode.getAttributes().getNamedItem(ID).getNodeValue();
							//System.out.println("word: "+word+" tokenId in loop methods: "+tokenId);
							String remOper = COPY;
							int remCountToken = 0;
							remCountToken=tokenList.indexOf(remTokenId);
							Pair remIndexPair = new Pair(remCountToken,remOper);
							Pair remTrackPair = new Pair(countToken,remWordNode);
							tempList.add(remIndexPair);
							trackList.add(remTrackPair);
						}
					}
					System.out.println("value of K: "+k+" countToken, markable high 1, coref=true and op=old: "+countToken+" word: "+wordNode.getTextContent()+" markableID "+markableId);
				}else{
					//System.out.println("node value infor in loopMakralbleKidNode: "+wordNode.getTextContent()+" what is it?? "+" "+wordNode.getAttributes().getNamedItem(ID).getNodeValue());
					//String kidNodeName = this.extractInfor(wordNode);
					//countToken = this.operIsCopy(countToken,indStructList);
					//markableHeadWordHM.put(markableId,String.valueOf(countToken));
					//countToken = this.operIsNew(countToken, markableId,replaceRefIndexHM,indStructList);
					if(corefNode!=null){
						System.out.println("countToken in operIsOld of loop Method"+countToken);
						this.operIsOld(countToken,refId);
						
					}else{
						this.operIsNew(countToken, markableId);
					}
					//System.out.println("value of K: "+k+" countToken, markable high 1, coref=true and op=old: "+countToken+" word: "+wordNode.getTextContent()+" markableID "+markableId);
				}
				//markableHeadWordHM.put(markableId,String.valueOf(countToken));
				////System.out.println("value of K: "+k+" countToken, markable high 1, coref=true and op=old: "+countToken+" word: "+wordNode.getTextContent()+" markableID "+markableId);
				//countToken = this.operIsOld(countToken, refId,markableHeadWordHM,replaceRefIndexHM,indStructList);
				System.out.println("value of K: "+k+" countToken, markable high 1, coref=true and op=old: "+countToken+" word: "+wordNode.getTextContent()+" markableID "+markableId);
			} else if(wordNode.getNodeName().equals(WORD) ||wordNode.getNodeName().equals(PUNC)) {
				oper = COPY;
				System.out.println("wordNode:"+wordNode.getTextContent());
				this.switchCountToken(wordNode, tempList, trackList, oper, markableId, refId);	
			}
			System.out.println("word: "+word+" tokenId in loop methods: "+tokenId);
		}
		
	}
	
	public void processMarkableNode(Node markableNode,List<Pair> tempList,List<Pair> trackList){
		Node corefNode = this.getFirstChildByTagName(markableNode, COREF);
		List<Node> eleWordList =  this.getChildrenByTagName(markableNode, WORD);
		List<Node> puncList = this.getChildrenByTagName(markableNode, PUNC);
		String headWord = "";
		String headNode = null;
		String oper = "";
		String refId = "";
		Node headWordNode = this.findHeadword(eleWordList);
		if(!eleWordList.isEmpty()){
			if(headWordNode!=null){
				headWord = (String) headWordNode.getTextContent();
			}
			
		}
		
		//System.out.println("node: "+markableNode.getAttributes().getNamedItem(ID)+" headword: "+headWord);
//		NamedNodeMap markableMap = markableNode.getAttributes();
//		String markableId = markableMap.getNamedItem(ID).getNodeValue();
		//List<Pair> tempList=new ArrayList<Pair>();
		// that is what I should start from here, COREF helps us to find previous MARKABLE tag
		// and find its headword.
		if (corefNode != null) {
			// find the head word, in this case, a new markable comes up,
			// but if the markable is composed of a few words.we should find the head 
			// word as the representation of the markable. In Reuters, we can find the head word by
			// the attribute DEP. what I need here is the countIndex of the head word.
			NamedNodeMap corefMap = corefNode.getAttributes();
			refId = corefMap.getNamedItem("SRC").getNodeValue();
			//System.out.println("value of SRC: "+corefMap.getNamedItem("SRC").getNodeValue());
			//String headWord = this.findHeadword(eleWordList);
			//for (int k = 0; k < eleWordList.size(); k++) {
			if(headWord==""){
				oper = OLD;
				this.makeDumHead(oper, markableNode, refId);
			}
			this.loopMarkableKidNode(markableNode, refId, headWord, tempList, trackList,corefNode);
		} else {
			NamedNodeMap markableMap = markableNode.getAttributes();
			String markableId = markableMap.getNamedItem(ID).getNodeValue();
			if(headWord==""){
				oper = NEW;
				this.makeDumHead(oper, markableNode, refId);
			}
			System.out.println("what is the headword from else of processMarkableNode: "+headWord);
			this.loopMarkableKidNode(markableNode, refId, headWord, tempList, trackList,corefNode);
		}
	}
	
	public void printIndStructList(){
		for(int i=0;i<indStructList.size();i++){
			FeaStruct indStruct = indStructList.get(i);
			System.out.println("ind: "+indStruct.getFea()+"indP: "+indStruct.getFeaP()+"op: "+indStruct.getOper());
		}
	}
	
	public int operIsNew (int count,String markableId){
		String oper = NEW;
		String ind = "-";
		String indP = "-";
		String[] indPArray = null;
		//Pair markableCountPair = new Pair(count,markableId);
		List<String> indPArrayList = new ArrayList<String>();
		//this is the first state when indStructList is empty, then, curInd is the present count
		//and the indP should be "-"
		if(indStructList.isEmpty()){
			//ind = tokenId;
			ind = String.valueOf(count);
			FeaStruct indStruct = new FeaStruct(NEW, indP, ind);
			//System.out.println("the first mention count: "+count+" "+oper+" "+ind+" "+indP);
			indStructList.add(indStruct);
			System.out.println("the first mention count: "+count+" "+oper+" "+indP+" "+ind);
			//this is first, ind = count and it is in the first position
			//Whereas, if the begining of the news story is not a new entity, this if will be skipped
			replaceRefIndexHM.put(ind, 0);
			//replaceRefIndexHM.put(markableCountPair, 0);
			//replaceRefIndexHM.put(tokenId, 0);
			//////System.out.println("new state: "+OP+" "+tokenId+"  "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
		}else{
			////////System.out.println("new state: "+ "coref is "+coref+" indStructList size: "+indStructList.size());
			//indP = indStrList.get(Integer.valueOf(tokenId)-1).getFea();
			indP = indStructList.get(count-1).getFea();
			indPArray = indP.split("_");
			//since Arrays.aslist(array) cannot change size, not so good.
			//so, I changed it to an ArrayList.
			//indPArrayList = Arrays.asList(indPArray);
			for(String indPArrayStr:indPArray){
				indPArrayList.add(indPArrayStr);
			}
			////////System.out.println("indPArrayList in new state: "+indPArrayList.size());
			
			//if size of indPArrayList is equal to MAXNUM, cut the first item off.  
			if(indPArrayList.size()==MAXNUM){
				String firstStr = indPArrayList.get(0);
				//now, I need to change the related values of the replaceRefIndexHM
				//it should be straightforward since for each cutoff tokenId, I will change its 
				//value, i.e. replaceIndex to 0+MAXNUM and the old one which has value MAXNUM will be changed
				//MAXNUM+1 and all of those which values are larger than MAXNUM will be added one 
				//sequentially, meanwhile, those with values which are smaller than MAXNUM will be deducted 
				//one sequentially
				//replaceRefIndexHM.get(firstStr);
				Iterator<String> it = replaceRefIndexHM.keySet().iterator();
				//Iterator<Pair> it = replaceRefIndexHM.keySet().iterator();
				while(it.hasNext()) { 
					String key = it.next();
					//Pair key = it.next();
					int val = replaceRefIndexHM.get(key);
					if(val==0){
						val=val+MAXNUM;
						replaceRefIndexHM.put(key, val);
					}else if(val>0 & val<MAXNUM){
						val=val-1;
						replaceRefIndexHM.put(key, val);
					}else{
						val=val+1;
						replaceRefIndexHM.put(key, val);
					}
				} 
				
				int indexFirstUndline = indP.indexOf("_");
				//the following substring method, +1 is very important. If not, split will 
				//not regard the first "_" as splitter, thus, the size will be 9, then, it will
				//enter into else, the above check is meaningless then.
				String indPSub = indP.substring(indexFirstUndline+1);
				//ind = indPSub +"_"+ tokenId;
				ind = indPSub+"_"+count;
				FeaStruct indStruct = new FeaStruct(NEW, indP, ind);
				indStructList.add(indStruct);
				System.out.println("new state more than 8  count: "+count+" "+oper+" "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
				//put the new added tokenId to MAXNUM-1, that is, it is in the last position of ind
				//replaceRefIndexHM.put(tokenId, MAXNUM-1);
				replaceRefIndexHM.put(String.valueOf(count), MAXNUM-1);
				//replaceRefIndexHM.put(markableCountPair, MAXNUM-1);
				//System.out.println("new state more than 8: "+oper+"  "+indP+" "+ind+" indStruct Size: "+ indStrList.size());
				
			}
			//size is fewer than 8
			else{
				////////System.out.println("how do you escape the check in the first if????");
				//ind = indP +"_"+ tokenId;
				if(indP=="-"){
					ind = String.valueOf(count);
				}else{
					ind = indP+"_"+count;
				}
				
				FeaStruct indStruct = new FeaStruct(NEW, indP, ind);
				indStructList.add(indStruct);
				
				System.out.println("new state fewer than 8  count: "+count+" "+oper+" "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
				//replaceRefIndexHM.put(tokenId,indPArrayList.size());
				if(indP=="-"){
					replaceRefIndexHM.put(String.valueOf(count),indPArrayList.size()-1);
				}else{
					replaceRefIndexHM.put(String.valueOf(count),indPArrayList.size());
				}
				//replaceRefIndexHM.put(markableCountPair, indPArrayList.size());
				//System.out.println("new state fewer than 8: "+"  indPArrayList size: "+indPArrayList.size()+" "+indP+" "+ind+" indStruct Size: "+ indStrList.size());
			}
		}
		//count++;
		return count;
	}
	
	public int operIsOld(int count,String refId){
		String oper = "";
		////////System.out.println(Integer.valueOf(value));
		//indP = indStructList.get(Integer.valueOf(tokenId)-1).getFea();
		//for old, I should go back from SCR given by the present COREF tag to that MARKABLE tag. 
		//then I need to find that headword and its index. So, it is a little bit complex here. 
		
		String ind = "";
		String indP ="";
		String [] indPArray = null;
		List<String> indPArrayList = new ArrayList<String>();
		System.out.println("count in operIsOld: "+count);
		indP = indStructList.get(count-1).getFea();
		indPArray = indP.split("_");
		//indPArrayList = Arrays.asList(indPArray);
		//since Arrays.aslist(array) cannot change size, not so good.
		//so, I changed it to an ArrayList.
		//indPArrayList = Arrays.asList(indPArray);
		for(String indPArrayStr:indPArray){
			indPArrayList.add(indPArrayStr);
		}
		String replaceStr = markableHeadWordHM.get(refId);
		System.out.println("refID: "+refId+" replaceStr: "+replaceStr);
		int replaceIndex = 0;
		if(indPArrayList.contains(replaceStr)){
			replaceIndex = indPArrayList.indexOf(replaceStr);
			//replaceIndexList.add(replaceIndex);
			//indPArrayList.set(replaceIndex,tokenId);
			//replaceRefIndexHM.put(replaceStr, replaceIndex);
			Iterator<String> refIndexHMIter = replaceRefIndexHM.keySet().iterator();
			while(refIndexHMIter.hasNext()){
				String strRefindex = refIndexHMIter.next();
				Integer valueRefIndex = replaceRefIndexHM.get(strRefindex);
				if(valueRefIndex>0 && valueRefIndex<indPArrayList.size()){
					valueRefIndex--;
				}
				replaceRefIndexHM.put(strRefindex, valueRefIndex);
			}
			replaceRefIndexHM.put(replaceStr, indPArrayList.size()-1);
		}
		// if replaceStr is not in the indPArrayList, it implies that it has been
		//replaced by other index. But we can still find it, I use a replaceIndexList to store
		//the position of the index. 
		else{
			
			replaceIndex = replaceRefIndexHM.get(replaceStr);
			System.out.println("replaceIndex: "+replaceIndex);
		}
		////System.out.println("replaceIndex: "+replaceIndex);
		if(replaceIndex<MAXNUM){
			oper = OLD;
			////////System.out.println("replaceIndex: "+replaceIndex+"size: "+indPArrayList.size());
			
			indPArrayList.remove(replaceIndex);
			//indPArrayList.add(tokenId);
			indPArrayList.add(String.valueOf(count));
			//indPArrayList.set(replaceIndex,tokenId);
			
			//indPArrayList.set(replaceIndex,tokenId);
			StringBuffer strBuffer = new StringBuffer();
			strBuffer.append(indPArrayList.get(0));
			for(int ii=1;ii<indPArrayList.size();ii++){
				strBuffer.append("_"+indPArrayList.get(ii));
			}
			ind = strBuffer.toString();
			FeaStruct indStruct = new FeaStruct(OLD, indP,ind);
			indStructList.add(indStruct);
			//System.out.println("old stat: indP "+oper+" "+indP+" ind "+ind );
			System.out.println("old state  count: "+count+" "+oper+" "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
		}
		//when replaceIndex is larger than or equal to MAXNUM, it implies 
		//that the item has been cut off and put into those with index out of range from
		//0 to MAXNUM, in this case, the item is regarded to be new, but different from 
		//brand new one, I put the index which refer to its ref back to the range from 
		//0 to MAXNUM
		else{
			//////System.out.println("replaceIndex bigger than MAXNUM: "+replaceIndex);
			oper = NEW;
			//indP = indStrList.get(Integer.valueOf(tokenId)-1).getFea();
			indP = indStructList.get(count-1).getFea();
			indPArray = indP.split("_");
			indPArrayList = Arrays.asList(indPArray);
			////////System.out.println("indPArrayList in new state: "+indPArrayList.size());
			
			//if size of indPArrayList is equal to MAXNUM, cut the first item off.  
			//if(indPArrayList.size()==MAXNUM){
				String firstStr = indPArrayList.get(0);
				//now, I need to change the related values of the replaceRefIndexHM
				//it should be straightforward since for each cutoff tokenId, I will change its 
				//value, i.e. replaceIndex to 0+MAXNUM and the old one which has value MAXNUM will be changed
				//MAXNUM+1 and all of those which values are larger than MAXNUM will be added one 
				//sequentially, meanwhile, those with values which are smaller than MAXNUM will be deducted 
				//one sequentially
				//replaceRefIndexHM.get(firstStr);
				Iterator<String> it = replaceRefIndexHM.keySet().iterator();
				while(it.hasNext()) { 
					String key = it.next(); 
					int val = replaceRefIndexHM.get(key);
					if(val==0){
						val=val+MAXNUM;
						replaceRefIndexHM.put(key, val);
					}else if(val>0 & val<MAXNUM){
						val=val-1;
						replaceRefIndexHM.put(key, val);
					}else if(val>MAXNUM & val<replaceIndex){
						val=val+1;
						replaceRefIndexHM.put(key, val);
					}else if(val == replaceIndex){
						val = MAXNUM-1;
						replaceRefIndexHM.put(key, val);
					}else{
						replaceRefIndexHM.put(key, val);
					}
				} 
				
				int indexFirstUndline = indP.indexOf("_");
				//the following substring method, +1 is very important. If not, split will 
				//not regard the first "_" as splitter, thus, the size will be 9, then, it will
				//enter into else, the above check is meaningless then.
				String indPSub = indP.substring(indexFirstUndline+1);
				//ind = indPSub +"_"+ tokenId;
				ind = indPSub +"_"+ String.valueOf(count);
				//FeaStruct indStruct = new FeaStruct(NEW, indP, ind);
				//indStrList.add(indStruct);
				//put the new added tokenId to MAXNUM-1, that is, it is in the last position of ind
				//replaceRefIndexHM.put(tokenId, MAXNUM-1);
				replaceRefIndexHM.put(String.valueOf(count), MAXNUM-1);
				//indPArrayList.set(MAXNUM-1,tokenId);
				FeaStruct indStruct = new FeaStruct(oper, indP,ind);
				indStructList.add(indStruct);
				System.out.println("new state from old state  count: "+count+" "+oper+" "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
				//System.out.println("new state from Old State: "+oper+"  "+indP+" "+ind+" indStruct Size: "+ indStrList.size());
			//}
		}
		//count++;
		return count;
		
	}
	
	public int operIsCopy(int count) {
		String oper = COPY;
		String pos = "-";
		String ind = "-";
		String indP = "-";
		if (count == 0) {
			// //System.out.println("count in count ==0: "+count);
			//ind = String.valueOf(count);
			// ////System.out.println("the first ind: "+ind);
			FeaStruct indStr = new FeaStruct(oper, ind, indP);
			System.out.println("count in count==0: "+count+" "+oper+" "+ind+" "+indP);
			indStructList.add(indStr);
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
			System.out.println("count: "+count+" "+oper+" "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
		}
		// //System.out.println("count: "+count);
		//count++;
		return count;
	}

	
	//public int extractInfor(Node node, String opValue, int count,HashMap<String,Integer> replaceRefIndexHM) {
	//the following method aims at extracting word and pos pairs.
	public String extractInfor(Node node) {
		String nodeName = node.getNodeName();
		// I cannot only extract WORD node since PUNC node is needed as well.
		// List<Node> wordNodeList = this.getChildrenByTagName(ithNode, WORD);
		//return count;
		NamedNodeMap tokenAttributes = node.getAttributes();
		//String word = node.getTextContent().trim().replaceAll("\t", "");
		String word = this.removeStrSpaces(node.getTextContent());
		String pos = "";
		if (nodeName.equals(PUNC)) {
			pos = word;
		} else {
			pos = tokenAttributes.getNamedItem(POS).getNodeValue();
			// //System.out.println(tokenAttributes.getNamedItem(ID)+" "+tokenAttributes.getNamedItem(POS).getNodeValue());
		}
		Pair wordPosPair = new Pair(word, pos);
		wordPosPairList.add(wordPosPair);
		return nodeName;
	}
	
	//sometimes, we cannot find the dependence among a Markable, in this case, let us use the last word
	//as the head word, e.g.
//	<MARKABLE COMMENT="" ID="26">
//	<COREF CERTAIN="TRUE" COMMENT="" ID="230" SRC="14" TYPE_REF="NP" TYPE_REL="IDENT">
//	</COREF>
//	<W DEP="W201" FUNC="DN" ID="W198" LEMMA="the" POS="DET">
//		the
//	</W>
//	<W DEP="W200" FUNC="A" ID="W199" LEMMA="guerrilla" POS="N">
//		guerrillas
//		'
//	</W>
// </MARKABLE>
	public Node findHeadword (List<Node> eleWordList){
		String headWord="";
		//String wordId="";
		//Pair headWordPair = null;
		int preDepID = 0;
		int curWordID = 0;
		int curDepID = 0;
		Node headNode = null;
		List<String> wordIDList = new ArrayList<String>();
		for(int j=0;j<eleWordList.size();j++){
			Node wordNode = eleWordList.get(j);
			NamedNodeMap wordAttributes = wordNode.getAttributes();
			Node wordIdNode = wordNode.getAttributes().getNamedItem(ID);
			String wordIdStr="";
			if(wordIdNode==null){
				wordIdStr=nullString;
			}else{
				wordIdStr = wordIdNode.getNodeValue();
			}
			//String wordIDStr = wordAttributes.getNamedItem(ID).getNodeValue();
			wordIDList.add(wordIdStr);
		}
		for(int j=0;j<eleWordList.size();j++){
			Node wordNode = eleWordList.get(j);
			Node wordIdNode = wordNode.getAttributes().getNamedItem(ID);
			NamedNodeMap wordAttributes = wordNode.getAttributes();
			String depStr = wordAttributes.getNamedItem(DEP).getNodeValue();	
			String wordFunc = wordAttributes.getNamedItem(FUNC).getNodeValue();
			String word = wordNode.getTextContent().trim().replaceAll("\t","");
			//for the moment, it seems that I can safely use the last word as the head word,
			//since I only consider flat structure rather than embedded Makkable. I know that this is not 
			//complete or correct at all. for example, "The girl of pink skin" in this corpus, is an embedded Markable structure,
			//as my simple handling, only pink skin will be processed. In my code, I need consider this case though 
			List<String> dependList = new ArrayList<String>();
			String wordIDStr = "";
			wordIDStr = wordIDList.get(j);
			curWordID = Integer.valueOf(wordIDStr.substring(1));
			if(wordFunc.equals(SUBJ)||wordFunc.equals(OBJ)){
				headNode = wordNode;
				headWord = word;
				if(!depStr.equalsIgnoreCase("none")){
					curDepID = Integer.valueOf(depStr.substring(1)); 
				}
				
				System.out.println("headWord is the subject or object: "+headWord);
				break;
				////System.out.println("head word from subj or obj indicator:: "+"curDepID: "+curDepID+" curWordID: "+curWordID+" headWord: "+headWord);
			}else{
				if(!depStr.equalsIgnoreCase("none")){
					curDepID = Integer.valueOf(depStr.substring(1));
				}else{
					curDepID = -1;
				}
				
				//not so sure how I can handle the above case, I will think about it later. 
				if (wordIDList.contains(depStr)) {
					preDepID = curDepID;
				} else if(!wordIDList.contains(depStr) && !stopWordList.contains(wordFunc )) {
					headWord = word;
					headNode = wordNode;
					System.out.println("headWord is the first one which wordIDList doesn't contain: "+headWord);
					break;
				}else if (j == eleWordList.size() - 1  && !wordFunc.equals("CC")) {
					headWord = word;
					headNode = wordNode;
					System.out.println("headWord is the last word: "+headWord);
					break;
					////System.out.println("preDepID: "+preDepID+" curDepID: " + curDepID + " curWordID: "
					//	+ curWordID + " headWord: " + headWord);
				}
			}
			////System.out.println(wordNode.getTextContent().trim()+" "+depStr);
			////System.out.println(wordNode.getTextContent().trim().replaceAll("\\b\\s{2,}\\b", " ")+" "+depStr);
			////System.out.println(wordNode.getTextContent().trim().replaceAll("\t","")+" "+depStr);
			////System.out.println(this.removeExtraSpaces(word)+" "+depStr+" "+wordIDStr);
			
			}
		return headNode;
	}
	
	public int getHeight(Node aNode){
		int height = 0;
		int interHeight = 0;
		if(aNode!=null){
			aNode.getNextSibling();
			NodeList childNodeList=aNode.getChildNodes();
			for(int i=0;i<childNodeList.getLength();i++){
				Node ithNode = childNodeList.item(i);
				int ithHeight = getHeight(ithNode);
				if(interHeight<ithHeight){
					interHeight = ithHeight;
				}
			}
		}
		height = 1+ interHeight;
		return height;
	}
	
//	public List<Pair> sortPairList(List<Pair> tempList,List<Pair> trackList){
//		for(int i=0;i<tempList.size();i++){
//			Pair ithTemPair = tempList.get(i);
//			Pair ithTrackPair = trackList.get(i);
//			
//			
//		}
//	}
	
	//the purpose of processTempList is that preorderTraverse traverse the XML tree from higher node to lower node and from
	//left to right. But I need to extract information from each Markalbe node which involves multiple embedded Markable node.
	//as result, outer markable node may put words which are behind some inner Markalbe node in the front of inner Markable node.
	//This will lead to the trouble when in the process of building indStructList since the index of some word belonging to outside
	//Makralbe node have larger index than words which are in the inner Markable node are processed firstly. out of bound error
	//will be caused. This is a complex way to build indStructList. In fact, I can use clearer way. That is, rather than
	//building the list on the fly, I can use ArrayList or HashMap to store all the values and then use those lists to 
	//build indStructList. But I have made much effort on this. It seems to be a beautiful work. So, I still keep this. Further,
	//there may be unexpected difficulties for the alternate approach. 
	//I need to do more for the following method, that is, I need to sort the tempList, do that tonight. 
	
	public void processTempList(List<Pair> tempList, List<Pair> trackList, String markableId){
		Collections.sort(tempList);
		Collections.sort(trackList);
		for(int i=0;i<tempList.size();i++){
			Pair ithPair=tempList.get(i);
			int index = (Integer)ithPair.getFirst();
			String oper=(String)ithPair.getSecond();
			if(oper==COPY){
				System.out.println(index+" "+trackList.get(i).getSecond());
				index=this.operIsCopy(index);
				
			}else if(oper==NEW){
				this.operIsNew(index, markableId);
				//System.out.println(index+" "+trackList.get(i).getSecond());
			}else{
				//refId is not correct here.
				//String refId = (String) trackList.get(i).getFirst();
				String refId =(String)((Pair) trackList.get(i).getSecond()).getFirst();
				System.out.println("what is refID from processTempLIst: "+refId);
				if(!refId.isEmpty()){
					this.operIsOld(index, refId);
				}
				
				//System.out.println(index+" "+trackList.get(i).getSecond());
			}
		}
		tempList = new ArrayList<Pair>();
		trackList = new ArrayList<Pair>();
	}
	
	

	
	
	
	
	
	public String getPuncTokenId(Node node) {
		String tokenId = "";
		if(node.getPreviousSibling()!=null){
			if(!node.getPreviousSibling().getNodeName().equals(PUNC)){
				String preNodeValue=node.getPreviousSibling().getAttributes().getNamedItem(ID).getNodeValue();
				String preNodeText =node.getPreviousSibling().getAttributes().getNamedItem(ID).getTextContent();
				if(preNodeValue==null){
					tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue()+preNodeText;
				}else{
					tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue()+preNodeValue;
				}
			}else{
				tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue()+node.getPreviousSibling().getTextContent();
			}
		}else{
			if(!node.getNextSibling().getNodeName().equals(PUNC)){
				String nextNodeValue=node.getNextSibling().getAttributes().getNamedItem(ID).getNodeValue();
				String nextNodeText = node.getNextSibling().getAttributes().getNamedItem(ID).getNodeValue();
				if(nextNodeValue==null){
					tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue()+nextNodeText;
				}else{
					tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue()+nextNodeValue;
				}					
			}else{
				tokenId = node.getParentNode().getAttributes().getNamedItem(ID).getNodeValue()+node.getNextSibling().getTextContent();
			}
			
		}
		return tokenId;
	}
	
	/**
	 * Performs full tree traversal using stack. rootNode here is a sentence node.
	 */
	
	  
	  public void inorderTraverse(Node rootNode, Stack<Node> markableNodeStack){
		    Node curNode = rootNode.getFirstChild();
			while(!markableNodeStack.isEmpty()||curNode!=null){
				//NodeList curNodeKidList=null;
				//int countKid = 0;
//				if(curNode!=null){
//					///curNodeKidList  = curNode.getChildNodes();
//				}
				//fine leftmost node with no left child by calling getFirstChild();
				while(curNode!=null){
					markableNodeStack.push(curNode);
					//System.out.println( curNode.getNodeName()+"="+curNode.getNodeValue());
					curNode=curNode.getFirstChild();
					
				}//end while
				
				//visit leftmost node, then traverse all its right subtrees.
				if(!markableNodeStack.isEmpty()){
					Node nextNode = markableNodeStack.pop();
					curNode = nextNode.getNextSibling();
					//curNodeKidList  = nextNode.getChildNodes();
					assert nextNode!=null; //since markalbeNodeStack was not empty before the pop
					//temNode = curNode;
					//temNode = nextNode;
					////System.out.println("what is curNode here: " +temNode.getNodeName());
					////System.out.println("countKid: "+countKid+" nextNode "+nextNode.getNodeName()+" "+nextNode.getFirstChild());
//					if(curNodeKidList.getLength()!=0){
//						curNode = curNodeKidList.item(countKid+1);
//						////System.out.println(curNode.getNodeName());
//					}
					
					//cool!!!!!!!!!!! Afte long term thinking and step by step derivation, I finally figure out the 
					//way for multinary inorder traversal. countKid is a key, especailly it should be put into the following
					//if clause. But unfortunatelly, this is still wrong. The reason is that I can only keep one countKid.
					//this is obviously wrong. As a result, countKid will be growing continuouslly. So, some items will be 
					//skipped since I cannot find a way to start from 0. Luckily, org.w3c.dom has implemented the method
					//getNextSibling, which can solve this problem quite easily now.
//					if(curNode!=null){
//						markableNodeStack.push(temNode);
//						//countKid++;
//					}
//					//System.out.println("what is temNode here: " +temNode.getNodeName()+" "+temNode.getTextContent()+" ");
				}
			}
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
	
	protected List<Node> getChildrenByTagName(Node parent, String tagName){
		List<Node> eleList = new ArrayList<Node>();
		NodeList nodeList = parent.getChildNodes();
		for(int i=0;i<nodeList.getLength();i++){
			Node node = nodeList.item(i);
			if(node.getNodeType()==Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(tagName)){
				eleList.add(node);
			}
			
		}
		return eleList;
	}
	
	public String removeSpaces(String s) {
		  StringTokenizer st = new StringTokenizer(s,"\t\n",false);
		  String t="";
		  while (st.hasMoreElements()) t += st.nextElement()+" ";
		  return t;
		}

	public String removeStrSpaces(String s) {
		String newStr = s.trim().replaceAll("\n", "");
		  return newStr;
		}
	
	private String removeExtraSpaces(String s){
		if(!s.contains(" ")) return s;
		return removeExtraSpaces(s.replace(" ", ""));
	}
	
	public void convertIndStruct(PrintWriter pwIndStruct,PrintWriter pwOldModel){
		//////System.out.println(indStructList.size());
		
		for(int i=0;i<indStructList.size();i++){
			
			FeaStruct indStruct = indStructList.get(i);
			pwIndStruct.print("Ind ");
			String oper = indStruct.getOper();
			String indPrev = indStruct.getFeaP();
			String indCur = indStruct.getFea();
			//HashMap<String, Integer> indHm = new HashMap<String,Integer>();
			HashMap<Integer, Integer> indHm = new HashMap<Integer,Integer>();
			String [] indPArray = indPrev.split("_");
			String [] indArray = indCur.split("_");
			//List<String> indPList = Arrays.asList(indPArray);
			//List<String> indPList = new ArrayList<String>();
			
			List<Integer> indPList = new ArrayList<Integer>();
			
			for(int ii=0;ii<indPArray.length;ii++){
				if(indPArray[ii].equals(start)){
					indPList.add(-1);
				}else{
					indPList.add(Integer.valueOf(indPArray[ii]));
				}
			}
			
			
			if(oper.equals(COPY)){
				pwIndStruct.print(COPY+" ");
				Collections.sort(indPList);
				for(int j=0;j<indPList.size();j++){
					indHm.put(indPList.get(j), j);
				}
				for(int j=0;j<indPArray.length-1;j++){
					pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[j]))+"_");
				}
				pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+" : ");
				for(int j=0;j<indArray.length-1;j++){
					pwIndStruct.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
				}
				pwIndStruct.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+" ");
				pwIndStruct.println();
			}
			
			else if(oper.equals(NEW) ){
				pwIndStruct.print(NEW+" ");
				
				if(indPList.size()==1 && indPList.contains(-1)){
					pwIndStruct.println("-1"+" : "+indArray[0]);
				}else if(indPList.size()==1 && indPList.get(0)==(0)){
					pwIndStruct.println("0"+" : "+indArray[0]+"_"+indArray[1]);
				}
				else {
					indPList.add(Integer.valueOf(indArray[indArray.length-1]));
					Collections.sort(indPList);
					for(int j=0;j<indPList.size();j++){
						indHm.put(indPList.get(j), j);
						////////System.out.print(indPList.get(j)+" ");
					}
					////////System.out.println();
					for(int j=0;j<indPArray.length-1;j++){
						pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[j]))+"_");
					}
					pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+" : ");
					for(int j=0;j<indArray.length-1;j++){
						pwIndStruct.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
					}
					pwIndStruct.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+" ");
					pwIndStruct.println();
				}
			}
			else if(oper.equals(OLD) ){
				pwIndStruct.print(OLD+" ");
				pwOldModel.print("Ind "+OLD+" ");
				
				if(indPList.size()==1 && indPList.contains(-1)){
					pwIndStruct.println("-1"+" "+indArray[0]);
					pwOldModel.println("1"+" "+indArray[0]);
				}else if(indPList.size()==1 && indPList.get(0)==(0)){
					pwIndStruct.println("0"+" "+indArray[0]+"_"+indArray[1]);
					pwOldModel.println("0"+" "+indArray[1]);
				}
				else {
					indPList.add(Integer.valueOf(indArray[indArray.length-1]));
					Collections.sort(indPList);
					for(int j=0;j<indPList.size();j++){
						indHm.put(indPList.get(j), j);
						////////System.out.print(indPList.get(j)+" ");
					}
					////////System.out.println();
					for(int j=0;j<indPArray.length-1;j++){
						pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[j]))+"_");
					}
					pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+" : ");
					pwOldModel.print(indPArray.length+" : ");
					for(int j=0;j<indArray.length-1;j++){
						pwIndStruct.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
					}
					pwIndStruct.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+" ");
					
					for(int j=0;j<indPArray.length;j++){
						////////System.out.println(indPArray.length+" the length of them : "+indArray.length);
						////////System.out.println()
						if(!Integer.valueOf(indPArray[j]).equals(Integer.valueOf(indArray[j]))){
							pwOldModel.print(indHm.get(Integer.valueOf(indPArray[j]))+" ");
							////////System.out.println("what is J::::::::::::"+j);
							break;
						}
//						//if(all items from both arrays are equal
//						else if(Integer.valueOf(indPArray[j])==Integer.valueOf(indArray[j]) && (j==indPArray.length-2)){
//							pwOldModel.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+" ");
//							break;
//						}
					}
					pwIndStruct.println();
					pwOldModel.println();
				}
			}

		}
	}
	
	public void buildPosDependence(PrintWriter pwPosDepend){
		for(int i=0;i<wordPosPairList.size();i++){
			pwPosDepend.print("POS ");
			if(i==0){
				String posP = "-";
				String pos = (String)wordPosPairList.get(i).getSecond();
				String OP = indStructList.get(i).getOper();
				String ind = indStructList.get(i).getFea();
				pwPosDepend.println(OP+" "+ind+" "+posP+" : "+pos);
			}else{
				String posP = (String)wordPosPairList.get(i-1).getSecond();
				String pos = (String)wordPosPairList.get(i).getSecond();
				String oper = indStructList.get(i).getOper();
				String indP = indStructList.get(i).getFeaP();
				String indCur = indStructList.get(i).getFea();
				String [] indPArray = indP.split("_");
				HashMap<Integer, Integer> indHm = new HashMap<Integer,Integer>();
				String [] indArray = indCur.split("_");
				//List<String> indPList = Arrays.asList(indPArray);
				//List<String> indPList = new ArrayList<String>();
				
				List<Integer> indPList = new ArrayList<Integer>();
				
				for(int ii=0;ii<indPArray.length;ii++){
					if(indPArray[ii].equals(start)){
						indPList.add(-1);
					}else{
						indPList.add(Integer.valueOf(indPArray[ii]));
					}
				}
				
				if(oper.equals(COPY)){
					pwPosDepend.print(COPY+" ");
					//Collections.sort(indPList);
					for(int j=0;j<indPList.size();j++){
						indHm.put(indPList.get(j), j);
					}
					for(int j=0;j<indPArray.length-1;j++){
						pwPosDepend.print(indHm.get(indPList.get(j))+"_");
					}
					pwPosDepend.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+" ");
					pwPosDepend.print(posP+" : "+pos);
					pwPosDepend.println();
				}
				else if(oper.equals(NEW) ){
					pwPosDepend.print(NEW+" ");
					indPList.add(Integer.valueOf(indArray[indArray.length-1]));
					//Collections.sort(indPList);
					for(int j=0;j<indPList.size();j++){
						indHm.put(indPList.get(j), j);
						////////System.out.print(indPList.get(j)+" ");
					}
					for(int j=0;j<indArray.length-1;j++){
						pwPosDepend.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
					}
					pwPosDepend.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+" ");
					pwPosDepend.print(posP+" : "+pos);
					pwPosDepend.println();
				}
				else if(oper.equals(OLD) ){
					pwPosDepend.print(OLD+" ");					
					indPList.add(Integer.valueOf(indArray[indArray.length-1]));
					//Collections.sort(indPList);
					for(int j=0;j<indPList.size();j++){
						indHm.put(indPList.get(j), j);
						////////System.out.print(indPList.get(j)+" ");
					}
					for(int j=0;j<indArray.length-1;j++){
						pwPosDepend.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
					}
					pwPosDepend.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+" ");
					pwPosDepend.print(posP+" : "+pos);
					pwPosDepend.println();
				}
			}
		}
	}
	
	public void buildOpDependence(PrintWriter pwOpDepend){
		for(int i=0;i<wordPosPairList.size();i++){
			pwOpDepend.print("OP ");
			if(i==0){
				String posP = "-";
				String opP = "-";
				String Op = indStructList.get(i).getOper();
				String indP = String.valueOf(-1);
				String [] indPArray = indP.split("_");
				pwOpDepend.print(opP+" "+posP+" ");
				for(int j=0;j<indPArray.length-1;j++){
					pwOpDepend.print(j+"_");
				}
				pwOpDepend.print(indPArray.length-1+" : ");
				pwOpDepend.print(Op);
			}else{
				String posP = (String)wordPosPairList.get(i-1).getSecond();
				//String pos = (String)wordPosPairList.get(i).getSecond();
				String Op = indStructList.get(i).getOper();
				String indP = indStructList.get(i-1).getFea();
				String opP = indStructList.get(i-1).getOper();
				String [] indPArray = indP.split("_");
				pwOpDepend.print(opP+" "+posP+" ");
				for(int j=0;j<indPArray.length-1;j++){
					pwOpDepend.print(j+"_");
				}
				pwOpDepend.print(indPArray.length-1+" : ");
				pwOpDepend.print(Op);
			}
			pwOpDepend.println();
		}
	}
	
	public void printTokenPosPair(PrintWriter pwPosTokenPair){
		for(int i=0;i<wordPosPairList.size();i++){
			pwPosTokenPair.println(wordPosPairList.get(i));
		}
	}
	
	public void buildWdDependence(PrintWriter pwWdDepend){
		for(int i=0;i<wordPosPairList.size();i++){
			pwWdDepend.print("Word ");
			
			if(i==0){
				String posP = "-";
				String word = (String)wordPosPairList.get(i).getFirst();
				String pos = (String)wordPosPairList.get(i).getSecond();
				String OP = indStructList.get(i).getOper();
				String ind = indStructList.get(i).getFea();
				pwWdDepend.print(OP+" "+pos+" "+ind+" : "+word);
			}else{
				String oper = indStructList.get(i).getOper();
				String indP = indStructList.get(i).getFeaP();
				String indCur = indStructList.get(i).getFea();
				String word = (String)wordPosPairList.get(i).getFirst();
				String pos =(String) wordPosPairList.get(i).getSecond();
				String [] indPArray = indP.split("_");
				HashMap<Integer, Integer> indHm = new HashMap<Integer,Integer>();
				String [] indArray = indCur.split("_");
				List<Integer> indPList = new ArrayList<Integer>();
				for(int ii=0;ii<indPArray.length;ii++){
					if(indPArray[ii].equals(start)){
						indPList.add(-1);
					}else{
						indPList.add(Integer.valueOf(indPArray[ii]));
					}
				}
				
				if(oper.equals(COPY)){
					pwWdDepend.print(COPY+" ");
					//Collections.sort(indPList);
					for(int j=0;j<indPList.size();j++){
						indHm.put(indPList.get(j), j);
					}
					for(int j=0;j<indPArray.length-1;j++){
						pwWdDepend.print(indHm.get(indPList.get(j))+"_");
					}
					pwWdDepend.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+" ");
					pwWdDepend.print(pos+" : "+word);
				}
				else if(oper.equals(NEW) ){
					pwWdDepend.print(NEW+" ");
					indPList.add(Integer.valueOf(indArray[indArray.length-1]));
					//Collections.sort(indPList);
					for(int j=0;j<indPList.size();j++){
						indHm.put(indPList.get(j), j);
						////////System.out.print(indPList.get(j)+" ");
					}
					for(int j=0;j<indArray.length-1;j++){
						pwWdDepend.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
					}
					pwWdDepend.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+" ");
					pwWdDepend.print(pos+" : "+word);
				}
				else if(oper.equals(OLD) ){
					pwWdDepend.print(OLD+" ");					
					indPList.add(Integer.valueOf(indArray[indArray.length-1]));
					//Collections.sort(indPList);
					for(int j=0;j<indPList.size();j++){
						indHm.put(indPList.get(j), j);
						////////System.out.print(indPList.get(j)+" ");
					}
					for(int j=0;j<indArray.length-1;j++){
						pwWdDepend.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
					}
					pwWdDepend.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+" ");
					pwWdDepend.print(pos+" : "+word);
				}
			}
				pwWdDepend.println();
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		// E:\Dissertation Writing\corpora\corefann\wxml
		//"/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"

		int item = 6;
		long start = System.currentTimeMillis();
		//////System.out.println("starting time: " + start);

		ProcessReutersOct27 processReutersCorpus = new ProcessReutersOct27();
		File input = new File(args[0]);
		BufferedReader brXMLCorpus = new BufferedReader(new FileReader(input));
		BufferedReader brXMLCorpus2 = new BufferedReader(new FileReader(input));
		processReutersCorpus.splitMarkableNode(args[0]);
		
		File outputDir = new File(args[1]);
		if(!outputDir.exists()){
			outputDir.mkdirs();
		}
		
		PrintWriter pwIndFormat = null;
		PrintWriter pwOldModel = null;
//		PrintWriter pwPosModel = null;
//		PrintWriter pwOpModel = null;
//		PrintWriter pwWdModel = null;
//		//the following is for debugging:
//		PrintWriter posTokenPw = null;
		try {
			pwIndFormat = new PrintWriter(new FileWriter(new File(outputDir,input.getName().substring(0,input.getName().length()-3)+"model")));
			pwOldModel = new PrintWriter(new FileWriter(new File(outputDir,input.getName().substring(0,input.getName().length()-3)+"input.model")));
//			pwPosModel = new PrintWriter(new FileWriter(new File(outputDir,input.getName().substring(0,input.getName().length()-6)+"posmodel")));
//			pwOpModel = new PrintWriter(new FileWriter(new File(outputDir,input.getName().substring(0,input.getName().length()-6)+"opmodel")));
//			pwWdModel = new PrintWriter(new FileWriter(new File(outputDir,input.getName().substring(0,input.getName().length()-6)+"wdModel")));
//			posTokenPw = new PrintWriter(new FileWriter(new File(outputDir,input.getName().substring(0,input.getName().length()-6)+"postoken")));
			processReutersCorpus.printIndStructList();
			processReutersCorpus.convertIndStruct(pwIndFormat,pwOldModel);
//			processCorefCorpus.buildPosDependence(pwPosModel);
//			processCorefCorpus.buildOpDependence(pwOpModel);
//			processCorefCorpus.buildWdDependence(pwWdModel);
//			processCorefCorpus.printTokenPosPair(posTokenPw);
		} catch (IOException e) {
//			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		pwIndFormat.close();
		pwOldModel.close();
//		pwPosModel.close();
//		pwOpModel.close();
//		pwWdModel.close();
//		posTokenPw.close();

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
		//////System.out.println("stopping time: " + stop);
		long elapsed = stop - start;
		//////System.out.println("this is the total running time: " + elapsed);
	}
}
