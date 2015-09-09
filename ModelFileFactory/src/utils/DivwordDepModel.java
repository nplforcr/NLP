package utils;


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

public class DivwordDepModel {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader bfGenderBergsma = new BufferedReader(new FileReader(
				new File(args[0])));
		BufferedReader bfWordDepReuter = new BufferedReader(new FileReader(
				new File(args[1])));
		PrintWriter pwGenderModel = new PrintWriter(new FileWriter(new File(
				args[2])));
		PrintWriter pwSynModel = new PrintWriter(new FileWriter(new File(
				args[3])));
		String lineReuter = "";
		String lineBergsma = "";
		HashMap<String, Integer> wordHMBergsma = new HashMap<String, Integer>();
		// List<String> wordListBergsma = new ArrayList<String>();
		// List<String> wordListReuter = new ArrayList<String>();
		try {
			while ((lineBergsma = bfGenderBergsma.readLine()) != null) {
				String startWord = lineBergsma.split(" ")[0];
				String secWord = lineBergsma.split(" ")[1];
				String colon = lineBergsma.split(" ")[2];
				String thirdWord = lineBergsma.split(" ")[3];
				String equal_symbol = lineBergsma.split(" ")[4];
				String prob = lineBergsma.split(" ")[5];
				if(startWord.equals("Gen_W")&& thirdWord.equals("UNK")){
					pwGenderModel.println(startWord+" "+secWord+" "+colon+" NEU "+equal_symbol+" "+prob);
				}
				else if(startWord.equals("Num_W")&& thirdWord.equals("UNK")){
					pwGenderModel.println(startWord+" "+secWord+" "+colon+" SING "+equal_symbol+" "+prob);
				}
				else if (!startWord.equals("Syn_W")) {
					pwGenderModel.println(lineBergsma);
				} 
			}
			while ((lineReuter = bfWordDepReuter.readLine()) != null) {
				String wordReuter = lineReuter.split(" ")[1];
				if (lineReuter.startsWith("Syn_W")) {
					System.out.println(lineReuter);
					pwSynModel.println(lineReuter);
				}
			}
			pwGenderModel.close();
			pwSynModel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
