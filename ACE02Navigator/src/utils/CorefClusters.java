package utils;



public class CorefClusters implements Comparable{
	private volatile int hashCode = 0; // (See Item 48)
	
	int corefIndex;
	String corefWord;
	IntArray corefIntArray;
	int anaphaIndex;
	String anaphor;
	IntArray anaphorIntArray;
	
	public String toString(){
		return corefIndex+" "+corefWord+" "+anaphaIndex+" "+anaphor;
	}
	
	public void setCorefIndex(int corefInd){
		this.corefIndex = corefInd;
	}
	public void setCorefWord(String corefWd){
		this.corefWord = corefWd.toLowerCase();
	}
	public void setCorefIntArray(IntArray corefIntArr){
		this.corefIntArray = corefIntArr;
	}
	
	public void setAnaIndex(int anaInd){
		this.anaphaIndex = anaInd;
	}
	public void setAnaphor(String anaWord){
		this.anaphor = anaWord.toLowerCase();
	}
	public void setAnaIntArray(IntArray anaIntArr){
		this.anaphorIntArray = anaIntArr;
	}
	
	public int getCorefIndex(){
		return corefIndex;
	}
	
	public String getCorefWord(){
		return corefWord;
	}
	
	public IntArray getCorefIntArray(){
		return corefIntArray;
	}
	
	public int getAnaIndex(){
		return anaphaIndex;
	}
	
	public String getAnaWord(){
		return anaphor;
	}
	
	public IntArray getAnaIntArray(){
		return anaphorIntArray;
	}
	
	/* Overload compareTo method */

	public int compareTo(Object obj)
	{
		CorefClusters tmp = (CorefClusters)obj;
		
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
		if (!(o instanceof CorefClusters))
			return false;
		CorefClusters pn = (CorefClusters)o;
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
