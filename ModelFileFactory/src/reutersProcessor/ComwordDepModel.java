package reutersProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ComwordDepModel {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader bfGenderBergsma=new BufferedReader(new FileReader(new File(args[0])));
		BufferedReader bfGenderReuter=new BufferedReader(new FileReader(new File(args[1])));
		PrintWriter pwGenderModel=new PrintWriter(new FileWriter(new File(args[2])));
		String lineReuter="";
		String lineBergsma="";
		HashMap<String,Integer> wordHMBergsma = new HashMap<String,Integer>();
		//List<String> wordListBergsma = new ArrayList<String>();
		//List<String> wordListReuter = new ArrayList<String>();
		try {
			while((lineBergsma=bfGenderBergsma.readLine())!=null){
				String wordBergsma = lineBergsma.split(" ")[1];
				//wordListBergsma.add(wordBergsma);
				
				wordHMBergsma.put(wordBergsma, 1);
				if(!wordBergsma.contains("_")){
					System.out.println("wordBergsma: "+wordBergsma);
					pwGenderModel.println(lineBergsma);
				}
			}
			while((lineReuter=bfGenderReuter.readLine())!=null){
				String wordReuter = lineReuter.split(" ")[1];
				//wordListReuter.add(wordReuter);
				System.out.println("wordReuter: "+wordReuter);
				if(!wordHMBergsma.containsKey(wordReuter)){
					pwGenderModel.println(lineReuter);
				}else if(lineReuter.startsWith("Syn_W")){
					System.out.println(lineReuter);
					pwGenderModel.println(lineReuter);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
