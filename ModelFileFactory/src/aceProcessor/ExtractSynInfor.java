package aceProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ExtractSynInfor {

	HashMap<String,String> wordChunkHm;
	
	public ExtractSynInfor(){
		wordChunkHm = new HashMap<String,String>();
	}
	
	public void exChunk(File inputFile) throws IOException{
		BufferedReader bfInput = new BufferedReader(new FileReader(inputFile));
		String line = "";
		while((line=(bfInput.readLine()))!=null){
			if(!line.startsWith("#")){
				StringTokenizer strTok = new StringTokenizer(line," ");
				int count = 0;
				String word = "";
				String chunk = "";
				while (strTok.hasMoreTokens()) {
					//System.out.println(strTok.nextToken());
					if(count==5){
						word = strTok.nextToken();
					}
					else if(count==6){
						chunk = strTok.nextToken();
						wordChunkHm.put(word, chunk);
						System.out.println("word: "+word+" chunk: "+chunk);
					}else{
						strTok.nextToken();
						//System.out.println("count: "+count);
					}
					count++;
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		ExtractSynInfor exSynInfor = new ExtractSynInfor();
		File inputFile = new File(args[0]);
		exSynInfor.exChunk(inputFile);
	}

}
