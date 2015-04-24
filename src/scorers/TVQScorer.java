package scorers;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.Bits;

import utils.StringUtils;

/**
 * 
 * @author Admin
 *
 */
public class TVQScorer implements TermScorer{

	/**
	 * tvq = sum(pow(fd)-(1/D*pow(sum(fd)))
	 * avgF, the average number of occurrences per document
	 */
	public double score(String term) throws IOException {
		int n = term.split("\t")[0].split(" ").length;
		m_specialistCorpusSearcher = SearchersInit.getspecialistIndexSearcher(n);
		long D = m_specialistCorpusSearcher.collectionStatistics(Constants.field).docCount();
		
		LinkedList<String> termsList = StringUtils.String2PhraseList(term);
		SpanOrQuery fullQuery = new SpanOrQuery();
		for(String t:termsList){
			fullQuery.addClause(new SpanTermQuery(new Term(Constants.field,t)));
		}
		HashMap<Integer,Integer> spanDocsMap = new HashMap<Integer, Integer>();
		IndexReader reader = m_specialistCorpusSearcher.getIndexReader();
		AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(reader);
		Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
		Spans spans = fullQuery.getSpans(wrapper.getContext(), new Bits.MatchAllBits(reader.numDocs()), termContexts);
		while (spans.next() == true) {
			if(spanDocsMap.containsKey(spans.doc())) {
				int count = spanDocsMap.get(spans.doc());
				spanDocsMap.put(spans.doc(),count+1);
			} else
				spanDocsMap.put(spans.doc(),1);
		}
		int sum = 0;
		double sigma = 0;
		for(Integer docId:spanDocsMap.keySet())
			sum += spanDocsMap.get(docId);
		
		for(Integer docId:spanDocsMap.keySet())
			sigma += Math.pow(spanDocsMap.get(docId),2);
		
		double tvq = sigma - (((double)1/D)*Math.pow(sum,2));
		return tvq;

	}
	
	private IndexSearcher m_specialistCorpusSearcher = null;
}
