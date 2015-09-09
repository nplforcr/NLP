package corpusProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListFunction {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		List<String> aList = new ArrayList<String>();
		aList.add("what");
		aList.add("the");
		aList.add("hell");
		aList.add("is");
		aList.add("going");
		aList.add("on");
		//aList.remove(1);
		
		List<String> bList = new ArrayList<String>();
		bList.add("what");
		bList.add("hell");
		bList.add("is");
		bList.add("going");
		bList.add("on");
		bList.add("?");
		
		
		for(int i=0;i<aList.size();i++){
			String itemA = aList.get(i);
			String itemB = bList.get(i);
			System.out.println(itemA+" "+itemB);
			
			if(!itemA.equals(itemB)){
			//if(itemA.compareTo(itemB)==1){
				System.out.println("find the one different in aList: "+itemA+" "+i);
				break;
			}
			else if(itemA.equals(itemB) && i==aList.size()){
				System.out.println("all the front are equal: "+itemA);
				break;
			}

		}
//		
//		String [] strArray = {"what","the","hell","is","going","on"};
//		List<String> arrayList = new ArrayList<String>();
//		arrayList = Arrays.asList(strArray);
//		//the arrayList is a fixed sized list which size cannot be changed
//		System.out.println(arrayList.set(2,"good"));

	}

}
