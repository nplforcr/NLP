package utils;

import java.util.ArrayList;
import java.util.List;

public enum FemalePronouns {
	she, her, hers,herself ;
	
	public static final List<FemalePronouns> femalePronList = new ArrayList<FemalePronouns>();
	static {
		for(FemalePronouns femalePronoun : FemalePronouns.values()){
			femalePronList.add(femalePronoun);
		}
	}
	
	public static final List<String> femalePronList2 = new ArrayList<String>();
	static {
		for(FemalePronouns femPronoun : FemalePronouns.values()){
			femalePronList2.add(femPronoun.toString().toLowerCase());				
		}
	}
}
