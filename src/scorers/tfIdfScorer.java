package scorers;

import java.io.IOException;

import org.apache.lucene.search.IndexSearcher;

import utils.MathUtils;

/**
 * 
 * @author Admin
 *
 */
public class tfIdfScorer implements TermScorer{

	/**
	 * tf-idf = (1+log(tf))(log(1+D/df))
	 */
	public double score(String term) throws IOException {
		int n = term.split("\t")[0].split(" ").length;
		m_specialistCorpusSearcher = SearchersInit.getspecialistIndexSearcher(n);
		long D = m_specialistCorpusSearcher.collectionStatistics(Constants.field).docCount();
		TFScorer tf = new TFScorer();
		DFScorer df = new DFScorer();
		return (1+MathUtils.Log(tf.score(term), 2))*(MathUtils.Log(1+(double)D/df.score(term), 2));
	}
	
	IndexSearcher m_specialistCorpusSearcher = null;
}
