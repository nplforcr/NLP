package reutersProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class BuildBindingFeatures {
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		// E:\Dissertation Writing\corpora\corefann\wxml
		//"/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"
		int item = 6;
		long start = System.currentTimeMillis();
		System.out.println("starting time: " + start);
		//ProcessReutersJdom processReutersCorpus = new ProcessReutersJdom();
		File input = new File(args[0]);
		//BufferedReader brXMLCorpus = new BufferedReader(new FileReader(input));
		//BufferedReader brXMLCorpus2 = new BufferedReader(new FileReader(input));
		File inputFile = new File(args[0]);
		if(inputFile.isDirectory()){
			File[] listOfFiles = inputFile.listFiles();
			for(int i=0;i<listOfFiles.length;i++){
				ProcessReutersJdomPron processReutersCorpus = new ProcessReutersJdomPron(listOfFiles[i]);
				System.out.println("file number: "+listOfFiles[i].getName());
				processReutersCorpus.splitMarkableNode();
				File outputDir = new File(args[1]);
				if(!outputDir.exists()){
					outputDir.mkdirs();
				}
				PrintWriter pwBinding = null; 
				try {
					pwBinding = new PrintWriter(new FileWriter(new File(
							outputDir, input.getName() + "bind.model"), true));
					processReutersCorpus.buildBindingFeat(pwBinding);
				} catch (IOException e) {
					//			// TODO Auto-generated catch block
					e.printStackTrace();
				}

				pwBinding.close();
			}
		}else{
			ProcessReutersJdomPron processReutersCorpus = new ProcessReutersJdomPron(inputFile);
			processReutersCorpus.splitMarkableNode();
			File outputDir = new File(args[1]);
			if(!outputDir.exists()){
				outputDir.mkdirs();
			}
			PrintWriter pwBinding = null; 
			try {
				pwBinding = new PrintWriter(new FileWriter(new File(outputDir,input.getName()+"bind.models")));
				processReutersCorpus.buildBindingFeat(pwBinding);
			} catch (IOException e) {
				//			// TODO Auto-generated catch block
				e.printStackTrace();
			}

			pwBinding.close();
		}

		// extractDJNML.extractDJNML(brDJFile);
		/*
		 * To get time in milliseconds, use long getTimeInMillis() method of
		 * Java Calendar class. It returns millseconds from Jan 1, 1970.
		 */
		/*
		 * Typical output would beCurrent milliseconds since Jan 1, 1970 are
		 * :1198628984102
		 */
		long stop = System.currentTimeMillis();
		System.out.println("stopping time: " + stop);
		long elapsed = stop - start;
		System.out.println("this is the total running time: " + elapsed);
	}
}
