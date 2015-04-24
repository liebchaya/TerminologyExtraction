package scorers;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.LMDirichletSimilarityAccurateDocLength;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SearchersInit {

	public static void init() throws IOException {
		Directory dir = FSDirectory.open(new File("F:/Responsa/indexes/unigDirichletAccurate"));
		DirectoryReader reader = DirectoryReader.open(dir);
		m_unigSearcher = new IndexSearcher(reader);
		Similarity sim = new LMDirichletSimilarityAccurateDocLength(1000);
		m_unigSearcher.setSimilarity(sim);
	
		dir = FSDirectory.open(new File("F:/Responsa/indexes/bigramDirichletAccurate"));
		reader = DirectoryReader.open(dir);
		m_bigramSearcher = new IndexSearcher(reader);
		sim = new LMDirichletSimilarityAccurateDocLength(1000);
		m_bigramSearcher.setSimilarity(sim);
	
		dir = FSDirectory.open(new File("F:/Responsa/indexes/triDirichletAccurate"));
		reader = DirectoryReader.open(dir);
		m_trigramSearcher = new IndexSearcher(reader);
		sim = new LMDirichletSimilarityAccurateDocLength(1000);
		m_trigramSearcher.setSimilarity(sim);
	
		dir = FSDirectory.open(new File("F:/Responsa/indexes/fourDirichletAccurate"));
		reader = DirectoryReader.open(dir);
		m_fourgramSearcher = new IndexSearcher(reader);
		sim = new LMDirichletSimilarityAccurateDocLength(1000);
		m_fourgramSearcher.setSimilarity(sim);

		
		dir = FSDirectory.open(new File("I:/Prediction/Responsa/indexes/unigWikiDirichletAccurate"));
		reader = DirectoryReader.open(dir);
		m_unigWikiSearcher = new IndexSearcher(reader);
		sim = new LMDirichletSimilarityAccurateDocLength(1000);
		m_unigWikiSearcher.setSimilarity(sim);
	
		dir = FSDirectory.open(new File("I:/Prediction/Responsa/indexes/bigramWikiDirichletAccurate"));
		reader = DirectoryReader.open(dir);
		m_bigramWikiSearcher = new IndexSearcher(reader);
		sim = new LMDirichletSimilarityAccurateDocLength(1000);
		m_bigramWikiSearcher.setSimilarity(sim);
		
		dir = FSDirectory.open(new File("I:/Prediction/Responsa/indexes/trigramWikiDirichletAccurate"));
		reader = DirectoryReader.open(dir);
		m_trigramWikiSearcher = new IndexSearcher(reader);
		sim = new LMDirichletSimilarityAccurateDocLength(1000);
		m_trigramWikiSearcher.setSimilarity(sim);
		
		dir = FSDirectory.open(new File("I:/Prediction/Responsa/indexes/fourgramWikiDirichletAccurate"));
		reader = DirectoryReader.open(dir);
		m_fourgramWikiSearcher = new IndexSearcher(reader);
		sim = new LMDirichletSimilarityAccurateDocLength(1000);
		m_fourgramWikiSearcher.setSimilarity(sim);
}
	
	public static IndexSearcher getGeneralIndexSearcher(int n) {
		switch(n){
			case 1: m_currWikiSearcher = m_unigWikiSearcher; break;
			case 2: m_currWikiSearcher = m_bigramWikiSearcher; break;
			case 3: m_currWikiSearcher = m_trigramWikiSearcher; break;
			case 4: m_currWikiSearcher = m_fourgramWikiSearcher; break;
			default: m_currWikiSearcher = null; break;
		}	
		return  m_currWikiSearcher;
	}
	
	public static IndexSearcher getspecialistIndexSearcher(int n) {
		switch(n){
		case 1: m_currSearcher = m_unigSearcher; break;
		case 2: m_currSearcher = m_bigramSearcher; break;
		case 3: m_currSearcher = m_trigramSearcher; break;
		case 4: m_currSearcher = m_fourgramSearcher; break;
		default: m_currSearcher = null; break;
	}	
		return  m_currSearcher;
	}

private static IndexSearcher m_unigSearcher;
private static IndexSearcher m_bigramSearcher;
private static IndexSearcher m_trigramSearcher;
private static IndexSearcher m_fourgramSearcher;

private static IndexSearcher m_unigWikiSearcher;
private static IndexSearcher m_bigramWikiSearcher;
private static IndexSearcher m_trigramWikiSearcher;
private static IndexSearcher m_fourgramWikiSearcher;

private static IndexSearcher m_currSearcher;
private static IndexSearcher m_currWikiSearcher;

}