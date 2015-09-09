package evaOtherSystems;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.xml.sax.SAXException;

import annotation.Mention;
import annotation.NamedEntity;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import readers.AceReader;
import readers.SgmReader;
import utils.CorefData;
import utils.CorefUser;
import utils.DpSentence;
import utils.FileFunctions;
import utils.IntArray;
import utils.Pair;
import utils.ThirdPersonPron;
import utils.ThirdReflexives;

public class ExtractACECoref extends AceReader {
	List<Pair> mentionSpanList;
	HashMap<Pair, String> bytespanIdM;
	HashMap<String, List<Pair>> idMentionListM;
	List<DpSentence> sentList;
	HashMap<DpSentence, Integer> sentM;

	public ExtractACECoref() {
		idMentionListM = new HashMap<String, List<Pair>>();
		bytespanIdM = new HashMap<Pair, String>();
		mentionSpanList = new ArrayList<Pair>();
		sentList = new ArrayList<DpSentence>();
		sentM = new HashMap<DpSentence, Integer>();
	}

	public void obtainGoldCr(String xmlFname)
			throws ParserConfigurationException, SAXException, IOException {
		File xmlFile = new File(xmlFname);
		processDocument(null, xmlFname);
		CorefUser.fillMentionList(idMentionM, mentionSpanList, bytespanIdM);
		CorefUser.fillIdMenListM(idMentionListM, idNeM);
	}

	public void process(String sgmFname) throws IOException {
		docBuf = SgmReader.readDoc2(sgmFname);
		// System.out.println(docBuf.toString());
		SentenceModel model = new SentenceModel(new FileInputStream(
				"models/EnglishSD.bin.gz"));
		SentenceDetectorME sdetector = new SentenceDetectorME(model);
		String text = docBuf.toString();
		String[] sents = sdetector.sentDetect(text);
		int fromInd = 0;
		for (int i = 1; i < sents.length; i++) {
			// System.out.println(i+" thsent: "+sents[i]);
			int[] sentByteSpan = new int[2];
			String sentence = sents[i];
			// String sentStr =
			// StringMethods.sentenceString(sentence).replaceAll("\\[ [0-9][0-9]* \\]",
			// ""); // replace all citations [ 45 ] seen in wikipedia -- this
			// messes up the openNLP parser.
			sentByteSpan = FileFunctions
					.findByteSpan(docBuf, sentence, fromInd);
			DpSentence sent = new DpSentence();
			sent.setSent(sentence);
			sent.setStart(sentByteSpan[0]);
			sent.setEnd(sentByteSpan[1]);
			if (!sentList.contains(sent)) {
				sentM.put(sent, 1);
				sentList.add(sent);
			}
			fromInd = sentByteSpan[1];
		}

		// break a paragraph into sentences and we get array of sentences and
		// also their types spans
		// they compose Sentence objects and are stored into both sentList and
		// sentM.
		// the reason to run the following on the data is to unify the sentences
		// from both output of emPronoun and
		// the gold annotations
		List<Sentence<? extends HasWord>> sentences = MaxentTagger
				.tokenizeText(new StringReader(docBuf.toString()));
		// I need to keep two StringBuffer. One is for the annotation. The gold
		// annotation has consistent bytes span
		// with docBuf. But the following new docBufNew is consistent with
		// output from emPronoun.

		// now, what I need to do is to correspond gold annotations to each
		// sentence.
		StringBuffer docBufNew = new StringBuffer();
		// I finally found solution to the inconsistencies brought by
		// MaxentTagger.tokenizeText between docBuf and sentences
		// the reason is that tokenizeText split punctuation marks and some
		// other symbols or change some symbols, such as from
		// ( to -LRB-. Consequently, the bytes in sentences are much more than
		// docBuf. Now, a simple solution is just to create
		// a new StringBuffer as docBufNew so that docBufNew fully follow
		// sentences. Then, the bytes span will match.
		// the same solution is made in NEPairDataFactory.java
		int count = 0;
		for (Sentence<? extends HasWord> stanfordSentence : sentences) {
			// System.out.println(count+"th stanfordSentence: "+stanfordSentence);
			docBufNew.append(stanfordSentence.toString());
			count++;
		}

		// System.out.println("sents size: "+sents.length+" sentences size: "+sentences.size());
		// System.out.println(docBufNew+" \n"+docBuf);
		// System.out.println("the difference between docBufNew and docBuf: "+docBufNew.length()+" "+docBuf.length());
	}

	public void matchEntity2Sent() {
		// now we have sentList which has bytespan in it . Meanwhile, we have
		// gold standard bytes span for
		// each mention. then, we can print out results similar to the following
		// format:
		// 700 sentNo: 2-0 tag: NP-0 mention: a widely recognized American
		// figure
		// I will finish this tomorrow morning.
		boolean firstCata = false;
		List<Pair> sortedMentionSpanList = mentionSpanList;
		// System.out.println("mentionSpanList size below: "+mentionSpanList.size());
		Collections.sort(sortedMentionSpanList);
		int sortedIndex = 0;

		int corefNo = 0;
		int sortedCorefIndex = 0;
		int preNeId = 0;
		for (Pair bytespan : sortedMentionSpanList) {
			String menId = bytespanIdM.get(bytespan);
			Mention mention = idMentionM.get(menId);

			int startInd = mention.getHeadCoveredText().indexOf("\"") + 1;
			int endInd = mention.getHeadCoveredText().lastIndexOf("\"");
			String word = mention.getHeadCoveredText()
					.substring(startInd, endInd).toLowerCase();
			if (word.contains(" ")) {
				String[] tokenArray = word.split(" ");
				word = tokenArray[tokenArray.length - 1];
			}

			if (sortedIndex == 0
					&& ThirdPersonPron.thirdPerPronList2.contains(word)) {
				firstCata = true;
				continue;
			}

			if (ThirdPersonPron.thirdPerPronList2.contains(word)
					|| ThirdReflexives.thirdRefList2.contains(word)) {
				String neId = menId.substring(0, menId.indexOf("-"));

				List<Pair> menList = idMentionListM.get(neId);
				int index = -1;

				index = menList.indexOf(bytespan);
				int preIndex = index - 1;
				Pair corefByteSpan = null;
				if (preIndex != -1) {
					corefByteSpan = menList.get(preIndex);
					String corefMenId = bytespanIdM.get(corefByteSpan);
					Mention corefMention = idMentionM.get(corefMenId);
					// System.out.println("corefMention: "+corefMention.getHeadCoveredText());
					// since the word looks like the following: string = "he",
					// we need to find the index of the left quote ".
					int corefStInd = corefMention.getHeadCoveredText().indexOf(
							"\"") + 1;
					// since the word looks like the following: string = "he",
					// we need to find the index of the right quote ".
					int corefEndInd = corefMention.getHeadCoveredText()
							.lastIndexOf("\"");
					String corefWord = corefMention.getHeadCoveredText()
							.substring(corefStInd, corefEndInd);
					if (corefWord.contains(" ")) {
						String[] tokenArray = corefWord.split(" ");
						corefWord = tokenArray[tokenArray.length - 1]
								.toLowerCase();
					}
					sortedCorefIndex = sortedMentionSpanList
							.indexOf(corefByteSpan);
					// System.out.println(mentionSpanList.get(index));

					if (firstCata == true) {
						sortedCorefIndex -= 1;
					}
					// System.out.println("IDENT "+sortedCorefIndex+" "+corefWord+" "+sortedIndex+" "+word);
					CorefData keyData = new CorefData();
					keyData.setCorefIndex(sortedCorefIndex);
					keyData.setCorefWord(corefWord);
					keyData.setAnaIndex(sortedIndex);
					keyData.setAnaphor(word);

					for (int j = 0; j < sentList.size(); j++) {
						// hard work! the following if clause is a trick. I
						// didn't notice this until today, Saturday, Feb. 26,
						// 2011.
						// why is it important since if we update corefNo
						// wrongly. The corefChain will be fully a mess!!!
						// note: the following if clause must be within the for
						// loop. The reason is that within the for loop
						// they mean the real coreferring chain. If outside,
						// some other pronouns may be mixed together.
						if (preNeId != Integer.valueOf(neId)) {
							corefNo++;
						}
						DpSentence jthSent = sentList.get(j);
						int sentByteStart = jthSent.getStart();
						int sentByteEnd = jthSent.getEnd();
						IntArray sentIntArray = new IntArray(sentByteStart,
								sentByteEnd);
						int proStart = Integer.valueOf(bytespan.o1.toString());
						int proEnd = Integer.valueOf(bytespan.o2.toString());
						IntArray proIntArray = new IntArray(proStart, proEnd);
						int corefStart = Integer.valueOf(corefByteSpan.o1
								.toString());
						int corefEnd = Integer.valueOf(corefByteSpan.o2
								.toString());
						IntArray corefIntArray = new IntArray(corefStart,
								corefEnd);
						// a case I need to pay attention. Maybe, for the
						// moment, we can just ignore it.
						// neId may be identical for mentions which have long
						// distance between. But I don't want to handle this
						// for the moment since the anaphora resolvers I am
						// using cannot handle it (emPronoun and my system).
						// I may return it later. Namely, if two mention are far
						// from each other even if they refer to the same
						// entity, we still regard them as different ones.
						if (corefIntArray.spanCompare(sentIntArray) == 3) {
							int sentNo = j - 1;
							System.out.println(corefNo + " neId: " + neId
									+ " sentNo: " + sentNo + " " + corefWord);
						}
						if (proIntArray.spanCompare(sentIntArray) == 3) {
							int sentNo = j - 1;
							System.out.println(corefNo + " neId: " + neId
									+ " sentNo: " + sentNo + " " + word);
						}
						preNeId = Integer.valueOf(neId);
					}
				}
				// System.out.println(preNeId+" "+neId);

			}
			sortedIndex++;
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException {
		// TODO Auto-generated method stub

		ExtractACECoref extractACECoref = new ExtractACECoref();
		extractACECoref
				.obtainGoldCr("input2/ABC19980108.1830.0711.sgm.tmx.rdc.xml");
		extractACECoref.process("input2/ABC19980108.1830.0711.sgm");
		extractACECoref.matchEntity2Sent();
	}

}
