package corpusProcessor;
/*
 * Copyright (c) 2006 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. Tous droits reserves.
 *
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * supplements a celles-ci.  Distribue par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants developpes par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques deposees de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */


import java.io.*;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;


public class TokenCorpusXmlBuilder extends DefaultHandler {
    static private Writer out;
    StringBuffer textBuffer;
    StringBuffer posTokenBuffer;
    int tokenCount = 0;
    static String TID = "tid";
    static String GEN = "gender";
    static String TTYPE = "token_type";
    static String POSTYPE = "pos";
    static String MASC = "LexemeGENDER.MASCULINE";
    static String FAML = "LexemeGENDER.FAMULINE";
    static String NEU = "LexemeGENDER.NEUTER";
    static String DEC = "TokenType.DECIMAL_NUMBER";
    static String LEX = "TokenType.LEXEME";
    static String PUC = "TokenType.PUNCTUATION";
    HashMap<String,String> genderMap;
    
    
    public TokenCorpusXmlBuilder(){
    	genderMap = new HashMap<String,String>();
    	genderMap.put("she", FAML);
    	genderMap.put("he", MASC);
    }

    public static void main(String[] argv) {
        if (argv.length < 1) {
            System.err.println("Usage: cmd filename");
            System.exit(1);
        }

        // Use an instance of ourselves as the SAX event handler
        DefaultHandler handler = new TokenCorpusXmlBuilder();

        //The following arguments used for testing
        
        ///project/nlp/dingcheng/nlplab/XMLWriter/slideSample01.xml
        //output

        
        //"/project/nlp/dingcheng/nlplab/corpora/corefann/dxml/access.dxml"
        //"/project/nlp/dingcheng/nlplab/corpora/corefann/posxml/"
        // Use the default (non-validating) parser
        SAXParserFactory factory = SAXParserFactory.newInstance();

        File inputDir = new File(argv[0]);
        File [] inputDirArray = null;
        if(inputDir.isDirectory()){
        	inputDirArray = inputDir.listFiles();
        	System.out.println(inputDirArray.length);
        }
               
        File outFile = new File(argv[1]);
        
        if(!outFile.exists()){
        	outFile.mkdirs();
        }
        
        try {
            // Set up output stream
            //out = new OutputStreamWriter(System.out, "UTF8");
            for(int i=0;i<inputDirArray.length;i++){
            	File inputFile = inputDirArray[i];
            	
            	out = new PrintWriter(new FileWriter(new File(outFile,inputFile.getName().substring(0,inputFile.getName().length()-4)+"posxml")));

                // Parse the input
                SAXParser saxParser = factory.newSAXParser();
                //saxParser.parse(new File(argv[0]), handler);
                saxParser.parse(inputFile, handler);	
            	
            }

            
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.exit(0);
    }

    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================
    public void startDocument() throws SAXException {
        emit("<?xml version='1.0' encoding='UTF-8'?>");
        nl();
    }

    public void endDocument() throws SAXException {
        try {
            nl();
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    public void startElement(String namespaceURI, String sName, // simple name
        String qName, // qualified name
        Attributes attrs) throws SAXException {
        echoText();

        String eName = sName; // element name

        if ("".equals(eName)) {
            eName = qName; // not namespaceAware
        }
        nl();

        emit("<" + eName);

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name 

                if ("".equals(aName)) {
                    aName = attrs.getQName(i);
                }

                emit(" ");
                //nl();
                emit(aName + "=\"" + attrs.getValue(i) + "\"");
            }
        }

        emit(">");
        nl();
    }

    public void endElement(String namespaceURI, String sName, // simple name
        String qName // qualified name
    ) throws SAXException {
        echoText();

        String eName = sName; // element name

        if ("".equals(eName)) {
            eName = qName; // not namespaceAware
        }

        //nl();
        emit("</" + eName + ">");
        nl();
    }

    //characters method is extracting texts from original xml file 
    //thus, I can make use of this method to do pos tagging on the fly though it is slow. This is a forced choice for the moment
    //then, I will create new xml element (token) and put them into the new xml file (called .posxml).
    //it looks like David's .wxml or .pxml
    public void characters(char[] buf, int offset, int len)
        throws SAXException {
    	posTokenBuffer = new StringBuffer();
        String s = new String(buf, offset, len);

        if (textBuffer == null) {
            textBuffer = new StringBuffer(s);
        } else {
            textBuffer.append(s);
        }
        String model = "/project/nlp/dingcheng/nlplab/models/bidirectional-wsj-0-18.tagger";
        try {
			MaxentTagger tagger = new MaxentTagger(model);
            List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(textBuffer.toString()));
    		for (Sentence<? extends HasWord> sentence : sentences) {
    		      Sentence<TaggedWord> tSentence = MaxentTagger.tagSentence(sentence);
    		      //out.append(tSentence.toString(false));
    		      String [] tokenPosArray = tSentence.toString(false).split(" ");
    		      //for(int i=0;i<tokenPosArray.length;i++)
    		      for(String tokenPosStr:tokenPosArray){
    		    	  String[] tokenPosPair = tokenPosStr.split("/");
    		    	  
    		    	  posTokenBuffer.append("<TOKEN id=\""+tokenCount + "\" pos=\""+tokenPosPair[1]+"\">"+tokenPosPair[0]+"</TOKEN>");
    		    	  posTokenBuffer.append("\n");
    		    	  //emit("<TOKEN id=\""+tokenCount);
    		    	  tokenCount++;
    		      }
    		      
    		      //posTokenBuffer.append(tSentence.toString(false));
    		      //System.out.println(tSentence.toString(false));
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

    //===========================================================
    // Utility Methods ...
    //===========================================================
    private void echoText() throws SAXException {
        if (textBuffer == null) {
            return;
        }

//        String s = "" + textBuffer;
//        emit(s);
        String s = "" + posTokenBuffer;
        emit(s);
        posTokenBuffer = null;
        textBuffer = null;
    }

    // Wrap I/O exceptions in SAX exceptions, to
    // suit handler signature requirements
    private void emit(String s) throws SAXException {
        try {
            out.write(s);
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    // Start a new line
    private void nl() throws SAXException {
        String lineEnd = System.getProperty("line.separator");

        try {
            out.write(lineEnd);
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }
}
