package utils;

import java.util.ArrayList;
import java.util.List;

public enum WhDemonstratives {
	which,whose,who,whom;
	
	public static final List<WhDemonstratives> whdemoList = new ArrayList<WhDemonstratives>();
	static {
		for(WhDemonstratives whdemo : WhDemonstratives.values()){
			whdemoList.add(whdemo);
		}
	}
	
	
	public static final List<String> whdemoList2 = new ArrayList<String>();
	static {
		for(WhDemonstratives whdemo : WhDemonstratives.values()){
				whdemoList2.add(whdemo.toString());				
		}
	}
}
