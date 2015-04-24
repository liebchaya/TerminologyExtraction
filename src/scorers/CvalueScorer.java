package scorers;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;

import utils.MathUtils;
import utils.StringUtils;

/**
 * 
 * @author Admin
 *
 */
public class CvalueScorer implements TermScorer{

	/**
	 * C-Value = c*f(a), a is not nested
	 * c = 1 + log(|a|)
	 * f(a) = frequency
	 */
	public double score(String term) throws IOException {
		int n = term.split("\t")[0].split(" ").length;
		m_specialistCorpusSearcher = SearchersInit.getspecialistIndexSearcher(n);
		
		LinkedList<String> termsList = StringUtils.String2PhraseList(term);
		int f = 0;
		for(String q:termsList){
			f += m_specialistCorpusSearcher.termStatistics(new Term(Constants.field,q),TermContext.build(m_specialistCorpusSearcher.getIndexReader().getContext(), new Term(Constants.field,q))).totalTermFreq();
		}
		return (1+MathUtils.Log(n, 2))*f;
	}
	
	IndexSearcher m_specialistCorpusSearcher = null;
}
