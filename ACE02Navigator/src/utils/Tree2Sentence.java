package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import edu.stanford.nlp.ling.HasWord;
//import edu.stanford.nlp.ling.Sentence;
//import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Tree2Sentence {

	/**
	 * it aims at returning a sentence by removing brackets and parsed tags such as NP, NN and so on.
	 * @param sentSb
	 * @param line
	 * @param indexList keeps the indice of each word in order to find bytespan of each word more accurately
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> sentFinder(StringBuffer sentSb,String line,ArrayList<Integer> indexList) throws Exception{
		String[] lineArray = line.split("\\s+");
		String cutLine = line;
		ArrayList<String> wordList = new ArrayList<String>();
		int lineLen = line.length();
		for(int i=0;i<lineArray.length;i++){
			String token = lineArray[i];
			String word = Tree2Sentence.wordFinder(token);
			if(!word.isEmpty()){
				sentSb.append(word+" ");
				wordList.add(word);
				int cutLen = cutLine.length();
				int difLen = lineLen-cutLen;
				int tokenIndex = cutLine.indexOf(word)+difLen;
				indexList.add(tokenIndex);
				
			}
			if(i==lineArray.length-1){
				cutLine = cutLine.substring(token.length());
			}else{
				cutLine = cutLine.substring(token.length()+1);
			}
			
		}
		return wordList;
	}

	/**
	 * it aims at find "word)" from a parsing sentence and return a word
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public static String wordFinder(String token) throws Exception {
		String subToken = "";
		Pattern closeBraP = Pattern.compile("\\)");
		Matcher matcher = closeBraP.matcher(token);
		boolean found = matcher.find();
		if(found){
			String match = matcher.group();
			subToken = token.substring(0,token.indexOf(match));
		}
		return subToken;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
