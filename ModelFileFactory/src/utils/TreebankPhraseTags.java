package utils;

import java.util.ArrayList;
import java.util.List;

public enum TreebankPhraseTags {
	ADJP,// - Adjective Phrase.
	ADVP,// - Adverb Phrase.
	CONJP,// - Conjunction Phrase.
	FRAG,// - Fragment.
	INTJ,// - Interjection. Corresponds approximately to the part-of-speech tag UH.
	LST,// - List marker. Includes surrounding punctuation.
	NAC,// - Not a Constituent; used to show the scope of certain prenominal modifiers within an NP.
	NP,// - Noun Phrase.
	NX,// - Used within certain complex NPs to mark the head of the NP. Corresponds very roughly to N-bar level but used quite differently.
	PP,// - Prepositional Phrase.
	PRN,// - Parenthetical.
	PRT,// - Particle. Category for words that should be tagged RP.
	QP,// - Quantifier Phrase (i.e. complex measure/amount phrase); used within NP.
	RRC,// - Reduced Relative Clause.
	UCP,// - Unlike Coordinated Phrase.
	VP,// - Vereb Phrase.
	WHADJP,// - Wh-adjective Phrase. Adjectival phrase containing a wh-adverb, as in how hot.
	WHAVP,// - Wh-adverb Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing a wh-adverb such as how or why.
	WHNP,// - Wh-noun Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing some wh-word, e.g. who, which book, whose daughter, none of which, or how many leopards.
	WHPP,// - Wh-prepositional Phrase. Prepositional phrase containing a wh-noun phrase (such as of which or by whose authority) that either introduces a PP gap or is contained by a WHNP.
	X;// - Unknown, uncertain, or unbracketable. X is often used for bracketing typos and in bracketing the...the-constructions.;
	
	public static final List<TreebankPhraseTags> tbPhraseList = new ArrayList<TreebankPhraseTags>();
	static {
		for(TreebankPhraseTags tbPhrase : TreebankPhraseTags.values()){
			tbPhraseList.add(tbPhrase);
		}
	}
}
