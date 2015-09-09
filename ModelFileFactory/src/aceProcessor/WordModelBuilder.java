package aceProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class WordModelBuilder {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File fileTag = new File(args[0]);
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		File dirOutput = new File(args[1]);
		DocumentBuilder builder;
		PrintWriter pwOutput = new PrintWriter(new FileWriter(new File(dirOutput,fileTag.getName()+".word.model"),true));
		if(fileTag.isDirectory()){
			File[] fileArray = fileTag.listFiles();
			for(int i=0;i<fileArray.length;i++){
				if(fileArray[i].getName().endsWith(".sgm")){
					String fileName = fileArray[i].getName();
					StringBuffer sbInput=new StringBuffer();
					ProcessACEDevJdom processACEDevJdom = new ProcessACEDevJdom();
					sbInput=processACEDevJdom.readSgmFile(fileArray[i]);
					try {
						builder = factory.newDocumentBuilder();
						try {
							document = builder.parse(new InputSource(new StringReader(sbInput.toString())));
							NodeList textNodeList=document.getElementsByTagName("TEXT");
							StringBuffer sbText=new StringBuffer();
							for(int j=0;j<textNodeList.getLength();j++){
								Node ithNode=textNodeList.item(j);
								sbText.append(ithNode.getTextContent());
							}
							//System.out.println(sbText);
							List<Sentence <? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(sbText.toString()));
							for (Sentence<? extends HasWord> sentence : sentences) {
								//System.out.println("sentence = "+sentence);
								for(int k=0;k<sentence.size();k++){
									String word=sentence.get(k).word();
									//System.out.println(word);
									if(word.contains(" ")){
										word=word.replaceAll("\\s", "");
									}
									
									if(word.contains("\\")){
										word=word.replace("\\", "");
									}
									pwOutput.println("Word : "+word.toLowerCase());
								}
							}
							pwOutput.println("Word : unkword");
							pwOutput.println("Word : eos");

						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
			pwOutput.println("Word : nato");
			pwOutput.println("Word : unscum");
			pwOutput.println("Word : wto");
			pwOutput.close();
		}
	}

}
