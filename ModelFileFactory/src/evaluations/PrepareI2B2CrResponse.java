/*package evaluations;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import utils.CorefClusters;
import utils.Pair;
import utils.Triple;
import edu.mayo.bmi.I2B2.modelfiles.BuildConceptsPool;
import edu.mayo.bmi.I2B2.utils.CONST;

public class PrepareI2B2CrResponse extends PrepareCrResponse {
	//sample parameters:
	///Users/m048100/Documents/I2B2_ResultSet/track1/data/test/i2b2_Pittsburgh_Test/PROGRESS/proncr_aug31_results /Users/m048100/Documents/I2B2_ResultSet/track1/data/test/i2b2_Pittsburgh_Test/PROGRESS/
	///Users/m048100/Documents/I2B2_ResultSet/track1/data/train/Pitt_Train_Release/Progress/proncr_cata_sept5_results /Users/m048100/Documents/I2B2_ResultSet/track1/data/train/Pitt_Train_Release/Progress/
	///Users/m048100/Documents/I2B2_ResultSet/track1/data/test/i2b2_Pittsburgh_Test/PROGRESS/proncr_cata_sept8_results /Users/m048100/Documents/I2B2_ResultSet/track1/data/test/i2b2_Pittsburgh_Test/PROGRESS/
	//notice, the code works only all input are exactly matching. Otherwise, it will fail. It seems that the following
	//ones work.
	///Users/m048100/Documents/I2B2_ResultSet/track1/data/train/I2B2_Beth_Train/concepts/clinical-772.txt.con
	///Users/m048100/Documents/I2B2_ResultSet/track1/data/train/I2B2_Beth_Train/proncr_aug2nd_results/clinical-772.txt
	File conFile;
	File responseFile;
	boolean debug=true;
	//BuildConceptsPool buildConceptsPool;
	List<CorefClusters> responseDataList;
	private SortedMap<Triple,Triple> tripToken2TriplePhraseHm;
	private SortedMap<Triple,Triple> tripPhrase2TripleTokendHm; //used to record the index of concept for retrieval, but not useful for now since the index cannot reflect the order
	private SortedMap<Triple,String> tripToken2conHm;
	private SortedMap<Triple,Integer> tripToken2conIndHm; //used to record the index of concept for retrieval, but not useful for now since the index cannot reflect the order
	private List<Triple> tripToken2conList;
	private SortedMap<Integer,Triple> index2tripConHm; //this hashmap is empty for the moment. maybe, I will make use of it in future.
	private SortedMap<Pair<Integer,String>,Triple> conPair2conTripleHm;
	private SortedMap<Integer,SortedMap<Triple,Integer>> resolvedChainMap;
	private HashMap<SortedMap<Triple,Integer>,String> typeChainMap;
	
	public PrepareI2B2CrResponse(File conFile,File respFile){
		this.conFile = conFile;
		this.responseFile = respFile;
		typeChainMap = new HashMap<SortedMap<Triple,Integer>,String>();
		menList = new ArrayList<Integer>();
		count2Coarray = new HashMap<Integer,ArrayList<Integer>>();
		count2Comention = new HashMap<Integer,ArrayList<String>>();
		listOfCoarray = new ArrayList<ArrayList<Integer>>();
		listOfComention = new ArrayList<ArrayList<String>>();
		resolvedChainMap = new TreeMap<Integer,SortedMap<Triple,Integer>>();
	}
	
	public void convertRespToI2B2Chains() throws IOException{
		buildConceptsPool = new BuildConceptsPool(conFile);
		buildConceptsPool.extractData(conFile);
		buildConceptsPool.buildConPair2conTriple();
		conPair2conTripleHm = buildConceptsPool.getConPair2conTripleHm();
		tripToken2TriplePhraseHm = buildConceptsPool.getTripToken2TriplePhraseHm();
		tripToken2conHm = buildConceptsPool.getTripToken2conHm();
		fillContainers(this.responseFile);
		int chainCount = 0;
		int debCount = 0;
		for(int i=0;i<this.listOfCoarray.size();i++){
			final ArrayList<Integer> coArray = listOfCoarray.get(i);
			ArrayList<String> coMention = listOfComention.get(i);
			String ithConType = "";
			if(coArray.size()>1){
				SortedMap<String,Integer> typeCountHm = new TreeMap<String,Integer>();
				SortedMap<Triple,Integer> ithChainMap = new TreeMap<Triple,Integer>();
				for(int j=0;j<coArray.size()-1;j++){ 
					//System.out.println("IDENT "+coArray.get(j)+" "+coMention.get(j)+" "+coArray.get(j+1)+" "+coMention.get(j+1));
					if(j==coArray.size()-2){
						//System.out.print(j+" th: "+coArray.get(j)+" mention: "+coMention.get(j)+" "+(j+1)+" th: "+coArray.get(j+1)+" mention: "+coMention.get(j+1));
						Pair<Integer,String> jthIndConPair = new Pair<Integer,String>(coArray.get(j),coMention.get(j));
						Triple jthTokenTriple = conPair2conTripleHm.get(jthIndConPair);
						System.out.println(jthTokenTriple+" "+jthIndConPair);
						String jthConType = tripToken2conHm.get(jthTokenTriple);
						if(typeCountHm.containsKey(jthConType)){
							int jthCount = typeCountHm.get(jthConType)+1;
							typeCountHm.put(jthConType, jthCount);
						}else{
							typeCountHm.put(jthConType, 1);
						}
						Triple jthPhraseTriple  = tripToken2TriplePhraseHm.get(jthTokenTriple);
						if(debug){
							System.out.print("from prepareI2B2CrResponse: "+j+" th: "+coArray.get(j)+" mention: "+coMention.get(j)+" jthPhraseTriple "+jthPhraseTriple);
						}
						
						ithChainMap.put(jthPhraseTriple, 1);
						Pair<Integer,String> j1IndConPair = new Pair<Integer,String>(coArray.get(j+1),coMention.get(j+1));
						Triple j1TokenTriple = conPair2conTripleHm.get(j1IndConPair);
						String j1ConType = tripToken2conHm.get(j1TokenTriple);
						if(typeCountHm.containsKey(j1ConType)){
							int j1Count = typeCountHm.get(j1ConType)+1;
							typeCountHm.put(j1ConType, j1Count);
						}else{
							typeCountHm.put(j1ConType, 1);
						}
						Triple j1PhraseTriple  = tripToken2TriplePhraseHm.get(j1TokenTriple);
						if(debug){
							System.out.print("from prepareI2B2CrResponse: "+(j+1)+" j1 th: "+coArray.get(j+1)+"j1 mention: "+coMention.get(j+1)+" j1PhraseTriple "+j1PhraseTriple);
						}
						
						ithChainMap.put(j1PhraseTriple, 1);
					}else{
						debCount++;
						if(debug){
							System.out.println(debCount);
						}
						Pair<Integer,String> indConPair = new Pair<Integer,String>(coArray.get(j),coMention.get(j));
						Triple tokenTriple = conPair2conTripleHm.get(indConPair);
						if(debug){
							System.out.println(indConPair);
						}
						System.out.println(tokenTriple);
						String conType = tripToken2conHm.get(tokenTriple);
						
						if(typeCountHm.containsKey(conType)){
							int count = typeCountHm.get(conType)+1;
							typeCountHm.put(conType, count);
						}else{
							typeCountHm.put(conType, 1);
						}
						Triple phraseTriple  = tripToken2TriplePhraseHm.get(tokenTriple);
						ithChainMap.put(phraseTriple, 1);
						if(debug){
							System.out.print("from prepareI2B2CrResponse: "+j+" th: "+coArray.get(j)+" mention: "+coMention.get(j)+" phraseTriple "+phraseTriple);
						}
						
					}	
				}
				
				Iterator<String> typeIter = typeCountHm.keySet().iterator();
				int max = 0;
				while(typeIter.hasNext()){
					String type = typeIter.next();
					int count = typeCountHm.get(type);
					if(count>max){
						max=count;
						ithConType = type;
					}
				}
				if(debug){
					System.out.println("ithConType: "+ithConType);
				}
				resolvedChainMap.put(chainCount, ithChainMap);
				typeChainMap.put(ithChainMap, ithConType);
				chainCount++;
				if(debug){
					System.out.println();
				}
			}
		}
	}
	
	public void printResolvedChainMap(PrintWriter pwI2B2Out){
		Iterator<Integer> iterResolvedChainMap = resolvedChainMap.keySet().iterator();
		while(iterResolvedChainMap.hasNext()){
			int index = iterResolvedChainMap.next();
			SortedMap<Triple,Integer> chainMap = resolvedChainMap.get(index);
			Iterator<Triple> iterMap = chainMap.keySet().iterator();
			while(iterMap.hasNext()){
				Triple triple = iterMap.next();
				System.out.print(((Pair<Integer,Integer>)triple.getFirst()).getFirst()+":"+((Pair<Integer,Integer>)triple.getFirst()).getSecond()+"|"+((Pair<Integer,Integer>)triple.getSecond()).getFirst()+":"+((Pair<Integer,Integer>)triple.getSecond()).getSecond()+"|"+triple.getThird()+"|");
				pwI2B2Out.print("c=\""+triple.getThird()+"\" "+((Pair<Integer,Integer>)triple.getFirst()).getFirst()+":"+((Pair<Integer,Integer>)triple.getFirst()).getSecond()+" "+((Pair<Integer,Integer>)triple.getSecond()).getFirst()+":"+((Pair<Integer,Integer>)triple.getSecond()).getSecond()+"||");
			}
			pwI2B2Out.print("t=\"coref "+typeChainMap.get(chainMap)+"\"");
			pwI2B2Out.println();
			System.out.println();
		}
		pwI2B2Out.flush();
		pwI2B2Out.close();
	}
	*//**
	 * @param args
	 * @throws IOException 
	 *//*
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File conFile  = new File(args[0]);
		File respFile = new File(args[1]);
		//if conFile is one file and respFile should be one file too.
		if(conFile.isFile()){
			File oneRespFile = new File(respFile,conFile.getName().substring(0,conFile.getName().indexOf(CONST.CON)));
			PrepareI2B2CrResponse preI2b2CrResponse = new PrepareI2B2CrResponse(conFile,oneRespFile);
			preI2b2CrResponse.convertRespToI2B2Chains();
			File i2b2FormDir = new File(respFile.getParentFile().getParentFile(),"i2b2FormOut");
			PrintWriter pwI2B2Format = new PrintWriter(i2b2FormDir,respFile.getName());
			preI2b2CrResponse.printResolvedChainMap(pwI2B2Format);
		}else{
			File[] listOfConFile = conFile.listFiles();
			for(int i=0;i<listOfConFile.length;i++){
				File ithConFile = listOfConFile[i];
				String ithConFileName = ithConFile.getName();
				String ithRespFileName = ithConFileName.substring(0,ithConFileName.indexOf(CONST.CON)); 
				File ithRespFile = new File(respFile,ithRespFileName);
				PrepareI2B2CrResponse preI2b2CrResponse = new PrepareI2B2CrResponse(ithConFile,ithRespFile);
				File i2b2FormDir = new File(respFile.getParentFile(),"i2b2FormBaseline");
				if(!i2b2FormDir.exists()){
					i2b2FormDir.mkdir();
				}
				File ithI2B2OutFile = new File(i2b2FormDir,ithRespFile.getName());
				PrintWriter pwI2B2Format = new PrintWriter(ithI2B2OutFile);
				preI2b2CrResponse.convertRespToI2B2Chains();
				preI2b2CrResponse.printResolvedChainMap(pwI2B2Format);
			}
		}
	}
}
*/