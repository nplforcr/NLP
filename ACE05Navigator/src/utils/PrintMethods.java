package utils;

import java.io.PrintWriter;

import annotation.SimpleRelMention;

import opennlp.tools.tokenize.SimpleTokenizer;

public class PrintMethods {

	public static void printTokens(PrintWriter pwInput, String relType, String text){
		//pwInput.print(relType+" : "+men1Str+" "+neType1+" ");
		pwInput.print(relType+" : "+" ");
		opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
		String[] tokens = tokenizer.tokenize(text);
		for(int k=0;k<tokens.length;k++){
			pwInput.print(tokens[k].toLowerCase()+" ");					
		}
		//pwInput.print(men2Str+" "+neType2+" ");
		pwInput.println();
	}
	
	public static String setText(StringBuffer dBuf,SimpleRelMention simRelMen){
		String text="";
		int[] men1Span = simRelMen.getMen1Span();
		int[] men2Span = simRelMen.getMen2Span();
		if(men1Span[0]<=men2Span[0]&&men1Span[1]<=men2Span[1]){
			text = dBuf.substring(men1Span[0],men2Span[1]+1);
			//System.out.println("case 1: men1Span[0] "+men1Span[0]+" men2Span[1] "+men2Span[1]);
		}else if(men1Span[0]>=men2Span[0]&&men1Span[1]<=men2Span[1]){
			text = dBuf.substring(men2Span[0],men2Span[1]+1);
			//System.out.println("case 2: men2Span[0] "+men2Span[0]+" men2Span[1] "+men2Span[1]);
		}else if(men1Span[0]>=men2Span[0]&&men1Span[1]>=men2Span[1]){
			text = dBuf.substring(men2Span[0],men1Span[1]+1);
			//System.out.println("case 3: men2Span[0] "+men2Span[0]+" men1Span[1] "+men1Span[1]);
		}else if(men1Span[0]>=men2Span[0]&&men1Span[1]>=men2Span[1]){
			text = dBuf.substring(men2Span[0],men1Span[1]+1);
			//System.out.println("case 4: men2Span[0] "+men2Span[0]+" men1Span[1] "+men1Span[1]);
		}else if(men1Span[0]>men2Span[0]&&men1Span[1]>men2Span[1]){
			text = dBuf.substring(men2Span[0],men1Span[1]+1);
			//System.out.println("case 5: men2Span[0] "+men2Span[0]+" men1Span[1] "+men1Span[1]);
		}else if(men1Span[0]<=men2Span[0]&&men1Span[1]>=men2Span[1]){
			text = dBuf.substring(men1Span[0],men1Span[1]+1);
			//System.out.println("case 6: men1Span[0] "+men1Span[0]+" men1Span[1] "+men1Span[1]+" men2Span[0] "+men2Span[0]+" men2Span[1] "+men2Span[1]);
		}
		return text;
	}
}
