package utils;

import java.util.ArrayList;
import java.util.List;

public enum SecondReflexives {
	yourself,yourselves;
	
	public static final List<SecondReflexives> secondRefList = new ArrayList<SecondReflexives>();
	static {
		for(SecondReflexives secondRef : SecondReflexives.values()){
			secondRefList.add(secondRef);
		}
	}
	
	public static final List<String> secondRefList2 = new ArrayList<String>();
	static {
		for(SecondReflexives secondRef : SecondReflexives.values()){
			secondRefList2.add(secondRef.toString());
		}
	}
}
