package evaluations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import utils.CorefClusters;
import utils.SortedIndex;

public class PrepareCrResponse {
	
	List<Integer> menList;
	HashMap<Integer,ArrayList<Integer>> count2Coarray;
	HashMap<Integer,ArrayList<String>> count2Comention;
	ArrayList<ArrayList<Integer>> listOfCoarray;
	ArrayList<ArrayList<String>> listOfComention;
	boolean copyIncluded = true;
	
	public PrepareCrResponse(){
		menList = new ArrayList<Integer>();
		count2Coarray = new HashMap<Integer,ArrayList<Integer>>();
		count2Comention = new HashMap<Integer,ArrayList<String>>();
		listOfCoarray = new ArrayList<ArrayList<Integer>>();
		listOfComention = new ArrayList<ArrayList<String>>();
	}
	
	public void fillContainers(File inputFile) throws IOException{
		System.out.println("this is response: "+inputFile.getName());
		BufferedReader brInput = new BufferedReader(new FileReader(inputFile));
		String line = "";
		int count = 0;
		ArrayList<Integer> bufferCountList = new ArrayList<Integer>();
		int bSize = 6;
		while((line=brInput.readLine())!=null){
			if(line.startsWith("HYPOTH")){
				String[] lineArray = line.split(" ");
				String[] indArray = lineArray[2].split(";")[1].split("\\|")[0].split(",");
				//System.out.println(lineArray[2].split(";")[1].split("\\|")[0]);
				int menInd = Integer.valueOf(lineArray[1]);
				String oper = lineArray[2].split(";")[0];
				String mention = lineArray[3];
				//int absInd = Integer.valueOf(lineArray[4]);
				if(oper.equals("NEW")){
					
					menList.add(count);
					if(bufferCountList.size()<bSize){
						if(copyIncluded){
							bufferCountList.add(count);	
						}else{
							bufferCountList.add(menInd);	
						}
						
					}else{
						bufferCountList.remove(0);
						if(copyIncluded){
							bufferCountList.add(count);	
						}else{
							bufferCountList.add(menInd);	
						}
					}
					ArrayList<Integer> coArray  = new ArrayList<Integer>();
					if(copyIncluded){
						coArray.add(count);
						count2Coarray.put(count, coArray);
					}else{
						coArray.add(menInd);
						count2Coarray.put(menInd, coArray);
					}
					listOfCoarray.add(coArray);
					ArrayList<String> coMention = new ArrayList<String>();
					coMention.add(mention);
					if(copyIncluded){
						count2Comention.put(count, coMention);
					}else{
						count2Comention.put(menInd, coMention);
					}
					listOfComention.add(coMention);
					count++;
				}else if(oper.equals("old")){
					
					int loc = Integer.valueOf(indArray[0]);
					int size = Integer.valueOf(indArray[1]);
					int posOfCoref= bufferCountList.get(loc);
					bufferCountList.remove(loc);
					bufferCountList.add(count);
					//bufferCountList.add(menInd);
					//the following line is totally wrong. I made a stupid mistake. After a few days
					//work, I found a quite simple way to output the correct response files for evaluation
					//it is a great step. Depending on my goal, I can select to sort the output or not. 
					//then I can use different evaluations to evaluate my coreference results.
					//int posOfCoref= count-(size-loc);
					ArrayList<Integer> coArray = count2Coarray.get(posOfCoref);
					int index = listOfCoarray.indexOf(coArray);
					//System.out.println("count: "+count);
					if(copyIncluded){
						coArray.add(count);
						count2Coarray.put(count, coArray);
					}else{
						coArray.add(menInd);
						count2Coarray.put(menInd, coArray);
					}
					count2Coarray.put(posOfCoref, coArray);
					ArrayList<String> coMention = count2Comention.get(posOfCoref);
					coMention.add(mention);
					if(copyIncluded){
						count2Comention.put(count, coMention);
						menList.add(count);
					}else{
						count2Comention.put(menInd, coMention);
						menList.add(menInd);
					}
					count2Comention.put(posOfCoref, coMention);
					listOfCoarray.set(index, coArray);
					listOfComention.set(index,coMention);
					count++;
				}
			}
			
		}
	}
	
	public List<CorefClusters> getResPair(){
		List<CorefClusters> responDataList = new ArrayList<CorefClusters>();
		for(int i=0;i<listOfCoarray.size();i++){
			ArrayList<Integer> coArray = listOfCoarray.get(i);
			//Collections.sort(coArray);
			ArrayList<String> coMention = listOfComention.get(i);
			for(int j=0;j<coArray.size()-1;j++){
				System.out.println("IDENT "+coArray.get(j)+" "+coMention.get(j)+" "+coArray.get(j+1)+" "+coMention.get(j+1));
				CorefClusters responData = new CorefClusters();
				responData.setCorefIndex(coArray.get(j));
				responData.setCorefWord(coMention.get(j));
				responData.setAnaIndex(coArray.get(j+1));
				responData.setAnaphor(coMention.get(j+1));
				responDataList.add(responData);
			}			 	
		}
		return responDataList;
	}

	public List<CorefClusters> getSortedResPair(){
		List<CorefClusters> responDataList = new ArrayList<CorefClusters>();
		for(int i=0;i<listOfCoarray.size();i++){
			final ArrayList<Integer> coArray = listOfCoarray.get(i);
			ArrayList<String> coMention = listOfComention.get(i);
			for(int j=0;j<coArray.size()-1;j++){
				//System.out.println("IDENT "+coArray.get(j)+" "+coMention.get(j)+" "+coArray.get(j+1)+" "+coMention.get(j+1));
				CorefClusters responData = new CorefClusters();
				responData.setCorefIndex(coArray.get(j));
				responData.setCorefWord(coMention.get(j));
				responData.setAnaIndex(coArray.get(j+1));
				responData.setAnaphor(coMention.get(j+1));
				responDataList.add(responData);
			}			 	
		}
		
		Collections.sort(responDataList);
		for(int i=0;i<responDataList.size();i++){
			CorefClusters ithResponData = responDataList.get(i);
			System.out.println("IDENT "+ithResponData.getCorefIndex()+" "+ithResponData.getCorefWord()+" "+ithResponData.getAnaIndex()+" "+ithResponData.getAnaWord());
		}
		
		return responDataList;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		PrepareCrResponse conCrOutput = new PrepareCrResponse();
		File inputFile = new File(args[0]);
		System.out.println("this is response: "+inputFile.getName());
		conCrOutput.fillContainers(inputFile);
		conCrOutput.getSortedResPair();
		//conCrOutput.getResPair();
	}

}
