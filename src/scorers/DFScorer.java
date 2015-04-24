package scorers;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.TermQuery;

import utils.StringUtils;

/**
 * 
 * @author Admin
 *
 */
public class DFScorer implements TermScorer{

	/**
	 * document frequency
	 */
	public double score(String term) throws IOException {
		int n = term.split("\t")[0].split(" ").length;
		m_specialistCorpusSearcher = SearchersInit.getspecialistIndexSearcher(n);
		
		LinkedList<String> termsList = StringUtils.String2PhraseList(term);
		BooleanQuery fullQuery = new BooleanQuery();
		for(String t:termsList){
			fullQuery.add(new TermQuery(new Term(Constants.field,t)),Occur.SHOULD);
		}
		int docFreq = m_specialistCorpusSearcher.search(fullQuery, 100000).totalHits;
		return (double)docFreq;
	}
	
	IndexSearcher m_specialistCorpusSearcher = null;
}
