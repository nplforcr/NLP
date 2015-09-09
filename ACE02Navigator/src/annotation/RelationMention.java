/**
 * 
 */
package annotation;

/**
 * @author somasw000
 *
 */
public class RelationMention {
	private String id;
	private String mentionId1;
	private String mentionId2;
	private String printString;
		
	public String getPrintString() {
		if(printString == null){
			printString  =  "relationMentionId= "+ id + "\targ1= "+ mentionId1 + "\targ2= "+ mentionId2;
		}
		return printString;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMentionId1() {
		return mentionId1;
	}
	public void setMentionId1(String mentionId1) {
		this.mentionId1 = mentionId1;
	}
	public String getMentionId2() {
		return mentionId2;
	}
	public void setMentionId2(String mentionId2) {
		this.mentionId2 = mentionId2;
	}
	
	
	
}
