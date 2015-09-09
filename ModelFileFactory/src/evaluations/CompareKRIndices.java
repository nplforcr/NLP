package evaluations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import aceProcessor.ProcessACEDevJdom;

public class CompareKRIndices {
	
	List<String> bsTokenResponseList;
	List<String> bsTokenKeyList;
	
	
	public CompareKRIndices(){
		bsTokenResponseList = new ArrayList<String>();
		bsTokenKeyList = new ArrayList<String>();
	}
	
	public void getResponseData(File fileTag) throws ParserConfigurationException, SAXException, IOException{
		ProcessACEDevJdom processACEJdom = new ProcessACEDevJdom(fileTag);
		System.out.println(fileTag+" "+fileTag.getParentFile().getParent());
		//System.out.println(dirOuttext.getAbsolutePath());
		String fileName = fileTag.getName();
		ProcessACEDevJdom processACESgmJdom = new ProcessACEDevJdom();
		StringBuffer sbDev = processACESgmJdom.readSgmFile(fileTag);
		// StringBuffer
		//textBuffer=processACESgmJdom.createInputFiles(pwOuttext,sbDev.toString());
		StringBuffer textBuffer = processACESgmJdom
		.createInputFiles(sbDev.toString());
		List<String> tokenList = processACESgmJdom.tokenList;
		//System.out.println("tokenList.get(0): " + tokenList.get(0));
		ProcessACEDevJdom processACEJdom2 = new ProcessACEDevJdom(new File(fileTag.getParent(),fileTag.getName()+".tmx.rdc.xml"));
		processACEJdom2.extractEntity();
		bsTokenResponseList=processACEJdom2.debugIndex(textBuffer, tokenList);
	}
	
	public void getKeyData(String inputDir) throws IOException, ParserConfigurationException, SAXException{
		PrepareACEKey prepareACEKey=new PrepareACEKey();
		prepareACEKey.process(inputDir);
		//prepareACEKey.fillMentionList();
		prepareACEKey.getKeyData(bsTokenKeyList);
	}
	
	public void printKEIndcies(){
		System.out.println(bsTokenResponseList.size()+" "+bsTokenKeyList.size());
		for(int i=0;i<bsTokenKeyList.size();i++){
			System.out.println("key: "+bsTokenKeyList.get(i)+" response: "+bsTokenResponseList.get(i));
		}
	}
	

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		CompareKRIndices comKRInds = new CompareKRIndices();
		File fileTagSgm = new File(args[0]);
		String fileTagXml = args[1];
		comKRInds.getResponseData(fileTagSgm);
		comKRInds.getKeyData(fileTagXml);
		comKRInds.printKEIndcies();
	}

}
