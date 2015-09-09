package utils;

import java.util.ArrayList;
import java.util.List;

public enum FSPersonPron {
	you, your, yours, I, me,my, mine, we, our, ours,us,shortUs;
	
	public static final List<FSPersonPron> fsPronList = new ArrayList<FSPersonPron>();
	static {
		for(FSPersonPron fsPronoun : FSPersonPron.values()){
			fsPronList.add(fsPronoun);
		}
	}
	
	public static final FSPersonPron convert(String fsPron){
		FSPersonPron special = null;
		if(fsPron.equals("'s")){
			special = FSPersonPron.valueOf("shortUs");
		}
		return special;
	}
	
	public static final List<String> fsPronList2 = new ArrayList<String>();
	static {
		for(FSPersonPron fsPronoun : FSPersonPron.values()){
			if(fsPronoun.toString().equals("shortUs")){
				fsPronList2.add("'s");
			}else{
				fsPronList2.add(fsPronoun.toString());				
			}
			
		}
	}
}
