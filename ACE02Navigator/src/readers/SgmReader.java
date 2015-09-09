package readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class SgmReader {
	
	public static String QUEST = "?";
	public static String PERIOD = ".";

	public enum Punc {
		QUEST,PERIOD;
	}

	public static int[] findByteSpan(StringBuffer textBuf, String phrase,
			int index) {
		int[] bytespan = new int[2];
		int start = 0, end = 0;
		// System.out.println("phrase in findByteSpan: "+phrase);
		String[] phraseArray = phrase.split(" ");
		if (phrase.isEmpty()) {
			bytespan[0] = start;
			bytespan[1] = end;
			return bytespan;
		} else {
			if (index == 0) {
				start = textBuf.indexOf(phraseArray[0]);
				// end = textBuf.indexOf(phraseArray[phraseArray.length-1]);
				end = start + phrase.length() - 1;
				// index=end;
			} else {
				start = textBuf.indexOf(phraseArray[0], index);
				// end = textBuf.indexOf(phraseArray[phraseArray.length-1]);
				end = start + phrase.length() - 1;
				// index=end;
				// System.out.println("from index in findBytesSpan: "+ index);
			}
		}
		// System.out.println("start and end: "+start+" "+end);

		bytespan[0] = start;
		bytespan[1] = end;
		return bytespan;
	}



	/**
	 * reads the file and returns a string
	 * 
	 * @param docName
	 * @return
	 * @throws IOException
	 */
	public static StringBuffer readDoc(String docName) {
		File inputFile = new File(docName);
		StringBuffer sbAce = new StringBuffer();
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(inputFile);
				Node child = document.getFirstChild();
				sbAce.append(child.getTextContent());
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
		return sbAce;
	}

	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing each line in <code>f</code>.
	 */
	public static String[] readFile(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		ArrayList list = new ArrayList();
		String line;
		while ((line = in.readLine()) != null){
			list.add(line);
		}
		return (String[]) list.toArray(new String[0]);
	}

	/**
	 * reads the file and returns a string
	 * 
	 * @param docName
	 * @return
	 * @throws IOException
	 */
	public static StringBuffer readDoc2(String docName) {
		File inputFile = new File(docName);
		StringBuffer sbAce = new StringBuffer();
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			BufferedReader brXml;
			try {
				try {
					brXml = new BufferedReader(new FileReader(inputFile));
					StringBuffer sbXml = new StringBuffer();
					String line = "";
					while ((line = brXml.readLine()) != null) {
						//it seems that we cannot ignore DOCTYPE since words there are counted in bytes in annotation.
						if (line.startsWith("<TURN>") ||line.startsWith("<time") || line.startsWith("<DOCID>") ||line.startsWith("<DATETIME>") || line.startsWith("<DOC_TYPE")) {
							//continue;
							//the reason that we must append a white space here is that there is a white space byte for each tag. If we skip them,
							//the bytes counting will be inconsistent.
							sbXml.append(" ");
						} else 
						if(line.contains("&")){
							line = line.replace("&", "d");
							sbXml.append(line+" ");
						}
						else {
							sbXml.append(line+" ");
						}
					}

					builder = factory.newDocumentBuilder();

					document = builder.parse(new InputSource(new StringReader(
							sbXml.toString())));
					Node child = document.getFirstChild();
					String contents = child.getTextContent();
					
					if(contents.endsWith("?") || contents.endsWith(".")){
						sbAce.append(child.getTextContent());
					}else{
						sbAce.append(child.getTextContent()+".");
					}
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
		return sbAce;
	}

	public static StringBuffer readDoc3(String docName){
		File inputFile = new File(docName);
		StringBuffer sbAce = new StringBuffer();
		readDoc3(inputFile);
		return sbAce;
	}

	/**
	 * reads the file and returns a string
	 * 
	 * @param docName
	 * @return
	 * @throws IOException
	 */
	public static StringBuffer readDoc3(File inputFile) {

		StringBuffer sbAce = new StringBuffer();
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			try {
				try {
					InputStream is = new FileInputStream(inputFile);
					byte[] bytes = new byte[(int)inputFile.length()];
					// Read in the bytes
					int offset = 0;
					int numRead = 0;
					while ( (offset < bytes.length)
							&&
							( (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) ) {

						offset += numRead;

					}
					// Ensure all the bytes have been read in
					if (offset < bytes.length) {
						throw new IOException("Could not completely read file " + inputFile.getName());
					}
					is.close();
					String byteStr = new String(bytes).replace("&", ":");
					//System.out.println(byteStr);

					builder = factory.newDocumentBuilder();

					document = builder.parse(new InputSource(new StringReader(
							byteStr)));
					Node child = document.getFirstChild();
					sbAce.append(child.getTextContent());
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
		return sbAce;
	}
}
