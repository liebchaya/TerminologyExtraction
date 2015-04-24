/**
 * 
 */
package scorers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;

import utils.MathUtils;
import utils.StringUtils;

/**
 * @author Admin
 *
 */
public class GlossExScorer implements TermScorer {
	
	/**
	 *  GlossEx = C(T)=alpha*TD(T)+beta*TC(T)
	   	Where: TD(T) = sum(log(ps(W))/pg(W))/|T|
	   		   TC(T) = ((|T|*log(ft)*ft)/sum(fw))*percent for unigrams
	 * @throws IOException 
	 */
	public double score(String term) throws IOException {
		double sum = 0;
		int sumW = 0;
		m_specialistCorpusSearcher = SearchersInit.getspecialistIndexSearcher(1);
		m_generalCorpusSearcher = SearchersInit.getGeneralIndexSearcher(1);
		long ts = m_specialistCorpusSearcher.collectionStatistics(Constants.field).sumTotalTermFreq();
		long tg = m_generalCorpusSearcher.collectionStatistics(Constants.field).sumTotalTermFreq();
		
		List<Set<String>> listSet = StringUtils.String2ListSet(term);
		for (Set<String> expSet:listSet){
			int ws = 0, wg = 0;
			for(String q:expSet){
				ws += m_specialistCorpusSearcher.termStatistics(new Term(Constants.field,q),TermContext.build(m_specialistCorpusSearcher.getIndexReader().getContext(), new Term(Constants.field,q))).totalTermFreq();
				wg += m_generalCorpusSearcher.termStatistics(new Term(Constants.field,q),TermContext.build(m_generalCorpusSearcher.getIndexReader().getContext(), new Term(Constants.field,q))).totalTermFreq();
			}
			double ps = (double)ws/ts;
			sumW += ws;
			double pg = (double)wg/tg;
			sum += MathUtils.Log(ps/pg,2);
		}
		int T = term.split("\t")[0].split(" ").length;
		double td = sum/T;
		
		TFScorer TfScorer = new TFScorer();
		double ft = TfScorer.score(term);
		
		double ct = (T*Math.log(ft)*ft)/sumW;
		int n = term.split("\t")[0].split(" ").length;
		if (n==1)
			ct = ct*unigram_percent;
		return alpha*td+beta*ct;
	}
	
	
	private IndexSearcher m_generalCorpusSearcher = null;
	private IndexSearcher m_specialistCorpusSearcher = null;
	private double alpha = 0.5;
	private double beta = 0.5;
	private double unigram_percent = 0.1;
	

}
