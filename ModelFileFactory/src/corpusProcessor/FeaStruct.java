package corpusProcessor;

public class FeaStruct{
	String operator;
	String feaP;
	String fea;
	
	public FeaStruct(String oper,String oldFea, String curFea){
		operator = oper;
		feaP = oldFea;
		fea = curFea;
		
	}

	public String getOper(){
		return operator;
	}
	
	public String getFeaP(){
		return feaP;
	}
	
	public String getFea(){
		return fea;
	}

}
