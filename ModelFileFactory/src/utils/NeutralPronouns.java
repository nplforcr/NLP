package utils;

import java.util.ArrayList;
import java.util.List;

public enum NeutralPronouns {
	it, its,itself, they, their, them, these, those,themselves,we, our, myself,ourselves,yourself,yourselves, us, shortus, you,your,yours,I, my,me,mine, which,ths,that;
	
	public static final List<NeutralPronouns> nePronList = new ArrayList<NeutralPronouns>();
	static {
		for(NeutralPronouns neutralPronoun : NeutralPronouns.values()){
			nePronList.add(neutralPronoun);
		}
	}
	
	public static final NeutralPronouns convert(String neuPron){
		NeutralPronouns special = null;
		if(neuPron.equals("this")){
			special = NeutralPronouns.valueOf("ths");
		}else if(neuPron.equals("'s")){
			special = NeutralPronouns.valueOf("shortus");
		}
		return special;
	}
	
	public static final List<String> neuPronList2 = new ArrayList<String>();
	static {
		for(NeutralPronouns neuPronoun : NeutralPronouns.values()){
			if(neuPronoun.toString().equals("ths")){
				neuPronList2.add("this");
			}else if(neuPronoun.toString().equals("'s")){
				neuPronList2.add("shortus");
			}else{
				neuPronList2.add(neuPronoun.toString().toLowerCase());				
			}
		}
	}
}
