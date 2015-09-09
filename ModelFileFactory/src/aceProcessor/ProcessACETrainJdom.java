package aceProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import corpusProcessor.FeaStruct;
import utils.Pair;

public class ProcessACETrainJdom {

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
	int tokenIndex = 0;
	int markableIndex = 0;
	List<FeaStruct> indStructList;
	List<Pair> wordPosPairList;
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
	HashMap<Pair,Pair> spanIdHM;
	HashMap<Pair,String> roleIdHM;
	HashMap<Pair,String> typeIdHM;
	HashMap<Pair,String> headIdHM;
	HashMap<Pair,Pair> idSpanHM;
	HashMap<Integer,Integer> indexHM;
	HashMap<Pair,String> entityHM;
	List<Pair> mentionIdList;
	List<Integer> startIndexList;
	
	File inputFile;

	public ProcessACETrainJdom(File inFile) {
		inputFile = inFile;
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
		reflexiveList = Arrays.asList(reflexiveArray);
		pronounList = Arrays.asList(pronounArray);
		maPronounsList = Arrays.asList(maPronounsArray);
		fePronounsList = Arrays.asList(fePronounsArray);
		nePronounsList = Arrays.asList(nePronounsArray);
		singDemonList = Arrays.asList(singDemonArray);
		pluralList = Arrays.asList(pluralArray);
		spanIdHM=new HashMap<Pair,Pair>();
		roleIdHM=new HashMap<Pair,String>();
		typeIdHM=new HashMap<Pair,String>();
		headIdHM=new HashMap<Pair,String>();
		entityHM=new HashMap<Pair,String>();
		mentionIdList=new ArrayList<Pair>();
		idSpanHM=new HashMap<Pair,Pair>();
		indexHM = new HashMap<Integer,Integer>();
		startIndexList=new ArrayList<Integer>();
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
				String type, role = "",head, token,start,end;
				for (int i = 0; i < entNodeList.getLength(); i++) {
					Node ithNode = entNodeList.item(i);
					String entType=this.getFirstChildByTagName(ithNode, ENTTYPE).getTextContent();
					System.out.println("entType: "+entType);
					List<Node> mentionList = this.getChildrenByTagName(ithNode, ENTMENTION);
					for(int j=0;j<mentionList.size();j++){
						Node mentionNode = mentionList.get(j);
						type = mentionNode.getAttributes().getNamedItem(TYPE).getNodeValue();
						System.out.println("type: "+type);
						if(mentionNode.getAttributes().getNamedItem(ROLE)!=null){
							role = mentionNode.getAttributes().getNamedItem(ROLE).getNodeValue();
							if(role.equals("PER")){
								role="PERSON";
							}else if(role.equals("ORG")){
								role="ORGANIZATION";
							}else if(role.equals("LOC")){
								role="LOCATION";
							}else if(role.equals("FACIL")){
								role="FACILITY";
							}
							System.out.println("role: "+role);
						}else{
							role=entType;
						}
						String[] idArray = mentionNode.getAttributes().getNamedItem(ID).getNodeValue().split("-");
						Pair idPair = new Pair(idArray[0],idArray[1]);
						//System.out.println(this.getFirstChildByTagName(mentionNode, EXTENT));
						//System.out.println(mentionNode.getChildNodes().getLength());
						NodeList childNodeList=mentionNode.getChildNodes();
//						for(int k=0;k<childNodeList.getLength();k++){
//							System.out.println(childNodeList.item(k));
//						}
						//Node charseqNode = mentionNode.getFirstChild().getNextSibling().getFirstChild().getNextSibling();
						Node charseqNode = mentionNode.getLastChild().getPreviousSibling().getFirstChild().getNextSibling();
						//System.out.println("charseqNode: "+charseqNode.getNodeName()+" "+this.getFirstChildByTagName(charseqNode, START));
						token=charseqNode.getFirstChild().getNextSibling().getTextContent();
						//System.out.println(token);
						//rightmost word as the head of the mention
						//it is impossible to have york-based as the mention. but why in input-entity, there
						//is such a mention? 
						token=token.substring(11,token.length()-2);
						
						if(token.contains("``")){
							System.out.println(token + " with bad quotes!");
							token=token.substring(2,token.length()-2);
						}
						if (token.contains(" ")) {
							String[] tokenArray = token.split(" ");
							token = tokenArray[tokenArray.length - 1];
							//one special case here is some word include quotes. we should remove them in order 
							//extract the real word. If we don't do so, it will lead to troubles so that `` may be 
							//extract as the mention rather than the word.
							//System.out.println("token ================= "+token);
							if(token.contains("``")){
								System.out.println(token + " with bad quotes!");
								token=token.substring(2,token.length()-2);
							}
						}
						start=this.getFirstChildByTagName(charseqNode, START).getTextContent();
						end=this.getFirstChildByTagName(charseqNode, END).getTextContent();
						System.out.println("token: "+token+" start: "+start+" end: "+end);
						Pair spanPair = new Pair(Integer.valueOf(start),Integer.valueOf(end));
						spanIdHM.put(idPair, spanPair);
						typeIdHM.put(idPair,type);
						roleIdHM.put(idPair,role);	
						headIdHM.put(idPair, token.toLowerCase());
						indexHM.put(Integer.valueOf(start), Integer.valueOf(end));
						entityHM.put(idPair, entType);
						startIndexList.add(Integer.valueOf(start));
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
	
	public void setCrFeatures(PrintWriter pwOper,PrintWriter pwDist,PrintWriter pwGender,PrintWriter pwNumber,PrintWriter pwNe,PrintWriter pwRole){
		Collections.sort(startIndexList);
		for(int i=0;i<startIndexList.size();i++){
			int ithStart=startIndexList.get(i);
		}
	}
	
	public void setNeFeatures(PrintWriter pwNe){
		//Set entries = map.entrySet();
		Iterator<Pair> itHm=roleIdHM.keySet().iterator();
		//Iterator it = entries.iterator();
		while (itHm.hasNext()) {
			Pair key = itHm.next();
			String role = roleIdHM.get(key);
			String type = typeIdHM.get(key);
			//System.out.println(type+" "+role);
			if(type.equals(PRON)){
				pwNe.println("Ne "+OLD+" : "+role);
			}else{
				pwNe.println("Ne "+NEW+" : "+role);
			}
		}
	}
	
	public void setNeRatio(PrintWriter pwNeRatio){
		//Set entries = map.entrySet();
		Iterator<Pair> itHm=roleIdHM.keySet().iterator();
		//Iterator it = entries.iterator();
		while (itHm.hasNext()) {
			Pair key = itHm.next();
			String role = roleIdHM.get(key);
			String type = typeIdHM.get(key);
			//System.out.println(type+" "+role);
			pwNeRatio.println("Name : "+role);
		}
		pwNeRatio.println("Name : -");
	}
	
	public void setTokenDependFeatures(PrintWriter pwTokenNe){
		//Set entries = map.entrySet();
		//roleIdHM stores id vs role pair where ID is the mentity mention ID such as "2-7" and role is the 
		//entity type such as LOC, ORG and etc. 
		//headIdHM also has ID as the key, but the value is the head token of the mention. So, we can 
		//construct the token/role pair based on the two HashMaps.
		Iterator<Pair> itHm=roleIdHM.keySet().iterator();
		//Iterator it = entries.iterator();
		System.out.println("roleIdHM: "+roleIdHM.size());
		while (itHm.hasNext()) {
			Pair key = itHm.next();
			String role = roleIdHM.get(key);
			String token = headIdHM.get(key);
			System.out.println(token+" "+role);
			pwTokenNe.println("Ne_W "+token+" : "+role);
			pwTokenNe.println("Ne_W unkword"+" : "+role);
		}
	//	pwTokenNe.println("Ne_W unkword : "+role);
		//names like o'donnell is tokenized as o ' donnell, as a result, o becomes a person's name. But in ACE annotation
		//it is the full name as the NE. In my model, I regard the last word to represent the NE, i.e. o'donnell. So, conflicts arise
		//in order to catch o as the person's name, I have to hard coded it as an NE for the moment.
		pwTokenNe.println("Ne_W o : PERSON");
		//similarly, 's in English can be the short form of us or is. But in the annotation, no such one, 
		pwTokenNe.println("Ne_W 's : PERSON");
		//similarly, ma for ma'am
		pwTokenNe.println("Ne_W ma : PERSON");
		pwTokenNe.println("Ne_W eos : -");
	}
	
	
	public void setTokenDependFeatures(PrintWriter pwTokenNe, PrintWriter pwNe){
		//Set entries = map.entrySet();
		//roleIdHM stores id vs role pair where ID is the mentity mention ID such as "2-7" and role is the 
		//entity type such as LOC, ORG and etc. 
		//headIdHM also has ID as the key, but the value is the head token of the mention. So, we can 
		//construct the token/role pair based on the two HashMaps.
		Iterator<Pair> itHm=roleIdHM.keySet().iterator();
		//Iterator it = entries.iterator();
		System.out.println("roleIdHM: "+roleIdHM.size());
		while (itHm.hasNext()) {
			Pair key = itHm.next();
			String role = roleIdHM.get(key);
			String token = headIdHM.get(key);
			System.out.println(token+" "+role);
			pwTokenNe.println("Ne_W "+token+" : "+role);
			pwTokenNe.println("Ne_W unkword"+" : "+role);
		}
		//pwTokenNe.println("Ne_W unkword : "+role);
		//names like o'donnell is tokenized as o ' donnell, as a result, o becomes a person's name. But in ACE annotation
		//it is the full name as the NE. In my model, I regard the last word to represent the NE, i.e. o'donnell. So, conflicts arise
		//in order to catch o as the person's name, I have to hard coded it as an NE for the moment.
		pwTokenNe.println("Ne_W o : PERSON");
		//similarly, 's in English can be the short form of us or is. But in the annotation, no such one, 
		pwTokenNe.println("Ne_W 's : PERSON");
		//similarly, ma for ma'am
		pwTokenNe.println("Ne_W ma : PERSON");
		pwTokenNe.println("Ne_W eos : -");
		
		//Set entries = map.entrySet();
		Iterator<Pair> itRoleHm=roleIdHM.keySet().iterator();
		//Iterator it = entries.iterator();
		while (itHm.hasNext()) {
			Pair key = itHm.next();
			String role = roleIdHM.get(key);
			String type = typeIdHM.get(key);
			//System.out.println(type+" "+role);
			if(type.equals(PRON)){
				pwNe.println("Ne "+OLD+" : "+role);
			}else{
				pwNe.println("Ne "+NEW+" : "+role);
			}
		}
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

	
	//now, I need to consider tokenize words as required format
	

	public static void main(String[] args) throws Exception {

		int item = 6;
		long start = System.currentTimeMillis();
		File fileTagTrain = new File(args[0]);
		File fileTagTest = null;
		if(args.length>1){
			fileTagTest = new File(args[1]);
		}
		
		File outputFile = new File(args[2]);
		//PrintWriter pwOutput = new PrintWriter(new FileWriter(outputFile));
		if(fileTagTrain.isDirectory()){
			File dirOutput = new File(outputFile,fileTagTrain.getName()+"Model");
			//System.out.println(fileTag);
			if(!dirOutput.exists()){
				dirOutput.mkdir();
			}
			PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(dirOutput,fileTagTrain.getName())+".ne.model",true));
			PrintWriter pwTokenNe = new PrintWriter(new FileWriter(new File(dirOutput,fileTagTrain.getName()+".neWord.model"),true));
			PrintWriter pwNeRatio = new PrintWriter(new FileWriter(new File(dirOutput,fileTagTrain.getName()+".neratio.model"),true));
			File[] fileArray = fileTagTrain.listFiles();
			for(int i=0;i<fileArray.length;i++){
				//System.out.println(fileArray[i]);
				if(fileArray[i].getName().endsWith(".sgm")||fileArray[i].getName().endsWith(".dtd") ||fileArray[i].getName().endsWith(".DS_Store") ){
					System.out.println(fileArray[i]);
					continue;
				}else{
					System.out.println(fileArray[i]);
					ProcessACETrainJdom processACEJdom = new ProcessACETrainJdom(fileArray[i]);
					processACEJdom.extractEntity();
					processACEJdom.setNeFeatures(pwOutput);
					processACEJdom.setTokenDependFeatures(pwTokenNe);
					processACEJdom.setNeRatio(pwNeRatio);
				}	
			}
			//the logic is that our model is a coreference resolver and Named entities can be obtained by NE recognizer.
			//So, it is unreasonable for words in test file don't have NE types. Hence, in training NE models, we include
			//testing files as well.
			if(fileTagTest!=null){
				File[] testFileArray = fileTagTest.listFiles();
				System.out.println(testFileArray[0]);
				for(int i=0;i<testFileArray.length;i++){
					System.out.println(testFileArray[i]);
					if(testFileArray[i].getName().endsWith(".sgm")||testFileArray[i].getName().endsWith(".dtd")){
						System.out.println(testFileArray[i]);
						continue;
					}else{
						System.out.println(testFileArray[i].getName());
						ProcessACETrainJdom processACEJdom = new ProcessACETrainJdom(testFileArray[i]);
						processACEJdom.extractEntity();
						processACEJdom.setNeFeatures(pwOutput);
						processACEJdom.setTokenDependFeatures(pwTokenNe);
						processACEJdom.setNeRatio(pwNeRatio);
					}	
				}
			}
			pwOutput.close();
			pwTokenNe.close();
			pwNeRatio.close();
		}else{
			ProcessACETrainJdom processACEJdom = new ProcessACETrainJdom(fileTagTrain);
			System.out.println(fileTagTrain);
			processACEJdom.extractEntity();
			PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(fileTagTrain.getName()+".tagged")));
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
