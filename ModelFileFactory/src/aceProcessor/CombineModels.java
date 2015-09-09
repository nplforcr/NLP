package aceProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CombineModels {
	
	public static void integrateModels(String inputDir) throws IOException{
		PrintWriter[] modelWriters = null;
		File inputFile = new File(inputDir);
		File outputDir = new File(inputFile,"ace_model");
		if(!outputDir.exists()){
			outputDir.mkdirs();
		}
		File[] modelFileDir = inputFile.listFiles();
		boolean writeyet = false;
		for(int i=0;i<modelFileDir.length;i++){
			File subdir=modelFileDir[i];
			if(subdir.getName().contains("model") && !subdir.getName().equals("ace_model")){
				File[] modelFiles=subdir.listFiles();
				if(writeyet==false){
					modelWriters = new PrintWriter[modelFiles.length];
					for(int j=0;j<modelFiles.length;j++){
						File jthModelFile=modelFiles[j];
						PrintWriter jthWriter = new PrintWriter(new FileWriter(new File(outputDir,
								(jthModelFile.getName().startsWith("pw")?(jthModelFile.getName().substring(0,jthModelFile.getName().indexOf("."))+".ace.tally")
										:"ace"+jthModelFile.getName().substring(jthModelFile.getName().indexOf(".")))),true));
							BufferedReader brJthModel = new BufferedReader(new FileReader(jthModelFile));
							String line = "";
							while((line=brJthModel.readLine())!=null){
								jthWriter.println(line);
							}
							modelWriters[j]=jthWriter;
					}
					writeyet=true;
				}else{
					for(int j=0;j<modelFiles.length;j++){
						File jthModelFile=modelFiles[j];
						PrintWriter jthWriter = modelWriters[j];
						BufferedReader brJthModel = new BufferedReader(new FileReader(jthModelFile));
						String line = "";
						while((line=brJthModel.readLine())!=null){
							jthWriter.println(line);
						}
					}
					
				}
				
			}
		}
		for(int i=0;i<modelWriters.length;i++){
			modelWriters[i].close();
		}
		
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		CombineModels.integrateModels(args[0]);
		
	}

}
