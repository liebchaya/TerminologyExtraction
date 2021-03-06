package scorers;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;

import utils.StringUtils;

/**
 * 
 * @author Admin
 *
 */
public class RFScorer implements TermScorer{

	/**
	 * relative term frequency
	 */
	public double score(String term) throws IOException {
		int n = term.split("\t")[0].split(" ").length;
		m_specialistCorpusSearcher = SearchersInit.getspecialistIndexSearcher(n);
		long W = m_specialistCorpusSearcher.collectionStatistics(Constants.field).sumTotalTermFreq();
		
		LinkedList<String> termsList = StringUtils.String2PhraseList(term);
		int f = 0;
		for(String q:termsList){
			f += m_specialistCorpusSearcher.termStatistics(new Term(Constants.field,q),TermContext.build(m_specialistCorpusSearcher.getIndexReader().getContext(), new Term(Constants.field,q))).totalTermFreq();
		}
		return (double)f/W;
	}
	
	IndexSearcher m_specialistCorpusSearcher = null;
}
