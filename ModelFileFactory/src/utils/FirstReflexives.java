package utils;

import java.util.ArrayList;
import java.util.List;

public enum FirstReflexives {
	myself,ourselves;
	
	public static final List<FirstReflexives> firstRefList = new ArrayList<FirstReflexives>();
	static {
		for(FirstReflexives firstRef : FirstReflexives.values()){
			firstRefList.add(firstRef);
		}
	}
	
	public static final List<String> firstRefList2 = new ArrayList<String>();
	static {
		for(FirstReflexives firstRef : FirstReflexives.values()){
			firstRefList2.add(firstRef.toString());
		}
	}
}
