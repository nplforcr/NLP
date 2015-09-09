package annotation;

public class MentionProperty {
	private String id;
	private String entityType;
	private String gender;
	private String nums;
	private String type;
	private String content;
	private int extentSt;
	private int extentEd;
	
	public MentionProperty()
	{
		id = entityType = gender = nums = type = content = null;
		extentSt = extentEd = -1;
	}
	
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getNums() {
		return nums;
	}
	public void setNums(String nums) {
		this.nums = nums;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getExtentSt() {
		return extentSt;
	}
	public void setExtentSt(int extentSt) {
		this.extentSt = extentSt;
	}
	public int getExtentEd() {
		return extentEd;
	}
	public void setExtentEd(int extentEd) {
		this.extentEd = extentEd;
	}
}
