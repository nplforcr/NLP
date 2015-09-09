package utils;

import java.util.ArrayList;
import java.util.List;

public enum OtherPronouns {
	which,that,ths,these,those,whose,who,whom;

	public static final List<OtherPronouns> otherPronList = new ArrayList<OtherPronouns>();
	static {
		for(OtherPronouns otherPronoun : OtherPronouns.values()){
			otherPronList.add(otherPronoun);
		}
	}

	public static final OtherPronouns convert(String otherPron){
		OtherPronouns special = null;
		if(otherPron.equals("this")){
			special = OtherPronouns.valueOf("ths");
		}
		return special;
	}
	
	public static final List<String> otherPronList2 = new ArrayList<String>();
	static {
		for(OtherPronouns otherPron : OtherPronouns.values()){
			if(otherPron.toString().equals("ths")){
				otherPronList2.add("this");
			}else{
				otherPronList2.add(otherPron.toString());
			}
		}
	}
}
