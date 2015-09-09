package utils;

import java.util.ArrayList;
import java.util.List;

public enum TreebankPosTags {
	//enum can not directly use special characters
	thirdRef,
	otherPron,
	COLON,
	DASH,
	COMMA,
	PERIOD,
	RRB,
	LRB,
	CC,  //,// - Coordinating conjunction
	CD,//,// - Cardinal number
	DT, //,// - Determiner
	EX,//,// - Existential there
	FW,// - Foreign word
	IN,// - Preposition or subordinating conjunction
	JJ,// - Adjective
	JJR,// - Adjective, comparative
	JJS,// - Adjective, superlative
	LS,// - List item marker
	MD,// - Modal
	NN,// - Noun, singular or mass
	NNS,// - Noun, plural
	NNP,// - Proper noun, singular
	NNPS,// - Proper noun, plural
	SQUOT, //single quotes
	DQUOTE, //double quotes
	WDQUOTE, //weird double quotes
	WDQUOTE2,//the second weird double quotes
	DOLLAR, //$ sign
	PDT,// - Predeterminer
	POS,// - Possessive ending
	PRP,// - Personal pronoun
	PRP$,// - Possessive pronoun (prolog version PRP-S)
	RB,// - Adverb
	RBR,// - Adverb, comparative
	RBS,// - Adverb, superlative
	RP,// - Particle
	SYM,// - Symbol
	TO,// - to
	UH,// - Interjection
	VB,// - Verb, base form
	VBD,// - Verb, past tense
	VBG,// - Verb, gerund or present participle
	VBN,// - Verb, past participle
	VBP,// - Verb, non-3rd person singular present
	VBZ,// - Verb, 3rd person singular present
	WDT,// - Wh-determiner
	WP,// - Wh-pronoun
	WP$,// - Possessive wh-pronoun (prolog version WP-S)
	WRB;// - Wh-adverb;
	
	//private static String RRB = "-RRB";
	
	public static final List<TreebankPosTags> tbPosList = new ArrayList<TreebankPosTags>();
	static {
		for(TreebankPosTags tbPos : TreebankPosTags.values()){
			tbPosList.add(tbPos);
		}
	}
	
	public static final TreebankPosTags convert(String pos){
		TreebankPosTags tbpt = null;
		if(pos.equals(":")){
			tbpt = TreebankPosTags.valueOf("COLON");
		}else if(pos.equals("-")){
			tbpt = TreebankPosTags.valueOf("DASH");
		}else if(pos.equals("'")){
			tbpt = TreebankPosTags.valueOf("SQUOTE");
		}else if(pos.equals("''")){
			tbpt = TreebankPosTags.valueOf("WDQUOTE2");
		}
		else if(pos.equals("``")){
			tbpt = TreebankPosTags.valueOf("WDQUOTE");
		}else if(pos.equals("\"")){
			tbpt = TreebankPosTags.valueOf("DQUOTE");
		}else if(pos.equals(",")){
			tbpt = TreebankPosTags.valueOf("COMMA");
		}else if(pos.equals(".")){
			tbpt = TreebankPosTags.valueOf("PERIOD");
		}else if(pos.equals("$")){
			tbpt = TreebankPosTags.valueOf("DOLLAR");
		}else if(pos.equals("-RRB-")){
			tbpt = TreebankPosTags.valueOf("RRB");
		}else if(pos.equals("-LRB-")){
			tbpt = TreebankPosTags.valueOf("LRB");
		}
		return tbpt;
	}
	
	//no difference between tbPosList2 and tbPosList3
	public static final List<String> tbPosList2 = new ArrayList<String>();
	static {
		for(TreebankPosTags tbPos : TreebankPosTags.values()){
			if(tbPos.toString().equals("COLON")){
				tbPosList2.add(":");
			}else if(tbPos.toString().equals("DASH")){
				tbPosList2.add("-");
			}else if(tbPos.toString().equals("SQUOTE")){
				tbPosList2.add("'");
			}else if(tbPos.toString().equals("WDQUOTE")){
				tbPosList2.add("``");
			}else if(tbPos.toString().equals("WDQUOTE2")){
				tbPosList2.add("''");
			}else if(tbPos.toString().equals("DQUOTE")){
				tbPosList2.add("\"");
			}else if(tbPos.toString().equals("COMMA")){
				tbPosList2.add(",");
			}else if(tbPos.toString().equals("PERIOD")){
				tbPosList2.add(".");
			}else if(tbPos.toString().equals("DOLLAR")){
				tbPosList2.add("$");
			}else if(tbPos.toString().equals("RRB")){
				tbPosList2.add("-RRB-");
			}else if(tbPos.toString().equals("LRB")){
				tbPosList2.add("-LRB-");
			}else{
				tbPosList2.add(tbPos.toString());
			}
		}
	}
	
	public static final List<String> tbPosList3 = new ArrayList<String>();
	static {
		for(TreebankPosTags tbPos : TreebankPosTags.values()){
			if(tbPos.toString().equals("COLON")){
				tbPosList3.add(":");
			}else if(tbPos.toString().equals("DASH")){
				tbPosList3.add("-");
			}else if(tbPos.toString().equals("SQUOTE")){
				tbPosList3.add("'");
			}else if(tbPos.toString().equals("DQUOTE")){
				tbPosList3.add("\"");
			}else if(tbPos.toString().equals("WDQUOTE")){
				tbPosList3.add("``");
			}else if(tbPos.toString().equals("WDQUOTE2")){
				tbPosList3.add("''");
			}else if(tbPos.toString().equals("COMMA")){
				tbPosList3.add(",");
			}else if(tbPos.toString().equals("PERIOD")){
				tbPosList3.add(".");
			}else if(tbPos.toString().equals("DOLLAR")){
				tbPosList3.add("$");
			}else if(tbPos.toString().equals("RRB")){
				tbPosList3.add("-RRB-");
			}else if(tbPos.toString().equals("LRB")){
				tbPosList3.add("-LRB-");
			}
			else{
				tbPosList3.add(tbPos.toString());
			}
		}
	}
}
