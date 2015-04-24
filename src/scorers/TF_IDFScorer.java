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
public class TF_IDFScorer implements TermScorer{

	/**
	 * tf-idf = (tf/W)(log(|G|/|Gc|))
	 */
	public double score(String term) throws IOException {
		int n = term.split("\t")[0].split(" ").length;
		m_generalCorpusSearcher = SearchersInit.getGeneralIndexSearcher(n);
		
		RFScorer rf = new RFScorer();
		LinkedList<String> termsList = StringUtils.String2PhraseList(term);
		double rfScore = rf.score(term);
		int gf = 0;
		for(String q:termsList){
			gf += m_generalCorpusSearcher.termStatistics(new Term(Constants.field,q),TermContext.build(m_generalCorpusSearcher.getIndexReader().getContext(), new Term(Constants.field,q))).totalTermFreq();
		}
		int countCorpora = 0;
		if (rfScore!=0)
			countCorpora++;
		if (gf!=0)
			countCorpora++;
		
		return rfScore*MathUtils.Log((double)2/countCorpora, 2);
	}
	
	IndexSearcher m_generalCorpusSearcher = null;
}
