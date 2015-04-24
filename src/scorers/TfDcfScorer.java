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
public class TfDcfScorer implements TermScorer{

	/**
	 * tf-dcf = sf/(1+log(1+gf))
	 */
	public double score(String term) throws IOException {
		int n = term.split("\t")[0].split(" ").length;
		m_generalCorpusSearcher = SearchersInit.getGeneralIndexSearcher(n);
		
		TFScorer sf = new TFScorer();
		LinkedList<String> termsList = StringUtils.String2PhraseList(term);
		int gf = 0;
		for(String q:termsList){
			gf += m_generalCorpusSearcher.termStatistics(new Term(Constants.field,q),TermContext.build(m_generalCorpusSearcher.getIndexReader().getContext(), new Term(Constants.field,q))).totalTermFreq();
		}
		
		return sf.score(term)/(1+MathUtils.Log(1+gf,2));
	}
	

	IndexSearcher m_generalCorpusSearcher = null;
}
