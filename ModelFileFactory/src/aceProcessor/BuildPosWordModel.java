package aceProcessor;

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
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import utils.CommonFunctions;
import utils.FineGrainedPron;
import utils.Pair;
import utils.ThirdPersonPron;
import utils.ThirdReflexives;
import utils.TreebankPosTags;
import utils.helper;

public class BuildPosWordModel {

	SortedMap<Pair,Integer> hmTagged;
	static String EOS = "eos"; 
	static String DASH = "-";
	static String SGMAFFIX = ".sgm";
	static String TAGAFFIX = ".tagged";
	static String XMLAFFIX = ".tmx.rdc.xml";
	List<String> opList;
	HashMap<Pair, String> headIdHM;
	HashMap<Pair, String> entityHM;
	File aceFileDir;
	File aceTagDir;

	public BuildPosWordModel(File aceFileDir,File aceTagDir){
		hmTagged = new TreeMap<Pair,Integer>();
		opList = new ArrayList<String>();
		this.aceFileDir = aceFileDir;
		this.aceTagDir = aceTagDir;
	}

	public void readTaggedFiles(File outputFile) throws IOException, ParserConfigurationException, SAXException{
		if(!outputFile.exists()){
			outputFile.mkdirs();
		}
		PrintWriter pwOutput = new PrintWriter(new FileWriter(outputFile));
		TokenPos tokenPos = new TokenPos();
		File[] tagFileArray = aceTagDir.listFiles();
		for(int i=0;i<tagFileArray.length;i++){
			File ithTaggedFile = tagFileArray[i];
			String line = "";
			String[] lineArray = null;
			Pair tokenPosListPair=tokenPos.tokenPosPair(ithTaggedFile);
			List<String> tokenList=(List<String>)tokenPosListPair.getFirst();
			List<String> posList=(List<String>)tokenPosListPair.getSecond();
			File sgmFile = new File(aceFileDir,ithTaggedFile.getName().replace(TAGAFFIX, ""));
			File xmlFile = new File(aceFileDir,ithTaggedFile.getName().replace(TAGAFFIX, XMLAFFIX));
			//the reason that I bother to obtain an opList is want to assign a reasonable probs to unkword.
			//for pos, I am using William's d-tree tagger to tag unkword, but it seems that this is not good enough
			//since some medical words are abbreated words and the d-tree tagger cannot tag them correctly. In this
			//case, I plan to do rerun a unkword prob assignment to assign a different tagger to it.
			//the following are different case
			//since sometimes, some words do exist in the corpus and they don't have correct pos because in medical 
			//corpus, some words are abbreviated and Stanford tagger cannot tag them correctly.
			///and have a pos tag as well. But the tag we want is different (this is different case). 
			List<String> opList = helper.obtainOpList(sgmFile, xmlFile, ithTaggedFile);
			for(int k=0;k<tokenList.size();k++){
				Pair tokenPosPair = new Pair(tokenList.get(k),posList.get(k));
				CommonFunctions.updateHashMap(hmTagged, tokenPosPair);
			}
			Pair wordPosPair = new Pair(EOS,DASH);
			CommonFunctions.updateHashMap(hmTagged, wordPosPair);
		}
	}



	public void buildPosWordModel(File fileTag){
		if(fileTag.isDirectory()){
			String outFileStr = fileTag.getName().substring(0,fileTag.getName().length()-7);
			File dirOutput = new File(fileTag.getParent(),outFileStr+"Model");
			System.out.println(fileTag);
			if(!dirOutput.exists()){
				dirOutput.mkdir();
			}
			File[] fileArray = fileTag.listFiles();
			PrintWriter pwOutput;
			PrintWriter pwPosratio;
			PrintWriter pwPostally;
			try {
				pwOutput = new PrintWriter(new FileWriter(new File(dirOutput,outFileStr+".posWord.model"),true));
				pwPosratio = new PrintWriter(new FileWriter(new File(dirOutput,outFileStr+".posratio.model"),true));
				pwPostally = new PrintWriter(new FileWriter(new File(dirOutput,"pw."+outFileStr+".tally"),true));
				try {
					for(int i=0;i<fileArray.length;i++){
						int fileLeng = fileArray[i].getName().length();
						String line = "";
						String[] lineArray = null;
						BufferedReader bfReader = new BufferedReader(new FileReader(fileArray[i]));
						System.out.println(fileArray[i]);

						//PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(dirOutput,fileArray[i].getName()+".posmodel")));
						while((line=bfReader.readLine())!=null){
							lineArray=line.split(" ");
							for(int j=0;j<lineArray.length;j++){
								if(lineArray[j].contains("/")){
									String[] wordPos = lineArray[j].split("/");
									String word = wordPos[0].toLowerCase();
									String pos = wordPos[1];
									TreebankPosTags tbPos = null;
									System.out.println("pos in PosTag2PosModel.java: "+pos);

									if(TreebankPosTags.tbPosList2.contains(pos)){
										tbPos= TreebankPosTags.convert(pos);	
										//System.out.println("tbPos: "+tbPos);
									}else{
										tbPos= TreebankPosTags.valueOf(pos);
									}

									System.out.println("be careful: "+tbPos.toString()+ " word: "+word);

									if((tbPos==TreebankPosTags.PRP) ||tbPos==TreebankPosTags.PRP$){

										if(ThirdPersonPron.thirdPerPronList2.contains(word)){
											pwOutput.println("Pos_W "+word+ " : "+pos);
											pwPosratio.println("Pos : "+pos);
											pwPostally.println("Pw : "+word+ " : "+pos);
										}else if(ThirdReflexives.thirdRefList2.contains(word)){
											pos = FineGrainedPron.thirdRef.toString();
											pwOutput.println("Pos_W "+word+ " : "+pos);
											pwPosratio.println("Pos : "+pos);
											pwPostally.println("Pw : "+word+ " : "+pos);
										}
										else{
											pos = FineGrainedPron.otherPronoun.toString();
											pwOutput.println("Pos_W "+word+ " : "+pos);
											pwPosratio.println("Pos : "+pos);
											pwPostally.println("Pw : "+word+ " : "+pos);
										}
									}

									else{
										pwOutput.println("Pos_W "+word.toLowerCase()+ " : "+pos);
										pwPosratio.println("Pos : "+pos);
										pwPostally.println("Pw : "+word.toLowerCase()+ " : "+pos);
									}
								}									
							}
						}
						pwOutput.println("Pos_W eos : -");
						pwPosratio.println("Pos : -");
					}
					pwOutput.println("Pos_W e : NNP");
					pwOutput.println("Pos_W unscum : NNP");
					pwOutput.println("Pos_W nato : NNP");
					pwOutput.println("Pos_W wto : NNP");
					pwOutput.close();
					pwPosratio.close();
					pwPostally.close();
				}catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}else{
			try {
				String line = "";
				String[] lineArray = null;
				PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(fileTag.getName()+".posmodel")));
				BufferedReader bfReader = new BufferedReader(new FileReader(fileTag));
				System.out.println(fileTag.getName());

				while((line=bfReader.readLine())!=null){
					lineArray=line.split(" ");
					for(int j=0;j<lineArray.length;j++){
						String[] wordPos = lineArray[j].split("/");
						String word = wordPos[0];
						String pos = wordPos[1];
						pwOutput.println("Pos_W "+word+ " : "+pos);
					}
				}
				//some special wors may be added to here. but mostly, we depend on dTree pos tagger to decide pos of unknown words
				pwOutput.println("Pos_W nato : NNP");
				pwOutput.println("Pos_W unscum : NNP");
				pwOutput.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		BuildPosWordModel posWordModel= new BuildPosWordModel(new File(args[0]), new File(args[0]));
		File file = new File(args[0]);
		posWordModel.buildPosWordModel(file);
	}

}
