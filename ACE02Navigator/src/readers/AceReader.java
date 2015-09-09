/**
 * 
 */
package readers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import annotation.Mention;
import annotation.Name;
import annotation.NamedEntity;
import annotation.Relation;
import annotation.RelationMention;

import xmlconversion.TagTypes;
import xmlconversion.XmlHelper;

/**
 * @author somasw000
 *
 */
public class AceReader {
	public HashMap<String, NamedEntity> idNeM ;
	public HashMap<String, Mention> idMentionM ;
	public HashMap<String, Relation>  idRelationM ;
	public StringBuffer docBuf;
	
	public AceReader(){
		idNeM = new HashMap<String, NamedEntity>();
		idMentionM =  new HashMap<String, Mention>();
		idRelationM  =  new HashMap<String, Relation>();
	}

	/**
	 * 
	 * @param sgmFname
	 * @param annotFname
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void processDocument(String sgmFname,  String annotFname) throws ParserConfigurationException, SAXException, IOException{
		if(sgmFname!=null){
			//docBuf =  SgmReader.readDoc(sgmFname);
			//System.out.println(docBuf);
			//I need to use readDoc2 since sgm file is not so standard in tagging, readDoc will meet problem.
			docBuf =  SgmReader.readDoc2(sgmFname);
		}
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(new File(annotFname));

		processEntities(document);
		processRelations(document);
		//printRelations();
	}

	/**
	 * 
	 * @param st
	 * @param ed
	 * @return
	 */
	String extractText (int st, int ed ){
		String str = "";
		if(docBuf!=null){
			str = docBuf.substring(st,ed);
		}
		return str;
	}

	/**
	 * 
	 * @param document
	 */
	public void processRelations (org.w3c.dom.Document document){
		boolean debug = false;
		NodeList relList = document.getElementsByTagName(TagTypes.REL);
		for(int i=0;i<relList.getLength();i++){
			Node relNode = relList.item(i);
			Relation relation =  new Relation();
			relation.setId(relNode.getAttributes().getNamedItem(TagTypes.ID).getNodeValue());
			relation.setType(relNode.getAttributes().getNamedItem(TagTypes.TYPE).getNodeValue());
			relation.setSubtype(relNode.getAttributes().getNamedItem(TagTypes.SUBTYPE).getNodeValue());
			relation.setClassType(relNode.getAttributes().getNamedItem(TagTypes.CLASS).getNodeValue());

			List<Node>  relEntities =  XmlHelper.getChildrenByTagName(relNode, TagTypes.ARG);
			for(int j=0; j< relEntities.size(); j++){
				int argNum =  Integer.parseInt(relEntities.get(j).getAttributes().getNamedItem(TagTypes.ARGNUM).getNodeValue());
				if(argNum ==  1){
					relation.setEntityArg1(relEntities.get(j).getAttributes().getNamedItem(TagTypes.ENTID).getNodeValue());
				} else if (argNum ==  2){
					relation.setEntityArg2(relEntities.get(j).getAttributes().getNamedItem(TagTypes.ENTID).getNodeValue());
				} 
			}			

			List<Node> relMensList = XmlHelper.getChildrenByTagName(relNode, TagTypes.RELMENS);
			for(int j=0; j< relMensList.size(); j++) {
				RelationMention relMention  =  new RelationMention();
				Node relMenN  = XmlHelper.getFirstChildByTagName(relMensList.get(j), TagTypes.RELMEN);

				if(debug) System.out.println("relation id ==" +relMenN.getAttributes().getNamedItem(TagTypes.ID).getNodeValue());
				relMention.setId(relMenN.getAttributes().getNamedItem(TagTypes.ID).getNodeValue());

				//				List<Node>  mentionArgNodes  =  XmlHelper.getChildrenByTagName(relMensList.get(j), TagTypes.RELMENARG);
				List<Node>  mentionArgNodes  =  XmlHelper.getChildrenByTagName(relMenN, TagTypes.RELMENARG);

				for(int k =0; k< mentionArgNodes.size(); k++){
					int argNum =  Integer.parseInt(mentionArgNodes.get(k).getAttributes().getNamedItem(TagTypes.ARGNUM).getNodeValue());
//					System.out.println(" argnum for mention = "+ argNum);
					if (argNum ==  1){
						relMention.setMentionId1(mentionArgNodes.get(k).getAttributes().getNamedItem(TagTypes.MENID).getNodeValue());
					} else if (argNum ==2){
						relMention.setMentionId2(mentionArgNodes.get(k).getAttributes().getNamedItem(TagTypes.MENID).getNodeValue());
					}
				}
				relation.addRelationMention(relMention);
			}
			
			idRelationM.put(relation.getId(), relation);
			//String relType = relNode.getAttributes().getNamedItem(TagTypes.TYPE).getNodeValue();
			//String relSubType = relNode.getAttributes().getNamedItem(TagTypes.SUBTYPE).getNodeValue();
			//System.out.println("relation type: "+relType+" subtype:  "+relSubType);
		}
	}

	/**
	 * 
	 * @param document
	 */
	public void processEntities (org.w3c.dom.Document document){
		boolean debug =  false;
		NodeList entList = document.getElementsByTagName(TagTypes.ENT);

		for(int i= 0; i < entList.getLength(); i++){
			Node entityN =  entList.item(i);
			NamedEntity ne =  new NamedEntity ();
			if(debug) System.out.println(entityN.getNodeName() +"  "+entityN.getAttributes().getNamedItem(TagTypes.ID).getNodeValue());
			ne.setId(entityN.getAttributes().getNamedItem(TagTypes.ID).getNodeValue());
			NodeList mentionList  = entityN.getChildNodes();
			for(int j=0; j< mentionList.getLength(); j++){
				Node childN =  mentionList.item(j);
				if(debug) System.out.println(" \t mention node name == "+ childN.getNodeName());
				if(childN.getNodeName().equals(TagTypes.ENTYPE)) {
					if(debug) System.out.println(" Entity Type= " + childN.getAttributes().getNamedItem(TagTypes.GENE)+ " == "+ childN.getTextContent());
					ne.setType(childN.getTextContent());
				}
				if(childN.getNodeName().equals(TagTypes.ENTMEN)) {
					Mention mention  = processMention(childN);
					ne.addMention(mention);
					idMentionM.put(mention.getId(), mention);
				}

				//the following lines seeem not so important. There is no differences from above if
				//just reduplicated information.
				if(childN.getNodeName().equals(TagTypes.ENTATTR)) {
					List<Node> nameList  = XmlHelper.getChildrenByTagName(childN, TagTypes.NAME);
					for(int k=0; k< nameList.size(); k++) {
						Name name =  processName(nameList.get(k));
						ne.addNames(name);
					}
				}
			}
			idNeM.put(ne.getId(), ne);
		}
	}

	/**
	 * 
	 * @param nameN
	 * @return
	 */
	Name processName(Node nameN){
		Name nameAnnot =  new Name();
		int span []  =  processExtentHead(nameN);
		nameAnnot.setStart(span[0]);
		nameAnnot.setEnd(span[1]);
		nameAnnot.setCoveredText(extractText(nameAnnot.getStart(), nameAnnot.getEnd()));
		return nameAnnot;
	}

	/**
	 * 
	 * @param mentionN
	 * @return
	 */
	Mention processMention (Node mentionN){
		boolean debug  =  false;
		Mention mentionAnnot = new Mention();
		
		if(debug)  System.out.println(mentionN.getAttributes().getNamedItem(TagTypes.TYPE).getNodeValue()+
				" + "+ mentionN.getAttributes().getNamedItem(TagTypes.ID).getNodeValue());
		Node extNode = XmlHelper.getFirstChildByTagName(mentionN,TagTypes.EXTENT);
		//System.out.println(extNode.getNodeName()+" "+extNode.getNodeValue());
		Node extentCharseq = XmlHelper.getFirstChildByTagName(extNode,TagTypes.CHARSEQ);
		String extentPhrase=extentCharseq.getFirstChild().getNextSibling().getTextContent();
		//String extentPhrase = extNode.getFirstChild().getChildNodes().item(0).getNodeName();
		if(debug) System.out.println("extentPhrase in AceReader: "+extentPhrase);
		
		Node headNode = XmlHelper.getFirstChildByTagName(mentionN,TagTypes.HEAD);

		int extSpan []  =  processExtentHead(extNode);
		int headSpan [] =  processExtentHead(headNode);

		mentionAnnot.setId(mentionN.getAttributes().getNamedItem(TagTypes.ID).getNodeValue());
		mentionAnnot.setType(mentionN.getAttributes().getNamedItem(TagTypes.TYPE).getNodeValue());
		mentionAnnot.setExtentSt(extSpan[0]);
		mentionAnnot.setEntentEd(extSpan[1]);
		
		mentionAnnot.setHeadSt(headSpan[0]);
		mentionAnnot.setHeadEd(headSpan[1]);
		
		Node charseqNode = mentionN.getLastChild().getPreviousSibling().getFirstChild().getNextSibling();
		//System.out.println("charseqNode: "+charseqNode.getNodeName()+" "+this.getFirstChildByTagName(charseqNode, START));
		String token=charseqNode.getFirstChild().getNextSibling().getTextContent();
		//String wholePhrase = 
		//mentionAnnot.setExtentCoveredText(extractText(mentionAnnot.getExtentSt(), mentionAnnot.getExtentEd()+1));
		mentionAnnot.setExtentCoveredText(extentPhrase);
		mentionAnnot.setHeadCoveredText(token);
		return mentionAnnot;
	}


	/**
	 * 
	 * @param aNode
	 * @return
	 */
	int [] processExtentHead (Node aNode){

		int[] aceByteSpan = new int[2];
		Node charNode = XmlHelper.getFirstChildByTagName(aNode,TagTypes.CHARSEQ);
		Node startNode = XmlHelper.getFirstChildByTagName(charNode,TagTypes.START);
		Node endNode = XmlHelper.getFirstChildByTagName(charNode,TagTypes.END);

		aceByteSpan[0] = Integer.parseInt(startNode.getTextContent());
		aceByteSpan[1] = Integer.parseInt(endNode.getTextContent());
		return aceByteSpan;
	}

	/**
	 * 
	 */
	public void printEntities(){
		
	}

	/**
	 * 
	 */
	public void printRelations(){
		for(Relation rel: idRelationM.values()){
			System.out.println("\n\nRelation =="+rel.getPrintString()+"\nMENTION="+rel.getRelMentionString());
			
			for(int i=0; i< rel.getRelMentions().size(); i++){
				int arr  []  =  new int [4];
				
				RelationMention rmen  = rel.getRelMentions().get(i);
				System.out.println("\n mention-1 detials = "+idMentionM.get(rmen.getMentionId1()).getPrintString());
				System.out.println("\n mention-2 detials = "+idMentionM.get(rmen.getMentionId2()).getPrintString());
				arr[0] = idMentionM.get(rmen.getMentionId1()).getExtentSt();
				arr[1] = idMentionM.get(rmen.getMentionId1()).getExtentEd();
				arr[2] = idMentionM.get(rmen.getMentionId2()).getExtentSt();
				arr[3] = idMentionM.get(rmen.getMentionId2()).getExtentEd();
				Arrays.sort(arr);
				int st =  arr[0]-10;
				int ed  =  arr[3]+10;
				if(st <0) st =0;
				if(ed> docBuf.length()) ed  =  docBuf.length()-1;
				String context =  extractText(st, ed);
				System.out.println("\nCONTEXT "+ st+"_"+ed+" == "+ context.replaceAll("\n", ""));
			}
			NamedEntity ne1  =  idNeM.get(rel.getEntityArg1());
			NamedEntity ne2  =  idNeM.get(rel.getEntityArg2());
			System.out.println("\n Enitiy-1 detials = "+ne1.getPrintString());
			System.out.println("\n Enitiy-2 detials = "+ne2.getPrintString());

			//			System.out.println("\n Enitiy-1 detials = "+ne1.getPrintString()+"\n"+ne1.getMentionString());
//			System.out.println("\n Enitiy-2 detials = "+ne2.getPrintString()+"\n"+ne2.getMentionString());
		}
	}
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		//String sgmFname = "C:\\ACE02\\npaper2\\9801.35.sgm";
		//String sgmFname = "C:\\Documents and Settings\\lidi00000\\SICCD_RD\\ace_phase2\\data\\ace2_train\\npaper2\\9801.35.sgm";
		//String sgmFname = "/home/dingcheng/Documents/OSU_corpora/ace_phase2/data/ace2_train/npaper/9801.35.sgm";
		//String annotFname = "C:\\ACE02\\npaper2\\9801.35.sgm.tmx.rdc.xml";
		//String annotFname = "C:\\Documents and Settings\\lidi00000\\SICCD_RD\\ace_phase2\\data\\ace2_train\\npaper2\\9801.35.sgm.tmx.rdc.xml";
		//String annotFname = "/home/dingcheng/Documents/OSU_corpora/ace_phase2/data/ace2_train/npaper/9801.35.sgm.tmx.rdc.xml";
		//String sgmFname = "/Users/lixxx345/Documents/corpora/ace_phase2/data/ace_devtest/three2one/CNN19981012.2130.0981.sgm";
		//String annotFname = "/Users/lixxx345/Documents/corpora/ace_phase2/data/ace2_devtest/three2one/CNN19981012.2130.0981.sgm.tmx.rdc.xml";
		String sgmFname = "D:/workspace/ACE02Navigator/data/ace2_train/bnews/ABC19980106.1830.0029.sgm"; //args[0]
		String annotFname = "D:/workspace/ACE02Navigator/data/ace2_train/bnews/ABC19980106.1830.0029.sgm.tmx.rdc.xml";//args[1];
		AceReader aread  = new AceReader();
		aread.processDocument(sgmFname, annotFname);
	}

}


