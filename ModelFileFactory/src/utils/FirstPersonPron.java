package utils;

import java.util.ArrayList;
import java.util.List;

public enum FirstPersonPron {
	i, me, mine,my, we,our, ours, us;
	
	public static final List<FirstPersonPron> firstPerPronList = new ArrayList<FirstPersonPron>();
	static {
		for(FirstPersonPron firstPerPronoun : FirstPersonPron.values()){
			firstPerPronList.add(firstPerPronoun);
		}
	}
	
	public static final List<String> firstPerPronList2 = new ArrayList<String>();
	static {
		for(FirstPersonPron firstPerPronoun : FirstPersonPron.values()){
			firstPerPronList2.add(firstPerPronoun.toString());
		}
	}
}
