package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import scorers.SearchersInit;

public class GenerateScoredFiles {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
	
		String targetTermsFile = "F:\\ScoredFile\\FinalTestSet_morph.txt";
//		String targetTermsFile = "F:\\ScoredFile\\AllTerms_morph.txt";
		File termExpScoredFile = new File("F:\\ScoredFileTest\\TerminologyExtraction\\Test");
		
		if(!termExpScoredFile.exists())
			termExpScoredFile.mkdirs();
		
		SearchersInit.init();
			
		String [] termExScorers = {"TF_IDFScorer"};
//		String [] termExScorers = {"TFScorer","DFScorer","WeirdnessScorer","GlossExScorer","CvalueScorer","NCvalueScorer","ATFScorer","RFScorer","RIDFScorer","TCScorer","TermExScorer"};
		
		for(String termScorer:termExScorers){
			
			BufferedReader fileReader = new BufferedReader(new FileReader(targetTermsFile));
			int lineCount=0;
			Class<?> c = Class.forName("scorers."+termScorer);
			Constructor<?> cons[] = c.getConstructors();
			Object scorer;
			
			if(termScorer.equals("NCvalueScorer"))
				scorer =  cons[0].newInstance(new File("F:\\ScoredFileTest\\TerminologyExtraction\\Cvalue.txt"));
			else
				scorer =  cons[0].newInstance();
			
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(termExpScoredFile.getAbsolutePath()+"\\"+termScorer.replaceAll("Scorer", "")+".txt"));
			String line = fileReader.readLine();
			while (line!=null){
				lineCount++;
				double score = 0;
				Method m = scorer.getClass().getMethod("score", new Class[]{String.class});
				score = (Double) m.invoke(scorer, line);
				fileWriter.write(line.split("\t")[0]+"\t" + score +"\n");
				if(lineCount%50==0){
					System.out.println(termScorer + ": " + lineCount);
					fileWriter.flush();
				}
				line = fileReader.readLine();
			}
			fileReader.close();
			fileWriter.close();
		}
	}
		
}