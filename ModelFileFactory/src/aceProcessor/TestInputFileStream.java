package aceProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class TestInputFileStream {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File fileTag = new File(args[0]);
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		StringBuffer sbInput=new StringBuffer();
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(fileTag);
				Node child = document.getFirstChild();
				NodeList docChildList = child.getChildNodes();
				for(int i=0;i<docChildList.getLength();i++){
					Node ithNode=docChildList.item(i);
					String ithString=ithNode.getTextContent();
					if(ithString!=null){
						sbInput.append(ithString);
					}	
				}
				char[] destination = new char[100];
				sbInput.getChars(65, 108, destination, 0);
				System.out.println(destination);
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

}
