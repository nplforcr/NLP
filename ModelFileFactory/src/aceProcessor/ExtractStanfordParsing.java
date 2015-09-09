package aceProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import utils.Pair;

import utils.ThirdPersonPron;
import utils.ThirdReflexives;
import xmlconversion.XmlHelper;

public class ExtractStanfordParsing {
	HashMap<Pair,Pair> spanIdHM;
	HashMap<Pair,String> roleIdHM;
	HashMap<Pair,String> typeIdHM;
	HashMap<Pair,String> headIdHM;
	HashMap<Pair,String> entityHM;
	HashMap<Pair,Pair> idSpanHM;
	HashMap<Integer,Integer> indexHM;
	static String ENTITY = "entity";
	static String ENTTYPE = "entity_type";
	static String ENTMENTION = "entity_mention";
	static String TYPE = "TYPE";
	static String ROLE = "ROLE";
	static String START = "start";
	static String END = "end";
	static String ID = "ID";
	ArrayList<String> synRoleList;

	public ExtractStanfordParsing() {
		// TODO Auto-generated constructor stub
		spanIdHM=new HashMap<Pair,Pair>();
		roleIdHM=new HashMap<Pair,String>();
		typeIdHM=new HashMap<Pair,String>();
		headIdHM=new HashMap<Pair,String>();
		entityHM=new HashMap<Pair,String>();
		idSpanHM=new HashMap<Pair,Pair>();
		indexHM = new HashMap<Integer,Integer>();
		synRoleList = new ArrayList<String>();
	}
	
	//the following function may not work since the bytespan cannot be consistent. So, adjustByteSpan must be used. Now, this method is only defined 
	//in PrepareACEKey.java and it can almost be consistent between different formats. But there are a little bit gap. So, it may lead to troubles here.
	//Then, it may take a long time for me to adjust this. I may use other simpler way to do this for the moment.
	public void extractEntity(File inputFile) {
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
					String entType=XmlHelper.getFirstChildByTagName(ithNode, ENTTYPE).getTextContent();
					System.out.println("entType: "+entType);
					List<Node> mentionList = XmlHelper.getChildrenByTagName(ithNode, ENTMENTION);
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
						start=XmlHelper.getFirstChildByTagName(charseqNode, START).getTextContent();
						end=XmlHelper.getFirstChildByTagName(charseqNode, END).getTextContent();
						System.out.println("token: "+token+" start: "+start+" end: "+end);
						Pair spanPair = new Pair(Integer.valueOf(start),Integer.valueOf(end));
						spanIdHM.put(idPair, spanPair);
						typeIdHM.put(idPair,type);
						roleIdHM.put(idPair,role);	
						headIdHM.put(idPair, token.toLowerCase());
						indexHM.put(Integer.valueOf(start), Integer.valueOf(end));
						entityHM.put(idPair, entType);
						//startIndexList.add(Integer.valueOf(start));
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
	
	public void synParse(File inputFile) throws IOException{
		BufferedReader bfInput = new BufferedReader(new FileReader(inputFile));
		String line = "";
		int sentNo = 0;
		while ((line = bfInput.readLine()) != null) {
			Pattern leftBra = Pattern.compile("\\(");
			Pattern rightBra = Pattern.compile("\\)");
			Matcher leftBraMatcher = leftBra.matcher(line);
			boolean foundLB = leftBraMatcher.find();
			Matcher rightBraMatcher = rightBra.matcher(line);
			boolean foundRB = rightBraMatcher.find();
			int menCount = 0;
			while (foundLB) {
				//System.out.println(leftBraMatcher.group());
				String LBmatch = leftBraMatcher.group();
				String RBmatch = rightBraMatcher.group();
				int indexLB = line.indexOf(LBmatch);
				int indexRB = line.indexOf(RBmatch);
				System.out.println(LBmatch+" "+RBmatch);
				//String subLine = line.substring(line.indexOf(match));
				foundLB = leftBraMatcher.find();
				foundRB = rightBraMatcher.find();
				menCount++;
			}
			sentNo++;
		}
	}
	
	public void synParse2(File inputFile, PrintWriter pwSyn, PrintWriter pwSyn_W, PrintWriter pwSyn_Ratio) throws IOException{
		BufferedReader bfInput = new BufferedReader(new FileReader(inputFile));
		String line = "";
		int sentNo = 0;
		while ((line = bfInput.readLine()) != null) {
			Pattern leftBra = Pattern.compile("\\(");
			Pattern rightBra = Pattern.compile("\\)");
			Matcher leftBraMatcher = leftBra.matcher(line);
			Matcher rightBraMatcher = rightBra.matcher(line);
			boolean foundLB = leftBraMatcher.find();
			boolean foundRB = rightBraMatcher.find();
			if(foundLB){
				String[] lineArray = line.split("\\s");
				//I increase i twice, one is in the for line and increase i once more at the end of the loop
				//so that I can keep the even i. 
				for(int i=0;i<lineArray.length;i++){
					String ithSubline = lineArray[i];
					if(i==0){
						ithSubline = ithSubline.substring(1);
					}
					String iplusSubline = lineArray[i+1];
					//System.out.println("i "+i+" "+ithSubline+" "+iplusSubline);
					leftBraMatcher = leftBra.matcher(ithSubline);
					foundLB = leftBraMatcher.find();
					rightBraMatcher = rightBra.matcher(iplusSubline);
					foundRB = rightBraMatcher.find();
					String LBmatch = "";
					if(foundLB){
						LBmatch = leftBraMatcher.group();
					}
					
					String RBmatch = "";
					if(foundRB){
						RBmatch = rightBraMatcher.group();
					}
					
					int indexLB = ithSubline.indexOf(LBmatch);
					int indexRB = iplusSubline.indexOf(RBmatch);
					int indexDash = iplusSubline.indexOf("-");
					//int whitespace = ithSubline.indexOf("\\s");
					if(foundLB){
						String word = iplusSubline.substring(0,indexDash).toLowerCase();
						String synrole = ithSubline.substring(0,indexLB);
						//System.out.println("Syn_W " +iplusSubline.substring(0,indexDash).toLowerCase()+" : "+ ithSubline.substring(0,indexLB));
						//pwSyn_W.println("Syn_W " +iplusSubline.substring(0,indexDash).toLowerCase()+" : "+ ithSubline.substring(0,indexLB));
						if(ThirdPersonPron.thirdPerPronList2.contains(word) || ThirdReflexives.thirdRefList2.contains(word)){
							//pwSyn.println("Syntax OLD : "+synrole);
							if(!synRoleList.contains(synrole)){
								synRoleList.add(synrole);
							}							
						}
//						else{
//							if(){
//								
//							}
//							pwSyn.println("Syntax NEW : "+synrole);
//						}
//						pwSyn_Ratio.println("Synname : "+synrole);
					}
					i++;
				}
			}
			sentNo++;
		}
		
		BufferedReader bfInput2 = new BufferedReader(new FileReader(inputFile));
		
		while ((line = bfInput2.readLine()) != null) {
			Pattern leftBra = Pattern.compile("\\(");
			Pattern rightBra = Pattern.compile("\\)");
			Matcher leftBraMatcher = leftBra.matcher(line);
			Matcher rightBraMatcher = rightBra.matcher(line);
			boolean foundLB = leftBraMatcher.find();
			boolean foundRB = rightBraMatcher.find();
			if(foundLB){
				String[] lineArray = line.split("\\s");
				//I increase i twice, one is in the for line and increase i once more at the end of the loop
				//so that I can keep the even i. 
				for(int i=0;i<lineArray.length;i++){
					String ithSubline = lineArray[i];
					if(i==0){
						ithSubline = ithSubline.substring(1);
					}
					String iplusSubline = lineArray[i+1];
					//System.out.println("i "+i+" "+ithSubline+" "+iplusSubline);
					leftBraMatcher = leftBra.matcher(ithSubline);
					foundLB = leftBraMatcher.find();
					rightBraMatcher = rightBra.matcher(iplusSubline);
					foundRB = rightBraMatcher.find();
					String LBmatch = "";
					if(foundLB){
						LBmatch = leftBraMatcher.group();
					}
					
					String RBmatch = "";
					if(foundRB){
						RBmatch = rightBraMatcher.group();
					}
					
					int indexLB = ithSubline.indexOf(LBmatch);
					int indexRB = iplusSubline.indexOf(RBmatch);
					int indexDash = iplusSubline.indexOf("-");
					//int whitespace = ithSubline.indexOf("\\s");
					if(foundLB){
						String word = iplusSubline.substring(0,indexDash).toLowerCase();
						String synrole = ithSubline.substring(0,indexLB);
						System.out.println("Syn_W " +iplusSubline.substring(0,indexDash).toLowerCase()+" : "+ ithSubline.substring(0,indexLB));
						pwSyn_W.println("Syn_W " +iplusSubline.substring(0,indexDash).toLowerCase()+" : "+ ithSubline.substring(0,indexLB));
						if(ThirdPersonPron.thirdPerPronList2.contains(word) || ThirdReflexives.thirdRefList2.contains(word)){
							pwSyn.println("Syntax OLD : "+synrole);
						}else{
							if(synRoleList.contains(synrole)){
								pwSyn.println("Syntax NEW : "+synrole);
							}
						}
						if(synRoleList.contains(synrole)){
							pwSyn_Ratio.println("Synname : "+synrole);
							pwSyn_W.println("Syn_W unkword : "+synrole);
						}
					}
					i++;
				}
			}
			sentNo++;
		}
		pwSyn_W.println("Syn_W eos : - = 1.00000000");
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ExtractStanfordParsing esParsing = new ExtractStanfordParsing();
		File inputFile = new File(args[0]);
		PrintWriter pwSyn = new PrintWriter(new FileWriter(new File(args[1],"three2one.syntax.model")));
		PrintWriter pwSyn_W = new PrintWriter(new FileWriter(new File(args[1],"three2one.synword.model")));
		PrintWriter pwSyn_Ratio = new PrintWriter(new FileWriter(new File(args[1],"three2one.synratio.model")));
		if(inputFile.isDirectory()){
			File[] files = inputFile.listFiles();
			for(int i=0;i<files.length;i++){
				esParsing.synParse2(files[i],pwSyn,pwSyn_W,pwSyn_Ratio);
			}
		}else{
			esParsing.synParse2(inputFile,pwSyn,pwSyn_W,pwSyn_Ratio);
		}
		pwSyn.close();
		pwSyn_W.close();
		pwSyn_Ratio.close();
	}

}
