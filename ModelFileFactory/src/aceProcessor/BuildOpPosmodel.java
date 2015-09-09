package aceProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import utils.OPEnum;
import utils.Pair;
import utils.TreebankPosTags;
import utils.helper;


public class BuildOpPosmodel {


	HashMap<String,Double> opKeyHM;
	HashMap<String,Double> posKeyHM;
	HashMap<String,Double> opHM;
	HashMap<String,Double> posHM;
	PrintWriter pwOpOutput;
	PrintWriter pwPosOutput;
	boolean smooth = true;
	boolean noCopy = true;
	List<List<Double>> opRatioList;
	List<List<Double>> posRatioList;
	List<List<String>> opKeyValueList;
	List<List<String>> posKeyValueList;
	boolean smoothOP = false;

	/**
	 * 
	 * @param fileTag
	 * @param fileTag2
	 * @param fileOut
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public BuildOpPosmodel(File fileTag,File fileTag2,File fileOut, File posModelOut) throws IOException, ParserConfigurationException, SAXException{
		
		opHM = new HashMap<String,Double>();
		posHM = new HashMap<String,Double>();
		
		if(!fileOut.exists()){
			fileOut.mkdirs();
		}
		
		//we should direct posModelOut directly to the path of modelfiles since the prababilities of smoothPosModel have been calculated. 
		if(!posModelOut.exists()){
			posModelOut.mkdirs();
		}
		
		//the smoothing does not work for opmodel, since smoothing this way does not add small probabilities to some rare
		//ones. Instead, add more instances which have their complete probabilty summation (to 1). 
		//I will think about better ways. Now, I decide to correct the cpp code. If the prabability is negative 
		//infinity, I will assign a small values to it. Simple way, but may have other troubles.
		
		if(smooth){
			if(smoothOP){
				pwOpOutput = new PrintWriter(new FileWriter(new File(posModelOut,fileTag2.getName() + ".smoothop.model")));
				smoothOpModel();
			}else{
				pwOpOutput = new PrintWriter(new FileWriter(new File(fileOut,fileTag2.getName() + ".op.model")));
				this.buildModelNoSmoothing(fileTag, fileTag2, fileOut);
			}
			
			pwPosOutput = new PrintWriter(new FileWriter(new File(posModelOut,fileTag2.getName() + ".smoothpos.model")));
			opKeyHM = new HashMap<String,Double>();
			posKeyHM = new HashMap<String,Double>();
			fillInstanceHM(fileTag,fileTag2);
			smoothPosModel();
			this.composeSmoothModel();
			
			//I don't want to bother to change the code too much. So, I simply call this by compose opmodel (opmodel doesn't need smoothing)
			//this.buildModelNoSmoothing(fileTag, fileTag2, fileOut);
			printOutput();
		}else{
			pwOpOutput = new PrintWriter(new FileWriter(new File(fileOut,fileTag2.getName() + "nsm.opmodel"), true));
			pwPosOutput = new PrintWriter(new FileWriter(new File(fileOut,fileTag2.getName() + "nsm.posmodel"), true));
			this.buildModelNoSmoothing(fileTag, fileTag2, fileOut);
		}
	}

	/**
	 * 
	 * @param fileTag
	 * @param fileTag2
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	
	public void fillInstanceHM(File fileTag,File fileTag2) throws ParserConfigurationException, SAXException, IOException{
		//I don't remember that I have written a class to extract token and pos and form a pair list then
		TokenPos tokenPos = new TokenPos();
		if (fileTag.isDirectory()) {
			File[] fileArray = fileTag.listFiles();
			for(int i=0;i<fileArray.length;i++){
				List<String> tokenList=new ArrayList<String>();
				List<String> posList=new ArrayList<String>();
				Pair tokenPosPair=tokenPos.tokenPosPair(fileArray[i]);
				List<String> opList=new ArrayList<String>();
				StringBuffer textBuffer=new StringBuffer();
				File[] fileArray2=fileTag2.listFiles();
				tokenList=(List<String>)tokenPosPair.getFirst();
				posList=(List<String>)tokenPosPair.getSecond();

				for(int j=0;j<fileArray2.length;j++){
					if(fileArray2[j].getName().endsWith(".sgm")){

						if(fileArray[i].getName().contains(fileArray2[j].getName())){
							System.out.println(fileArray2[j]);
							ProcessACEDevJdom processACESgmJdom = new ProcessACEDevJdom();
							StringBuffer sbDev=processACESgmJdom.readSgmFile(fileArray2[j]);					
							textBuffer=processACESgmJdom.createInputFiles(sbDev.toString());
							//List<String> tokenList=processACESgmJdom.tokenList;
							//System.out.println("tokenList.get(0): "+tokenList.get(0));			
						}
					}
				}

				for(int j=0;j<fileArray2.length;j++){
					if(fileArray2[j].getName().endsWith(".xml")){
						String fileName1 =fileArray[i].getName();
						String fileName2 = fileArray2[j].getName();
						//System.out.println(fileName1.substring(0, fileName1.length()-11)+" "+fileName2.substring(0,fileName2.length()-16));
						if(fileName1.substring(0, fileName1.length()-11).equals(fileName2.substring(0,fileName2.length()-16))){
							System.out.println(fileArray2[j]);
							ProcessACEDevJdom processACEXmlJdom = new ProcessACEDevJdom(fileArray2[j]);;
							processACEXmlJdom.extractEntity();
							opList=processACEXmlJdom.listMention(textBuffer, tokenList);
						}
					}
				}

				String word, wordP, pos,posP,op, opP;
				String strPosOp[];
				String strPosOpP[];
				op=opList.get(0);
				word = tokenList.get(0);
				pos=posList.get(0);
				//the following method convertPosAndOp is only used for the moment when we only want to do third person pronoun resolutions.
				strPosOp=helper.convertPosAndOp(pos, word,op);
				pos=strPosOp[0];
				op=strPosOp[1];
				
			
				//strOp is composed of opP posP and op
				String strOp = "- - : "+op;
				if(!opHM.containsKey(strOp)){
					opHM.put(strOp, new Double(1));
				}else{
					double count = opHM.get(strOp)+1;
					opHM.put(strOp,count);
				}
				
				
				String strOpKey = "- -";
				if(!opKeyHM.containsKey(strOpKey)){
					opKeyHM.put(strOpKey, new Double(1));
				}else{
					double count = opKeyHM.get(strOpKey)+1;
					opKeyHM.put(strOpKey,count);
				}
				//String strPos = op+" - "+pos;
				//op can be copy or NEW. So, the condition should be here.
				//I know why, I missed the colon, so the condition is lost.
				String strPosKey = "- "+op;
				if(!posKeyHM.containsKey(strPosKey)){
					posKeyHM.put(strPosKey, new Double(1));
				}else{
					double count = posKeyHM.get(strPosKey)+1;
					posKeyHM.put(strPosKey, count);
				}
				String strPos = "- "+op+" : " +pos;
				if(!posHM.containsKey(strPos)){
					posHM.put(strPos, new Double(1));
				}else{
					double count = posHM.get(strPos)+1;
					posHM.put(strPos, count);
				}
				System.out.println("fillInstanceHM three list size should be equal: "+tokenList.size()+" "+posList.size()+" "+opList.size());

				for(int j=1;j<tokenList.size();j++){
					word=tokenList.get(j);
					pos=posList.get(j);
					op=opList.get(j);
					strPosOp=helper.convertPosAndOp(pos, word,op);
					//System.out.println(pos+" ================ "+op+" "+strPosOp);
					pos=strPosOp[0];
					op=strPosOp[1];
					posP=posList.get(j-1);
					wordP = tokenList.get(j-1);
					opP=opList.get(j-1);
					strPosOpP=helper.convertPosAndOp(posP, wordP,opP);
					posP=strPosOpP[0];
					opP=strPosOpP[1];
					
					strOpKey = opP+" "+posP;
					if(!opKeyHM.containsKey(strOpKey)){
						opKeyHM.put(strOpKey, new Double(1));
					}else{
						double count = opKeyHM.get(strOpKey)+1;
						opKeyHM.put(strOpKey,count);
					}
					strPosKey = posP+" "+op;
					System.out.println("strOpKey: "+strOpKey +" strPosKey "+strPosKey);
					if(!posKeyHM.containsKey(strPosKey)){
						posKeyHM.put(strPosKey, new Double(1));
					}else{
						double count = posKeyHM.get(strPosKey)+1;
						posKeyHM.put(strPosKey, count);
					}
					
					strOp = opP+" "+posP+" : "+op;
					if(!opHM.containsKey(strOp)){
						opHM.put(strOp, new Double(1));
					}else{
						double count = opHM.get(strOp)+1;
						opHM.put(strOp,count);
					}
					strPos = posP+" "+op+" : "+pos;
					if(!posHM.containsKey(strPos)){
						posHM.put(strPos, new Double(1));
					}else{
						double count = posHM.get(strPos)+1;
						posHM.put(strPos, count);
					}
				}
				opP = opList.get(opList.size()-1);
				wordP=tokenList.get(tokenList.size()-1);
				posP=posList.get(posList.size()-1);
				strPosOpP=helper.convertPosAndOp(posP, wordP,opP);
				posP=strPosOpP[0];
				opP=strPosOpP[1];
				
				strOpKey = opList.get(opList.size()-1)+" "+posP;
				if(!opKeyHM.containsKey(strOpKey)){
					opKeyHM.put(strOpKey, new Double(1));
				}else{
					double count = opKeyHM.get(strOpKey)+1;
					opKeyHM.put(strOpKey,count);
				}
				strPosKey = posP+" -";
				if(!posKeyHM.containsKey(strPosKey)){
					posKeyHM.put(strPosKey, new Double(1));
				}else{
					double count = posKeyHM.get(strPosKey)+1;
					posKeyHM.put(strPosKey, count);
				}
				
				strOp = opList.get(opList.size()-1)+" "+posP+" : -";
				if(!opHM.containsKey(strOp)){
					opHM.put(strOp, new Double(1));
				}else{
					double count = opHM.get(strOp)+1;
					opHM.put(strOp,count);
				}
				strPos = posP+ " - : -";
				if(!posHM.containsKey(strPos)){
					posHM.put(strPos, new Double(1));
				}else{
					double count = posHM.get(strPos)+1;
					posHM.put(strPos, count);
				}
			}
		}
	}

	public void smoothOpModel(){
		System.out.println("opHM size before smoothing: "+opHM.size());
		for(OPEnum opEnum : OPEnum.values()){
			String op = opEnum.toString();
			for(int i=0;i<TreebankPosTags.tbPosList3.size();i++){
				String postag = TreebankPosTags.tbPosList3.get(i);
				double addSum = 0.0;
				String strOpKey = op+" "+postag;
				for(OPEnum opEnum2 : OPEnum.values()){
					String op2 = opEnum2.toString();
					
					String strOp = op+" "+postag+" : "+op2;
//					if(opKeyHM.containsKey(strOpKey)){
//						addSum+=(double)opKeyHM.get(strOpKey);
//					}
					//System.out.println("strOP outside : "+strOp);
					if(!opHM.containsKey(strOp) && opKeyHM.containsKey(strOpKey)){
						//System.out.println("strOP inside : "+strOp);
						//System.out.println("strOp in smoothOpModel: "+strOp);
						//System.out.println(opKeyHM.get(strOpKey));
						//System.out.println("what: "+(double)1/(opKeyHM.get(strOpKey)*10));
						opHM.put(strOp, (double)(opKeyHM.get(strOpKey)/100));
						addSum+=(double)(opKeyHM.get(strOpKey)/100);
					}
				}
				if(opKeyHM.containsKey(strOpKey)){
					double count = opKeyHM.get(strOpKey)+addSum;
					opKeyHM.put(strOpKey, count);
				}
			}
		}
		System.out.println("opHM size after smoothing: "+opHM.size());
	}

	public void smoothPosModel(){
		System.out.println("posHM size before smoothing: "+posHM.size());
		
		for(int i=0;i<TreebankPosTags.tbPosList3.size();i++){
			String postag = TreebankPosTags.tbPosList3.get(i);
			for(OPEnum opEnum : OPEnum.values()){
				String op = opEnum.toString();
				double addSum = 0.0;
				String strPosKey = postag+" "+ op;
				for(int j=0;j<TreebankPosTags.tbPosList3.size();j++){
					String postag2 = TreebankPosTags.tbPosList3.get(j);
					String strPos = postag+" "+op+" : "+postag2;
					//System.out.println("strPos: "+strPos+" strPosKey: "+strPosKey);
					//addSum add the number added for missing strPos
					//but obviously, we add too much for missing strPos if we add the number of strPosKey to 
					//missing ones.
					//we need to add the existing posKeyHM total to addSum now.
//					if(posKeyHM.containsKey(strPosKey)){
//						addSum = posKeyHM.get(strPosKey);
//					}
					if(!posHM.containsKey(strPos) && posKeyHM.containsKey(strPosKey)){
						//System.out.println("strPos in smoothPosModel: "+strPos);
						//System.out.println(posKeyHM.get(strPosKey));
						//System.out.println((double)(1/((posKeyHM.get(strPosKey)*10))));
						//we should add low number about propotional to 1/keytotal*1/10 depending on how much the keytal is
						//so, we may write a little bit complex lines.
						//if(posKeyHM.get(key))
						posHM.put(strPos, (double)((posKeyHM.get(strPosKey)/100)));
						addSum=addSum+(double)(posKeyHM.get(strPosKey)/100);
						//System.out.println("addSum in strPos: "+addSum);
					}	
				}
				//after we finish add all of missing, we add addSum to posKeyHM so that 
				//the total number of posKey is equal to total number of posStr. 
				if(posKeyHM.containsKey(strPosKey)){
					//System.out.println("addSum in strPos total: "+addSum);
					double count = posKeyHM.get(strPosKey)+addSum;
					posKeyHM.put(strPosKey, count);
				}
			}
		}
		
		System.out.println("posHM size after smoothing: "+posHM.size());
		
		
//		Iterator<String> iterator = posHM.keySet().iterator();
//		while(iterator.hasNext()){
//			String next = iterator.next();
//			double value = posHM.get(next);
//			System.out.println(" strpos: "+next+" value: "+value);
//		}
		
	}

	public void composeSmoothModel(){
		if(smoothOP){
			Iterator<String> iterOpHM = opHM.keySet().iterator();
			opRatioList = new ArrayList<List<Double>>();
			opKeyValueList = new ArrayList<List<String>>();
			List<String> opKeyList = new ArrayList<String>();
			// It seems that we should not smooth opmodel
			//but we do need some smoothing ones. for example, I found that there is not NEW PRP$ : NEW for OPModel. Yet,
			//there is a PRP$ NEW. why?
			while(iterOpHM.hasNext()){
				String opStr= iterOpHM.next();
				String[] opStrArray = new String[2];
				if(opStr.contains(" : :")){
					//System.out.println(opStr+" index of : "+opStr.indexOf(":")+" the length of opStr: "+opStr.length()+" "+opStr.substring(0,opStr.indexOf(":")+1));
					opStrArray[0]=opStr.substring(0,opStr.indexOf(":")+1);
					opStrArray[1]=opStr.substring(opStr.indexOf(":")+2);
				}else{
					opStrArray = opStr.split(" :");
				}
				
				double count = opHM.get(opStr);
				if(!opKeyList.contains(opStrArray[0])){
					List<Double> ratioList = new ArrayList<Double>();
					List<String> opStrList = new ArrayList<String>();
					//opKeyList helps to keep indices
					opKeyList.add(opStrArray[0]);
					//System.out.println("what is opStrArray: "+opStrArray[0]);
					double ratio = count/opKeyHM.get(opStrArray[0]);
					ratioList.add(ratio);
					opStrList.add(opStr);
					opRatioList.add(ratioList);
					opKeyValueList.add(opStrList);
				}else{
					int index = opKeyList.indexOf(opStrArray[0]);
					List<Double> ratioList = opRatioList.get(index);
					List<String> opStrList = opKeyValueList.get(index);
					double ratio = count/opKeyHM.get(opStrArray[0]);
					ratioList.add(ratio);
					if(!opStrList.contains(opStr)){
						opStrList.add(opStr);
					}
					opKeyValueList.set(index,opStrList);
					opRatioList.set(index,ratioList);
				}
			}
		}
		
		Iterator<String> iterPosHM = posHM.keySet().iterator();
		posRatioList = new ArrayList<List<Double>>();
		posKeyValueList = new ArrayList<List<String>>();
		List<String> posKeyList = new ArrayList<String>();
		while(iterPosHM.hasNext()){
			String posStr= iterPosHM.next();
			
			String[] posStrArray = new String[2];
			if(posStr.startsWith(":") && !posStr.endsWith(":")){
				//System.out.println(posStr+" index of : "+posStr.lastIndexOf(":")+" the length of opStr: "+posStr.length()+" "+posStr.substring(0,posStr.lastIndexOf(":")-1));
				posStrArray[0]=posStr.substring(0,posStr.lastIndexOf(":")-1);
				posStrArray[1]=posStr.substring(posStr.lastIndexOf(":")+2);
			}else if(posStr.endsWith(":")){
				posStrArray[0]=posStr.substring(0,posStr.lastIndexOf(":")-3);
				posStrArray[1]=posStr.substring(posStr.lastIndexOf(":"));
			}else if(!posStr.startsWith(":") && !posStr.endsWith(":")){
				posStrArray = posStr.split(" :");
			}
			double count = posHM.get(posStr);
			//when posKeyList is empty
			if(!posKeyList.contains(posStrArray[0])){
				List<Double> ratioList = new ArrayList<Double>();
				List<String> posStrList = new ArrayList<String>();
				//posKeyList helps to keep indices
				posKeyList.add(posStrArray[0]);
				//System.out.println("what is posStrArray: "+posStr);
				double ratio = count/posKeyHM.get(posStrArray[0]);
				ratioList.add(ratio);
				posStrList.add(posStr);
				//System.out.println("posStr is in if: "+posStr);
				posRatioList.add(ratioList);
				posKeyValueList.add(posStrList);
			}
			//when posKeyList has added the key as posStrArray[0]
			else{
				int index = posKeyList.indexOf(posStrArray[0]);
				List<Double> ratioList = posRatioList.get(index);
				List<String> posStrList = posKeyValueList.get(index);
				double ratio = count/posKeyHM.get(posStrArray[0]);
				ratioList.add(ratio);
				if(!posStrList.contains(posStr)){
					//System.out.println("posStr is in else: "+posStr);
					posStrList.add(posStr);
				}
				posKeyValueList.set(index,posStrList);
				posRatioList.set(index,ratioList);
			}
		}
	}
	
	/**
	 * it reminds of what I have done here. opKeyHm stores opKey in which posP and opP is included while posKeyHm stores
	 * posKey in which posP and op is inlcuded. Then, combine them together, if combined this way: opP, pos, op that is OPModel
	 * if combined with: posP, op, pos, that is PosModel
	 */
	public void printOutput(){
		//System.out.println("opRatioList size : "+opRatioList.size());
		if(smoothOP){
			for(int i=0;i<opRatioList.size();i++){
				List<Double> ratioList = opRatioList.get(i);
				List<String> opStrList = opKeyValueList.get(i);
				//System.out.println("ratioList size : "+ratioList.size()+" "+opStrList.size());
				for(int j=0;j<ratioList.size();j++){
					pwOpOutput.println("Op "+opStrList.get(j)+" = "+ratioList.get(j));
				}
			}
			//pwOpOutput.println("Op copy JJR : old");
			pwOpOutput.println("Op copy NNP : old");
			pwOpOutput.close();
		}
		
		//System.out.println("posRatioList size : "+posRatioList.size());
		for(int i=0;i<posRatioList.size();i++){
			List<Double> ratioList = posRatioList.get(i);
			List<String> posStrList = posKeyValueList.get(i);
			//System.out.println("ratioList size : "+ratioList.size()+" "+posStrList.size());
			for(int j=0;j<ratioList.size();j++){
				pwPosOutput.println("Pos "+posStrList.get(j)+" = "+ratioList.get(j));
			}
		}
		
		pwPosOutput.close();
	}
	
//	public void printOutput(){
//		Iterator<String> iterOpHM = opHM.keySet().iterator();
//		while(iterOpHM.hasNext()){
//			String opStr= iterOpHM.next();
//			int count = opHM.get(opStr);
//			for(int i=0;i<count;i++){
//				pwOpOutput.println("Op "+opStr);
//			}
//		}
//		pwOpOutput.close();
//		Iterator<String> iterPosHM = posHM.keySet().iterator();
//		while(iterPosHM.hasNext()){
//			String posStr= iterPosHM.next();
//			int count = posHM.get(posStr);
//			for(int i=0;i<count;i++){
//				pwPosOutput.println("Pos "+posStr);
//			}
//		}
//		pwPosOutput.close();
//	}

	public void buildModelNoSmoothing(File fileTag,File fileTag2,File fileOut) throws ParserConfigurationException, SAXException, IOException{
		TokenPos tokenPos = new TokenPos();
		if (fileTag.isDirectory()) {
			File[] fileArray = fileTag.listFiles();
			for(int i=0;i<fileArray.length;i++){
				List<String> tokenList=new ArrayList<String>();
				List<String> posList=new ArrayList<String>();
				Pair tokenPosPair=tokenPos.tokenPosPair(fileArray[i]);
				List<String> opList=new ArrayList<String>();
				StringBuffer textBuffer=new StringBuffer();
				File[] fileArray2=fileTag2.listFiles();
				tokenList=(List<String>)tokenPosPair.getFirst();
				posList=(List<String>)tokenPosPair.getSecond();

				for(int j=0;j<fileArray2.length;j++){
					if(fileArray2[j].getName().endsWith(".sgm")){

						if(fileArray[i].getName().contains(fileArray2[j].getName())){
							System.out.println(fileArray2[j]);
							ProcessACEDevJdom processACESgmJdom = new ProcessACEDevJdom();
							StringBuffer sbDev=processACESgmJdom.readSgmFile(fileArray2[j]);					
							textBuffer=processACESgmJdom.createInputFiles(sbDev.toString());
							//List<String> tokenList=processACESgmJdom.tokenList;
							//System.out.println("tokenList.get(0): "+tokenList.get(0));			
						}
					}
				}

				for(int j=0;j<fileArray2.length;j++){
					if(fileArray2[j].getName().endsWith(".xml")){
						String fileName1 =fileArray[i].getName();
						String fileName2 = fileArray2[j].getName();
						//System.out.println(fileName1.substring(0, fileName1.length()-11)+" "+fileName2.substring(0,fileName2.length()-16));
						if(fileName1.substring(0, fileName1.length()-11).equals(fileName2.substring(0,fileName2.length()-16))){
							System.out.println(fileArray2[j]);
							ProcessACEDevJdom processACEXmlJdom = new ProcessACEDevJdom(fileArray2[j]);
							processACEXmlJdom.extractEntity();
							opList=processACEXmlJdom.listMention(textBuffer, tokenList);
						}
					}
				}

				String word, wordP, pos,posP,op, opP;
				String strPosOp[];
				String strPosOpP[];
				op=opList.get(0);
				word = tokenList.get(0);
				pos=posList.get(0);
				strPosOp=helper.convertPosAndOp(pos, word,op);
				pos=strPosOp[0];
				op=strPosOp[1];
				pwOpOutput.println("Op - - : "+op);
				if(smooth==false){
					pwPosOutput.println("Pos - "+op+ " : "+pos);
				}
				System.out.println(tokenList.size()+" "+posList.size()+" "+opList.size());

				for(int j=1;j<tokenList.size();j++){
					word=tokenList.get(j);
					pos=posList.get(j);
					op=opList.get(j);
					strPosOp=helper.convertPosAndOp(pos, word,op);
					pos=strPosOp[0];
					op=strPosOp[1];
					posP=posList.get(j-1);
					wordP = tokenList.get(j-1);
					opP=opList.get(j-1);
					strPosOpP=helper.convertPosAndOp(posP, wordP,opP);
					posP=strPosOpP[0];
					opP=strPosOpP[1];
					pwOpOutput.println("Op "+opP+" "+posP+" : "+op);
					if(smooth==false){
						pwPosOutput.println("Pos - "+op+ " : "+pos);
					}
					if(op.equals("old")){
						System.out.println("j: "+j+" op: "+op+" pos: "+pos+" token: "+word);
					}
				}
				opP = opList.get(opList.size()-1);
				wordP=tokenList.get(tokenList.size()-1);
				posP=posList.get(posList.size()-1);
				strPosOpP=helper.convertPosAndOp(posP, wordP,opP);
				posP=strPosOpP[0];
				opP=strPosOpP[1];
				pwOpOutput.println("Op "+opList.get(opList.size()-1)+" "+posP+" : -");
				if(smooth==false){
					pwPosOutput.println("Pos - "+op+ " : "+pos);
				}
			}
			//we should not use the first line, it may lead to some troubles in posmodel. JJR old no such conditions. 
			//pwOpOutput.println("Op copy JJR : old");
			pwOpOutput.println("Op copy NNP : old");
			pwOpOutput.close();
			if(smooth==false){
				pwPosOutput.close();
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		//note: if args[0] involves a few subdirs and there are no files, it will get something like
		// /Users/dingcheng/Documents/modelblocks/corefResolver/intermediateModelFiles/bnews_tagged (No such file or directory)
		//obviously, will return no file found exception
		File fileTag = new File(args[0]);
		File fileTag2 = new File(args[1]);
		File fileOut = new File(args[2]);
		File posModelOut = new File(args[3]);
		BuildOpPosmodel buildOpPosmodel = new BuildOpPosmodel(fileTag,fileTag2,fileOut,posModelOut);

	}
}
