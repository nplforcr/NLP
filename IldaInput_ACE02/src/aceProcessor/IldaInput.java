package aceProcessor;

import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NoInitialContextException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import annotation.MentionProperty;
import readers.IldaSgmReader;
import utils.FemalePronouns;
import utils.MalePronouns;
import utils.MentionsType;
import utils.NameList;
import utils.NominalList;
import utils.PluralPronouns;
import utils.SingularPronouns;
import xmlConversion.TagTypes;
import xmlConversion.XmlHelper;

public class IldaInput {
	public HashMap<Integer, HashMap<String, Integer>> dw; // fileId --> (word
															// --> count)
	public HashMap<String, Integer> wn; // word --> sum of this word in corpus
	public HashMap<String, Integer> wmn; // word --> appearing in sum of files
	public HashMap<Integer, String> wid; // word --> id(descending sorted)
	
	public FileWriter fw1 = null; // nips.corpus
	public FileWriter fw2 = null; // nips.vocab
	public FileWriter fw3 = null; // nips.docnames
	public FileWriter fw4 = null; // mention's position
	public FileWriter fw5 = null; // mention and its property

	/**
	 * main class constructor
	 * 
	 * @param dirName
	 *            input directory
	 * @param outDir
	 *            output directory
	 */
	public IldaInput(String dirName, String outDir) throws IOException,
			ParserConfigurationException, SAXException {
		// 主类
		dw = new HashMap<Integer, HashMap<String, Integer>>();
		wn = new HashMap<String, Integer>();
		wmn = new HashMap<String, Integer>();
		wid = new HashMap<Integer, String>();
		this.process(dirName, outDir);
	}

	/**
	 * addressing sgm file
	 * 
	 * @param dirName
	 *            input directory
	 * @param outDir
	 *            output directory
	 */
	private void process(String dirName, String outDir) throws IOException,
			ParserConfigurationException, SAXException {
		// addressing sgm file
		
		File dFile = new File(dirName);
		File outputFile = new File(outDir);
		if (!dFile.exists()) {
			dFile.mkdirs();
		}
		if (!outputFile.exists()) {
			outputFile.mkdirs();
		}
		int sgmId = 0;
		int fileId = 0;
		if (dFile.isDirectory()) {
			fw1 = new FileWriter(outDir + "/nips.docnames.txt");
			fw2 = new FileWriter(outDir + "/nips.corpus.txt");
			fw3 = new FileWriter(outDir + "/nips.vocab.txt");
			fw4 = new FileWriter(outDir + "/nips.mention.txt");
			fw5 = new FileWriter(outDir + "/nips.porperty.txt");
			File files[] = dFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					String filename = files[i].getName();
					if (filename.endsWith(".sgm")) {
						fw1.write(filename + "\r\n");
						System.out.println("sgm file name: " + filename);
						StringBuffer docBuf = IldaSgmReader.readDoc2(dirName
								+ "/" + filename);
						if (docBuf != null) {
							sgmId++;
						}
					} else if (filename.endsWith(".xml")) {
						System.out.println("xml name: " + filename);
						fw5.write("xml name: " + filename + "\r\n");
						List<MentionProperty> wm = new ArrayList<MentionProperty>(); 
						// word --> property
						HashMap<String, Integer> mp = processEntities(
								files[i].getAbsolutePath(), wm);
						// nips.mentionPos
						for (int k = 0; k < wm.size(); k++) {
							String mention = wm.get(k).getContent();
							int pos = wm.get(k).getExtentSt();
							fw4.write(mention + "=" + pos);
							if (k < wm.size() - 1)
								fw4.write("=");
						}
						fw4.write("\r\n");
						dw.put(fileId, mp);
						fileId++;
					} else {
						continue;
					}
				}
			}
			sortByCount(wn, wid);
			// nips.corpus
			for (int i = 0; i < dw.size(); i++) {
				HashMap<String, Integer> m = dw.get(i);
				fw2.write(dw.get(i).size() + " ");
				for (Entry<Integer, String> entry : wid.entrySet()) {
					String str = entry.getValue();
					if (m.containsKey(str)) {
						fw2.write(entry.getKey() + ":" + m.get(str) + " ");
					}
				}
				fw2.write("\r\n");
			}
			// nips.vocab
			for (int i = 0; i < wid.size(); i++) {
				String str = "";
				String tmp = wid.get(i);
				String[] split = tmp.split("(\r\n|\r|\n|\n\r)");
				for (String s : split) {
					str += s;
				}
				int count = wn.get(tmp);
				int countm = wmn.get(tmp);
				fw3.write(str + " = " + i + " = " + count + " " + countm
						+ "\r\n");
			}
			fw1.close();
			fw2.close();
			fw3.close();
			fw4.close();
			fw5.close();
		}
	}

	/**
	 * addressing xml file
	 * 
	 * @param annotFname
	 *            xml file name
	 * @param list
	 *            save mention propery
	 */
	private HashMap<String, Integer> processEntities(String annotFname,
			List<MentionProperty> list) throws ParserConfigurationException,
			SAXException, IOException {
		// 处理xml文件，得到特征词语
		HashMap<String, Integer> res = new HashMap<String, Integer>(); // word
																		// -->
																		// count
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(new File(annotFname));
		NodeList entList = document.getElementsByTagName(TagTypes.ENT);
		for (int i = 0; i < entList.getLength(); i++) {
			NodeList mentionList = entList.item(i).getChildNodes();
			String entityType = null;
			for (int j = 0; j < mentionList.getLength(); j++) {
				Node childN = mentionList.item(j);
				if (childN.getNodeName().equals(TagTypes.ENTYPE)) {
					entityType = childN.getTextContent();
				}
				if (childN.getNodeName().equals(TagTypes.ENTMEN)) {
					Node extNode = XmlHelper.getFirstChildByTagName(childN,
							TagTypes.EXTENT);
					Node headNode = XmlHelper.getFirstChildByTagName(childN,
							TagTypes.HEAD);
					Node extEntity = XmlHelper.getFirstChildByTagName(extNode,
							TagTypes.CHARSEQ);
					Node headEntity =  XmlHelper.getFirstChildByTagName(headNode,
							TagTypes.CHARSEQ);
					String mention = getContentOfQuotation(extEntity
							.getFirstChild().getNextSibling().getTextContent());
					String head = getContentOfQuotation(headEntity
							.getFirstChild().getNextSibling().getTextContent());
					String type = childN.getAttributes()
							.getNamedItem(TagTypes.TYPE).getNodeValue();
					String ID = childN.getAttributes()
							.getNamedItem(TagTypes.ID).getNodeValue();
					String sPos = XmlHelper.getFirstChildByTagName(extEntity,
							TagTypes.START).getTextContent();
					String ePos = XmlHelper.getFirstChildByTagName(extEntity,
							TagTypes.END).getTextContent();
					if(mention.isEmpty()) continue;
					MentionProperty ment = new MentionProperty();
					ment.setId(ID);
					ment.setType(type);
					ment.setEntityType(entityType);
					ment.setExtentSt(Integer.parseInt(sPos));
					ment.setExtentEd(Integer.parseInt(ePos));
					ment.setContent(mention);
					if (type.equals(MentionsType.PRONOUN)) {
						if (FemalePronouns.femalePronList2.contains(head.toLowerCase())) {
							ment.setGender(MentionsType.FEMALE);
						} else if (MalePronouns.malePronList2.contains(head.toLowerCase())) {
							ment.setGender(MentionsType.MALE);
						}
						
						if (SingularPronouns.singularPronList2
								.contains(head.toLowerCase())) {
							ment.setNums(MentionsType.SINGULAR);
						} else if(PluralPronouns.pluralPronList2.contains(head.toLowerCase())){
							ment.setNums(MentionsType.PLURAL);
						}
					} else if (type.equals(MentionsType.NAME)
							&& entityType.equals(MentionsType.PERSON)) {
						String s = mention.split(" ")[0];
						if (s.equals("Mr."))
							ment.setGender(MentionsType.MALE);
						else if (NameList.nameList2.containsKey(s)) {
							String gender = NameList.nameList2.get(s);
							ment.setGender(gender);
						}
					} else if (type.equals(MentionsType.NOMINAL)) {
						if (NominalList.nominalList2.contains(head.toLowerCase())) {
							ment.setNums(MentionsType.SINGULAR);
						} else if (head.toLowerCase().endsWith("s")) {
							ment.setNums(MentionsType.PLURAL);
						}else ment.setNums(MentionsType.SINGULAR);
					}
					list.add(ment);
					// mention --> count (in single file)
					System.out.println("Mention:" + mention + " ID:" + ID
							+ " entity_type:" + entityType + " type:" + type
							+ " gender:" + ment.getGender() + " nums:"
							+ ment.getNums() + " startPosition:" + sPos
							+ " endPosition:" + ePos);
					fw5.write("Mention:" + mention + "=ID:" + ID
							+ "=entity_type:" + entityType + "=type:" + type
							+ "=gender:" + ment.getGender() + "=nums:"
							+ ment.getNums() + "=startPosition:" + sPos
							+ "=endPosition:" + ePos + "\r\n");
					if (!res.containsKey(mention)) {
						res.put(mention, 1);
						// mention --> file count
						if (!wmn.containsKey(mention)) {
							wmn.put(mention, 1);
						} else {
							int count = wmn.get(mention);
							wmn.put(mention, count + 1);
						}
					} else {
						int count = res.get(mention);
						res.put(mention, count + 1);
					}
					// mention --> count (all files)
					if (!wn.containsKey(mention)) {
						wn.put(mention, 1);
					} else {
						int count = wn.get(mention);
						wn.put(mention, count + 1);
					}
				}
			}
		}
		return res;
	}

	/**
	 * sort terms by their frequency
	 * 
	 * @param m1
	 *            origin map
	 * @param m2
	 *            new map
	 */
	private void sortByCount(HashMap<String, Integer> m1,
			HashMap<Integer, String> m2) {
		int id = 0;
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.putAll(m1);
		while (!map.isEmpty()) {
			int maxCount = 0;
			String str = "";
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				if (entry.getValue() > maxCount) {
					str = entry.getKey();
					maxCount = entry.getValue();
				}
			}
			map.remove(str);
			m2.put(id, str);
			id++;
		}
	}

	/**
	 * get content in quotation,
	 * 
	 * for example:"I am a student" -> I am a student
	 * 
	 * @param input
	 *            a string containing quotaion
	 */
	private String getContentOfQuotation(String input) {
		String res = "";
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '"') {
				res = input.substring(i + 1, input.length() - 2);
				res.trim();
				break;
			}
		}
		return res;
	}

	public static void main(String[] args) throws IOException,
			ParserConfigurationException, SAXException {
		// main 函数
		String inputDir = "input";
		String outputDir = "output";
		IldaInput obj = new IldaInput(inputDir, outputDir);
	}

}
