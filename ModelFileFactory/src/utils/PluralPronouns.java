package utils;

import java.util.ArrayList;
import java.util.List;

public enum PluralPronouns {
	they, their, them, these, those,themselves,yourselves,we,us,our,ourselves,which,shortUs;
	
	
	public static final List<PluralPronouns> pluralPronList = new ArrayList<PluralPronouns>();

	static {
		for(PluralPronouns pluralPronoun : PluralPronouns.values()){
			pluralPronList.add(pluralPronoun);
		}
	}
	
	public static final PluralPronouns convert(String plPron){
		PluralPronouns special = null;
		if(plPron.equals("'s")){
			special = PluralPronouns.valueOf("shortUs");
		}
		return special;
	}
	
	public static final List<String> pluralPronList2 = new ArrayList<String>();
	static {
		for(PluralPronouns plPronoun : PluralPronouns.values()){
			if(plPronoun.toString().equals("shortUs")){
				pluralPronList2.add("'s");
			}else{
				pluralPronList2.add(plPronoun.toString());				
			}
			
		}
	}
}
