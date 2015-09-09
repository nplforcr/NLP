package utils;

import java.util.ArrayList;
import java.util.List;

public enum SecondPersonPron {
	you, your, yours;
	
	public static final List<SecondPersonPron> secondPerPronList = new ArrayList<SecondPersonPron>();
	static {
		for(SecondPersonPron secondPerPronoun : SecondPersonPron.values()){
			secondPerPronList.add(secondPerPronoun);
		}
	}
	
	public static final List<String> secondPerPronList2 = new ArrayList<String>();
	static {
		for(SecondPersonPron secondPerPronoun : SecondPersonPron.values()){
			secondPerPronList2.add(secondPerPronoun.toString());
		}
	}
}
