package utils;

import java.util.ArrayList;
import java.util.List;

public enum ThirdReflexives {
	himself,herself,itself,themselves;
	
	public static final List<ThirdReflexives> thirdRefList = new ArrayList<ThirdReflexives>();
	static {
		for(ThirdReflexives thirdRefoun : ThirdReflexives.values()){
			thirdRefList.add(thirdRefoun);
		}
	}
	
	public static final List<String> thirdRefList2 = new ArrayList<String>();
	static {
		for(ThirdReflexives thirdRefoun : ThirdReflexives.values()){
			thirdRefList2.add(thirdRefoun.toString());
		}
	}
}
