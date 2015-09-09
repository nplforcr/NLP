package corpusProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;



public class RelationParser {

	File inputFile;
	static String SENTENCE = "s";
	static String PARA = "p";
	public RelationParser(File inFile) {
		inputFile = inFile;
	}
	
	public void splitMarkableNode(LexicalizedParser lp) {
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(inputFile);
				Node child = document.getFirstChild();
				NodeList docChildList = child.getChildNodes();
				NodeList senNodeList = document.getElementsByTagName(SENTENCE);
				for (int i = 0; i < senNodeList.getLength(); i++) {
					Node ithNode = senNodeList.item(i);
					String ithSentence = ithNode.getTextContent();
					System.out.println(ithNode.getTextContent());
					String[] ithsent = ithSentence.split(" ");
					Tree parse = (Tree) lp.apply(Arrays.asList(ithsent));
					parse.pennPrint();
					TreebankLanguagePack tlp = new PennTreebankLanguagePack();
					GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
					GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
					Collection tdl = gs.typedDependenciesCollapsed();
					System.out.println(tdl);
					System.out.println();
			
					TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
					tp.printTree(parse);
				}

			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println(args[0]);
		File inputFile = new File(args[0]);
		RelationParser relationParser= new RelationParser(inputFile);
		LexicalizedParser lp = new LexicalizedParser("/project/nlp03/stanford-parser-2008-10-26/englishPCFG.ser.gz");
		lp.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});
		relationParser.splitMarkableNode(lp);
		
//		LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz");
//		lp.setOptionFlags(new String[]{"-maxLength", "80", "-retainTmpSubcategories"});
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(args[0]));
//			String l;
//			while ((l = br.readLine()) != null) {
//
//			}
//		} catch (Exception e) { 
//			e.printStackTrace(); 
//		}

//		String[] sent = { "This", "is", "an", "easy", "sentence", "." };
//		Tree parse = (Tree) lp.apply(Arrays.asList(sent));
//		parse.pennPrint();
//		System.out.println();
//
//		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
//		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
//		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
//		Collection tdl = gs.typedDependenciesCollapsed();
//		System.out.println(tdl);
//		System.out.println();
//
//		TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
//		tp.printTree(parse);
	}
}
