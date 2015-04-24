package scorers;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 
 * @author Admin
 *
 */
public class NCvalueScorer implements TermScorer{

	
	public NCvalueScorer(File CvalueFile) throws IOException {
		Directory dir = FSDirectory.open(new File("F:/Responsa/indexes/PositionsIndexUnig"));
		DirectoryReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		m_ncFactor = new NcFactor(searcher,CvalueFile,0.10,2,20);
	}
	/**
	 * NC-Value = 0.8*C-Value+0.2*NcFactor
	 */
	public double score(String term) throws IOException {
		CvalueScorer cv = new CvalueScorer();
		return (0.8*cv.score(term)+0.2*m_ncFactor.calcNcFactor(term, m_window));
	}
	
	private int m_window = 2;
	private NcFactor m_ncFactor = null;
	
}
