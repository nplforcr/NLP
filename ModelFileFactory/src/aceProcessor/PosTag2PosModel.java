package aceProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import utils.FemalePronouns;
import utils.FineGrainedPron;
import utils.ThirdPersonPron;
import utils.ThirdReflexives;
import utils.TreebankPosTags;


public class PosTag2PosModel {

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
									String[] wordPos = new String[2];
									if(lineArray[j].contains("\\/")){
										int lastInd = lineArray[j].lastIndexOf("/");
										wordPos[0]=lineArray[j].substring(0,lastInd).replace("\\/", "/");
										wordPos[1]=lineArray[j].substring(lastInd+1);
										System.out.println(wordPos[0]+" "+wordPos[1]);
									}else{
										int lastInd = lineArray[j].lastIndexOf("/");
										wordPos[0]=lineArray[j].substring(0,lastInd);
										wordPos[1]=lineArray[j].substring(lastInd+1);
										System.out.println(wordPos[0]+" "+wordPos[1]);
									}

									//the following line remove spaces between tokens
									String word = wordPos[0].toLowerCase().replace("\\s", "");
									if(word.contains(":")){
										word = word.replace(":", "-");
									}
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
					//the following are special cases. Probably, they don't appear in training data. But they 
					//appear in testing data.
					pwOutput.println("Pos_W : : :");
					//it seems that amp problem is from the replacement of & with and. Not sure. Anyway,
					//it is not so important. But it affects the running. So, I add this. 
					pwOutput.println("Pos_W amp : NNP");
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
						if(lineArray[j].contains("/")){
							String[] wordPos =new String[2];
							if(lineArray[j].contains("\\/")){
								
								int lastInd = lineArray[j].lastIndexOf("/");
								wordPos[0]=lineArray[j].substring(0,lastInd).replace("\\/", "/");
								wordPos[1]=lineArray[j].substring(lastInd+1);
								System.out.println(wordPos[0]+" "+wordPos[1]);
							}else{
								int lastInd = lineArray[j].lastIndexOf("/");
								wordPos[0]=lineArray[j].substring(0,lastInd);
								wordPos[1]=lineArray[j].substring(lastInd+1);
								System.out.println(wordPos[0]+" "+wordPos[1]);
							}
							pwOutput.println("Pos_W "+wordPos[0].replace("\\s", "").replace(":", "-")+ " : "+wordPos[1]);
						}
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
		PosTag2PosModel posTag2PosModel = new PosTag2PosModel();
		File fileTag = new File(args[0]);
		posTag2PosModel.buildPosWordModel(fileTag);

	}

}
