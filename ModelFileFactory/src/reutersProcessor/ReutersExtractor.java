package reutersProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import corpusProcessor.FeaStruct;
import utils.Pair;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

//I am using the SAXParser to extract coreference information from REUTERS. The REUTERS include <MARKABLE/>, <W/>,<PUNC/> under <S/>
//under <MARKABLE/>, there may be <COREF/> as well as <MARKABLE/>, <W/>,<PUNC/>. <COREF/> is the indicator that the <MARKABLE/> refers
//back to some other <MARKABLE/>. For the present model, I have to ignore embedded <MARKABLE/> since it is impossible for the present 
//model to handle such complicated structure.
public class ReutersExtractor extends DefaultHandler {
    static private Writer out;
    StringBuffer textBuffer;
    StringBuffer posTokenBuffer;
    int tokenCount = 0;
    static String ID = "ID";
    static String GEN = "gender";
    static String TTYPE = "token_type";
    static String POSTYPE = "pos";
    static String MASC = "LexemeGENDER.MASCULINE";
    static String FAML = "LexemeGENDER.FAMULINE";
    static String NEU = "LexemeGENDER.NEUTER";
    static String DEC = "TokenType.DECIMAL_NUMBER";
    static String LEX = "TokenType.LEXEME";
    static String PUNC = "TokenType.PUNCTUATION";
    HashMap<String,String> genderMap;
    List<FeaStruct> indStructList;
    static String DOCU = "DOCUMENT";
	static String PARA = "PARAGRAPH";
	static String SENT = "SENTENCE";
	static String ELEMARK = "[Element: <MARKABLE/>]";
	static String ELEWORD = "[Element: <W/>]";
	static String ELECOREF = "[Element: <COREF/>]";
	static String ELEPUNC = "[Element: <PUNC/>]";
	static String MARKABLE = "MARKABLE";
	static String COREF = "COREF";
	static String TOKEN = "TOKEN";
	static String WORD = "W";
	static String TOKTYPE = "token_type";
	static String LEXEM = "TokenType.LEXEME";
	static String COPY = "copy";
	static String OLD = "old";
	static String NEW = "new";
	static String ELEMENT = "Element"; 
	static String CID = "ID";
	static String REF = "REF";
	static String nullString = "ZERO";
	static String POS = "POS";
	static String SRC = "SRC";
	static int MAXNUM = 8;
	static String start = "-";
	String OP = "";
	String ind = "-";
	String indP = "-";
	List<Pair> wordPosPairList;
	int countTags = 0;
	int countToken = 0;
	boolean isMarkable = false;
	boolean isCoref = false;
	boolean isEmbedded = false;
    String preMarkableID = "";
    String curMarkableID = "";
    
    
    public ReutersExtractor(){
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
        DefaultHandler handler = new ReutersExtractor();

        //The following arguments used for testing
        
        ///project/nlp/dingcheng/nlplab/XMLWriter/slideSample01.xml
        //output
        //"/project/nlp/dingcheng/nlplab/corpora/corefann/dxml/access.dxml"
        //"/project/nlp/dingcheng/nlplab/corpora/corefann/posxml/"
        // Use the default (non-validating) parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        
        File outFile = new File(argv[1]);
        
        if(!outFile.exists()){
        	outFile.mkdirs();
        }
        File inputDir = new File(argv[0]);
        File [] inputDirArray = null;
        if(inputDir.isDirectory()){
        	inputDirArray = inputDir.listFiles();
        	System.out.println(inputDirArray.length);
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
        }else{
        	File inputFile=inputDir;
        	
        	try {
                out = new PrintWriter(new FileWriter(new File(outFile,inputFile.getName().substring(0,inputFile.getName().length()-4)+"posxml")));
                // Parse the input
                SAXParser saxParser = factory.newSAXParser();
                //saxParser.parse(new File(argv[0]), handler);
                saxParser.parse(inputFile, handler);		
            } catch (Throwable t) {
                t.printStackTrace();
            }
            System.exit(0);
        	
        }

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
        //echoText();
        
    	//for the following cases, I intentionally ignore the embedded structure since my present model cannot 
    	//handle them. In future, when syntactic model added in, I will consider how to solve the embedded 
    	//structure.
        if(qName.equals(MARKABLE) && isMarkable == false && isEmbedded == false){
        	emit(preMarkableID+" ");
        	//echoText();
        	nl();
        	curMarkableID = "Markable ID: "+attrs.getValue(ID);
        	preMarkableID = curMarkableID;
        	//emit("Markable ID: "+attrs.getValue(ID));

        	isMarkable = true;
        	isEmbedded = false;
        }
        else if(qName.equals(MARKABLE) && isMarkable == false && isEmbedded == true){
        	//emit(preMarkableID);
        	emit(preMarkableID+" ");
        	//echoText();
        	nl();
        	curMarkableID = "Markable ID: "+attrs.getValue(ID);
        	preMarkableID = curMarkableID;
        	//emit("Markable ID: "+attrs.getValue(ID));
        	isMarkable = true;
        	isEmbedded = false;
        }else if(qName.equals(MARKABLE) && isMarkable == true && isEmbedded == false){
        	//emit("Markable ID: "+attrs.getValue(ID));
        	//emit(preMarkableID);
        	//nl();
        	//echoText();
        	curMarkableID = "Markable ID: "+attrs.getValue(ID);
        	preMarkableID = curMarkableID;
        	isMarkable = true;
        	isEmbedded = true;
        }else if(qName.equals(MARKABLE) && isMarkable == true && isEmbedded == true){
        	//emit("Markable ID: "+attrs.getValue(ID));
        	//emit(preMarkableID);
        	//nl();
        	//echoText();
        	curMarkableID = "Markable ID: "+attrs.getValue(ID);
        	preMarkableID = curMarkableID;
        	isMarkable = true;
        	//isEmbedded = false;
        }else if(qName.equals(WORD)){
        	
        }
        
        
//        else if(qName.equals(COREF) && isEmbedded == false){
//        	emit("source: "+attrs.getValue(SRC));
//        	nl();
//        }
        //preMarkableID = curMarkableID;
        //echoText();
    }

    public void endElement(String namespaceURI, String sName, // simple name
        String qName // qualified name
    ) throws SAXException {
        //echoText();
        if(qName.equals(MARKABLE)){
        	isMarkable = false;
        	//isEmbedded = false;
        }else if(qName.equals(COREF)){
        	isCoref = false;
        }
        echoText();
    }

    //characters method is extracting texts from original xml file 
    //thus, I can make use of this method to do pos tagging on the fly though it is slow. This is a forced choice for the moment
    //then, I will create new xml element (token) and put them into the new xml file (called .posxml).
    //it looks like David's .wxml or .pxml
    public void characters(char[] buf, int offset, int len) throws SAXException {
		String s = new String(buf, offset, len);
		if (isMarkable) {
			if (textBuffer == null) {
				textBuffer = new StringBuffer(s);
			} else {
				textBuffer.append(s);
			}
		}


	}

    // ===========================================================
    // Utility Methods ...
    //===========================================================
    private void echoText() throws SAXException {
        if (textBuffer == null) {
            return;
        }

        String s = "" + textBuffer;
        
		if(s.trim().isEmpty()){
			//isEmbedded = false;
		}else{
			emit(s.trim()+" ");
			//isEmbedded = true;
		}
        
        //emit(s.trim()+" ");
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
