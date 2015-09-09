package utils;

import java.util.ArrayList;
import java.util.List;

public enum Abbreviations {
	UDSD, UDND;
	//public static final List<TreebankPosTags> tbPosList = new ArrayList<TreebankPosTags>();
	public static final List<String> tbPosList = new ArrayList<String>();
	static {
		for(Abbreviations abbrev : Abbreviations.values()){
			tbPosList.add(abbrev.toString());
		}
	}
	
	public static final Abbreviations convert(String shortW){
		Abbreviations abbrev = null;
		if(shortW.equals("U.S.")){
			abbrev = Abbreviations.valueOf("UDSD");
		}else if(shortW.equals("U.N.")){
			abbrev = Abbreviations.valueOf("UDND");
		}
		return abbrev;
	}
	
	public static final List<String> abbrevList = new ArrayList<String>();
	static {
		for(Abbreviations abbrev : Abbreviations.values()){
			if(abbrev.toString().equals("UDSD")){
				abbrevList.add("U.S.");
			}else if(abbrev.toString().equals("UDND")){
				abbrevList.add("U.N.");
			}
		}
	}
	
}
