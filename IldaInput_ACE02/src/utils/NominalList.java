package utils;

import java.util.ArrayList;
import java.util.List;

public enum NominalList {
	alms,amends,analysis,antibiotics,acoustics,acrobatics,aeronautics,aesthetics,appendicitis ,arms,arthritis,ashes,atherosclerosis,athletics,authorities,barracks,belongings,besides ,borrowings ,brains,classes,catalysis ,clothes,contents,corps,diabetes,diagnosis,didactics,dietetics,dynamics,earnings,economics,electronics,encephalitis,energetics,faeces,gunds,gains,gastritis,genetics,glasses,goods,graphics,gymnastics,hepatitis,heroics,hers,homiletics,hydraulics,hypnosis,imports,inessentials,inwards,kinetics,laryngitis,lines,linguistics,logistics,mathematics,means,measles,mechanics,meningitis,metaphysics,mists,mnemonics,morphemics,mumps,nephritis,neurosis,news,obstetrics,odds,ofttimes,olympics,
	optics,orthodontics,outdoors,outskirts,papers,paralysis,parenthesis,particulars,pediatrics,peritonitis,pertussis,pharyngitis,phonetics,photosynthesis,physics,plastics,pleadings,poliomyelitis,possessions,proceeds,psychosis,rabies,refinements,relics,remains,remedies,reminiscences,rhinitis,riches,rickets,rudiments,sales,sands,savings,sepsis,shavings,shorts,smalls,pirits,sports,statics,statistics,stores,strategics,surroundings,synopsis,synthesis,syphilis,tactics,terms,tetanus,thanks,theatricals,thesis,thews,throes,thrombosis,times,tonsillitis,tracheitis,troops,tropics,typhus,valuables,woods,workings,works;
	public static final List<NominalList> nominalList1 =  new ArrayList<NominalList>();
	static {
		for(NominalList nominalList : NominalList.values()){
			nominalList1.add(nominalList);
		}
	}
	
	public static final List<String> nominalList2 =  new ArrayList<String>();
	static {
		for(NominalList nominalList : NominalList.values()){
			nominalList2.add(nominalList.toString());
		}
	}
}
