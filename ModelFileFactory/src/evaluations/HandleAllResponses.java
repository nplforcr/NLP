/*package evaluations;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;

import utils.Triple;

//import edu.mayo.bmi.I2B2.utils.CONST;

public class HandleAllResponses {

	File conFileDir;
	File respFileDir;
	public HandleAllResponses(File conFileDir,File respFileDir) throws IOException{
		this.conFileDir=conFileDir;
		this.respFileDir=respFileDir;
		File[] listOfConFile = conFileDir.listFiles();
		for(int i=0;i<listOfConFile.length;i++){
			File ithConFile = listOfConFile[i];
			String ithConFileName = ithConFile.getName();
			String ithRespFileName = ithConFileName.substring(0,ithConFileName.indexOf(CONST.CON));
			File ithRespFile = new File(respFileDir,ithRespFileName);
			System.out.println(ithConFile+" "+ithRespFile);
			if(ithConFile.exists() && ithRespFile.exists()){
				System.out.println(ithConFile+" "+ithRespFile);
				SortedMap<Integer, SortedMap<Triple, Integer>> resolvedChainMap  = runOneFile(ithConFile,ithRespFile);
				resolvedChainMap=this.runOneFile(ithConFile, ithRespFile);
			}
		}
	}
	
	public SortedMap<Integer, SortedMap<Triple, Integer>> runOneFile(File ithConFile,File ithRespFile) throws IOException{
		PrepareI2B2CrResponse preI2b2CrResponse = new PrepareI2B2CrResponse(ithConFile,ithRespFile);
		preI2b2CrResponse.fillContainers(ithConFile);
		preI2b2CrResponse.convertRespToI2B2Chains();	 
		SortedMap<Integer, SortedMap<Triple, Integer>> resolvedChainMap = preI2b2CrResponse.printResolvedChainMap(pwI2B2Out);
		return resolvedChainMap;
	}
	
	*//**
	 * @param args
	 * @throws IOException 
	 *//*
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		File conFileDir = new File(args[0]);
		File respFileDir = new File(args[1]);
		HandleAllResponses handAll = new HandleAllResponses(conFileDir,respFileDir);
		
	}

}
*/