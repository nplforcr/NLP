package utils;

import java.util.ArrayList;
import java.util.List;

public enum SingularPronouns {
	he, his, him,himself, she, her, hers,herself, it, its,itself,I, my,mine,me,myself,yourself,which, that, you, your,ths;
	
	public static final List<SingularPronouns> singularPronList = new ArrayList<SingularPronouns>();
	static {
		for(SingularPronouns singularPronoun : SingularPronouns.values()){
			singularPronList.add(singularPronoun);
		}
	}
	
	public static final SingularPronouns convert(String singPron){
		SingularPronouns special = null;
		if(singPron.equals("this")){
			special = SingularPronouns.valueOf("ths");
		}
		return special;
	}
	
	public static final List<String> singPronList2 = new ArrayList<String>();
	static {
		for(SingularPronouns singPronoun : SingularPronouns.values()){
			if(singPronoun.toString().equals("ths")){
				singPronList2.add("this");
			}else{
				singPronList2.add(singPronoun.toString());				
			}
			
		}
	}
}
