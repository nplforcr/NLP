package aceProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.FineGrainedPron;
import utils.Pair;
import utils.ThirdReflexives;
import utils.TreebankPosTags;

public class TokenPos {
	public Pair tokenPosPair(File file){
		List<String> tokenList=new ArrayList<String>();
		List<String> posList=new ArrayList<String>();
		String line = "";
		String[] lineArray = null;
		BufferedReader bfReader;
		try {
			bfReader = new BufferedReader(new FileReader(file));
			//PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(dirOutput,fileArray[i].getName()+".posmodel")));
			try {
				while((line=bfReader.readLine())!=null){
					lineArray=line.split(" ");
					for(int j=0;j<lineArray.length;j++){
						if(lineArray[j].contains("/")){
							//String[] wordPos = lineArray[j].split("/");
							//String word = wordPos[0].toLowerCase();
							//String pos = wordPos[1];
							String word = lineArray[j].substring(0,lineArray[j].lastIndexOf("/"));
							String pos = lineArray[j].substring(lineArray[j].lastIndexOf("/")+1);
							
							TreebankPosTags tbPos = null;
							//the reason that we do the following conversion is that some pos taggs are punctuation marks.
							//but in our conreference project, we need to remove those marks. They are not so useful.
							if(TreebankPosTags.tbPosList2.contains(pos)){
								tbPos= TreebankPosTags.convert(pos);	
								//System.out.println("tbPos: "+tbPos);
							}else{
								//System.out.println("what pos here :"+pos);
								tbPos= TreebankPosTags.valueOf(pos);
							}
							//I should consider cases where 
							
							//System.out.println("be careful: "+tbPos.toString()+ " word: "+word);

							if((tbPos==TreebankPosTags.PRP) ||tbPos==TreebankPosTags.PRP$){
								if(ThirdReflexives.thirdRefList2.contains(word)){
									pos = FineGrainedPron.thirdRef.toString();

								}
								else{
									pos = FineGrainedPron.otherPronoun.toString();
								}
							}
							tokenList.add(word);
							posList.add(pos);
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Pair tokenPosListPair=new Pair(tokenList,posList);
		return tokenPosListPair;
	}	
}
