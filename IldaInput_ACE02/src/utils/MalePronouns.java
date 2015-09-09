package utils;

import java.util.ArrayList;
import java.util.List;

public enum MalePronouns {
	he, his, him,himself;
	
	public static final List<MalePronouns> malePronList = new ArrayList<MalePronouns>();
	static {
		for(MalePronouns malePronoun : MalePronouns.values()){
			malePronList.add(malePronoun);
		}
	}
	
	public static final List<String> malePronList2 = new ArrayList<String>();
	static {
		for(MalePronouns neuPronoun : MalePronouns.values()){
			malePronList2.add(neuPronoun.toString().toLowerCase());				
		}
	}
}
