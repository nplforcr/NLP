package aceProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import annotation.Mention;
import annotation.NamedEntity;

import readers.AceReader;
import utils.Pair;

public class BuildIgnModel extends AceReader {
	
	List<Pair> mentionSpanList;
	HashMap<Pair,String> bytespanIdM;
	
	List<String> pronounList;
	List<String> thirdRefList;
	List<String> maPronounsList;
	List<String> fePronounsList;
	List<String> nePronounsList;
	List<String> singPronounList;
	List<String> pluralList;

	/**
	 * constructor, a few lists of pronouns are constructed
	 */
	public BuildIgnModel(){
		String[] thirdRefArray = {"himself","herself","itself","themselves"};
		String[] thirdPronArray = {"he", "his", "him","she", "her","it", "its", "hers","they", "their", "them",};
		String[] maPronounsArray = { "he", "his", "him","himself"};
		String[] fePronounsArray = { "she", "her", "hers","herself" };
		String[] nePronounsArray = { "it", "its","itself", "they", "their", "them", "these", "those","themselves" };
		String[] singPronounArray = { "this", "that", "he", "his", "him","himself", "she", "her", "hers","herself", "it", "its","itself" };
		String[] pluralArray = { "they", "their", "them", "these", "those","themselves"};
		thirdRefList = Arrays.asList(thirdRefArray);
		pronounList = Arrays.asList(thirdPronArray);
		maPronounsList = Arrays.asList(maPronounsArray);
		//System.out.println("true or false:::::: "+maPronounsList.contains("he"));
		fePronounsList = Arrays.asList(fePronounsArray);
		nePronounsList = Arrays.asList(nePronounsArray);
		singPronounList = Arrays.asList(singPronounArray);
		pluralList = Arrays.asList(pluralArray);
	}
	
	public void processDir (String dirName,String outDir) throws IOException, ParserConfigurationException, SAXException{

		File dFile =  new File (dirName);
		String outStr = dFile.getName();
		System.out.println(outStr);
//		File outDirFile = new File(outDir,outStr+"Model");
//		if(!outDirFile.exists()){
//			outDirFile.mkdir();
//		}
		PrintWriter pwIndModel = new PrintWriter(new FileWriter(new File(outDir,outStr+".ind.model"), true));
		PrintWriter pwGenModel = new PrintWriter(new FileWriter(new File(outDir,outStr+".gen.model"), true));
		PrintWriter pwNumModel = new PrintWriter(new FileWriter(new File(outDir,outStr+".num.model"), true));
		if( dFile.isDirectory()){
			File files[]  =  dFile.listFiles();
			for(int i=0; i< files.length; i++){
				if(files[i].isFile()){
					String filename = files[i].getName();
					if(filename.endsWith(".xml")){
						System.out.println("xml file name: "+filename);
						processDocument(null,files[i].getAbsolutePath());
						fillMentionList();
						this.printIndModel(pwIndModel);
						this.printGenNumModel(pwGenModel,pwNumModel);
					}
				} else if(files[i].isDirectory()){
					processDir(files[i].getAbsolutePath(),outDir);
				}
			}
		}
		pwIndModel.close();
		pwGenModel.close();
		pwNumModel.close();
	}
	
	public void process (String dirName,String outputFileName) throws IOException, ParserConfigurationException, SAXException{

		File dFile =  new File (dirName);
		if( dFile.isDirectory()){
			File files[]  =  dFile.listFiles();
			for(int i=0; i< files.length; i++){
				if(files[i].isFile()){
					String filename1 = files[i].getName();
					if(filename1.endsWith(".xml")){
						System.out.println("xml file name: "+filename1);
						processDocument(null,files[i].getAbsolutePath());
						File indModelFile = new File(outputFileName);
						if(!indModelFile.exists()){
							indModelFile.mkdir();
						}
						PrintWriter pwIndModelInput = new PrintWriter(new FileWriter(new File(indModelFile,dFile.getName()+".indmodel"),true));
					}
				} else if(files[i].isDirectory()){
					processDir(files[i].getAbsolutePath(),outputFileName);
				}
			}
		}
	}
	
	/**
	 * 
	 */
	public void fillMentionList(){
		bytespanIdM = new HashMap<Pair,String>();
		mentionSpanList = new ArrayList<Pair>();
		//idNeM;
		//idMentionM;
		Iterator<String> menIter = idMentionM.keySet().iterator();
		while(menIter.hasNext()){
			String menId = menIter.next();
			Mention mention = idMentionM.get(menId);
			//System.out.println(mention.getType());
			//int[] bytespan = new int[2];
			//bytespan[0]=mention.getExtentSt();
			//bytespan[1]=mention.getExtentEd();
			Pair bytespan = new Pair(mention.getExtentSt(),mention.getExtentEd());
			mentionSpanList.add(bytespan);
			bytespanIdM.put(bytespan, menId);
		}
	}
	
	//this method seems to be wrong, NEW should refer to words which are not pronouns
	//I will consider this method later. But it may be OK to use this ratio for the moment
	public void printGenNumModel(PrintWriter pwGenModel,PrintWriter pwNumModel){
		Iterator<String> neIter = idNeM.keySet().iterator();
		while(neIter.hasNext()){
			String neId = neIter.next();
			NamedEntity ne = idNeM.get(neId);
			ArrayList<Mention> menList = ne.getMentions();
			for(int i=0;i<menList.size();i++){
				Mention ithMention = menList.get(i);
				String menType = ithMention.getType();
				if(menType.equalsIgnoreCase("pronoun")){
					String word = ithMention.getHeadCoveredText().toLowerCase();
					word = word.substring(11,word.length()-2);
					//System.out.println(word+"++++++++++++++++++++++++++++++++");
					if(maPronounsList.contains(word)){
						//System.out.println(word+"++++++++++ inside mapronouns +++++++++");
						for(int j=0;j<menList.size();j++){
							pwGenModel.println("Gen old : MAS");
						}
					}else if(fePronounsList.contains(word)){
						for(int j=0;j<menList.size();j++){
							pwGenModel.println("Gen old : FAM");
						}
					}else{
						for(int j=0;j<menList.size();j++){
							pwGenModel.println("Gen NEW : NEU");
						}
					}
					
					if(pluralList.contains(word)){
						for(int j=0;j<menList.size();j++){
							pwNumModel.println("Num old : PLUR");
						}
					}else{
						for(int j=0;j<menList.size();j++){
							pwNumModel.println("Num NEW : SING");
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param pwIndModel loop through sorted mentionSpanList and 
	 * find the distance between pronouns and their antecendents.
	 * Print them into model files
	 */
	
	public void printIndModel(PrintWriter pwIndModel){
		Collections.sort(mentionSpanList);
		//String[] temNeIdArray = new String[6];
		List<String> temNeIdList = new ArrayList<String>();
		for(int i=0;i<mentionSpanList.size();i++){
			Pair bytespan = mentionSpanList.get(i);
			String menId = bytespanIdM.get(bytespan);
			String neId = menId.substring(0,menId.indexOf("-"));
			Mention mention=idMentionM.get(menId);
			String menType = mention.getType();
			//System.out.println("bytespan: "+bytespan+"menId: "+menId+" neId: "+neId+" menType: "+menType+" mention: "+mention.getHeadCoveredText());
			//this "if" adds new mentions into the temNeIdArray 
			if(!menType.equalsIgnoreCase("pronoun") && temNeIdList.size()<6){
				//temNeIdArray[i] = neId;
				//if necessary, I will divide pronouns into third-person reflexives third-person pronouns and other-person pronouns 
				//#################################################################################################################				
				temNeIdList.add(neId);
			}else{
				//now, menType is pronoun, therefore, we should not add it to temNeIdList, instead, 
				//it is time to print out IndModel here now.
				for(int j=0;j<temNeIdList.size() && temNeIdList.size()<=6;j++){
					//String preNeId = temNeIdArray[j];
					//System.out.println("size of temNeIdList: "+temNeIdList.size());
					String preNeId = temNeIdList.get(j);
					if(preNeId.equals(neId)){						
						//It may be necessary to distinguish types of pronouns here since this change will
						//affect the distributions of third-person pronouns
						pwIndModel.println("Ind old "+ temNeIdList.size()+" : "+j);
						temNeIdList.remove(j);
						temNeIdList.add(neId);
						break;
					}
				}
			}
		}	
	}
	

	/**
	 * @param args
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub

		BuildIgnModel buildIgnModel=new BuildIgnModel();
		String dir= args[0]; //"/home/dingcheng/Documents/OSU_corpora/ace_phase2/data/ace2_train/bnews";
		String dirOut = args[1];
		buildIgnModel.processDir(dir,dirOut);
		//buildIndModel.fillMentionList();
	}

}
