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

import utils.Pair;

import corpusProcessor.FeaStruct;

public class ProcessReuters {

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
	String OP = "";	
	List<FeaStruct> indStructList;
	List<Pair> wordPosPairList;
	
	int countTags = 0;
	int countToken = 0;
	boolean isMarkable = false;
	boolean isCoref = false;
	boolean isEmbedded = false;
	
	public ProcessReuters() {
		indStructList = new ArrayList<FeaStruct>();
		wordPosPairList = new ArrayList<Pair>();
	}
	
	public int operIsNew (int count,String markableId,HashMap<String,Integer> replaceRefIndexHM,List<FeaStruct> indStrList){
		OP = NEW;
		String ind = "";
		String indP = "";
		String[] indPArray = null;
		//Pair markableCountPair = new Pair(count,markableId);
		List<String> indPArrayList = new ArrayList<String>();
		//this is the first state when indStructList is empty
		if(indStrList.isEmpty()){
			//ind = tokenId;
			ind = String.valueOf(count);
			FeaStruct indStruct = new FeaStruct(NEW, indP, ind);
			indStrList.add(indStruct);
			replaceRefIndexHM.put(ind, 0);
			
			//replaceRefIndexHM.put(markableCountPair, 0);
			//replaceRefIndexHM.put(tokenId, 0);
			////System.out.println("new state: "+OP+" "+tokenId+"  "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
		}else{
			//////System.out.println("new state: "+ "coref is "+coref+" indStructList size: "+indStructList.size());
			//indP = indStrList.get(Integer.valueOf(tokenId)-1).getFea();
			indP = indStrList.get(count-1).getFea();
			indPArray = indP.split("_");
			//since Arrays.aslist(array) cannot change size, not so good.
			//so, I changed it to an ArrayList.
			//indPArrayList = Arrays.asList(indPArray);
			for(String indPArrayStr:indPArray){
				indPArrayList.add(indPArrayStr);
			}
			//////System.out.println("indPArrayList in new state: "+indPArrayList.size());
			
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
				indStrList.add(indStruct);
				//put the new added tokenId to MAXNUM-1, that is, it is in the last position of ind
				//replaceRefIndexHM.put(tokenId, MAXNUM-1);
				replaceRefIndexHM.put(String.valueOf(count), MAXNUM-1);
				//replaceRefIndexHM.put(markableCountPair, MAXNUM-1);
				////System.out.println("new state more than 8: "+OP+" "+tokenId+"  "+indP+" "+ind+" indStruct Size: "+ indStrList.size());
				
			}
			//size is fewer than 8
			else{
				//////System.out.println("how do you escape the check in the first if????");
				//ind = indP +"_"+ tokenId;
				ind = indP+"_"+count;
				FeaStruct indStruct = new FeaStruct(NEW, indP, ind);
				indStrList.add(indStruct);
				//replaceRefIndexHM.put(tokenId,indPArrayList.size());
				replaceRefIndexHM.put(String.valueOf(count),indPArrayList.size());
				//replaceRefIndexHM.put(markableCountPair, indPArrayList.size());
				////System.out.println("new state fewer than 8: "+OP+" "+tokenId+"  "+indP+" "+ind+" indStruct Size: "+ indStrList.size());
			}
		}
		count++;
		return count;
	}
	
	public int operIsOld(int count,String refId, HashMap<String,String> markableHeadHM, HashMap<String,Integer> replaceRefIndexHM,List<FeaStruct> indStrList){
		//OP = OLD;
		//////System.out.println(Integer.valueOf(value));
		//indP = indStructList.get(Integer.valueOf(tokenId)-1).getFea();
		//for old, I should go back from SCR given by the present COREF tag to that MARKABLE tag. 
		//then I need to find that headword and its index. So, it is a little bit complex here. 
		
		String ind = "";
		String indP ="";
		String [] indPArray = null;
		List<String> indPArrayList = new ArrayList<String>();
		indP = indStrList.get(count-1).getFea();
		indPArray = indP.split("_");
		//indPArrayList = Arrays.asList(indPArray);
		//since Arrays.aslist(array) cannot change size, not so good.
		//so, I changed it to an ArrayList.
		//indPArrayList = Arrays.asList(indPArray);
		for(String indPArrayStr:indPArray){
			indPArrayList.add(indPArrayStr);
		}
		String replaceStr = markableHeadHM.get(refId);
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
			System.out.println("replaceStr: "+replaceStr);
			replaceIndex = replaceRefIndexHM.get(replaceStr);
		}
		//System.out.println("replaceIndex: "+replaceIndex);
		if(replaceIndex<MAXNUM){
			OP = OLD;
			//////System.out.println("replaceIndex: "+replaceIndex+"size: "+indPArrayList.size());
			
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
			indStrList.add(indStruct);
			//System.out.println("old stat: indP "+OP+" "+indP+" ind "+ind );
		}
		//when replaceIndex is larger than or equal to MAXNUM, it implies 
		//that the item has been cut off and put into those with index out of range from
		//0 to MAXNUM, in this case, the item is regarded to be new, but different from 
		//brand new one, I put the index which refer to its ref back to the range from 
		//0 to MAXNUM
		else{
			////System.out.println("replaceIndex bigger than MAXNUM: "+replaceIndex);
			OP = NEW;
			//indP = indStrList.get(Integer.valueOf(tokenId)-1).getFea();
			indP = indStrList.get(count-1).getFea();
			indPArray = indP.split("_");
			indPArrayList = Arrays.asList(indPArray);
			//////System.out.println("indPArrayList in new state: "+indPArrayList.size());
			
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
				FeaStruct indStruct = new FeaStruct(OP, indP,ind);
				indStrList.add(indStruct);
				//System.out.println("new state from Old State: "+OP+" "+tokenId+"  "+indP+" "+ind+" indStruct Size: "+ indStrList.size());
			//}
		}
		count++;
		return count;
		
	}
	
	public int operIsCopy(int count, List<FeaStruct> instrList) {
		OP = COPY;
		String pos = "";
		String ind = "";
		String indP = "";
		if (count == 0) {
			// System.out.println("count in count ==0: "+count);
			ind = String.valueOf(count);
			// //System.out.println("the first ind: "+ind);
			FeaStruct indStr = new FeaStruct(OP, ind, ind);
			indStructList.add(indStr);
		} else {
			// System.out.println("count in count !=0: "+count);
			// indP =
			// indStructList.get(Integer.valueOf(tokenId)-1).getFea();
			indP = indStructList.get(count - 1).getFea();
			// indP = indStructList.get(count).getFea();
			// count++;
			ind = indP;
			FeaStruct indStr = new FeaStruct(OP, indP, ind);
			indStructList.add(indStr);
		}
		// System.out.println("count: "+count);
		count++;
		return count;
	}

	
	//public int extractInfor(Node node, String opValue, int count,HashMap<String,Integer> replaceRefIndexHM) {
	public String extractInfor(Node node) {
		String nodeName = node.getNodeName();
		if (nodeName.equals(WORD) || nodeName.equals(PUNC)) {
			NamedNodeMap tokenAttributes = node.getAttributes();
			//String word = node.getTextContent().trim().replaceAll("\t", "");
			String word = this.removeStrSpaces(node.getTextContent());
			String pos = "";
			String ind = "";
			String indP = "";
			if (node.getNodeName().equals(PUNC)) {
				pos = word;
			} else {
				pos = tokenAttributes.getNamedItem(POS).getNodeValue();
				// System.out.println(tokenAttributes.getNamedItem(ID)+" "+tokenAttributes.getNamedItem(POS).getNodeValue());
			}
			Pair wordPosPair = new Pair(word, pos);
			wordPosPairList.add(wordPosPair);

			// for(int k=0;k<wordPosPairList.size();k++){
			// System.out.println(wordPosPairList.get(k).getFirst()+" "+wordPosPairList.get(k).getSecond());
			// }
//			OP = opValue;
//			if (opValue == COPY) {
//				if (count == 0) {
//					//System.out.println("count in count ==0: "+count);
//					ind = String.valueOf(count);
//					// //System.out.println("the first ind: "+ind);
//					FeaStruct indStr = new FeaStruct(OP, ind, ind);
//					indStructList.add(indStr);
//				} else {
//					//System.out.println("count in count !=0: "+count);
//					// indP =
//					// indStructList.get(Integer.valueOf(tokenId)-1).getFea();
//					indP = indStructList.get(count - 1).getFea();
//					// indP = indStructList.get(count).getFea();
//					// count++;
//					ind = indP;
//					FeaStruct indStr = new FeaStruct(OP, indP, ind);
//					indStructList.add(indStr);
//				}
//				// System.out.println("count: "+count);
//				count++;
//			} else if (opValue == OLD) {
//				indStructList =this.operIsOld(count,replaceRefIndexHM, indStructList);
//				count++;
//			} else if (opValue == NEW) {
//				indStructList =this.operIsNew(count,replaceRefIndexHM, indStructList);
//				count++;
//			}
		}
		// I cannot only extract WORD node since PUNC node is needed as well.
		// List<Node> wordNodeList = this.getChildrenByTagName(ithNode, WORD);
		//return count;
		return nodeName;
	}
	
	public String findHeadword (List<Node> eleWordList){
		//sometimes, we cannot find the dependence among a Markable, in this case, let us use the last word
		//as the head word, e.g.
//		<MARKABLE COMMENT="" ID="26">
//		<COREF CERTAIN="TRUE" COMMENT="" ID="230" SRC="14" TYPE_REF="NP" TYPE_REL="IDENT">
//		</COREF>
//		<W DEP="W201" FUNC="DN" ID="W198" LEMMA="the" POS="DET">
//			the
//		</W>
//		<W DEP="W200" FUNC="A" ID="W199" LEMMA="guerrilla" POS="N">
//			guerrillas
//			'
//		</W>
//	 </MARKABLE>
		String headWord="";
		int preDepID = 0;
		int curWordID = 0;
		int curDepID = 0;
		List<String> wordIDList = new ArrayList<String>();
		for(int j=0;j<eleWordList.size();j++){
			Node wordNode = eleWordList.get(j);
			NamedNodeMap wordAttributes = wordNode.getAttributes();
			String wordIDStr = wordAttributes.getNamedItem(CID).getNodeValue();
			wordIDList.add(wordIDStr);
		}
		for(int j=0;j<eleWordList.size();j++){
			Node wordNode = eleWordList.get(j);
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
				headWord = word;
				if(!depStr.equalsIgnoreCase("none")){
					curDepID = Integer.valueOf(depStr.substring(1)); 
				}
				//System.out.println("head word from subj or obj indicator:: "+"curDepID: "+curDepID+" curWordID: "+curWordID+" headWord: "+headWord);
			}else{
				if(!depStr.equalsIgnoreCase("none")){
					curDepID = Integer.valueOf(depStr.substring(1));
				}else{
					curDepID = -1;
				}
				
				//not so sure how I can handle the above case, I will think about it later. 
				if (wordIDList.contains(depStr)) {
					preDepID = curDepID;
				} else if (j == eleWordList.size() - 1) {
					headWord = word;
					//System.out.println("preDepID: "+preDepID+" curDepID: " + curDepID + " curWordID: "
						//	+ curWordID + " headWord: " + headWord);
				}
				
			}
			//System.out.println(wordNode.getTextContent().trim()+" "+depStr);
			//System.out.println(wordNode.getTextContent().trim().replaceAll("\\b\\s{2,}\\b", " ")+" "+depStr);
			//System.out.println(wordNode.getTextContent().trim().replaceAll("\t","")+" "+depStr);
			//System.out.println(this.removeExtraSpaces(word)+" "+depStr+" "+wordIDStr);
			
			}
		return headWord;
	}
	
	public void splitMarkableNode(String inputFile) {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		File f = new File(inputFile);
		HashMap<String,Integer> replaceRefIndexHM = new HashMap<String,Integer>();
		HashMap<String,String> markableHeadWordHM = new HashMap<String,String>();
		//HashMap<Pair,Integer> replaceRefIndexHM = new HashMap<Pair,Integer>();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(f);
				// Get the first <slide> element in the DOM
				// NodeList allChildNodeList = document.getChildNodes();
				Node child = document.getFirstChild();
				NodeList docChildList = child.getChildNodes();
				// for(int i=0;i<docChildList.getLength();i++){
				// System.out.println("docChildName: "+docChildList.item(i).getNodeName());
				// }

				NodeList senNodeList = document.getElementsByTagName(SENTENCE);

				for (int i = 0; i < senNodeList.getLength(); i++) {
					// System.out.println("docChildName: "+senNodeList.item(i));
					Node ithNode = senNodeList.item(i);
					NodeList senKidList = ithNode.getChildNodes();
					for(int j=0;j<senKidList.getLength();j++){
						Node jthNode = senKidList.item(j);
						jthNode.getNodeValue();
						String nodeName = this.extractInfor(jthNode);
						
						String jthWord = jthNode.getTextContent();
						if(nodeName.equals(WORD)||nodeName.equals(PUNC)){
							countToken = this.operIsCopy(countToken, indStructList);
						}
						//countToken = this.extractInfor(jthNode, COPY, countToken,replaceRefIndexHM);
					}
					//countToken = this.extractInfor(ithNode, COPY, countToken);
					// I cannot only extract WORD node since PUNC node is needed
					// as
					// well.
					// List<Node> wordNodeList =
					// this.getChildrenByTagName(ithNode,
					// WORD);
				}
				NodeList list = document.getElementsByTagName(MARKABLE);
				for (int i = 0; i < list.getLength(); i++) {
					Node ithNode = list.item(i);
					List<Node> eleWordList = new ArrayList<Node>();
					NamedNodeMap markableMap = ithNode.getAttributes();
					// System.out.println(ithNode.getNodeType()+" "+ithNode.getNodeValue()+" "+ithNode.getNodeName());
					NodeList markableKidList = ithNode.getChildNodes();
					// even though I will temporarily ignore the embedded
					// Markable, but
					// I cannot
					// ignore words here since words here will still be counted
					// as copy,
					// otherwise
					// the dependency information may be missing.
					if (this.getFirstChildByTagName(ithNode, MARKABLE) != null) {
						isEmbedded = true;
						// int count = 0;
						// That is interesting. return countToken is so
						// important. That is: extractInfor must return the countToken, otherwise,
						// countToken will always start from 0. In fact, this is very obvious! But I
						// knew just now. Oct. 17, 09
						
						
						for(int j=0;j<markableKidList.getLength();j++){
							Node jthNode = markableKidList.item(j);
							String nodeName=this.extractInfor(jthNode);
							String jthWord = jthNode.getTextContent();
							if(nodeName.equals(WORD)||nodeName.equals(PUNC)){
								countToken = this.operIsCopy(countToken, indStructList);
							}
							//countToken = this.extractInfor(jthNode, COPY, countToken,replaceRefIndexHM);
						}
						// this.extractInfor(ithNode, COPY, countToken);
						// countToken+=count;
						// break;
					}
					// else
					// if(ithNode.getFirstChild().getNodeName().equals(COREF)){
					// OP = OLD;
					// FeaStruct indStruct = new FeaStruct();
					// indStructList.add(indStruct);
					// }
					else {
						eleWordList = this.getChildrenByTagName(ithNode, WORD);
						//System.out.println("headWord: "+ithNode.getChildNodes());
						Node corefNode = this.getFirstChildByTagName(ithNode, COREF);
						//that is what I should start from here, COREF helps us to find previous MARKABLE tag
						//and find its headword.
						
						if (corefNode!=null) {
							// find the head word,
							// in this case, a new markable comes up, but if the
							// markable is composed of a few words.we should find 
							// the head word as the representation of the markable. 
							//In Reuters, we can find the head word by the attribute DEP.
							//what I need here is the countIndex of the head word.
							NamedNodeMap corefMap=corefNode.getAttributes();
							String refId = corefMap.getNamedItem("SRC").getNodeValue();
							//System.out.println(corefMap.getNamedItem("SRC").getNodeValue());
							String headWord = this.findHeadword(eleWordList);
							//System.out.println("headWord: "+headWord);
							for(int j = 0;j<eleWordList.size();j++){
								Node wordNode = eleWordList.get(j);
								String word = this.removeStrSpaces(wordNode.getTextContent());
								//System.out.println("word------------++++++++++:: "+word);
								if(word.equals(headWord)){
									OP = OLD;
									String nodeName=this.extractInfor(wordNode);
									if(nodeName.equals(WORD)||nodeName.equals(PUNC)){
										countToken=this.operIsOld(countToken, refId, markableHeadWordHM,replaceRefIndexHM,indStructList);
									}
									
									//countToken = this.extractInfor(wordNode, OP, countToken,replaceRefIndexHM);
									//System.out.println("not here??????, head word:::: ");
								}else{
									OP = COPY;
									String nodeName=this.extractInfor(wordNode);
									if(nodeName.equals(WORD)||nodeName.equals(PUNC)){
										countToken = this.operIsCopy(countToken, indStructList);
									}
									//countToken = this.extractInfor(wordNode, OP, countToken,replaceRefIndexHM);
								}
							}
						} else {
							// find the head word, I wonder if there is redundant work here.
							String headWord = this.findHeadword(eleWordList);
							//System.out.println("headWord: "+headWord);
							for(int j = 0;j<eleWordList.size();j++){
								Node wordNode = eleWordList.get(j);
								String word = this.removeStrSpaces(wordNode.getTextContent());
								String markableId=markableMap.getNamedItem(ID).getNodeValue();
								//System.out.println(markableMap.getNamedItem(ID).getNodeValue());
								if(word.equals(headWord)){
									OP = NEW;
									String wordId = wordNode.getAttributes().getNamedItem(ID).getNodeValue();
									//Pair markWordPair=new Pair(markableId,wordId);
									//Pair markWordPair=new Pair(markableId,countToken);
									String nodeName=this.extractInfor(wordNode);
									
									markableHeadWordHM.put(markableId, String.valueOf(countToken));
									if(nodeName.equals(WORD)||nodeName.equals(PUNC)){
										countToken=this.operIsNew(countToken, markableId, replaceRefIndexHM,indStructList);
									}
									
									
									//countToken = this.extractInfor(wordNode, OP, countToken,replaceRefIndexHM);
								}else{
									OP = COPY;
									String nodeName=this.extractInfor(wordNode);
									if(nodeName.equals(WORD)||nodeName.equals(PUNC)){
										countToken = this.operIsCopy(countToken, indStructList);
									}
									//countToken = this.extractInfor(wordNode, OP, countToken,replaceRefIndexHM);
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

	public void printCountToken(){
		System.out.println("how many tokens in the end:: "+countToken);
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
		  StringTokenizer st = new StringTokenizer(s,"         ",false);
		  String t="";
		  while (st.hasMoreElements()) t += st.nextElement();
		  return t;
		}

	public String removeStrSpaces(String s) {
		String newStr = s.trim().replaceAll("\t", "");
		  return newStr;
		}
	
	private String removeExtraSpaces(String s){
		if(!s.contains(" ")) return s;
		return removeExtraSpaces(s.replace(" ", ""));
	}
	
	public void convertIndStruct(PrintWriter pwIndStruct,PrintWriter pwOldModel){
		////System.out.println(indStructList.size());
		
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
						//////System.out.print(indPList.get(j)+" ");
					}
					//////System.out.println();
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
						//////System.out.print(indPList.get(j)+" ");
					}
					//////System.out.println();
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
						//////System.out.println(indPArray.length+" the length of them : "+indArray.length);
						//////System.out.println()
						if(!Integer.valueOf(indPArray[j]).equals(Integer.valueOf(indArray[j]))){
							pwOldModel.print(indHm.get(Integer.valueOf(indPArray[j]))+" ");
							//////System.out.println("what is J::::::::::::"+j);
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
						//////System.out.print(indPList.get(j)+" ");
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
						//////System.out.print(indPList.get(j)+" ");
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
						//////System.out.print(indPList.get(j)+" ");
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
						//////System.out.print(indPList.get(j)+" ");
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
		////System.out.println("starting time: " + start);

		ProcessReuters processCorefCorpus = new ProcessReuters();
		File input = new File(args[0]);
		BufferedReader brXMLCorpus = new BufferedReader(new FileReader(input));
		BufferedReader brXMLCorpus2 = new BufferedReader(new FileReader(input));
		processCorefCorpus.splitMarkableNode(args[0]);
		processCorefCorpus.printCountToken();
		
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
			processCorefCorpus.convertIndStruct(pwIndFormat,pwOldModel);
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
		////System.out.println("stopping time: " + stop);
		long elapsed = stop - start;
		////System.out.println("this is the total running time: " + elapsed);
	}
}
