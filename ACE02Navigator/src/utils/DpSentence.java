package utils;

import java.util.List;


public class DpSentence {

	String sent;
	int start;
	int end;	
	public void setSent(String sent){
		this.sent=sent;
	}
	
	public boolean equal(DpSentence sentence){
		boolean isContain = false;
		if(this.getSent().equals(sentence.getSent()) && this.getStart()==sentence.getStart()&& this.getEnd()==sentence.getEnd()){
			isContain =true;
		}
		return isContain;
	}
	
	public String getSent(){
		return this.sent;
	}
	
	public void setStart(int start){
		this.start=start;
	}
	public int getStart(){
		return this.start;
	}
	
	public void setEnd(int end){
		this.end=end;
	}
	public int getEnd(){
		return this.end;
	}
	
}
