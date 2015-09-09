package corpusProcessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

import mucProcessor.ConvertReuterToMuc;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


public class PrepareCherryPickerInput {

	/**@author leonlee
	 * @param args
	 */

	static String DOT = ".";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PrepareCherryPickerInput prePareCherryPickerInput = new PrepareCherryPickerInput();
		BufferedReader brTagged;
		try {
			brTagged = new BufferedReader(new FileReader(new File(args[0])));
			BufferedReader brXMLCorpus = new BufferedReader(new FileReader(new File(args[1])));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		// E:\Dissertation Writing\corpora\corefann\wxml
		// "/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"
		long start = System.currentTimeMillis();
		File inputFile = new File(args[0]);
		if (inputFile.isDirectory()) {
			File[] listOfFiles = inputFile.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				System.out.println("file number: " + listOfFiles[i].getName());
				try {
					File outputDir = new File(args[1]);
					if (!outputDir.exists()) {
						outputDir.mkdirs();
					}
					PrintWriter pwCorpus = null; 	
					pwCorpus=new PrintWriter(new FileWriter(new File(outputDir, listOfFiles[i].getName())));
					String line="";
					String[] lineArray=null;
					brTagged = new BufferedReader(new FileReader(listOfFiles[i]));
					while((line=brTagged.readLine())!=null){
						lineArray=line.split("\t");
						List<String> lineList=Arrays.asList(lineArray);
						lineList.indexOf(DOT);
					}
					pwCorpus.close();
				} catch (IOException e) {
					// // TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		} else {

			File outputDir = new File(args[1]);
			if (!outputDir.exists()) {
				outputDir.mkdirs();
			}
			try {
				brTagged = new BufferedReader(new FileReader(inputFile));
				PrintWriter pwCorpus = new PrintWriter(new FileWriter(new File(args[1])));
				pwCorpus.close();
			} catch (IOException e) {
				// // TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		long stop = System.currentTimeMillis();
		System.out.println("stopping time: " + stop);
		long elapsed = stop - start;
		System.out.println("this is the total running time: " + elapsed);
	}

}
