package utils;

import java.util.ArrayList;
import java.util.List;

public enum ThirdPersonPron {
	he, his, him,she, her,it, its, hers,they, their, them;
	
	public static final List<ThirdPersonPron> thirdPerPronList = new ArrayList<ThirdPersonPron>();
	static {
		for(ThirdPersonPron thirdPerPronoun : ThirdPersonPron.values()){
			thirdPerPronList.add(thirdPerPronoun);
		}
	}
	
	public static final List<String> thirdPerPronList2 = new ArrayList<String>();
	static {
		for(ThirdPersonPron thirdPerPronoun : ThirdPersonPron.values()){
			thirdPerPronList2.add(thirdPerPronoun.toString());
		}
	}
}
