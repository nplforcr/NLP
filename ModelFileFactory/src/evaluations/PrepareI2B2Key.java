/*package evaluations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

import edu.mayo.bmi.I2B2.modelfiles.BuildI2B2Model;
import edu.mayo.bmi.I2B2.modelfiles.ConceptOrderer;
import edu.mayo.bmi.I2B2.modelfiles.CorefdataOrganizer;
import edu.mayo.bmi.I2B2.modelfiles.IndexCreator;
import edu.mayo.bmi.I2B2.utils.CONST;

import annotation.Mention;
import annotation.NamedEntity;

import utils.CorefClusters;
import utils.FemalePronouns;
import utils.MalePronouns;
import utils.NeutralPronouns;
import utils.OPEnum;
import utils.Pair;
import utils.SingularPronouns;
import utils.Triple;

public class PrepareI2B2Key extends BuildI2B2Model implements PrepareKey{

	SortedMap<Pair,Integer> indCondHm;
	//the second map is used to store <keyPair, keyValueMap>	
	SortedMap<Pair,SortedMap<Pair,Integer>> indModelHm;

	static int MAXSIZE = 11;
	static boolean anaphor = true;

	boolean debug = false;
	File keyFileDir;


	*//**
	 * 
	 * @param fileList
	 * @throws IOException
	 *//*
	public PrepareI2B2Key(List<File> fileList) throws IOException {
		super(fileList);
	}


	*//**
	 * 
	 * @throws IOException
	 *//*
	public void comKey() throws IOException{
		File conFile = fileList.get(0);
		File chainFile = fileList.get(1);
		File docFile  = fileList.get(2);

		if(conFile.isDirectory()){
			keyFileDir = new File(rootDir,"keyFile");
			if(!keyFileDir.exists()){
				keyFileDir.mkdirs();
			}
			File[] listConFile = conFile.listFiles();
			for(int i=0;i<listConFile.length;i++){
				File ithConFile = listConFile[i];
				File ithChainFile = new File(chainFile,ithConFile.getName().replace(CONST.CON, CONST.CHAINS));
				File ithDocFile = new File(chainFile,ithConFile.getName().replace(CONST.CON, ""));
				File ithKeyFile = new File(keyFileDir,ithConFile.getName().replace(CONST.CON, CONST.KEYS));
				PrintWriter ithPwKey = new PrintWriter(new FileWriter(ithKeyFile));
				System.out.println(ithConFile);
				this.runOneFilePron(ithConFile, ithChainFile, ithDocFile, ithPwKey);
				ithPwKey.flush();
				ithPwKey.close();
			}
		}else{
			keyFileDir = new File(conFile.getParentFile().getParentFile(),"keyFile");
			if(!keyFileDir.exists()){
				keyFileDir.mkdirs();
			}
			File keyFile = new File(keyFileDir,conFile.getName().replace(CONST.CON, CONST.KEYS));
			PrintWriter pwKey = new PrintWriter(new FileWriter(keyFile));

			this.runOneFilePron(conFile, chainFile, docFile, pwKey);
			pwKey.flush();
			pwKey.close();
		}

		if(debug){
			Iterator<Pair> iterNeModelHm = indModelHm.keySet().iterator();
			int keyCount = 0;
			while(iterNeModelHm.hasNext()){
				Pair keyPair = iterNeModelHm.next();
				System.out.println("keyCount: "+keyCount+" new key now: "+keyPair);
				SortedMap<Pair,Integer> valueMap =  indModelHm.get(keyPair);
				Iterator<Pair> valueIter = valueMap.keySet().iterator();
				int keyValueCount = 0;
				while(valueIter.hasNext()){
					Pair keyValuePair = valueIter.next();
					int count = valueMap.get(keyValuePair);
					System.out.println(keyValueCount+" valuePair: "+keyValuePair+" "+count);
					keyValueCount++;
				}
				keyCount++;
			}
		}
	}

	@Override
	public List<CorefClusters> getKeyData(File conFile, File chainFile)
	throws IOException {
		System.out.println("this is key: "+conFile.getName());
		return runOneFilePron(conFile,chainFile,null);
	}

	public List<CorefClusters> runOneFilePron(File ithConFile,File ithChainFile,PrintWriter pwKey) throws IOException{
		List<CorefClusters> keyDataList = new ArrayList<CorefClusters>();
		ConceptOrderer conOrderer = new ConceptOrderer(ithConFile);
		conOrderer.extractData(ithConFile);
		CorefdataOrganizer corefDataOrganizer = new CorefdataOrganizer(ithChainFile);
		corefDataOrganizer.extractData(ithChainFile);
		SortedMap<Triple,String> tripToken2conHm = conOrderer.getTripToken2conHm();
		SortedMap<Triple,Integer> tripToken2indConHm = conOrderer.getTripToken2conIndHm();
		List<Triple> tripToken2conList = conOrderer.getTripToken2conList();
		SortedMap<Triple, Triple> cur2antHm = corefDataOrganizer.getCur2antHm();
		SortedMap<Triple,Triple> ant2curHm = corefDataOrganizer.getAnt2curHm();
		SortedMap<Triple,Integer> chainIndHm = corefDataOrganizer.getChainIndHm();
		List<LinkedList<Triple>> corefList = corefDataOrganizer.getCorefList();
		List<Integer> temNeIdList = new ArrayList<Integer>();
		//now we have tokens and their ne tags and also index, then, we can check what ne tags the
		//concept have and use them to construct unkword. Others, just print them out. 
		Iterator<Triple> iterTripleToken2conHm = tripToken2conHm.keySet().iterator();
		Triple preConTriple = null;
		int corefInd = 0,distance  = 0, countCoref = 0, countToken  = 0 ;
		while(iterTripleToken2conHm.hasNext()){
			Triple conTriple = iterTripleToken2conHm.next();
			String conType = tripToken2conHm.get(conTriple);
			int conInd = tripToken2conList.indexOf(conTriple);
			temNeIdList.add(conInd);
			//in this case, it indicates that though cur2antHm doesn't contain the token, it is still in concept set.
			//so count it as NEW
			//the following is to create model files when op is old, where, what kind of dependencies between current old and previous old
			if(cur2antHm.containsKey(conTriple)){
				countCoref++;
				if(debug){
					System.out.println("countCoref: "+countCoref);
				}

				String token = (String)conTriple.o3;
				//this is the index the current token is in the list of concepts.

				if(MalePronouns.malePronList2.contains(token) || FemalePronouns.femalePronList2.contains(token)
						|| NeutralPronouns.neuPronList2.contains(token)){
					//int conInd = tripToken2indConHm.get(conTriple);
					//this is the index the current chain is in the list of chains
					int chainInd = chainIndHm.get(conTriple);
					//the list where the tokenTriple is located
					List<Triple> chainList = corefList.get(chainInd);

					//the preInd should not be negative since cur2antHm starts from the second tokenTriple rather than the first one in the chain
					//preInd is different from preTokenInd, preInd is the location the preTokenTriple is in the chainList
					int preInd = chainList.indexOf(conTriple)-1;
					if(preInd!=-1){
						preConTriple = chainList.get(preInd);
						//preTokenInd is the location preTokenTriple is in the tripToken2conList
						corefInd = tripToken2conList.indexOf(preConTriple);
						String preConType = tripToken2conHm.get(preConTriple);
						String preConToken = (String) preConTriple.getThird();
						int loc = temNeIdList.indexOf(corefInd);
						distance = conInd - corefInd;

						if(loc>=0){
							if(pwKey!=null){
								pwKey.println("IDENT "+corefInd+" "+preConToken+" "+conInd+" "+token);
							}
							//since corefInd and conInd starts from 0 while the output of HHMMParser starts from 1. I may either
							//change output of HHMMParser or change of key index.
							System.out.println("IDENT "+(corefInd)+" "+preConToken+" "+(conInd)+" "+token);
							//System.out.println("IDENT "+corefInd+" "+absCorefInd+" "+preConToken+" "+conInd+" "+absConInd+" "+token);
							//System.out.println("IDENT "+" "+absCorefInd+" "+preConToken+" "+" "+absConInd+" "+token);
							CorefClusters keyData = new CorefClusters();
							keyData.setCorefIndex(corefInd);
							keyData.setCorefWord(preConToken);
							keyData.setAnaIndex(conInd);
							keyData.setAnaphor(token);
							keyDataList.add(keyData);
						}



						if(debug){
							System.out.println("conInd: "+conInd + " curTokenTriple: " + conTriple+ " cur Contype: "+conType+ 
									" corefInd: "+corefInd+" corefTriple: "+tripToken2conList.get(corefInd)+" preNeTriple: "+preConType+" distance: "+distance);
						}
						//preTokenTriple = new Triple(OPEnum.old, distance, ne);
						//corefInd = conInd;

					}
				}

			}

			if(temNeIdList.size()==MAXSIZE){
				temNeIdList.remove(0);
			}
			countToken++;
		}
		return keyDataList;
	}

	public List<CorefClusters> getKeyData(File ithConFile,File ithChainFile,File ithDocFile) throws IOException{
		System.out.println("this is key: "+ithConFile.getName());
		return runOneFilePron(ithConFile,ithChainFile,ithDocFile,null);
	}


	public List<CorefClusters> runOneFilePron(File ithConFile,File ithChainFile,File ithDocFile,PrintWriter pwKey) throws IOException{
		List<CorefClusters> keyDataList = new ArrayList<CorefClusters>();
		ConceptOrderer conOrderer = new ConceptOrderer(ithConFile);
		conOrderer.extractData(ithConFile);
		CorefdataOrganizer corefDataOrganizer = new CorefdataOrganizer(ithChainFile);
		corefDataOrganizer.extractData(ithChainFile);
		IndexCreator indexCreator = new IndexCreator(ithDocFile,CONST.tokenOnly);
		indexCreator.extractData(ithDocFile);
		SortedMap<Triple,Integer> tripToken2indexHm = indexCreator.getTripToken2indexHm();
		SortedMap<Integer,Triple> index2tripTokenHm = indexCreator.getIndex2tripTokenHm();
		SortedMap<Triple,String> tripToken2conHm = conOrderer.getTripToken2conHm();
		SortedMap<Triple,Integer> tripToken2indConHm = conOrderer.getTripToken2conIndHm();
		List<Triple> tripToken2conList = conOrderer.getTripToken2conList();
		SortedMap<Triple, Triple> cur2antHm = corefDataOrganizer.getCur2antHm();
		SortedMap<Triple,Triple> ant2curHm = corefDataOrganizer.getAnt2curHm();
		SortedMap<Triple,Integer> chainIndHm = corefDataOrganizer.getChainIndHm();
		List<LinkedList<Triple>> corefList = corefDataOrganizer.getCorefList();
		List<Integer> temNeIdList = new ArrayList<Integer>();
		//now we have tokens and their ne tags and also index, then, we can check what ne tags the
		//concept have and use them to construct unkword. Others, just print them out. 
		Iterator<Triple> iterTripleToken2conHm = tripToken2conHm.keySet().iterator();
		Triple preConTriple = null;
		int corefInd = 0,distance  = 0, countCoref = 0, countToken  = 0 ;
		int  absConInd =0, absCorefInd = 0;
		while(iterTripleToken2conHm.hasNext()){
			Triple conTriple = iterTripleToken2conHm.next();
			String conType = tripToken2conHm.get(conTriple);
			int conInd = tripToken2conList.indexOf(conTriple);
			temNeIdList.add(conInd);
			//in this case, it indicates that though cur2antHm doesn't contain the token, it is still in concept set.
			//so count it as NEW
			//the following is to create model files when op is old, where, what kind of dependencies between current old and previous old
			if(cur2antHm.containsKey(conTriple)){
				countCoref++;
				if(debug){
					System.out.println("countCoref: "+countCoref);
				}

				String token = ((String)conTriple.o3).toLowerCase();
				//this is the index the current token is in the list of concepts.

				if(MalePronouns.malePronList2.contains(token) || FemalePronouns.femalePronList2.contains(token)
						|| NeutralPronouns.neuPronList2.contains(token)){
					//int conInd = tripToken2indConHm.get(conTriple);
					//this is the index the current chain is in the list of chains
					int chainInd = chainIndHm.get(conTriple);
					absConInd = tripToken2indexHm.get(conTriple);
					//the list where the tokenTriple is located
					List<Triple> chainList = corefList.get(chainInd);
					//the preInd should not be negative since cur2antHm starts from the second tokenTriple rather than the first one in the chain
					//preInd is different from preTokenInd, preInd is the location the preTokenTriple is in the chainList
					int preInd = chainList.indexOf(conTriple)-1;

					if(preInd!=-1){
						preConTriple = chainList.get(preInd);
						absCorefInd = tripToken2indexHm.get(preConTriple);
						//preTokenInd is the location preTokenTriple is in the tripToken2conList
						corefInd = tripToken2conList.indexOf(preConTriple);
						String preConType = tripToken2conHm.get(preConTriple);
						String preConToken = (String) preConTriple.getThird();
						int loc = temNeIdList.indexOf(corefInd);
						distance = conInd - corefInd;

						if(loc>=0){
							if(pwKey!=null){
								pwKey.println("IDENT "+corefInd+" "+absCorefInd+" "+preConToken+" "+conInd+" "+absConInd+" "+token);
							}
							//System.out.println("IDENT "+corefInd+" "+absCorefInd+" "+preConToken+" "+conInd+" "+absConInd+" "+token);
							//System.out.println("IDENT "+corefInd+" "+absCorefInd+" "+preConToken+" "+conInd+" "+absConInd+" "+token);
							System.out.println("IDENT "+" "+absCorefInd+" "+preConToken+" "+" "+absConInd+" "+token);
							CorefClusters keyData = new CorefClusters();
							keyData.setCorefIndex(corefInd);
							keyData.setCorefWord(preConToken);
							keyData.setAnaIndex(conInd);
							keyData.setAnaphor(token);
							keyDataList.add(keyData);
						}
						if(debug){
							System.out.println("conInd: "+conInd + " curTokenTriple: " + conTriple+ " cur Contype: "+conType+ 
									" corefInd: "+corefInd+" corefTriple: "+tripToken2conList.get(corefInd)+" preNeTriple: "+preConType+" distance: "+distance);
						}

					}

					//preTokenTriple = new Triple(OPEnum.old, distance, ne);
					//corefInd = conInd;
				}

			}

			if(temNeIdList.size()==MAXSIZE){
				temNeIdList.remove(0);
			}
			countToken++;
		}
		return keyDataList;
	}

	*//**
	 * for the moment, I will not use the following one since I only focus on pronouns.
	 * @param ithConFile
	 * @param ithChainFile
	 * @param pwKey
	 * @throws IOException
	 *//*
	public void runOneFileGeneral(File ithConFile,File ithChainFile,PrintWriter pwKey) throws IOException{
		ConceptOrderer conOrderer = new ConceptOrderer(ithConFile);
		conOrderer.extractData(ithConFile);
		CorefdataOrganizer corefDataOrganizer = new CorefdataOrganizer(ithChainFile);
		corefDataOrganizer.extractData(ithChainFile);
		SortedMap<Triple,String> tripToken2conHm = conOrderer.getTripToken2conHm();
		SortedMap<Triple,Integer> tripToken2indConHm = conOrderer.getTripToken2conIndHm();
		List<Triple> tripToken2conList = conOrderer.getTripToken2conList();
		SortedMap<Triple, Triple> cur2antHm = corefDataOrganizer.getCur2antHm();
		SortedMap<Triple,Triple> ant2curHm = corefDataOrganizer.getAnt2curHm();
		SortedMap<Triple,Integer> chainIndHm = corefDataOrganizer.getChainIndHm();
		List<LinkedList<Triple>> corefList = corefDataOrganizer.getCorefList();
		List<Integer> temNeIdList = new ArrayList<Integer>();
		//now we have tokens and their ne tags and also index, then, we can check what ne tags the
		//concept have and use them to construct unkword. Others, just print them out. 
		Iterator<Triple> iterTripleToken2conHm = tripToken2conHm.keySet().iterator();
		Triple preConTriple = null;
		int corefInd = 0,distance  = 0, countCoref = 0, countToken  = 0 ;

		while(iterTripleToken2conHm.hasNext()){
			Triple conTriple = iterTripleToken2conHm.next();
			String conType = tripToken2conHm.get(conTriple);
			int conInd = tripToken2conList.indexOf(conTriple);
			temNeIdList.add(conInd);
			//in this case, it indicates that though cur2antHm doesn't contain the token, it is still in concept set.
			//so count it as NEW
			//the following is to create model files when op is old, where, what kind of dependencies between current old and previous old
			if(cur2antHm.containsKey(conTriple)){
				countCoref++;
				if(debug){
					System.out.println("countCoref: "+countCoref);
				}

				String token = (String)conTriple.o3;
				//this is the index the current token is in the list of concepts.


				//int conInd = tripToken2indConHm.get(conTriple);
				//this is the index the current chain is in the list of chains
				int chainInd = chainIndHm.get(conTriple);
				//the list where the tokenTriple is located
				List<Triple> chainList = corefList.get(chainInd);
				//the preInd should not be negative since cur2antHm starts from the second tokenTriple rather than the first one in the chain
				//preInd is different from preTokenInd, preInd is the location the preTokenTriple is in the chainList
				int preInd = chainList.indexOf(conTriple)-1;
				preConTriple = chainList.get(preInd);
				//preTokenInd is the location preTokenTriple is in the tripToken2conList
				corefInd = tripToken2conList.indexOf(preConTriple);
				String preConType = tripToken2conHm.get(preConTriple);
				int loc = temNeIdList.indexOf(corefInd);
				distance = conInd - corefInd;
				//if(distance>temNeIdList.size() && loc==-1){
				//}

				if(loc>=0){
					//here, temNeIdList.size minus 1 is because that if we don't subtract 1, there will never be
					//size : size-1 since the one on size-1 is the one with length of size which is curInd. 
					//	pwKey.println("IDENT "+corefInd+" "+preConToken+" "+conInd+" "+token);
					//after we print out the feature line, we need to use present conInd to replace the old one since this update will keep
					//mentions unique.
					System.out.println(loc);
					temNeIdList.remove(loc);
					//we should add conInd to temNeIdList twice.
					//temNeIdList.add(conInd);
				}

				if(debug){
					System.out.println("conInd: "+conInd + " curTokenTriple: " + conTriple+ " cur Contype: "+conType+ 
							" corefInd: "+corefInd+" corefTriple: "+tripToken2conList.get(corefInd)+" preNeTriple: "+preConType+" distance: "+distance);
				}
				//preTokenTriple = new Triple(OPEnum.old, distance, ne);
				//corefInd = conInd;
			}

			if(temNeIdList.size()==MAXSIZE){
				temNeIdList.remove(0);
			}
			countToken++;
		}
	}

	*//**
	 * @param args
	 * @throws IOException 
	 *//*
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File conFile = new File(args[0]);
		File chainFile = new File(args[1]);
		File docFile = new File(args[2]);
		List<File> fileList = new ArrayList<File>();
		fileList.add(conFile);
		fileList.add(chainFile);
		fileList.add(docFile);
		PrepareI2B2Key buildI2B2Key = new PrepareI2B2Key(fileList);
		buildI2B2Key.comKey();
	}


	@Override
	public ArrayList<CorefClusters> getKeyData(List<String> bsTokenList) {
		// TODO Auto-generated method stub
		return null;
	}

}
*/