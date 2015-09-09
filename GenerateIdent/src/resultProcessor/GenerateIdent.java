package resultProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Pair;
import annotation.BaseResultProperty;
import annotation.MentionProperty;

public class GenerateIdent {
	public final static int fileNum = 194;

	public static HashMap<String, List<Integer>> mpos; // mention => id
	public static HashMap<Integer, String> mmid; // position => mention
	public static HashMap<String, MentionProperty> mp; // mention property
	public static List<String> mm; // mention
	public static List<Pair> mid; // id1 => id2(descending)

	public GenerateIdent(String inputFile1, String inputFile2,
			String inputFile3, String outputFile) throws IOException {
		this.processDoc(inputFile1, inputFile2, inputFile3, outputFile);
	} 

	/**
	 * addressing baseresult and mention list
	 * 
	 * @param inputFile1
	 * @param inputFile2
	 * @param inputFile3
	 * @param outputFile
	 * @throws IOException
	 */
	private void processDoc(String inputFile1, String inputFile2,
			String inputFile3, String outputFile) throws IOException {
		// mention id => property
		FileReader fr1 = new FileReader(inputFile1);
		FileReader fr2 = null;
		FileReader fr3 = null;
		BufferedReader br1 = new BufferedReader(fr1);
		BufferedReader br2 = null;
		BufferedReader br3 = null;
		FileWriter fw = new FileWriter(outputFile);
		String str1 = null, str2 = null, str3 = null;
		int fileID = 0;
		while (fileID < fileNum) {
			HashMap<Integer, BaseResultProperty> map = new HashMap<Integer, BaseResultProperty>();
			List<List<BaseResultProperty>> resList = new ArrayList<List<BaseResultProperty>>();
			List<Integer> lpos = new ArrayList<Integer>();
			mpos = new HashMap<String, List<Integer>>();
			mmid = new HashMap<Integer, String>();
			mp = new HashMap<String, MentionProperty>();
			mm = new ArrayList<String>();
			mid = new ArrayList<Pair>();
			int rowno = 0, fileRealID = -1;
			while ((str1 = br1.readLine()) != null) {
				if (str1.isEmpty())
					break;
				if (rowno < 3) {
					fw.write(str1 + "\r\n");
					if (rowno == 1) {
						fileRealID = Integer
								.parseInt(str1.split("=")[1].trim());
					}
					rowno++;
					continue;
				}
				String[] data = str1.split(":");
				int topic = Integer.parseInt(data[0].trim().split(" ")[0]);
				float topicProb = Float
						.parseFloat(getContentOfParentheses(data[0]));
				process(map, topic, topicProb, data[1].trim().split("\\s+"),
						resList);
				str1 = null;
			}

			rowno = 0;
			fr2 = new FileReader(inputFile2);
			br2 = new BufferedReader(fr2);
			while ((str2 = br2.readLine()) != null) {
				if (rowno < fileRealID) {
					rowno++;
					continue;
				}
				String[] data = str2.split("=");
				int id = 0;
				for (int i = data.length - 2; i >= 0; i = i - 2) {
					if(!mpos.containsKey(data[i])) 
					{
						List<Integer> l = new ArrayList<Integer>();
						l.add(id);
						mpos.put(data[i], l);
					}
					else 
					{
						mpos.get(data[i]).add(id);
					}
					id++;
				}
				fr2 = null;
				str2 = null;
				break;
			}
			br2 = null;

			rowno = -1;
			fr3 = new FileReader(inputFile3);
			br3 = new BufferedReader(fr3);
			while ((str3 = br3.readLine()) != null && rowno <= fileRealID) {
				if (str3.endsWith(".xml")) {
					rowno++;
				}
			    if(rowno < fileRealID) {
			    	continue;
			    }
			    else if (rowno == fileRealID) {
					MentionProperty mpObj = new MentionProperty();
					String[] tmp = str3.split("=");
					getMentionProperty(tmp, mpObj);
					mp.put(mpObj.getContent(), mpObj);
				} else if (rowno > fileRealID) {
					fr3 = null;
					str3 = null;
					break;
				}
			}
			br3 = null;

			for (int i = 0; i < mm.size() - 1; i += 2) {
				int pos1 = -1, pos2 = -1;
				if (mpos.containsKey(mm.get(i))
						&& mpos.containsKey(mm.get(i + 1))) { 
					processList(mpos.get(mm.get(i)), mpos.get(mm.get(i + 1)), i);
				}
			}
			Collections.sort(mid);
			String pos1 = "", pos2 = "";
			for (int i = 0; i < mid.size(); i++) {
				if (mid.get(i).getFirst().toString().equals(pos1))
					continue;
				pos1 = mid.get(i).getFirst().toString();
				pos2 = mid.get(i).getSecond().toString();
				String mention1 = mmid.get(Integer.parseInt(pos1));
				String mention2 = mmid.get(Integer.parseInt(pos2));
				if (mention1.equals(mention2) || !compareTwoMention(mention1, mention2, mp))
				   continue;
					
				System.out.println("IDENT " + pos1 + " " + mention1 + " "
						+ pos2 + " " + mention2);
				fw.write("\t" + "IDENT " + pos1 + " " + pos2
						 + "\r\n");
			}
			fw.write("\r\n");
			fileID++;
			mpos = null;
			mmid = null;
			mp = null;
			mm = null;
			mid = null;
		}
		fr1.close();
		fw.close();
	}

	/**
	 * get string in quotation
	 * 
	 * @param s
	 * @return String
	 */
	private String getContentOfParentheses(String s) {
		String res = "";
		int flag = 0;
		for (int i = 0; i < s.length() && s.charAt(i) != ')'; i++) {
			if (s.charAt(i) == '(') {
				flag = 1;
				continue;
			}
			if (1 == flag) {
				res += s.charAt(i);
			}
		} 
		return res;
	}

	/**
	 * addressing baseresult
	 * 
	 * @param map
	 * @param topic
	 * @param topicProb
	 * @param str
	 * @param lid
	 */
	private void process(HashMap<Integer, BaseResultProperty> map, int topic,
			float topicProb, String[] str,
			List<List<BaseResultProperty>> resList) {
		List<BaseResultProperty> tmp = new ArrayList<BaseResultProperty>();
		for (int i = 0; i < str.length; i += 3) {
			int position = Integer.parseInt(str[i].trim());
			String mention = str[i + 1].trim();
			while (getContentOfParentheses(str[i + 2].trim()).isEmpty()) {
				mention = "" + mention + " " + str[i + 2];
				i++;
			}
			String s = getContentOfParentheses(str[i + 2].trim());
			float prob = Float.parseFloat(s);

			if (prob < 0.01)
				continue;

			if (map.containsKey(position)
					&& (prob * topicProb <= map.get(position).getProb()
							* map.get(position).getTopicProb())) {
				continue;
			}
			BaseResultProperty mp = new BaseResultProperty();
			mp.setTopic(topic);
			mp.setPosition(position);
			mp.setMention(mention);
			mp.setTopicProb(topicProb);
			mp.setProb(prob);
			if (!map.containsKey(position)) {
				map.put(position, mp);
				tmp.add(mp);
				mm.add(mention);
			}
		}
		resList.add(tmp);
	}

	/**
	 * get a mention's property on a line
	 * 
	 * @param input
	 * @param mpObj
	 */
	private void getMentionProperty(String[] input, MentionProperty mpObj) {
		for (int i = 0; i < input.length; i++) {
			String property = input[i].split(":")[0];
			String value = input[i].split(":")[1];
			switch (property) {
			case "Mention":
				mpObj.setContent(value);
				break;
			case "ID":
				mpObj.setId(value.split("-")[0]);
				break;
			case "entity_type":
				mpObj.setEntityType(value);
				break;
			case "type":
				mpObj.setType(value);
				break;
			case "gender":
				mpObj.setGender(value);
				break;
			case "nums":
				mpObj.setNums(value);
				break;
			case "startPosition":
				mpObj.setExtentSt(Integer.parseInt(value));
				break;
			case "endPosition":
				mpObj.setExtentEd(Integer.parseInt(value));
				break;
			default:
				break;
			}
		}
	}

	/**
	 * determine if mention1 and mention2 belong same type
	 * 
	 * @param m1
	 * @param m2
	 * @return
	 */
	private boolean compareTwoMention(String m1, String m2,
			HashMap<String, MentionProperty> mp) {
		MentionProperty mp1 = mp.get(m1);
		MentionProperty mp2 = mp.get(m2);
		if (mp1.getGender().equals(mp2.getGender()) && mp1.getNums().equals(mp2.getNums()))
			return true;
		return false;
	}
	
	/**
	 * 
	 * @param list l1
	 * @param list l2
	 */
	private void processList(List<Integer> l1, List<Integer> l2, int i)
	{
		for(int j = 0; j < l1.size(); j++)
		{
			int pos1 = l1.get(j);
			for(int k = 0; k < l2.size(); k++)
			{
				int pos2 = l2.get(k);
				mmid.put(pos1, mm.get(i));
				mmid.put(pos2, mm.get(i + 1));
				if (pos1 > pos2) {
					int tmp = pos1;
					pos1 = pos2;
					pos2 = tmp;
				}
				if (pos1 == pos2)
					continue;
				Pair pair = new Pair(pos1, pos2);
				mid.add(pair);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		String inputFile1 = "input/nips.result.txt";
		String inputFile2 = "input/nips.mention.txt";
		String inputFile3 = "input/nips.property.txt";
		String outputFile = "output/output.txt";
		GenerateIdent gi = new GenerateIdent(inputFile1, inputFile2,
				inputFile3, outputFile);
	}
}
