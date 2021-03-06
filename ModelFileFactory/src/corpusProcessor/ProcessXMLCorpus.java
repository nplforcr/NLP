package corpusProcessor;
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import utils.Pair;

public class ProcessXMLCorpus {

	// static String startXML =
	// "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
	static String startXML = "<?xml version=\"1.0\"?>";

	static String DOCU = "DOCUMENT";
	static String PARA = "PARAGRAPH";
	static String SENT = "SENTENCE";
	static String COREF = "COREF";
	static String TOKEN = "TOKEN";
	static String TOKTYPE = "token_type";
	static String PUNC = "TokenType.PUNCTUATION";
	static String LEXEM = "TokenType.LEXEME";
	static String COPY = "copy";
	static String OLD = "old";
	static String NEW = "new";
	static String ELEMENT = "Element"; 
	static String ID = "id";
	static String REF = "ref";
	static String nullString = "ZERO";
	static int MAXNUM = 8;
	static String start = "-";
	String OP = "";
	String ind = "-";
	String indP = "-";
	List<IndStruct> indStructList;
	List<Pair> wordPosPairList;
	
	public ProcessXMLCorpus() {
		indStructList = new ArrayList<IndStruct>();
		wordPosPairList = new ArrayList<Pair>();
	}
	
	class IndStruct{
		String operator;
		String indP;
		String ind;
		
		public IndStruct(String oper,String oldInd, String curInd){
			operator = oper;
			indP = oldInd;
			ind = curInd;
		}

		String getOper(){
			return operator;
		}
		
		String getIndP(){
			return indP;
		}
		
		String getInd(){
			return ind;
		}

	}
	
	public void buildWordPosPair(BufferedReader taggedFile){
		String taggedLine = "";
		String [] taggedLineArray;
		try {
			while((taggedLine=taggedFile.readLine())!=null){
				taggedLineArray = taggedLine.split(" ");
				for(int i=0;i<taggedLineArray.length;i++){
					String[] wordPos = taggedLineArray[i].split("/");
					System.out.println(taggedLineArray[i]);
					Pair wordPosPair = new Pair(wordPos[0],wordPos[1]);
					wordPosPairList.add(wordPosPair);
				}
			}
			System.out.println(wordPosPairList.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void extractCorefann(BufferedReader corefFile) {
		// String[] accHead = new String[2];
		String corefLine = " ";
		int countAllCoref = 0;
		// pwDJFile.println(bfDJFile.readLine());
		StringBuffer sbCoref = new StringBuffer();
		try {
			// sbCoref.append(brDJFile.readLine() + "\n");
			//InputStream inputStream = null;
			String displayDate = " ";
			while ((corefLine = corefFile.readLine()) != null) {
				//System.out.println(corefLine);
				countAllCoref++;
				sbCoref.append(corefLine + "\n");
				//inputStream = new ByteArrayInputStream(sbCoref.toString().getBytes());
			}
			
			int countChildCoref = 0;
			SAXBuilder saxBuilder = new SAXBuilder();
			try {
				Document doc = saxBuilder.build(new StringReader(sbCoref.toString()));
				Element root = doc.getRootElement();
				//System.out.println("hey, it is here???");
				//System.out.println("ROOT: "+root.getName());
				//Element child = root.getChild(PARA);
				List<Element> childrenPara = root.getChildren();
				//Stack<String> stackCoref = new Stack<String>();
				Queue<String> queueCoref = new LinkedList<String>();
				int countCoref = 0;
				int countCorefAll = 0;
				List<String> refList = new ArrayList<String>();
				List<String> corefIdList = new ArrayList<String>();
				List<String> tokenIdList = new ArrayList<String>();
				//List<Integer> replaceIndexList = new ArrayList<Integer>();
				HashMap<String,Integer> replaceRefIndexHM = new HashMap<String,Integer>();

				for(int i=0;i<childrenPara.size();i++){
					Element childPara = childrenPara.get(i);
					List<Element> childrenSent = childPara.getChildren();
					//System.out.println("childPara: "+childPara);
					for(int j=0;j<childrenSent.size();j++){
						Element childSent = childrenSent.get(j);
						Iterator itr = childSent.getDescendants();
						while(itr.hasNext()){
							Content c = (Content) itr.next();
							//System.out.println(c);
							//System.out.println("++++++++++++++++++");
							//System.out.println("content from iterator: "+c+" value: "+c.getValue());
							//c.getParentElement();
							if(c.toString().equals("[Element: <COREF/>]")){
							//if(c.toString().equals(ELEMENT+": <"+COREF+">")){
								//stackCoref.push(COREF);								
								List<Attribute> attributeList = ((Element)c).getAttributes();
								System.out.println(attributeList.get(2));
								//System.out.println(attributeList.get(2).getName());
								String corefId=((Element)c).getAttributeValue(ID);
								//System.out.println("value: "+corefId);
								corefIdList.add(corefId);
								//when there is not a REF in the Coref mark, simply add ZERO value
								//for the refList in order keep the consistency of index with CorefList
								if(!attributeList.get(2).getName().equals(REF)){
									queueCoref.offer(corefId);
									//System.out.println(attributeList.contains(REF));
									refList.add(nullString);
									//System.out.println("size of refList in Zero: "+refList.size()+"  "+refList.get(0));
								}
								//when there is a REF in the Coref mark, add REF value to the refList 
								//in order to find the its coreferring mention easily
								else{
									//System.out.println("are you here?????????????");
									String ref = ((Element)c).getAttributeValue(REF);
									queueCoref.offer(corefId+"_"+ref);
									refList.add(ref);
									//System.out.println("ref: "+ref);
								}
								//refList.add(ref);
								//corefIdRefList.add(value+"_"+ref);
								System.out.println("queueCoref size in the adding states: "+queueCoref.size());
								countCoref++;
								countCorefAll++;
							}
							
							//there is a possibility that the text is in the beginning, that is, the beginning words 
							//are noun or noun phrases which may be corefered by later words. 
							else if(c.toString().equals("[Element: <TOKEN/>]") && !queueCoref.isEmpty()){
								System.out.println("queueCoref size in the pop states: "+queueCoref.size());
								//else if(c.toString().equals("[Element: <TOKEN/>]") && !stackCoref.isEmpty()){
							//else if(c.toString().equals(ELEMENT+": <"+TOKEN+">")){
								//stackCoref.pop();
								//tokeIdList not only keep the token Id and also keep its coref ID 
								String coref = queueCoref.poll();
								String tokenId=((Element)c).getAttributeValue(ID);
								//tokenIdList.add(tokenId);
								tokenIdList.add(tokenId+"_"+coref);
								System.out.println("tokenId+coref+ref: "+tokenId+"_"+coref);
								//if ref for this coref equals to id of some coref
								//we need to replace token id of previous token of that coref with id of
								//the present token.
								//indP = "";
								//the following is not the final yet
								//if(indStructList.isEmpty()){
								String [] indPArray;
								List<String> indPArrayList = new ArrayList<String>();
								
								//if coref doesn't include "_", it implies that coref here is a new state.
								if(!coref.contains("_")){
									OP = NEW;
									if(indStructList.isEmpty()){
										ind = tokenId;
										IndStruct indStruct = new IndStruct(NEW, indP, ind);
										indStructList.add(indStruct);
										replaceRefIndexHM.put(tokenId, 0);
										System.out.println("new state: "+OP+" "+tokenId+"  "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
									}else{
										//System.out.println("new state: "+ "coref is "+coref+" indStructList size: "+indStructList.size());
										indP = indStructList.get(Integer.valueOf(tokenId)-1).getInd();
										indPArray = indP.split("_");
										indPArrayList = Arrays.asList(indPArray);
										//System.out.println("indPArrayList in new state: "+indPArrayList.size());
										
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
											
											while(it.hasNext()) { 
												String key = it.next(); 
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
											ind = indPSub +"_"+ tokenId;
											IndStruct indStruct = new IndStruct(NEW, indP, ind);
											indStructList.add(indStruct);
											//put the new added tokenId to MAXNUM-1, that is, it is in the last position of ind
											replaceRefIndexHM.put(tokenId, MAXNUM-1);
											System.out.println("new state more than 8: "+OP+" "+tokenId+"  "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
											
										}
										//size is fewer than 8
										else{
											//System.out.println("how do you escape the check in the first if????");
											ind = indP +"_"+ tokenId;
											IndStruct indStruct = new IndStruct(NEW, indP, ind);
											indStructList.add(indStruct);
											replaceRefIndexHM.put(tokenId,indPArrayList.size());
											System.out.println("new state fewer than 8: "+OP+" "+tokenId+"  "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
										}
									}
									System.out.println();
								}
								//coref contains "_" implies that this coref mention refer back to 
								//some other coref mention since there is a ref value (it is after "_")
								//so, coref here is an old state
								else{
									String[] corefPair = coref.split("_");
									String corefId = corefPair[0];
									String refId = corefPair[1];
									
									//OP = OLD;
									//System.out.println(Integer.valueOf(value));
									indP = indStructList.get(Integer.valueOf(tokenId)-1).getInd();
									indPArray = indP.split("_");
									indPArrayList = Arrays.asList(indPArray);
									String replaceStr = "";
									for(int jj=0;jj<tokenIdList.size();jj++){
										String[] tokenIdArray = tokenIdList.get(jj).split("_");
										//don't be confused, tokenIdArray[1] is the corefId rather than tokenId,
										//tokenIdArray[0] is the tokenId
										if(tokenIdArray[1].equals(refId)){
											replaceStr = tokenIdArray[0];
										}
									}
									//so far, so good, everything is going smoothly
									//I am trying to concatenate all words with corefering information
									//now, a problem happens: the corpus is taking the approach to annotate data
									//when all the coreferring points to the earliest state. So, if that state
									//is replaced in the middle, the later ones cannot find it. 
									//now, I am considering how to solve it. Hopefully it is not so hard to solve.
									System.out.println("replaceStr: "+replaceStr);
									int replaceIndex = 0;
									if(indPArrayList.contains(replaceStr)){
										replaceIndex = indPArrayList.indexOf(replaceStr);
										//replaceIndexList.add(replaceIndex);
										//indPArrayList.set(replaceIndex,tokenId);
										//replaceRefIndexHM.put(replaceStr, replaceIndex);
									}
									// if replaceStr is not in the indPArrayList, it implies that it has been
									//replaced by other index. But we can still find it, I use a replaceIndexList to store
									//the position of the index. 
									else{
										replaceIndex = replaceRefIndexHM.get(replaceStr);
									}
									
									if(replaceIndex<MAXNUM){
										OP = OLD;
										indPArrayList.set(replaceIndex,tokenId);
										System.out.println("replaceIndex: "+replaceIndex);
										//indPArrayList.set(replaceIndex,tokenId);
										StringBuffer strBuffer = new StringBuffer();
										strBuffer.append(indPArrayList.get(0));
										for(int ii=1;ii<indPArrayList.size();ii++){
											strBuffer.append("_"+indPArrayList.get(ii));
										}
										ind = strBuffer.toString();
										IndStruct indStruct = new IndStruct(OLD, indP,ind);
										indStructList.add(indStruct);
										System.out.println("old stat: indP "+OP+" "+indP+" ind "+ind );
									}
									//when replaceIndex is larger than or equal to MAXNUM, it implies 
									//that the item has been cut off and put into those with index out of range from
									//0 to MAXNUM, in this case, the item is regarded to be new, but different from 
									//brand new one, I put the index which refer to its ref back to the range from 
									//0 to MAXNUM
									else{
										System.out.println("replaceIndex bigger than MAXNUM: "+replaceIndex);
										OP = NEW;
										indP = indStructList.get(Integer.valueOf(tokenId)-1).getInd();
										indPArray = indP.split("_");
										indPArrayList = Arrays.asList(indPArray);
										//System.out.println("indPArrayList in new state: "+indPArrayList.size());
										
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
											ind = indPSub +"_"+ tokenId;
											//IndStruct indStruct = new IndStruct(NEW, indP, ind);
											//indStructList.add(indStruct);
											//put the new added tokenId to MAXNUM-1, that is, it is in the last position of ind
											replaceRefIndexHM.put(tokenId, MAXNUM-1);
											//indPArrayList.set(MAXNUM-1,tokenId);
											
											IndStruct indStruct = new IndStruct(OP, indP,ind);
											indStructList.add(indStruct);
											
											System.out.println("new state from Old State: "+OP+" "+tokenId+"  "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
										//}
									}
									
								}

								System.out.println("queueCoref size after pop: "+queueCoref.size());
								countCoref--;
								//System.out.println("what are you: "+value);
							}
							
							//in the following case, it is possible that they are in the beginning the text
							else if(c.toString().equals("[Element: <TOKEN/>]") && queueCoref.isEmpty()){
							//else if(c.toString().equals("[Element: <TOKEN/>]") && stackCoref.isEmpty()){
//								System.out.println("------------------------token parell to Coref---------------------------------");
								String tokenId=((Element)c).getAttributeValue(ID);
								//System.out.println("not in the coref: "+tokenId);
								//System.out.println("what are you::::::::::: "+((Element)c).getAttribute(TOKTYPE).getValue());
								// I found that if I ignored PUNC, the trouble is that the size of 
								//indStructList will be inconsistent. Similar cases will happen if
								//I ignore cases where Coref involves more than one word.
								//I will handle them later. 
							
								//if(((Element)c).getAttribute(TOKTYPE).getValue().equals(PUNC)){
									//continue;
								//}else if(((Element)c).getAttribute(TOKTYPE).getValue().equals(LEXEM)){
									OP = COPY;
									indP = indStructList.get(Integer.valueOf(tokenId)-1).getInd();
									ind = indP;
									IndStruct indStr = new IndStruct(OP, indP, ind);
									indStructList.add(indStr);
									System.out.println("copy state: "+OP+" "+tokenId+"  "+indP+" "+ind+" indStruct Size: "+ indStructList.size());
								//}								
							}
							//System.out.println(c.getParentElement());
						}
					}
				}
				System.out.println("countChildCoref: "+countChildCoref);				
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	public void extractPosInfor(BufferedReader corefFile) {
		// String[] accHead = new String[2];
		String corefLine = " ";
		int countAllCoref = 0;
		// pwDJFile.println(bfDJFile.readLine());
		StringBuffer sbCoref = new StringBuffer();
		try {
			// sbCoref.append(brDJFile.readLine() + "\n");
			//InputStream inputStream = null;
			String displayDate = " ";
			while ((corefLine = corefFile.readLine()) != null) {
				//System.out.println(corefLine);
				countAllCoref++;
				sbCoref.append(corefLine + "\n");
				//inputStream = new ByteArrayInputStream(sbCoref.toString().getBytes());
			}
			SAXBuilder saxBuilder = new SAXBuilder();
			try {
				Document doc = saxBuilder.build(new StringReader(sbCoref.toString()));
				Element root = doc.getRootElement();
				List<Element> childrenPara = root.getChildren();
				Queue<String> queueCoref = new LinkedList<String>();
				int countCoref = 0;
				int countCorefAll = 0;
				int countToken = 0;
				List<String> refList = new ArrayList<String>();
				List<String> corefIdList = new ArrayList<String>();
				List<String> tokenIdList = new ArrayList<String>();
				HashMap<String,Integer> replaceRefIndexHM = new HashMap<String,Integer>();
				for(int i=0;i<childrenPara.size();i++){
					Element childPara = childrenPara.get(i);
					List<Element> childrenSent = childPara.getChildren();
					for(int j=0;j<childrenSent.size();j++){
						Element childSent = childrenSent.get(j);
						Iterator itr = childSent.getDescendants();
						String preToken = "";
						while(itr.hasNext()){
							countToken++;
							Content c = (Content) itr.next();
							if(c.toString().equals("[Element: <TOKEN/>]")){
								String token =((Element)c).getText();
								int indexToken = wordPosPairList.indexOf(token);
								//wordPosPairList.
								Pair prePair = null;
								if(indexToken>0){
									prePair = wordPosPairList.get(indexToken-1);
									String preWord = (String)prePair.getFirst();
									String prePos =  (String)prePair.getSecond();
									
									
								}
								//the same output for the following line
								//String token =((Element)c).getName();
								
								System.out.println("token: "+token);
							}
						}
					}
				}
				System.out.println(countToken);
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void convertIndStruct(PrintWriter pwIndStruct){
		System.out.println(indStructList.size());
		
		for(int i=0;i<indStructList.size();i++){
			
			IndStruct indStruct = indStructList.get(i);
			pwIndStruct.print("Ind ");
			String oper = indStruct.getOper();
			String indPrev = indStruct.getIndP();
			String indCur = indStruct.getInd();
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
					indHm.put(Integer.valueOf(indPList.get(j)), j);
				}
				for(int j=0;j<indPArray.length-1;j++){
					pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[j]))+"_");
				}
				pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+" ");
				for(int j=0;j<indArray.length-1;j++){
					pwIndStruct.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
				}
				pwIndStruct.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+" ");
				pwIndStruct.println();
			}
			
			else if(oper.equals(NEW)){
				pwIndStruct.print(NEW+" ");
				
				if(indPList.size()==1 && indPList.contains(-1)){
					pwIndStruct.println("-1"+" "+indArray[0]);
				}else if(indPList.size()==1 && indPList.get(0)==(0)){
					pwIndStruct.println("0"+" "+indArray[0]+"_"+indArray[1]);
				}
				else {
					indPList.add(Integer.valueOf(indArray[indArray.length-1]));
					Collections.sort(indPList);
					for(int j=0;j<indPList.size();j++){
						indHm.put(indPList.get(j), j);
						//System.out.print(indPList.get(j)+" ");
					}
					//System.out.println();
					for(int j=0;j<indPArray.length-1;j++){
						pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[j]))+"_");
					}
					pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+" ");
					for(int j=0;j<indArray.length-1;j++){
						pwIndStruct.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
					}
					pwIndStruct.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+" ");
					pwIndStruct.println();
				}
			}
			
			else if(oper.equals(OLD)){
				//System.out.println("+++++++++++++++++++++++++++++++++++++++");
				pwIndStruct.print(OLD+" ");
				for(int j=0;j<indPArray.length;j++){
					Integer itemIndP = Integer.valueOf(indPArray[j]);
					Integer itemInd = Integer.valueOf(indArray[j]);
					//System.out.println(itemIndP+" "+itemInd);
					if(!itemIndP.equals(itemInd)){
						indPList.add(itemInd);
						break;
					}
				}
				//System.out.println(indPList.size());
				Collections.sort(indPList);
				for(int j=0;j<indPList.size();j++){
					indHm.put(indPList.get(j), j);
				}
				for(int j=0;j<indPArray.length-1;j++){
					pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[j]))+"_");
				}
				pwIndStruct.print(indHm.get(Integer.valueOf(indPArray[indPArray.length-1]))+" ");
				for(int j=0;j<indArray.length-1;j++){
					pwIndStruct.print(indHm.get(Integer.valueOf(indArray[j]))+"_");
				}
				pwIndStruct.print(indHm.get(Integer.valueOf(indArray[indArray.length-1]))+" ");
				pwIndStruct.println();
			}
		}
	}
	
	public void buildPosDependence(){
		
	}

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		// E:\Dissertation Writing\corpora\corefann\wxml
		//"/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"

		int item = 6;
		long start = System.currentTimeMillis();
		System.out.println("starting time: " + start);

		ProcessXMLCorpus processXMLCorpus = new ProcessXMLCorpus();
		File input = new File(args[0]);
		BufferedReader brXMLCorpus = new BufferedReader(new FileReader(input));
		BufferedReader brXMLCorpus2 = new BufferedReader(new FileReader(input));
		BufferedReader brTagged = new BufferedReader(new FileReader(new File(args[1])));
		processXMLCorpus.extractCorefann(brXMLCorpus);
		processXMLCorpus.buildWordPosPair(brTagged);
		processXMLCorpus.extractPosInfor(brXMLCorpus2);
		
		File outputDir = new File(args[2]);
		if(!outputDir.exists()){
			outputDir.mkdirs();
		}
		
		PrintWriter pwIndFormat = null;
		try {
			pwIndFormat = new PrintWriter(new FileWriter(new File(outputDir,input.getName().substring(0,input.getName().length()-5)+".model")));
			processXMLCorpus.convertIndStruct(pwIndFormat);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		pwIndFormat.close();
		

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
