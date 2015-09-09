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

import utils.CorefData;

public class ConvertCrOutput {
	
	List<Integer> menList;
	HashMap<Integer,ArrayList<Integer>> count2Coarray;
	HashMap<Integer,ArrayList<String>> count2Comention;
	ArrayList<ArrayList<Integer>> listOfCoarray;
	ArrayList<ArrayList<String>> listOfComention;
	
	public ConvertCrOutput(){
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
		int count = -1;
		//note, count only counts mentions, so, it is not matching the number in the output file as HYPOTH 24 for the first old in VOA19981106.0500.0674.sgm
		//the count is only 5, then, its size is also 5 and loc is 0. So, 5-(5-0)=0. So, it means that the first old refers back to 0. 
		while((line=brInput.readLine())!=null){
			if(line.startsWith("HYPOTH")){
				String[] lineArray = line.split(" ");
				String[] indArray = lineArray[2].split(";")[1].split("\\|")[0].split(",");
				//System.out.println(lineArray[2].split(";")[1].split("\\|")[0]);
				String oper = lineArray[2].split(";")[0];
				String mention = lineArray[3];
				if(oper.equals("NEW")){
					count++;
					menList.add(count);
					ArrayList<Integer> coArray  = new ArrayList<Integer>();
					coArray.add(count);
					count2Coarray.put(count, coArray);
					listOfCoarray.add(coArray);
					ArrayList<String> coMention = new ArrayList<String>();
					coMention.add(mention);
					count2Comention.put(count, coMention);
					listOfComention.add(coMention);
				}else if(oper.equals("old")){
					count++;
					if(indArray[0].equals("-")){
						indArray[0] = "0";
					}
					int loc = Integer.valueOf(indArray[0]);
					int size = Integer.valueOf(indArray[1]);
					//for example, size is 6 and loc is 5, it means that the 6th mention refers back to (6-5)th mention (since loc is counted from 
					//the size to 0)
					//then, we need to find position of coreferring mention in the whole text, so, we need to use count (say 62) minus (size-loc)
					//then the posOfCoref is 62-(6-5)=61
					int posOfCoref= count-(size-loc);
					//coArray is a list which add each coreferring mentions to the same list, count2Coarray is a hashmap which 
					//stores the count or posOfCoref as the key and the coArray related to the count as the value. So, probably,
					//coArray can be values of different counts
					
					ArrayList<Integer> coArray = count2Coarray.get(posOfCoref);
					int index = listOfCoarray.indexOf(coArray);
					//System.out.println("count: "+count);
					//amazing, when coArray add count, the corresponding one in the count2Coarray automatically updated. ?????????
					coArray.add(count);
					count2Coarray.put(count, coArray);
					//So, the following line is not so useful though it doesn't hurt.
					//count2Coarray.put(posOfCoref, coArray);
					ArrayList<String> coMention = count2Comention.get(posOfCoref);
					coMention.add(mention);
					count2Comention.put(count, coMention);
					//count2Comention.put(posOfCoref, coMention);
					menList.add(count);
					//listOfCoarray is simlar to count2Coarray. it is an arrayList and no repetitions.
					listOfCoarray.set(index, coArray);
					listOfComention.set(index,coMention);
				}
			}
		}
	}
	
	public List<CorefData> getResPair(){
		List<CorefData> responDataList = new ArrayList<CorefData>();
		for(int i=0;i<listOfCoarray.size();i++){
			ArrayList<Integer> coArray = listOfCoarray.get(i);
			ArrayList<String> coMention = listOfComention.get(i);
			for(int j=0;j<coArray.size()-1;j++){
				//System.out.println("IDENT "+coArray.get(j)+" "+coMention.get(j)+" "+coArray.get(j+1)+" "+coMention.get(j+1));
				CorefData responData = new CorefData();
				responData.setCorefIndex(coArray.get(j));
				responData.setCorefWord(coMention.get(j));
				responData.setAnaIndex(coArray.get(j+1));
				responData.setAnaphor(coMention.get(j+1));
				responDataList.add(responData);
			}			 	
		}
		
		Collections.sort(responDataList);
		
		for(int i=0 ;i<responDataList.size();i++){
			CorefData ithData = responDataList.get(i);
			System.out.println("IDENT "+ithData.getCorefIndex()+" "+ithData.getCorefWord()+" "+ithData.getAnaIndex()+" "+ithData.getAnaWord());
		}
		
		return responDataList;
	}

	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		ConvertCrOutput conCrOutput = new ConvertCrOutput();
		File inputFile = new File(args[0]);
		System.out.println("this is response: "+inputFile.getName());
		conCrOutput.fillContainers(inputFile);
		conCrOutput.getResPair();
	}

}
