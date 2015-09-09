package annotation;

public class BaseResultProperty {
    private int topic;
    private int position;
    private String mention;
    private float topicProb;
    private float prob;
	public int getTopic() {
		return topic;
	}
	public void setTopic(int topic) {
		this.topic = topic;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public String getMention() {
		return mention;
	}
	public void setMention(String mention) {
		this.mention = mention;
	}
	public float getTopicProb() {
		return topicProb;
	}
	public void setTopicProb(float topicProb) {
		this.topicProb = topicProb;
	}
	public float getProb() {
		return prob;
	}
	public void setProb(float prob) {
		this.prob = prob;
	}
    
}
