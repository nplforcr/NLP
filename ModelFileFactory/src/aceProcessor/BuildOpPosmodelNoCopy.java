package aceProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import utils.OPEnum;
import utils.Pair;
import utils.TreebankPosTags;
import utils.helper;

/**
 * 
 * @author m048100 since now, I don't need copy in my model, I also don't need copy-related pos. Instead, I only consider pos which involves entities. 
 *
 */
public class BuildOpPosmodelNoCopy {


	HashMap<String,Double> opKeyHM;
	HashMap<String,Double> posKeyHM;
	HashMap<String,Double> opHM;
	HashMap<String,Double> posHM;
	HashMap<String,Integer> opCountHM;
	HashMap<String,Integer> posCountHM;
	PrintWriter pwOpOutput;
	PrintWriter pwPosOutput;
	boolean smooth = false;
	boolean noSmooth = true;
	boolean noCopy = true;
	List<List<Double>> opRatioList;
	List<List<Double>> posRatioList;
	List<List<String>> opKeyValueList;
	List<List<String>> posKeyValueList;
	List<String> existingPosList;
	boolean smoothOP = false;
	boolean debug = false;
	static String EOS = "eos"; 
	static String DASH = "-";
	static String SGMAFFIX = ".sgm";
	static String TAGAFFIX = ".tagged";
	static String XMLAFFIX = ".tmx.rdc.xml";

	/**
	 * 
	 * @param tagFileDir
	 * @param aceFileDir
	 * @param fileOut
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public BuildOpPosmodelNoCopy(File tagFileDir,File aceFileDir,File fileOut, File posModelOut) throws IOException, ParserConfigurationException, SAXException{

		opHM = new HashMap<String,Double>();
		posHM = new HashMap<String,Double>();
		existingPosList = new ArrayList<String>();

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

		if(noSmooth){
			if(smoothOP){
				pwOpOutput = new PrintWriter(new FileWriter(new File(posModelOut,aceFileDir.getName() + ".smopNocopy.model")));
				smoothOpModel();
			}else{
				pwOpOutput = new PrintWriter(new FileWriter(new File(fileOut,aceFileDir.getName() + ".opNocopy.model")));
				//this.buildModelNoSmoothing(tagFileDir, aceFileDir, fileOut);
			}

			pwPosOutput = new PrintWriter(new FileWriter(new File(posModelOut,aceFileDir.getName() + ".smposNocopy.model")));
			opKeyHM = new HashMap<String,Double>();
			posKeyHM = new HashMap<String,Double>();
			fillInstanceHM(tagFileDir,aceFileDir);
			//I decide not smooth here. Instead, I will do smooth with other general procedures,
			//the reason is that there are many data from different sources. I will pool them together. so, it is better 
			//to do smooth after that.
			smoothPosModel();
			this.composeModel();

			//I don't want to bother to change the code too much. So, I simply call this by compose opmodel (opmodel doesn't need smoothing)
			//this.buildModelNoSmoothing(tagFileDir, aceFileDir, fileOut);
			//printOutput();
		}else{
			pwOpOutput = new PrintWriter(new FileWriter(new File(fileOut,aceFileDir.getName() + "nsm.opmodel"), true));
			pwPosOutput = new PrintWriter(new FileWriter(new File(fileOut,aceFileDir.getName() + "nsm.posmodel"), true));
			//this.buildModelNoSmoothing(tagFileDir, aceFileDir, fileOut);
		}
	}

	/**
	 * 
	 * @param tagFileDir the directory where tagged files are located 
	 * @param aceFileDir the director where the original files are located
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */

	public void fillInstanceHM(File tagFileDir,File aceFileDir) throws ParserConfigurationException, SAXException, IOException{
		//I don't remember that I have written a class to extract token and pos and form a pair list then
		TokenPos tokenPos = new TokenPos();
		if (tagFileDir.isDirectory()) {
			File[] fileArray = tagFileDir.listFiles();
			for(int i=0;i<fileArray.length;i++){
				List<String> tokenList=new ArrayList<String>();
				List<String> posList=new ArrayList<String>();
				//it seems that tagFileDir is the dir for tagged files
				File ithTagFile = fileArray[i];
				Pair tokenPosPair=tokenPos.tokenPosPair(ithTagFile);
				List<String> opList=new ArrayList<String>();
				StringBuffer textBuffer=new StringBuffer();
				File[] fileArray2=aceFileDir.listFiles();
				tokenList=(List<String>)tokenPosPair.getFirst();
				posList=(List<String>)tokenPosPair.getSecond();
				//no wonder, existingPosList is from posList. So, it is natural to include everything about pos.
				//existingPosList = posList;
				//we should not bother to loop this way. Simply,
				File ithSgmFile = new File(aceFileDir,ithTagFile.getName().replace(TAGAFFIX, ""));
				File ithXmlFile = new File(aceFileDir,ithTagFile.getName().replace(TAGAFFIX, XMLAFFIX));
				ProcessACEDevJdom processACESgmJdom = new ProcessACEDevJdom();
				StringBuffer sbDev=processACESgmJdom.readSgmFile(ithSgmFile);					
				textBuffer=processACESgmJdom.createInputFiles(sbDev.toString());
				ProcessACEDevJdom processACEXmlJdom = new ProcessACEDevJdom(ithXmlFile);;
				//the following function create many global HM file. It is used by function listMention, kind of hidden here.
				processACEXmlJdom.extractEntity();
				opList=processACEXmlJdom.listMention(textBuffer, tokenList);

				String word="", wordP="", pos="",posP="",op = "", opP="";
				//for model without copy, we should not get(0), instead, we should get the the first NEW. Not Old either since
				//without NEW, without old.
				int preInd = -1;
				for(int j=0;j<opList.size();j++){
					op = opList.get(j);
					if(!op.equals(OPEnum.copy.toString())){
						word = tokenList.get(j);
						pos = posList.get(j);
						if(!existingPosList.contains(pos)){
							System.out.println(pos);
							existingPosList.add(pos);
						}
						//the following method convertPosAndOp is only used for the moment when we only want to do third person pronoun resolutions.
						//strOp is composed of opP posP and op
						wordP = "-";
						opP = "-";
						posP = "-";
						//function update updates global data structure such as opKeyHM where opKey refers to [opP, posP], posKeyHM where
						//posKey refers to [posP , op ], opHM refers to [opP, posP : op], posHM refers to [posP,op : pos]
						update(op,word,pos,opP,wordP,posP);
						//String strPos = op+" - "+pos;
						//op can be copy or NEW. So, the condition should be here.
						//I know why, I missed the colon, so the condition is lost.
						//now, I don't want to the value of op is copy. So, I need to add constraints here. July 12, 2011

						if(debug){
							System.out.println("fillInstanceHM three list size should be equal: "+tokenList.size()+" "+posList.size()+" "+opList.size());
						}
						//
						//make sense here. loop through opList to find the first op which is not copy. 
						//Note:
						//opList here is list of document. Namely, it has the op information for each token in the article. 
						//after we find the first op which is not copy, we break the loop.
						preInd = j;
						break;
					}
				}

				//now, start from the index where op is not copy, usually should be new since old is not starting yet. 
				for(int j=preInd+1;j<tokenList.size();j++){
					op=opList.get(j);
					if(!op.equals(OPEnum.copy.toString())){
						word=tokenList.get(j);
						pos=posList.get(j);
						posP=posList.get(preInd);
						wordP = tokenList.get(preInd);
						opP=opList.get(preInd);
						//update updates global data structure such as opKeyHM where opKey refers to [opP, posP], posKeyHM where
						//posKey refers to [posP , op ], opHM refers to [opP, posP : op], posHM refers to [posP,op : pos]
						update(op,word,pos,opP,wordP,posP);
						//keep update preInd to the current index.
						preInd = j;
					}
				}
				//the following is the final, we need to have a dummy one for eos. But the problem should be fixed.
				//I know why eos can be OLD or NEW. word here should be eos rather than "-".
				opP = opList.get(preInd);
				wordP=tokenList.get(preInd);
				posP=posList.get(preInd);
				op = "-"; 
				word="eos";
				pos ="-";
				//update updates global data structure such as opKeyHM where opKey refers to [opP, posP], posKeyHM where
				//posKey refers to [posP , op ], opHM refers to [opP, posP : op], posHM refers to [posP,op : pos]
				update(op,word,pos,opP,wordP,posP);
			}
		}
	}

	/**
	 * update updates global data structure such as opKeyHM where opKey refers to [opP, posP], posKeyHM where
	 * posKey refers to [posP , op ], opHM refers to [opP, posP : op], posHM refers to [posP,op : pos]
	 * @param op
	 * @param word
	 * @param pos
	 * @param opP
	 * @param wordP
	 * @param posP
	 */
	public void update(String op,String word,String pos,String opP,String wordP,String posP){
		String strPosOp[]=helper.convertPosAndOp(pos, word,op);
		String strPosOpP[];
		//System.out.println(pos+" ================ "+op+" "+strPosOp);
		pos=strPosOp[0];
		op=strPosOp[1];
		strPosOpP=helper.convertPosAndOp(posP, wordP,opP);
		posP=strPosOpP[0];
		opP=strPosOpP[1];

		String strOpKey = opP+" "+posP;
		if(!opKeyHM.containsKey(strOpKey)){
			opKeyHM.put(strOpKey, new Double(1));
		}else{
			double count = opKeyHM.get(strOpKey)+1;
			opKeyHM.put(strOpKey,count);
		}
		String strPosKey = posP+" "+op;
		if(debug){
			System.out.println("strOpKey: "+strOpKey +" strPosKey "+strPosKey);
		}

		if(!posKeyHM.containsKey(strPosKey)){
			posKeyHM.put(strPosKey, new Double(1));
		}else{
			double count = posKeyHM.get(strPosKey)+1;
			posKeyHM.put(strPosKey, count);
		}

		String strOp = opP+" "+posP+" : "+op;
		if(!opHM.containsKey(strOp)){
			opHM.put(strOp, new Double(1));
		}else{
			double count = opHM.get(strOp)+1;
			opHM.put(strOp,count);
		}
		String strPos = posP+" "+op+" : "+pos;
		if(!posHM.containsKey(strPos)){
			posHM.put(strPos, new Double(1));
		}else{
			double count = posHM.get(strPos)+1;
			posHM.put(strPos, count);
		}
	}

	public void smoothOpModel(){
		if(debug){
			System.out.println("opHM size before smoothing: "+opHM.size());
		}

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
		if(debug){
			System.out.println("opHM size after smoothing: "+opHM.size());
		}

	}

	public void smoothPosModel(){
		if(debug){
			System.out.println("posHM size before smoothing: "+posHM.size());
		}


		for(int i=0;i<existingPosList.size();i++){
			String postag = existingPosList.get(i);
			for(OPEnum opEnum : OPEnum.values()){
				String op = opEnum.toString();
				double addSum = 0.0;
				String strPosKey = postag+" "+ op;
				for(int j=0;j<existingPosList.size();j++){
					String postag2 = existingPosList.get(j);
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

		if(debug){
			System.out.println("posHM size after smoothing: "+posHM.size());
		}



		//		Iterator<String> iterator = posHM.keySet().iterator();
		//		while(iterator.hasNext()){
		//			String next = iterator.next();
		//			double value = posHM.get(next);
		//			System.out.println(" strpos: "+next+" value: "+value);
		//		}

	}

	public void smoothPosModel2(){
		if(debug){
			System.out.println("posHM size before smoothing: "+posHM.size());
		}


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
		if(debug){
			System.out.println("posHM size after smoothing: "+posHM.size());
		}




		//		Iterator<String> iterator = posHM.keySet().iterator();
		//		while(iterator.hasNext()){
		//			String next = iterator.next();
		//			double value = posHM.get(next);
		//			System.out.println(" strpos: "+next+" value: "+value);
		//		}

	}

	public void composeModel(){
		if(smooth){
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

			//System.out.println("posRatioList size : "+posRatioList.size());
			for(int i=0;i<posRatioList.size();i++){
				List<Double> ratioList = posRatioList.get(i);
				List<String> posStrList = posKeyValueList.get(i);
				//System.out.println("ratioList size : "+ratioList.size()+" "+posStrList.size());
				for(int j=0;j<ratioList.size();j++){
					pwPosOutput.println("Pos "+posStrList.get(j)+" = "+ratioList.get(j));
				}
			}
			//pwOpOutput.println("Op copy JJR : old");
			pwOpOutput.close();
		}else{
			Iterator<String> iterOpHM = opHM.keySet().iterator();
			while(iterOpHM.hasNext()){
				String opStr= iterOpHM.next();
				double count = opHM.get(opStr);
				for(int i=0;i<count;i++){
					pwOpOutput.println("Op "+opStr);
				}
			}

			Iterator<String> iterPosHM = posHM.keySet().iterator();
			while(iterPosHM.hasNext()){
				String posStr= iterPosHM.next();
				double count = posHM.get(posStr);
				for(int i=0;i<count;i++){
					pwPosOutput.println("Pos "+posStr);
				}
			}
		}

		pwOpOutput.close();
		pwPosOutput.close();
	}


	/**
	 * I don't use the following ones now. 
	 */
	public void printCountOutput(){
		//the idea here is to remember the count of each opStr or posStr and print them count times.
		Iterator<String> iterOpHM = opHM.keySet().iterator();
		while(iterOpHM.hasNext()){
			String opStr= iterOpHM.next();
			int count = opCountHM.get(opStr);
			for(int i=0;i<count;i++){
				pwOpOutput.println("Op "+opStr);
			}
		}
		pwOpOutput.close();
		Iterator<String> iterPosHM = posHM.keySet().iterator();
		while(iterPosHM.hasNext()){
			String posStr= iterPosHM.next();
			int count = posCountHM.get(posStr);
			for(int i=0;i<count;i++){
				pwPosOutput.println("Pos "+posStr);
			}
		}
		pwPosOutput.close();
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
		File tagFileDir = new File(args[0]);
		File aceFileDir = new File(args[1]);
		File fileOut = new File(args[2]);
		File posModelOut = new File(args[3]);
		BuildOpPosmodelNoCopy buildOpPosmodelNoCopy = new BuildOpPosmodelNoCopy(tagFileDir,aceFileDir,fileOut,posModelOut);
		buildOpPosmodelNoCopy.fillInstanceHM(tagFileDir, aceFileDir);
		buildOpPosmodelNoCopy.printOutput();

	}
}
