package evaluations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

//import edu.mayo.bmi.I2B2.utils.CONST;

import utils.CorefClusters;
import utils.Pair;

public class EvaCoref {
	//After a few days work, I found a quite simple way to output the correct response files for evaluation
	//it is a great step. Depending on my goal, I can select to sort the output or not. 
	//then I can use different evaluations to evaluate my coreference results.
	PrintWriter pwResults;
	List<CorefClusters> keyDataList;
	List<CorefClusters> responseDataList;
	//PrepareACEKey preACEKey;
	PrepareKey prepareKey;
	//ConvertCrOutput conCrOutput;
	PrepareCrResponse conCrOutput;
	List<String> bsTokenList;
	static String CORPUS = "ACE";
	//String[] ignoreArray = {"PRI19981103.2000.0382.sgm","MNB19981027.2100.2610.sgm", "VOA19981012.1700.0187.sgm","VOA19981203.1600.3026.sgm","VOA19981224.0500.0051.sgm"};
	//PRI19981103.2000.2245.sgm, no third pronoun.
	String[] ignoreArray = {"ea980123.1830.0089.sgm","PRI19980515.2000.1255.sgm","PRI19980112.2000.2076.sgm","PRI19981103.2000.2245.sgm","PRI19980303.2000.2550.sgm","ABC19981129.1830.1138.sgm","PRI19981126.2000.1978.sgm","PRI19981210.2000.0340.sgm"};
	static String sgmDir = "response";
	static String rdcxmlDir = "key";
	/**
	 * 
	 * @param keyDir
	 * @param responseDir
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public EvaCoref(String responseDir,String keyDir) throws IOException, ParserConfigurationException, SAXException{
		//in I2B2, the keyFile should be directory like 
		///Users/m048100/Documents/I2B2_ResultSet/track1/data/train/I2B2_Partners_Train/ 
		//under which, there are more directories
		//while in ACE, /Users/m048100/Documents/corpora/ace_phase2/data/ace2_devtest/bnews
		//under which are files.
		 
		File responseFile = new File(responseDir);
		File keyFile = new File(keyDir);
		pwResults = new PrintWriter(new FileWriter(new File(responseFile.getParent(),"sept5_totalResults.txt")));
		//File[] keyFiles=keyFile.listFiles();
		File[] responseFiles=responseFile.listFiles();
		List<String> ignoreList=Arrays.asList(ignoreArray);
		int totalPair  = 0;
		int totalCorre = 0;
		double finalAccuracy = 0.0;
		for(int i=0;i<responseFiles.length;i++){
			File ithResponseFile = responseFiles[i];
			//System.out.println(ignoreList.contains(ithResponseFile)+" "+ithResponseFile.getName()+" ignoreList: "+ignoreList.get(0));
			if(ignoreList.contains(ithResponseFile.getName())){
				System.out.println("the file include a single cataphora or include an annotation which cannot refer back to anything, I will handle it later");
				continue;
			}

			if(CORPUS.equals("ACE")){
				File ithKeyFile = new File(keyFile,ithResponseFile.getName()+".tmx.rdc.xml");
				bsTokenList = new ArrayList<String>();
				prepareKey = new PrepareACEKey();
				((PrepareACEKey) prepareKey).process(ithKeyFile);
				keyDataList = prepareKey.getKeyData(bsTokenList);
			}else if(CORPUS.equals("I2B2")){
				/*File conFileDir = new File(keyFile,CONST.CONCEPT);
				File chainFileDir = new File(keyFile,CONST.CHAINDIR);
				File ithConFile = new File(conFileDir,ithResponseFile.getName()+CONST.CON);
				File ithChainFile = new File(chainFileDir,ithResponseFile.getName()+CONST.CHAINS);
				List<File> fileList = new ArrayList<File>();
				fileList.add(ithConFile);
				fileList.add(ithChainFile);
				prepareKey = new PrepareI2B2Key(fileList);
				keyDataList=prepareKey.getKeyData(ithConFile, ithChainFile);
				pwResults.println("this is key: "+ithConFile.getName());
				for(int j=0;j<keyDataList.size();j++){
					CorefClusters jthKeyCluster = keyDataList.get(j);
					pwResults.println("IDENT "+jthKeyCluster.getCorefIndex()+" "+jthKeyCluster.getCorefWord()+" "+jthKeyCluster.getAnaIndex()+" "+jthKeyCluster.getAnaWord());
				}*/
			}
			
			//conCrOutput = new ConvertCrOutput();
			conCrOutput = new PrepareCrResponse();
			conCrOutput.fillContainers(ithResponseFile);
			responseDataList = conCrOutput.getSortedResPair();
			pwResults.println("this is response: "+ithResponseFile.getName());
			for(int j=0;j<keyDataList.size();j++){
				CorefClusters jthResponData = keyDataList.get(j);
				//pwResults.println("IDENT "+jthResponData.getCorefIndex()+" "+jthResponData.getCorefWord()+" "+jthResponData.getAnaIndex()+" "+jthResponData.getAnaWord());
				pwResults.println("IDENT "+jthResponData.getCorefIndex()+" "+jthResponData.getAnaIndex());
			}
			//responseDataList = conCrOutput.getResPair();
			//this.calculateAccuracy(keyDataList, responseDataList);
			Pair<Integer,Boolean> returnPair=this.countCorrect(keyDataList, responseDataList);
			int countCorrect = returnPair.getFirst();
			boolean success=returnPair.getSecond();
			if(success==true){
				totalPair += keyDataList.size();
				totalCorre+=countCorrect;
			}
		}
		finalAccuracy = (double)totalCorre/totalPair;
		System.out.println("totalPair: "+totalPair+" totalCorre: "+totalCorre+" finalAccuracy: "+finalAccuracy);
		pwResults.println("totalPair: "+totalPair+" totalCorre: "+totalCorre+" finalAccuracy: "+finalAccuracy);
		pwResults.flush();
		pwResults.close();
	}
	
	public EvaCoref(File responseFile,String keyFileName) throws IOException, ParserConfigurationException, SAXException{
		//conCrOutput = new ConvertCrOutput();
		conCrOutput = new PrepareCrResponse();
		//this is response is printed out in fillContainers
		conCrOutput.fillContainers(responseFile);
		responseDataList = conCrOutput.getResPair();
		
		if(CORPUS.equals("ACE")){
			bsTokenList = new ArrayList<String>();
			prepareKey = new PrepareACEKey();
			((PrepareACEKey) prepareKey).process(keyFileName);
			keyDataList = prepareKey.getKeyData(bsTokenList);
		}else if(CORPUS.equals("I2B2")){
			/*File keyFile = new File(keyFileName);
			File conFileDir = new File(keyFile,CONST.CONCEPT);
			File chainFileDir = new File(keyFile,CONST.CHAINDIR);
			File ithConFile = new File(conFileDir,responseFile.getName()+CONST.CON);
			File ithChainFile = new File(chainFileDir,responseFile.getName()+CONST.CHAINS);
			List<File> fileList = new ArrayList<File>();
			fileList.add(ithConFile);
			fileList.add(ithChainFile);
			prepareKey = new PrepareI2B2Key(fileList);
			keyDataList=prepareKey.getKeyData(ithConFile, ithChainFile);*/
		}
		
		this.calculateAccuracy(keyDataList, responseDataList);
	}

	/**
	 * 
	 * @param keyDataList
	 * @param responseDataList
	 * @return
	 */
	public Pair<Integer,Boolean> countCorrect(List<CorefClusters> keyDataList,List<CorefClusters> responseDataList){
		int totalPair = keyDataList.size();
		int correctPair = 0;
		boolean success = false;
		double finalAccuracy = 0;
		Pair<Integer,Boolean> returnPair=new Pair<Integer,Boolean>(correctPair,success);
		for(int i=0;i<keyDataList.size();i++){
			CorefClusters keyData = keyDataList.get(i);
			int jLoopSize = 0;
			if(responseDataList.size()>keyDataList.size()){
				jLoopSize = responseDataList.size();
			}else{
				jLoopSize = keyDataList.size();
			}
			for(int j=0;j<jLoopSize;j++){
				//System.out.println(keyData.toString()+" "+responseData.toString());
				if(responseDataList.size()==0){
					System.out.println("responseDataList.size == 0!");
					return returnPair;
				}
				/**
				else if(responseDataList.size()<keyDataList.size()){
					System.out.println("there is a bug in the input entity file and I will fix it later!");
					return 0;
				}*/
				if(responseDataList.size()>j){
					CorefClusters responseData = responseDataList.get(j);
					int keyAnaIndex  = keyData.getAnaIndex();
					String keyAnaToken  = keyData.getAnaWord();
					int resAnaIndex = responseData.getAnaIndex();
					String resAnaToken = responseData.getAnaWord();
					int keyCorefIndex = keyData.getCorefIndex();
					String keyCorefToken = keyData.getCorefWord();
					int resCorefIndex = responseData.getCorefIndex();
					String resCorefToken = responseData.getCorefWord();
//					pwResults.println("keyAnaIndex: "+keyAnaIndex+" keyToken: "+keyAnaToken + " resAnaIndex: "+ resAnaIndex
//							+resAnaToken+" keyCorefIndex: "+keyCorefIndex+" keyCorefToken: "+keyCorefToken+" resCorefIndex: "+
//							resCorefIndex+" resCorefToken: "+resCorefToken);
					if(keyData.getAnaIndex()==responseData.getAnaIndex() &&keyData.getAnaWord().equals(responseData.getAnaWord()) ){
						if(keyData.getCorefIndex()==responseData.getCorefIndex() && keyData.getCorefWord().equals(responseData.getCorefWord())){
							correctPair++;
						}

					}
				}
			}
		}
		if(totalPair!=0){
			finalAccuracy = (double)correctPair/totalPair;
		}
		System.out.println("totalPair: "+totalPair+" totalCorre: "+correctPair+" finalAccuracy: "+finalAccuracy);
		if(finalAccuracy==0){
			success=false;
		}else{
			success=true;
		}
		returnPair=new Pair<Integer,Boolean>(correctPair,success);
		pwResults.println("totalPair: "+totalPair+" totalCorre: "+correctPair+" finalAccuracy: "+finalAccuracy);
		return returnPair;
	}
	
	/**
	 * 
	 * @param keyDataList
	 * @param responseDataList
	 * @return
	 */
	public double calculateAccuracy(List<CorefClusters> keyDataList,List<CorefClusters> responseDataList){
		int totalPair = keyDataList.size();
		int correctPair = 0;
		double finalAccuracy = 0;
		for(int i=0;i<keyDataList.size();i++){
			CorefClusters keyData = keyDataList.get(i);	
			for(int j=0;j<responseDataList.size();j++){
				//System.out.println(keyData.toString()+" "+responseData.toString());
				if(responseDataList.size()==0){
					System.out.println("there is a bug in the parsing process and I will fix it later!");
					return 0;
				}
				/**
				else if(responseDataList.size()<keyDataList.size()){
					System.out.println("there is a bug in the input entity file and I will fix it later!");
					return 0;
				}**/
				CorefClusters responseData = responseDataList.get(j);
				if(keyData.getAnaIndex()==responseData.getAnaIndex() &&keyData.getAnaWord().equals(responseData.getAnaWord()) ){
					if(keyData.getCorefIndex()==responseData.getCorefIndex() && keyData.getCorefWord().equals(responseData.getCorefWord())){
						correctPair++;
					}
				}
			}
		}
		finalAccuracy = (double)correctPair/totalPair;
		System.out.println("totalPair: "+totalPair+" totalCorre: "+correctPair+" finalAccuracy: "+finalAccuracy);
		return finalAccuracy;
	}

	/**
	 * @param args
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		File responseFile = new File(sgmDir);
		if(responseFile.isDirectory()){
			EvaCoref evaCoref = new EvaCoref(sgmDir, rdcxmlDir);
		}else{
			EvaCoref evaCoref = new EvaCoref(responseFile, rdcxmlDir);
		}
		//evaCoref.calculateAccuracy(evaCoref.keyDataList, evaCoref.responseDataList);
	}
}
