package utils;

import java.util.ArrayList;
import java.util.List;

public enum Demonstratives {
	ths,that,these,those;
	
	public static final List<Demonstratives> demoList = new ArrayList<Demonstratives>();
	static {
		for(Demonstratives demo : Demonstratives.values()){
			demoList.add(demo);
		}
	}
	
	public static final Demonstratives convert(String demon){
		Demonstratives special = null;
		if(demon.equals("this")){
			special = Demonstratives.valueOf("ths");
		}
		return special;
	}
	
	
	public static final List<String> demoList2 = new ArrayList<String>();
	static {
		for(Demonstratives demo : Demonstratives.values()){
			if(demo.toString().equals("ths")){
				demoList2.add("this");
			}else{
				demoList2.add(demo.toString());				
			}
		}
	}
}
