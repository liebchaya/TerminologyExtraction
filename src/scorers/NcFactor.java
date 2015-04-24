package scorers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

import utils.StringUtils;

public class NcFactor {

	public NcFactor(IndexSearcher searcher,File CvalueFile, double topPercent, int window, int contextWordNum) throws IOException {
		m_searcher = searcher;
		m_NcFactorContexTermWeight = new NcFactorContextTermWeight(searcher, CvalueFile, topPercent, window, contextWordNum);
	}
	
	public double calcNcFactor(String term, int window) throws IOException{
		List<Set<String>> queryList = StringUtils.String2ListSet(term);
		SpanQuery query;
		if (queryList.size() == 1){
			if(queryList.get(0).size() == 1)
				query = new SpanTermQuery(new Term(Constants.field,term));
			else {
				query = new SpanOrQuery();
				for (String t:queryList.get(0))
					((SpanOrQuery) query).addClause(new SpanTermQuery(new Term(Constants.field,t)));
//					((BooleanQuery) query).add(new BooleanClause(new TermQuery(new Term(Constants.field,t)), Occur.SHOULD));
			}
		}
		
		else {
			// start with the query
			// extract the set of documents containing term t
			SpanQuery[] andQ = new SpanQuery[queryList.size()];
			int i=0;
			for(Set<String> queryTerms:queryList){
				if (queryTerms.size() > 1) {
					SpanOrQuery orQ = new SpanOrQuery();
					for(String q:queryTerms)
						orQ.addClause(new SpanTermQuery(new Term(Constants.field,q)));
					andQ[i] = orQ;
				} else {
					SpanQuery tq = new SpanTermQuery(new Term(Constants.field,(String)queryTerms.toArray()[0]));
					andQ[i] = tq;
				}
				i++;
			}
			query = new SpanNearQuery(andQ,0,true);
		}
		HashMap<String, Long> contextMap = generateContextData(query,window);
		double factor = 0;
		for(String cw:m_NcFactorContexTermWeight.getAllContextWords()){
			if(contextMap.containsKey(cw))
				factor += contextMap.get(cw)*m_NcFactorContexTermWeight.getContextWordWeight(cw);
		}
		return factor;
		
	}
	
	private HashMap<String,Long> generateContextData(SpanQuery q, int window) throws IOException {
		HashMap<String,Long> contextMap = new HashMap<String, Long>();
		IndexReader reader = m_searcher.getIndexReader();
		AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(reader);
		Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
		Spans spans = q.getSpans(wrapper.getContext(), new Bits.MatchAllBits(reader.numDocs()), termContexts);
		while (spans.next() == true) {
//			System.out.println("Doc: " + spans.doc() + " Start: " + spans.start() + " End: " + spans.end());
			int start = spans.start() - window;
			int end = spans.end() + window;
			Terms content = reader.getTermVector(spans.doc(), Constants.field);
			TermsEnum termsEnum = content.iterator(null);
			BytesRef term;
			while ((term = termsEnum.next()) != null) {
				//could store the BytesRef here, but String is easier for this example
//				String s = new String(term.bytes, term.offset, term.length);
				String s = term.utf8ToString();
				DocsAndPositionsEnum positionsEnum = termsEnum.docsAndPositions(null, null);
				if (positionsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
					int i = 0;
					int position = -1;
					while (i < positionsEnum.freq() && (position = positionsEnum.nextPosition()) != -1) {
						if (position >= start && position <= end) {
							if (!(position >= spans.start() && position < spans.end())){
								if (contextMap.containsKey(s)) {
									long freq = contextMap.get(s)+1;
									contextMap.put(s, freq);
								} else
									contextMap.put(s, (long) 1);
							}
						}
						i++;
					}
				}
			}
		}
		return contextMap;
		
	}
	
	

	
	private IndexSearcher m_searcher=null;
	private NcFactorContextTermWeight m_NcFactorContexTermWeight=null;
	
 
}
