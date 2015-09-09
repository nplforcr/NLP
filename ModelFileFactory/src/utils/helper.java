package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import aceProcessor.ProcessACEDevJdom;
import aceProcessor.TokenPos;

/**
 * 
 * @author dingcheng
 *
 */

public class helper {
	
	/**
	 * this method convert pronouns to fine-grained pronouns
	 * @param pos
	 * @param word
	 * @return
	 */

	public static String[] convertPosAndOp(String pos, String word, String op){
		String[] strPosOp= new String[2];
		TreebankPosTags tbPos = null;
		//System.out.println("pos in PosTag2PosModel.java: "+pos);
		
		if(TreebankPosTags.tbPosList3.contains(pos)){			
			//enum can not directly use special characters
			if(pos.equals(":")){
				tbPos= TreebankPosTags.valueOf("COLON");
			}else if(pos.equals("-")){
				tbPos= TreebankPosTags.valueOf("DASH");
			}else if(pos.equals(",")){
				tbPos= TreebankPosTags.valueOf("COMMA");
			}else if(pos.equals(".")){
				tbPos= TreebankPosTags.valueOf("PERIOD");
			}else if(pos.equals("``")){
				tbPos= TreebankPosTags.valueOf("WDQUOTE");
			}else if(pos.equals("''")){
				tbPos= TreebankPosTags.valueOf("WDQUOTE2");
			}else if(pos.equals("$")){
				tbPos= TreebankPosTags.valueOf("DOLLAR");
			}else if(pos.equals("'")){
				tbPos= TreebankPosTags.valueOf("SQUOTE");
			}else if(pos.equals("\"")){
				tbPos= TreebankPosTags.valueOf("DQUOTE");
			}else if(pos.equals("-RRB-")){
				tbPos= TreebankPosTags.valueOf("RRB");
			}else if(pos.equals("-LRB-")){
				tbPos= TreebankPosTags.valueOf("LRB");
			}else{
				//System.out.println(pos);
				tbPos= TreebankPosTags.valueOf(pos);	
			}
		}
		
		if((tbPos==TreebankPosTags.PRP) ||tbPos==TreebankPosTags.PRP$){
			if(ThirdPersonPron.thirdPerPronList2.contains(word)){
				pos = FineGrainedPron.thirdPron.toString();
			}
			else if(ThirdReflexives.thirdRefList2.contains(word)){
				pos = FineGrainedPron.thirdRef.toString();
				//System.out.println("pos in ThirdReflexives from convertPosAndOp: "+pos);
			}
			else if(SecondPersonPron.secondPerPronList2.contains(word)){
				pos = FineGrainedPron.secondPron.toString();
			}
			else if(SecondReflexives.secondRefList2.contains(word)){
				pos = FineGrainedPron.secondRef.toString();
				//System.out.println("pos in ThirdReflexives from convertPosAndOp: "+pos);
			}
			if(FirstPersonPron.firstPerPronList2.contains(word)){
				pos = FineGrainedPron.firstPron.toString();
			}
			else if(FirstReflexives.firstRefList2.contains(word)){
				pos = FineGrainedPron.firstRef.toString();
				//System.out.println("pos in ThirdReflexives from convertPosAndOp: "+pos);
			}
			else if(Demonstratives.demoList2.contains(word)){
				pos = FineGrainedPron.domonstrative.toString();
				//System.out.println("pos in ThirdReflexives from convertPosAndOp: "+pos);
			}
			else if(WhDemonstratives.whdemoList2.contains(word)){
				pos = FineGrainedPron.thirdRef.toString();
				//System.out.println("pos in ThirdReflexives from convertPosAndOp: "+pos);
			}
		}
		strPosOp[0]=pos;
		strPosOp[1]=op;
		return strPosOp;
	}

	
	public static String convertPos(String pos, String word){
		TreebankPosTags tbPos = null;
		//System.out.println("pos in PosTag2PosModel.java: "+pos);
		
		if(TreebankPosTags.tbPosList3.contains(pos)){			
			//enum can not directly use special characters
			if(pos.equals(":")){
				tbPos= TreebankPosTags.valueOf("COLON");
			}else if(pos.equals("-")){
				tbPos= TreebankPosTags.valueOf("DASH");
			}else if(pos.equals(",")){
				tbPos= TreebankPosTags.valueOf("COMMA");
			}else if(pos.equals(".")){
				tbPos= TreebankPosTags.valueOf("PERIOD");
			}else if(pos.equals("``")){
				tbPos= TreebankPosTags.valueOf("WDQUOTE");
			}else if(pos.equals("''")){
				tbPos= TreebankPosTags.valueOf("WDQUOTE2");
			}else if(pos.equals("$")){
				tbPos= TreebankPosTags.valueOf("DOLLAR");
			}else if(pos.equals("'")){
				tbPos= TreebankPosTags.valueOf("SQUOTE");
			}else if(pos.equals("\"")){
				tbPos= TreebankPosTags.valueOf("DQUOTE");
			}else if(pos.equals("-RRB-")){
				tbPos= TreebankPosTags.valueOf("RRB");
			}else if(pos.equals("-LRB-")){
				tbPos= TreebankPosTags.valueOf("LRB");
			}else{
				//System.out.println(pos);
				tbPos= TreebankPosTags.valueOf(pos);	
			}
		}
		
		if((tbPos==TreebankPosTags.PRP) ||tbPos==TreebankPosTags.PRP$ || tbPos==TreebankPosTags.DT ||tbPos==TreebankPosTags.WDT) {
			if(ThirdPersonPron.thirdPerPronList2.contains(word)){
				pos = FineGrainedPron.thirdPron.toString();
			}
			else if(ThirdReflexives.thirdRefList2.contains(word)){
				pos = FineGrainedPron.thirdRef.toString();
				//System.out.println("pos in ThirdReflexives from convertPosAndOp: "+pos);
			}
			else if(SecondPersonPron.secondPerPronList2.contains(word)){
				pos = FineGrainedPron.secondPron.toString();
			}
			else if(SecondReflexives.secondRefList2.contains(word)){
				pos = FineGrainedPron.secondRef.toString();
				//System.out.println("pos in ThirdReflexives from convertPosAndOp: "+pos);
			}
			if(FirstPersonPron.firstPerPronList2.contains(word)){
				pos = FineGrainedPron.firstPron.toString();
			}
			else if(FirstReflexives.firstRefList2.contains(word)){
				pos = FineGrainedPron.firstRef.toString();
				//System.out.println("pos in ThirdReflexives from convertPosAndOp: "+pos);
			}
			else if(Demonstratives.demoList2.contains(word)){
				pos = FineGrainedPron.domonstrative.toString();
				//System.out.println("pos in ThirdReflexives from convertPosAndOp: "+pos);
			}
			else if(WhDemonstratives.whdemoList2.contains(word)){
				pos = FineGrainedPron.whdemonstrative.toString();
				//System.out.println("pos in ThirdReflexives from convertPosAndOp: "+pos);
			}
		}
		return pos;
	}
	
	/**
	 * 
	 * @param sgmFile
	 * @param xmlFile
	 * @param tagFile
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static List<String> obtainOpList(File sgmFile,File xmlFile,File tagFile) throws ParserConfigurationException, SAXException, IOException{
		//I don't remember that I have written a class to extract token and pos and form a pair list then
		List<String> opList=new ArrayList<String>();
		TokenPos tokenPos = new TokenPos();
		Pair tokenPosPair=tokenPos.tokenPosPair(tagFile);
		List<String> tokenList=(List<String>)tokenPosPair.getFirst();
		List<String> posList=(List<String>)tokenPosPair.getSecond();
		ProcessACEDevJdom processACESgmJdom = new ProcessACEDevJdom();
		StringBuffer sbDev=processACESgmJdom.readSgmFile(sgmFile);					
		StringBuffer textBuffer=processACESgmJdom.createInputFiles(sbDev.toString());
		ProcessACEDevJdom processACEXmlJdom = new ProcessACEDevJdom(xmlFile);;
		processACEXmlJdom.extractEntity();
		opList=processACEXmlJdom.listMention(textBuffer, tokenList);
		return opList;
	}
	
	public void assignUnk(){
		
	}
}
