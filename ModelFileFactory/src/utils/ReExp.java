package utils;

public class ReExp {
	public static String typeReConType = "\\|\\|t=\"\\w+\"";
	public static String typeReAspType = "\\|\\|a=\"\\w+\"";
	public static String typeRe2 = "\\|\\|t=\"\\w+\\s\\w+\"";
	public static String symbol = "=@`\\^\\]\\[\\$&*+><~#%\\(\\)/\\.,;:\\-!?'\"";
	public static String symbols = "[=@`\\^\\]\\[\\$&*+><~#%\\(\\)/\\.,;:\\-!?'\"]+";
	public static String item = "[^ ]*";
	public static String cptPattern = "[^ ]";
	//now, we don't need all of the following since only they are all combinations of symbols
	//the following re adds c= because the type has the form "type". Though the compile starts from left to right, 
	//I still include c= here.
	//public static String phraseRe1 = "\"\\w+";
	//public static String phraseRe2 = "\\s+\\w+";
	//public static String phraseRe3 = "\\s+\\w+[=@`\\^\\]\\[\\$&*><~#%\\(\\)/.,;:\\-!?'\"]*";
	//public static String phraseRe4 = "=@`\\^\\]\\[\\$&*+><~#%\\(\\)/.,;:\\-!?'\"";
	//public static String phraseRe5 = "\\w+";
	//public static String phraseRe = "=@`\\^\\]\\[\\$&*+><~#%\\(\\)/.,;:\\-!?'\"";
	//public static String phraseRe3 = "a-zA-Z0-9=@`\\^\\]\\[\\$&*><~#%\\(\\)/.,;:\\-!?'\"";
	//static String phraseRe2 = "^ ";
	public static String rangeRe = "\\d+:\\d+\\s\\d+:\\d+";
}
