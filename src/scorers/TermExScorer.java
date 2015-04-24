/**
 * 
 */
package scorers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;

import utils.MathUtils;
import utils.StringUtils;

/**
 * @author Admin
 *
 */
public class TermExScorer implements TermScorer {
	
	/**
	 *  TermEx = w(t,D)=alpha*DR+beta*DC+gama*LC
	   	Where: DR = freq(t,D)/max(freq(t,otherDs)
	   		   DC = -sum(norm_freq(t,d)*log(norm_freq(t,d) (entropy)
	   		   LC = (|T|*freq(t,D)*log(freq(t,D)))/sum(freq(W))
	 * @throws IOException 
	 */
	public double score(String term) throws IOException {
		int n = term.split("\t")[0].split(" ").length;
		m_specialistCorpusSearcher = SearchersInit.getspecialistIndexSearcher(n);
		m_generalCorpusSearcher = SearchersInit.getGeneralIndexSearcher(n);
		
		LinkedList<String> termsList = StringUtils.String2PhraseList(term);
		int ftSp = 0, ftG = 0;
		for(String q:termsList){
			ftSp += m_specialistCorpusSearcher.termStatistics(new Term(Constants.field,q),TermContext.build(m_specialistCorpusSearcher.getIndexReader().getContext(), new Term(Constants.field,q))).totalTermFreq();
			ftG += m_generalCorpusSearcher.termStatistics(new Term(Constants.field,q),TermContext.build(m_generalCorpusSearcher.getIndexReader().getContext(), new Term(Constants.field,q))).totalTermFreq();
		}
		double DR = (double)ftSp/ftG;
		
		// extract the set of documents containing term t
		BooleanQuery query = new BooleanQuery();
		for(String q:termsList)
			query.add(new BooleanClause(new TermQuery(new Term(Constants.field,q)), Occur.SHOULD));
		TopDocs td = m_specialistCorpusSearcher.search(query, 100000);
		double sum = 0;
		for (ScoreDoc scoreDoc : td.scoreDocs) {
			int tf = 0;
			int d = 0;
			int docId = scoreDoc.doc;
			Fields termVector = m_specialistCorpusSearcher.getIndexReader().getTermVectors(docId);
			Terms terms = termVector.terms(Constants.field);
			TermsEnum te = terms.iterator(null);
			while (te.next() != null) {
				// recognize one of the query's terms
				if (termsList.contains(te.term().utf8ToString()))
					tf+=te.totalTermFreq();
				d+=te.totalTermFreq();
			}
			double norm_freq = (double)tf/(double)d;
			sum += norm_freq*MathUtils.Log(norm_freq,2);
		}
		double DC = -sum;
		
			
		int sumW = 0;
		m_specialistCorpusSearcher = SearchersInit.getspecialistIndexSearcher(1);
		
		List<Set<String>> listSet = StringUtils.String2ListSet(term);
		for (Set<String> expSet:listSet){
			int ws = 0;
			for(String q:expSet){
				ws += m_specialistCorpusSearcher.termStatistics(new Term(Constants.field,q),TermContext.build(m_specialistCorpusSearcher.getIndexReader().getContext(), new Term(Constants.field,q))).totalTermFreq();
			}
			sumW += ws;
		}
		
		double LC = (n*ftSp*MathUtils.Log((double)ftSp, 2))/sumW;
		return alpha*DR+beta*DC+gama*LC;
	}
	
	
	private IndexSearcher m_generalCorpusSearcher = null;
	private IndexSearcher m_specialistCorpusSearcher = null;
	private double alpha = 0.333;
	private double beta = 0.333;
	private double gama = 0.333;

}
