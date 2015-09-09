package corpusProcessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import utils.Pair;


public class TokenXMLFileBuilder {

	/**@author leonlee
	 * @param args
	 */
	
	List<Pair> wordPosPairList; 
	
	//"/project/nlp/dingcheng/nlplab/corpora/corefann/wxml/access.wxml"
	public TokenXMLFileBuilder(){
		wordPosPairList = new ArrayList<Pair>();	
	}
	
	
	public void buildWordPosPair(BufferedReader taggedFile){
		String taggedLine = "";
		String [] taggedLineArray;
		try {
			while((taggedLine=taggedFile.readLine())!=null){
				taggedLineArray = taggedLine.split(" ");
				for(int i=0;i<taggedLineArray.length;i++){
					String[] wordPos = taggedLineArray[i].split("/");
					//System.out.println(taggedLineArray[i]);
					Pair wordPosPair = new Pair(wordPos[0],wordPos[1]);
					wordPosPairList.add(wordPosPair);
				}
			}
			//System.out.println(wordPosPairList.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("wordPosPairList: "+wordPosPairList.size());
	}

	@SuppressWarnings("unchecked")
	public void extractPosInfor(BufferedReader corefFile) {
		// String[] accHead = new String[2];
		String corefLine = " ";
		int countAllCoref = 0;
		// pwDJFile.println(bfDJFile.readLine());
		StringBuffer sbCoref = new StringBuffer();
		try {
			// sbCoref.append(brDJFile.readLine() + "\n");
			//InputStream inputStream = null;
			String displayDate = " ";
			while ((corefLine = corefFile.readLine()) != null) {
				//System.out.println(corefLine);
				countAllCoref++;
				sbCoref.append(corefLine + "\n");
				//inputStream = new ByteArrayInputStream(sbCoref.toString().getBytes());
			}
			SAXBuilder saxBuilder = new SAXBuilder();
			try {
				Document doc = saxBuilder.build(new StringReader(sbCoref.toString()));
				Element root = doc.getRootElement();
				List<Element> childrenPara = root.getChildren();
				Queue<String> queueCoref = new LinkedList<String>();
				int countCoref = 0;
				int countCorefAll = 0;
				int countToken = 0;
				List<String> tokenIdList = new ArrayList<String>();
				HashMap<String,Integer> replaceRefIndexHM = new HashMap<String,Integer>();
				for(int i=0;i<childrenPara.size();i++){
					Element childPara = childrenPara.get(i);
					List<Element> childrenSent = childPara.getChildren();
					for(int j=0;j<childrenSent.size();j++){
						Element childSent = childrenSent.get(j);
	//					System.out.println(childSent.getText());
						Iterator itr = childSent.getDescendants();
						StringBuffer sbSent = new StringBuffer();
						StringTokenizer stSent = null; 
						while(itr.hasNext()){
							countToken++;
							Content c = (Content) itr.next();
								//String token =((Element)c).getText();
							if(c instanceof Element){
								//System.out.println(((Element)c).getText()+" ");
							}else{
								sbSent.append(c.getValue());
								//System.out.print(c.getValue()+" // ");
							}
						}
						stSent=new StringTokenizer(sbSent.toString().trim(), " \t");
						while(stSent.hasMoreTokens()){
							String nextStr = stSent.nextToken();
							if(nextStr.startsWith("(")&&!nextStr.endsWith(")")){
								String lrb = "(";
								tokenIdList.add(lrb);
								String remStr = nextStr.substring(1);
								tokenIdList.add(remStr);
							}else if(!nextStr.startsWith("(")&&nextStr.endsWith(")")){
								String begStr = nextStr.substring(0,nextStr.length()-1);
								tokenIdList.add(begStr);
								String rrb = ")";
								tokenIdList.add(rrb);
							}else if(nextStr.startsWith("(")&&nextStr.endsWith(")")){
								String lrb = "(";
								tokenIdList.add(lrb);
								String remStr = nextStr.substring(1,nextStr.length()-1);
								tokenIdList.add(remStr);
								String rrb = ")";
								tokenIdList.add(rrb);
							}else if(nextStr.startsWith("\"")&& !nextStr.endsWith("\"")){
								String quote = "\"";
								tokenIdList.add(quote);
								String remStr = nextStr.substring(1);
								tokenIdList.add(remStr);
							}else if(!nextStr.endsWith("\"") && nextStr.endsWith("\"")){
								String begStr = nextStr.substring(0,nextStr.length()-1);
								tokenIdList.add(begStr);
								String quote = "\"";
								tokenIdList.add(quote);
							}else if(nextStr.startsWith("\"") && nextStr.endsWith("\"")){
								String begquote = "\"";
								tokenIdList.add(begquote);
								String begStr = nextStr.substring(1,nextStr.length());
								tokenIdList.add(begStr);
								String endquote = "\"";
								tokenIdList.add(endquote);
							}else if(nextStr.contains(".")){
								int indDot=nextStr.indexOf(".");
								String front = nextStr.substring(0,indDot);
								tokenIdList.add(front);
								String back = nextStr.substring(indDot+1);
								tokenIdList.add(back);
							}else if(nextStr.startsWith("-")){
								String dash = "-";
								tokenIdList.add(dash);
								tokenIdList.add(nextStr.substring(1));
							}
							
							else{
								tokenIdList.add(nextStr);
							}
							
						}
						
						//System.out.println(sbSent.toString());
					}
				}
				
				System.out.println("tokenIdList size: "+tokenIdList.size());
				for(int i=0;i<tokenIdList.size();i++){
					System.out.println(tokenIdList.get(i));
				}
				
				System.out.println(countToken);
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TokenXMLFileBuilder tokenXmlFileBuilder = new TokenXMLFileBuilder();
		BufferedReader brTagged;
		try {
			brTagged = new BufferedReader(new FileReader(new File(args[0])));
			BufferedReader brXMLCorpus = new BufferedReader(new FileReader(new File(args[1])));
			tokenXmlFileBuilder.buildWordPosPair(brTagged);
			tokenXmlFileBuilder.extractPosInfor(brXMLCorpus);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
