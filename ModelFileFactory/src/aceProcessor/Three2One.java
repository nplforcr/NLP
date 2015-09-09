package aceProcessor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Three2One {
	
	static String[] legalFileNames = {"bnews","npaper","nwire"}; 
	
	public static void combineACECorpus(File aceFiles, File movedDir){
		List legalFileNameList = Arrays.asList(legalFileNames);
		System.out.println(aceFiles);
		File[] subDirs = null;
		System.out.println(aceFiles.isDirectory());
		if(aceFiles.isDirectory()){
			System.out.println("is here? ");
			subDirs = aceFiles.listFiles();
		}
		
		System.out.println(subDirs.length);
		for(int i=0;i<subDirs.length;i++){
			System.out.println(subDirs[i]);
			File ithSubDir = subDirs[i];
			File movedTestDir = new File(movedDir,ithSubDir.getName());
			if(!movedTestDir.exists()){
				movedTestDir.mkdirs();
			}
			if(ithSubDir.getName().contains("devtest")){
				File[] testDirs = ithSubDir.listFiles();
				int count = 0;
				while(count<testDirs.length){
					if(testDirs[count].getName().contains("three2one")){
						count++;
						continue;
					}else{
						File[] testDatas = testDirs[count].listFiles();
						if(!legalFileNameList.contains(testDirs[count].getName())){
							count++;
							continue;
						}
						System.out.println(testDirs[count]);
						for(int j=0;j<testDatas.length;j++){
							try {
							      FilesCopy.copy(testDatas[j].getAbsolutePath(), movedTestDir.getAbsolutePath());
							    } catch (IOException e) {
							      System.err.println(e.getMessage());
							    }
						}
						
					}
					count++;
				}
			}else if(ithSubDir.getName().contains("train")){
				File[] trainDirs = ithSubDir.listFiles();
				int count = 0;
				while(count<trainDirs.length){
					if(trainDirs[count].getName().contains("three2one")){
						count++;
						continue;
					}else{
						File[] testDatas = trainDirs[count].listFiles();
						if(!legalFileNameList.contains(trainDirs[count].getName())){
							count++;
							continue;
						}
						System.out.println(trainDirs[count]);
						for(int j=0;j<testDatas.length;j++){
							try {
							      FilesCopy.copy(testDatas[j].getAbsolutePath(), movedTestDir.getAbsolutePath());
							    } catch (IOException e) {
							      System.err.println(e.getMessage());
							    }
						}
						
					}
					count++;
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		///Users/dingcheng/Documents/corpora/ace_phase2/data/
		///Users/lixxx345/Documents/corpora/ace_phase2/data/
		
		File aceFiles=new File(args[0]);
		File movedDir = new File(args[1]);
		
		Three2One.combineACECorpus(aceFiles, movedDir);

	}
}
