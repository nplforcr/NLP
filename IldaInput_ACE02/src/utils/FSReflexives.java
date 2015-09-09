package utils;

import java.util.ArrayList;
import java.util.List;

public enum FSReflexives {
	myself,yourself,ourselves;
	
	public static final List<FSReflexives> fsRefList = new ArrayList<FSReflexives>();
	static {
		for(FSReflexives fsRefpronoun : FSReflexives.values()){
			fsRefList.add(fsRefpronoun);
		}
	}
	
	public static final List<String> fsRefList2 = new ArrayList<String>();
	static {
		for(FSReflexives fsRefoun : FSReflexives.values()){
			fsRefList2.add(fsRefoun.toString());
		}
	}
}
