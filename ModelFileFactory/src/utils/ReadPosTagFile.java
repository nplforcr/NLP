package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadPosTagFile {
	
	static boolean debug = false;

	/**
	 * create such a list from stanford postagged files
	 * @param fileTag
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<ArrayList<Pair>> createPosWordList(File fileTag) throws IOException{
		ArrayList<ArrayList<Pair>> sentWordPosList = new ArrayList<ArrayList<Pair>>();

		BufferedReader bfReader = new BufferedReader(new FileReader(fileTag));
		String line = "";
		String[] lineArray =null;
		while((line=bfReader.readLine())!=null){
			lineArray=line.split(" ");
			for(int j=0;j<lineArray.length;j++){
				if(lineArray[j].contains("/")){
					String[] wordPos = lineArray[j].split("/");
					String word = wordPos[0].toLowerCase();
					String pos = wordPos[1];
					TreebankPosTags tbPos = null;

					if(TreebankPosTags.tbPosList2.contains(pos)){
						tbPos= TreebankPosTags.convert(pos);	
						//System.out.println("tbPos: "+tbPos);
					}else{
						tbPos= TreebankPosTags.valueOf(pos);
					}

					if((tbPos==TreebankPosTags.PRP) ||tbPos==TreebankPosTags.PRP$){

						if(ThirdPersonPron.thirdPerPronList2.contains(word)){

						}else if(ThirdReflexives.thirdRefList2.contains(word)){
							pos = FineGrainedPron.thirdRef.toString();

						}
						else{
							pos = FineGrainedPron.otherPronoun.toString();

						}
					}
				}									
			}
		}
		return sentWordPosList;
	}
	
	/**
	 * create such a list from stanford parsed files
	 * @param fileTag
	 * @return
	 * @throws IOException 
	 */
	public static ArrayList<ArrayList<Pair>> createPosWordList2(File fileTag) throws IOException{
		ArrayList<ArrayList<Pair>> sentWordPosList = new ArrayList<ArrayList<Pair>>();
		BufferedReader bfReader = new BufferedReader(new FileReader(fileTag));
		String line = "";
		while((line=bfReader.readLine())!=null){
			ArrayList<Pair> posWordSent = new ArrayList<Pair>();
			//in the following regular expressions, it means that there are brackets as (). Between them,
			//symbols among [] must be at least one. But if there are more than one, there can be their combinations.
			//for example, if we have a number as 1\/2, converted to re in java should be \\w\\\\/ or \\w/\\\\. Note:
			//it seems that \ should be represented as foud "\\\\".
			//but I don't quite understand why "\\([-\\w!,?:;.$\"`\'%]+\\s[\\-\\w!,?:;.$\"`\'%/\\]+\\)" is not working
			//Exception in thread "main" java.util.regex.PatternSyntaxException: Unclosed character class near index 42
			//\([-\w!,?:;.$"`'%]+\s[\-\w!,?:;.$"`'%/\]+\)
			if(line.contains("15 3\\/8")){
				System.out.println(line);
			}
			Pattern pattern = Pattern.compile("\\([-\\w!,?:;.$\"`\'%]+\\s[-\\w!,?:;.$\"`\'%&/\\\\]+\\)");
			Matcher matcher = pattern.matcher(line);
			boolean found = matcher.find();
			String match=matcher.group();
			while(found){
				match=matcher.group();
				int indWS = match.indexOf(" ");
				String pos = match.substring(1,indWS);
				String token = match.substring(indWS+1,match.length()-1);
				if(debug){
					System.out.println("createPosWordList2 in ReadPosTagFile.java: "+match+" pos: "+pos+" token: "+token );
				}
				Pair wordPos = new Pair(token,pos);
				posWordSent.add(wordPos);
				found = matcher.find();
			}
			sentWordPosList.add(posWordSent);
		}
		return sentWordPosList;
	}
}
