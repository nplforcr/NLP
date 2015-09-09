package aceProcessor;
/*
 * @ author Dingcheng Li
 * @ date Oct. 15, 09
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RelationFrequency {
	File inputFile;
	static String RELATION = "relation";
	static String TYPE = "TYPE";
	static String SUBTYPE = "SUBTYPE";
	static String CLASS = "CLASS";
	static String ID = "ID";
	static String AT = "AT";
	static String BASEIN = "Based-In";
	static String LOC = "LOCATED";
	static String RESIDE = "Residence";
	static String NEAR = "NEAR";
	static String RELOC = "Relative-Location";
	static String PART = "PART";
	static String AFFPART = "Affiliate-Partner";
	static String MEM = "Member";
	static String OTHER = "Other";
	static String PARTOF = "Part-Of";
	static String SUBSID = "Subsidiary";
	static String ROLE = "ROLE";
	static String CITIZEN = "Citizen-Of";
	static String CLIENT = "Client";
	static String FOUNDER = "Founder";
	static String GSTAF = "General-Staff";
	static String MANAGE = "Management";
	static String OWNER = "Owner"; 
	static String SOC ="SOC"; 
	static String ASSO ="Associate";
	static String GPAR = "Grandparent";
	static String OPER ="Other-Personal";
	static String OPRO ="Other-Professional";
	static String OREL = "Other-Relative";
	static String PAR = "Parent";
	static String SIBLING = "Sibling";
	static String SPOUSE = "Spouse" ;
	List<HashMap<String,Integer>> listOfType;
	HashMap<String, Integer> atHM;
	HashMap<String, Integer> atBasedHM;
	HashMap<String, Integer> atLocHM;
	HashMap<String, Integer> atResHM;
	HashMap<String, Integer> nearHM;
	HashMap<String, Integer> nearRelocHM;
	
	HashMap<String, Integer> partHM;
	HashMap<String, Integer> partAffParHM;
	HashMap<String, Integer> partMemHM;
	HashMap<String, Integer> partOtherHM;
	HashMap<String, Integer> partPartofHM;
	HashMap<String, Integer> partSubsidHM;
	//HashMap<String, Integer> atHM;
	HashMap<String, Integer> roleHM;
	HashMap<String, Integer> roleAffParHM;
	HashMap<String, Integer> roleCitizenHM;	
	HashMap<String, Integer> roleClientHM;
	HashMap<String, Integer> roleFounderHM;
	HashMap<String, Integer> roleGenstafHM;
	HashMap<String, Integer> roleManageHM;
	HashMap<String, Integer> roleMemHM;
	HashMap<String, Integer> roleOtherHM;
	HashMap<String, Integer> roleOwnerHM;
	HashMap<String, Integer> socHM;
	HashMap<String, Integer> socAssocHM;
	HashMap<String, Integer> socOtherPerHM;
	HashMap<String, Integer> socOtherProHM;
	HashMap<String, Integer> socOtherRelHM;
	HashMap<String, Integer> soParHM;
	HashMap<String, Integer> soSiblingHM;
	HashMap<String, Integer> soSpouseHM;

	public RelationFrequency (File inFile) {
		inputFile = inFile;
		listOfType = new ArrayList<HashMap<String,Integer>>();
		atHM = new HashMap<String,Integer>();
		atBasedHM = new HashMap<String,Integer>();
		atLocHM = new HashMap<String,Integer>();
		atResHM = new HashMap<String,Integer>();
		nearHM = new HashMap<String,Integer>();
		nearRelocHM = new HashMap<String,Integer>();
		partHM = new HashMap<String,Integer>();
		partAffParHM = new HashMap<String,Integer>();
		partMemHM = new HashMap<String,Integer>();
		partOtherHM = new HashMap<String,Integer>();
		partPartofHM = new HashMap<String,Integer>();
		partSubsidHM = new HashMap<String,Integer>();
		//atHM;
		roleHM = new HashMap<String,Integer>();
		roleAffParHM = new HashMap<String,Integer>();
		roleCitizenHM = new HashMap<String,Integer>();	
		roleClientHM = new HashMap<String,Integer>();
		roleFounderHM = new HashMap<String,Integer>();
		roleGenstafHM = new HashMap<String,Integer>();
		roleManageHM = new HashMap<String,Integer>();
		roleMemHM = new HashMap<String,Integer>();
		roleOtherHM = new HashMap<String,Integer>();
		roleOwnerHM = new HashMap<String,Integer>();
		socHM = new HashMap<String,Integer>();
		socAssocHM = new HashMap<String,Integer>();
		socOtherPerHM = new HashMap<String,Integer>();
		socOtherProHM = new HashMap<String,Integer>();
		socOtherRelHM = new HashMap<String,Integer>();
		soParHM = new HashMap<String,Integer>();
		soSiblingHM = new HashMap<String,Integer>();
		soSpouseHM = new HashMap<String,Integer>();
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
				System.out.println("child: "+child.getNodeName()+""+child.getTextContent());
				NodeList relationNodeList = document.getElementsByTagName(RELATION);
				
				int count=0;
				
				for (int i = 0; i < relationNodeList.getLength(); i++) {
					Node ithNode = relationNodeList.item(i);
					String markableId = ithNode.getAttributes().getNamedItem(ID).getNodeValue();
					System.out.println(markableId);
					//System.out.println(ithNode.getNodeName()+" : "+ithNode.getAttributes().getNamedItem(ID).getNodeValue());
					
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
	
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		// E:\Dissertation Writing\corpora\corefann\wxml
		//"/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"
		///home/dingcheng/Documents/OSU_corpora/ace_phase2/data/ace2_train/bnews/ABC19980106.1830.0029.sgm.tmx.rdc.xml
		int item = 6;
		long start = System.currentTimeMillis();
		System.out.println("starting time: " + start);
		File inputFile = new File(args[0]);
		File outputDir = null;
		if(args.length>1){
			outputDir = new File(args[1]);
			if(!outputDir.exists()){
				outputDir.mkdirs();
			}
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
				RelationFrequency prepareEvaData = new RelationFrequency(listOfFiles[i]);
				PrintWriter pwDevData = null;
				PrintWriter pwTestData = null;
				try {
					pwDevData = new PrintWriter(new FileWriter(new File(devDir,listOfFiles[i].getName()+".dev")));
					////System.out.println("where are you? "+devDir.getAbsolutePath());
					prepareEvaData.prepareCorefList();
					pwDevData.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}else{
			RelationFrequency prepareEvaData = new RelationFrequency(inputFile);
			try {
				PrintWriter pwDevTestData=new PrintWriter(new FileWriter(new File(outputDir,inputFile.getName().substring(0,inputFile.getName().length()-3)+"eva")));
				prepareEvaData.prepareCorefList();
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
