/**
 * 
 */
package scorers;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;

import utils.StringUtils;

/**
 * @author Admin
 *
 */
public class WeirdnessScorer implements TermScorer {
	
	/**
	 * Weirdness = (ws/ts)/(wg/tg)
	   Where: ws = frequency of word in specialist language corpus
	          wg = frequency of word in general language corpus
			  ts = total count of words in specialist language corpus
			  tg = total count of words in general language corpus
	 * @throws IOException 
	 */
	public double score(String term) throws IOException {
		int n = term.split("\t")[0].split(" ").length;
		m_specialistCorpusSearcher = SearchersInit.getspecialistIndexSearcher(n);
		m_generalCorpusSearcher = SearchersInit.getGeneralIndexSearcher(n);
		long ts = m_specialistCorpusSearcher.collectionStatistics(Constants.field).sumTotalTermFreq();
		long tg = m_generalCorpusSearcher.collectionStatistics(Constants.field).sumTotalTermFreq();
		
		LinkedList<String> termsList = StringUtils.String2PhraseList(term);
		int ws = 0;
		for(String q:termsList){
			ws += m_specialistCorpusSearcher.termStatistics(new Term(Constants.field,q),TermContext.build(m_specialistCorpusSearcher.getIndexReader().getContext(), new Term(Constants.field,q))).totalTermFreq();
		}
		
		int wg = 0;
		for(String q:termsList){
			wg += m_generalCorpusSearcher.termStatistics(new Term(Constants.field,q),TermContext.build(m_generalCorpusSearcher.getIndexReader().getContext(), new Term(Constants.field,q))).totalTermFreq();
		}
		
		return ((double)ws/ts)/((double)wg/tg);
	}
	
	
	
	IndexSearcher m_generalCorpusSearcher = null;
	IndexSearcher m_specialistCorpusSearcher = null;
	

}
