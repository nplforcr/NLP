package utils;

import java.util.ArrayList;
import java.util.List;

public enum OtherPronouns {
	you,yourself,youselves,yours,your,i,me,my,mine,we,our,ourselves,us,aposHis;
	
	
	public static final List<OtherPronouns> otherPronList = new ArrayList<OtherPronouns>();
	static {
		for(OtherPronouns otherPronoun : OtherPronouns.values()){
			otherPronList.add(otherPronoun);
		}
	}
	
	public static final List<String> otherPronList2 = new ArrayList<String>();
	static {
		for(OtherPronouns otherPronoun : OtherPronouns.values()){
			if(otherPronoun.equals(aposHis)){
				otherPronList2.add("'s");
			}else{
				otherPronList2.add(otherPronoun.toString());
			}
		}
	}
	
	public static final OtherPronouns convert(String pronoun){
		OtherPronouns otherPron = null;
		if(pronoun.equals("'s")){
			otherPron = OtherPronouns.valueOf("aposHis");
		}
		return otherPron;
	}
}
