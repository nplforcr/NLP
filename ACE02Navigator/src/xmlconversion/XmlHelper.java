package xmlconversion;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlHelper {
	public static Node getFirstChildByTagName(Node parent, String tagName) {
		NodeList nodeList = parent.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& node.getNodeName().equalsIgnoreCase(tagName))
				return node;
		}
		return null;
	}

	public static List<Node> getChildrenByTagName(Node parent, String tagName) {
		List<Node> eleList = new ArrayList<Node>();
		NodeList nodeList = parent.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& node.getNodeName().equalsIgnoreCase(tagName)) {
				eleList.add(node);
			}

		}
		return eleList;
	}
	
	public static int[] extractAceXmlByteSpan(Node menNode){
		int[] aceByteSpan = new int[2];
		Node extNode = XmlHelper.getFirstChildByTagName(menNode,TagTypes.EXTENT);
		Node charNode = XmlHelper.getFirstChildByTagName(extNode,TagTypes.CHARSEQ);
		Node startNode = XmlHelper.getFirstChildByTagName(charNode,TagTypes.START);
		Node endNode = XmlHelper.getFirstChildByTagName(charNode,TagTypes.END);
		String menType = menNode.getAttributes().getNamedItem(TagTypes.TYPE).getNodeValue();
		//System.out.println(startNode.getTextContent()+" "+endNode.getTextContent());
//		if(!menType.equals("PRONOUN")){
//			aceByteSpan[0] = Integer.parseInt(startNode.getTextContent());
//			aceByteSpan[1] = Integer.parseInt(endNode.getTextContent());								
//		}
		aceByteSpan[0] = Integer.parseInt(startNode.getTextContent());
		aceByteSpan[1] = Integer.parseInt(endNode.getTextContent());
		return aceByteSpan;
	}
	
	public static Node getNodeByName(Node n, String name){
		NodeList l = n.getChildNodes();
		if (l == null)
			return null;

		for(int i=0; i<l.getLength(); i++){
			if(l.item(i).getNodeName().equals(name))
				return l.item(i);
			else{
				Node n2 = getNodeByName(l.item(i), name);
				if(n2 != null){
					return n2;
				}
			}
		}
		return null;
	}
	
	public static List<String[]> getNeRelMenArgs(Node relNode){
		List<String[]> listOfRelMenArgs = new ArrayList<String[]>();
		List<Node> relMensList = XmlHelper.getChildrenByTagName(relNode, TagTypes.RELMENS);
		for(int i=0;i<relMensList.size();i++ ){
			Node relMens = relMensList.get(i);
			List<Node> menNodeList = XmlHelper.getChildrenByTagName(relMens, TagTypes.RELMEN);
			for(int j=0;j<menNodeList.size();j++){
				Node relMen = menNodeList.get(j);
				//System.out.println(relMen.get);
				List<Node> relMenArgList = XmlHelper.getChildrenByTagName(relMen, TagTypes.RELMENARG);
				//System.out.println(relMenArgList.size());
				String relMenArg1 = relMenArgList.get(0).getAttributes().getNamedItem(TagTypes.MENID).getNodeValue();
				String relMenArg2 = relMenArgList.get(1).getAttributes().getNamedItem(TagTypes.MENID).getNodeValue();
				String[] relMenArg = new String[2];
				relMenArg[0] = relMenArg1;
				relMenArg[1] = relMenArg2;
				listOfRelMenArgs.add(relMenArg);
			}
		}
		return listOfRelMenArgs;
	}
	
}
