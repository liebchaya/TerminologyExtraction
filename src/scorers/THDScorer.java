package scorers;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;

import utils.StringUtils;

/**
 * 
 * @author Admin
 *
 */
public class THDScorer implements TermScorer{

	/**
	 * thd = r(s)/|Ws|-r(g)/|Wg|
	 * maximal rank of query's morphological variants
	 */
	public double score(String term) throws IOException {
		int n = term.split("\t")[0].split(" ").length;
		m_specialistCorpusSearcher = SearchersInit.getspecialistIndexSearcher(n);
		long Ws = MultiFields.getFields(m_specialistCorpusSearcher.getIndexReader()).terms(Constants.field).size();
		m_generalCorpusSearcher = SearchersInit.getGeneralIndexSearcher(n);
		long Wg = MultiFields.getFields(m_generalCorpusSearcher.getIndexReader()).terms(Constants.field).size();
		
		long rs = 0, rg = 0;
		LinkedList<String> termsList = StringUtils.String2PhraseList(term);
		for(String t:termsList){
			rs = Math.max(m_speciaListRankMap.get(t),rs);
			rg = Math.max(m_generalListRankMap.get(t),rg);
		}
		double thd = ((double)rs/Ws)-((double)rg/Wg);
		return thd;
	}
	
	public void initRankMaps() throws IOException{
		m_generalListRankMap = generateRankMap(m_generalCorpusSearcher);
		m_speciaListRankMap = generateRankMap(m_specialistCorpusSearcher);
	}
	
	private HashMap<String, Long> generateRankMap(IndexSearcher searcher) throws IOException{
		TreeMap<Long,LinkedList<String>> freqMap = new TreeMap<Long, LinkedList<String>>();
		IndexReader reader = searcher.getIndexReader();
		Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms(Constants.field);
        TermsEnum termsEnum = terms.iterator(null);
        while (termsEnum.next() != null) {
        	long freq = termsEnum.totalTermFreq();
        	if(freqMap.containsKey(freq))
        		freqMap.get(freq).add(termsEnum.term().utf8ToString());
        	else {
        		LinkedList<String> termsList = new LinkedList<String>();
        		termsList.add(termsEnum.term().utf8ToString());
        		freqMap.put(freq, termsList);
        	}
        }
        long V = terms.size();
        long rank = V;
        HashMap<String, Long> rankMap = new HashMap<String, Long>();
        for (Long freq:freqMap.descendingKeySet()) {
        	for (String term:freqMap.get(freq)) {
        		rankMap.put(term, rank);
        	}
        	rank -= freqMap.get(freq).size();
        }
        return rankMap;
	}
	
	private IndexSearcher m_specialistCorpusSearcher = null;
	private HashMap<String, Long> m_speciaListRankMap = null;
	
	private IndexSearcher m_generalCorpusSearcher = null;
	private HashMap<String, Long> m_generalListRankMap = null;
}
