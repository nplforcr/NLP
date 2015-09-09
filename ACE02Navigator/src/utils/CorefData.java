package utils;


public class CorefData implements Comparable{
	private volatile int hashCode = 0; // (See Item 48)
	
	int corefIndex;
	String corefWord;
	int anaphaIndex;
	String anaphor;
	
	public String toString(){
		return corefIndex+" "+corefWord+" "+anaphaIndex+" "+anaphor;
	}
	
	public void setCorefIndex(int corefInd){
		corefIndex = corefInd;
	}
	public void setCorefWord(String corefWd){
		corefWord = corefWd.toLowerCase();
	}
	public void setAnaIndex(int anaInd){
		anaphaIndex = anaInd;
	}
	public void setAnaphor(String anaWord){
		anaphor = anaWord.toLowerCase();
	}
	
	public int getCorefIndex(){
		return corefIndex;
	}
	
	public String getCorefWord(){
		return corefWord;
	}
	
	public int getAnaIndex(){
		return anaphaIndex;
	}
	
	public String getAnaWord(){
		return anaphor;
	}
	
	
	/* Overload compareTo method */

	public int compareTo(Object obj)
	{
		CorefData tmp = (CorefData)obj;
		
		if(this.anaphaIndex<tmp.anaphaIndex){
			return -1;
		}
		else if(this.anaphaIndex == tmp.anaphaIndex && this.corefIndex < tmp.corefIndex)
		{
			/* instance lt received */
			return -1;
		}else if(this.anaphaIndex>tmp.anaphaIndex){
			return 1;
		}
		else if(this.anaphaIndex == tmp.anaphaIndex && this.corefIndex > tmp.corefIndex)
		{
			/* instance gt received */
			return 1;
		}
		/* instance == received */
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof CorefData))
			return false;
		CorefData pn = (CorefData)o;
		return pn.anaphaIndex == this.anaphaIndex &&
		pn.corefIndex == this.corefIndex;
	}

	public int hashCode() {
		if (hashCode == 0) {
			int result = 17;
			result = 37*result + this.anaphaIndex;
			result = 37*result + this.corefIndex;
			hashCode = result;
		}
		return hashCode;
	}	

}
