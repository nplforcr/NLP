package corpusProcessor;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;



import javax.sql.rowset.spi.XmlWriter;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import utils.Pair;


import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class TokenXMLCorpusBuilder extends DefaultHandler {

	/**@author leonlee
	 * @param args
	 */
	
	List<Pair> wordPosPairList; 
	PrintWriter pwTokenXML;
	XMLEventWriter writer;
	XmlWriter xmlWriter;
	StringBuffer textBuffer;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DefaultHandler tokenXmlFileBuilder = null;
		File inputFile = new File(args[1]);
		//TokenXMLCorpusBuilder tokenXmlFileBuilder = new TokenXMLCorpusBuilder(args[0],inputFile.getName().substring(0,inputFile.getName().length()-5)+".posxml");
		InputStream inputStream =null;
        InputSource inputSource = null;
        PrintWriter printWriter = null;
        
        try {
        	inputSource = new InputSource(new FileReader(new File(args[1])));
			inputStream=new FileInputStream(new File(args[1]));
			//inputSource = new InputSource(inputStream);
			String line=" "; 
			
			int b;
			do {
			  b = inputStream.read();
			  if (b != -1) {
			    //System.out.println("The next byte is " + b);
			  }
			} while (b != -1);
			inputStream.close();

			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Use an instance of ourselves as the SAX event handler
		tokenXmlFileBuilder = new TokenXMLCorpusBuilder(args[0],inputFile.getName().substring(0,inputFile.getName().length()-5)+".posxml");

        // Use the default (non-validating) parser
        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {
            // Set up output stream
            //out = new OutputStreamWriter(System.out, "UTF8");
            printWriter = new PrintWriter(new File(args[1]),"UTF8");

            // Parse the input
            SAXParser saxParser = factory.newSAXParser();
            //it seems that once the parser calls .parse, the following methods
            //such as startElement, endElement will be called automatically,
            //we don't need to call them explicitly in the main method.
            //probably, the method parse from SaxParser has done that by calling
            //setContentHandler. Probably, that is the function of an interface
            //saxParser.parse(new File(argv[0]), handler);
            saxParser.parse(inputSource, tokenXmlFileBuilder);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        System.exit(0);


		
		BufferedReader brTagged;
		try {
			//brTagged = new BufferedReader(new FileReader(new File(args[0])));
			BufferedReader brXMLCorpus = new BufferedReader(new FileReader(inputFile));
			MaxentTagger tagger = new MaxentTagger(args[2]);
			((TokenXMLCorpusBuilder) tokenXmlFileBuilder).extractPosInfor(brXMLCorpus,tagger);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public TokenXMLCorpusBuilder(String outDirStr, String outFile){
		wordPosPairList = new ArrayList<Pair>();
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		String fileName = "documentXML.xml";
	    //XMLEventWriter writer = outputFactory.createXMLEventWriter(System.out);	
		try {
			File outDir = new File(outDirStr);
			if(!outDir.exists()){
				outDir.mkdirs();
			}
			writer = outputFactory.createXMLEventWriter(new FileWriter(new File(outDirStr, outFile)));
			//pwTokenXML = new PrintWriter(writer); 
			pwTokenXML = new PrintWriter(new FileWriter(new File(outDirStr,outFile)));
			pwTokenXML.println("<?xml version=\"1.0\"?>");
			pwTokenXML.println("<DOCUMENT>");
		}catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
	}
	
	@SuppressWarnings("unchecked")
	public void extractPosInfor(BufferedReader corefFile,MaxentTagger tagger) {
		// String[] accHead = new String[2];
		String corefLine = " ";
		int countAllCoref = 0;
		// pwDJFile.println(bfDJFile.readLine());
		StringBuffer sbCoref = new StringBuffer();
		try {
			while ((corefLine = corefFile.readLine()) != null) {
				//System.out.println(corefLine);
				countAllCoref++;
				sbCoref.append(corefLine + "\n");
				//inputStream = new ByteArrayInputStream(sbCoref.toString().getBytes());
			}
			SAXBuilder saxBuilder = new SAXBuilder();
			try {
				Document doc = saxBuilder.build(new StringReader(sbCoref.toString()));
				//System.out.println(doc);
				Element root = doc.getRootElement();
				System.out.println(root.getName());
				List<Element> childrenPara = root.getChildren();
				Queue<String> queueCoref = new LinkedList<String>();
				int countToken = 0;
				List<String> tokenIdList = new ArrayList<String>();
				HashMap<String,Integer> replaceRefIndexHM = new HashMap<String,Integer>();
				for(int i=0;i<childrenPara.size();i++){
					Element childPara = childrenPara.get(i);
					pwTokenXML.println("<"+childPara.getName()+">");
					System.out.println("<"+childPara.getName()+">");
					List<Element> childrenSent = childPara.getChildren();
					for(int j=0;j<childrenSent.size();j++){
						Element childSent = childrenSent.get(j);
						pwTokenXML.println("<"+childSent.getName()+">");
						System.out.println("<"+childSent.getName()+">");
	//					System.out.println(childSent.getText());
						Iterator itr = childSent.getDescendants();
						StringBuffer sbSent = new StringBuffer();
						StringTokenizer stSent = null; 
						while(itr.hasNext()){
							countToken++;
							Content c = (Content) itr.next();
								//String token =((Element)c).getText();
							if(c instanceof Element){
								pwTokenXML.println("<"+((Element)c).getName());
								List<Attribute> elementList = ((Element)c).getAttributes();
								for(int k=0;k<elementList.size();k++){
									Attribute attribute=elementList.get(k);
									
									///System.out.println(attribute);
								}
								
								System.out.println("<"+((Element)c).getName()+">");
								//pwTokenXML.print(((Element)c).getText());
								//System.out.println(((Element)c).getText());
								//pwTokenXML.println("</"+((Element)c).getName()+">");
								//System.out.println("</"+((Element)c).getName()+">");
								//System.out.println(((Element)c).getText()+" ");
							}else{
								sbSent.append(c.getValue());
								//System.out.print(c.getValue()+" // ");
							}
						}
			    		List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(sbSent.toString()));
			    		for (Sentence<? extends HasWord> sentence : sentences) {
			    		      Sentence<TaggedWord> tSentence = MaxentTagger.tagSentence(sentence);
			    		      //System.out.println(tSentence.toString(false));
			    		      pwTokenXML.println(tSentence.toString(false));
			    		    }
						//System.out.println(sbSent.toString().trim());
					}
				}
				pwTokenXML.close();
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
}
