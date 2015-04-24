package scorers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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

import utils.MapUtils;
import utils.StringUtils;

public class NcFactorContextTermWeight {

	public NcFactorContextTermWeight(IndexSearcher searcher, File CvalueFile, double topPercent, int window, int contextWordNum) throws IOException {
		m_searcher = searcher;
		Map<String, Double> CvalueMap = new HashMap<String,Double>();
		BufferedReader reader = new BufferedReader(new FileReader(CvalueFile));
		String line = reader.readLine();
		int lineCount = 0;
		while (line!=null) {
			CvalueMap.put(line.split("\t")[0],Double.parseDouble(line.split("\t")[1]));
			lineCount++;
			line = reader.readLine();
		}
		int top = (int)(lineCount*topPercent);
		reader.close();
		CvalueMap = MapUtils.sortByValueDesc(CvalueMap);
		int counter = 0;
		Map<String, Double> contexTermsMap = new HashMap<String, Double>();
		IndexReader iReader = m_searcher.getIndexReader();
		AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(iReader);
		int tw=0;
		for(String t:CvalueMap.keySet()){
			if(counter>=top)
				break;
			counter++;
			SpanQuery query = generateSpanQueryFromTerm(t);
			Set<String> curTerms = new HashSet<String>();
			Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
			Spans spans = query.getSpans(wrapper.getContext(), new Bits.MatchAllBits(iReader.numDocs()), termContexts);
			while (spans.next() == true) {
				int start = spans.start() - window;
				int end = spans.end() + window;
				Terms content = iReader.getTermVector(spans.doc(), Constants.field);
				TermsEnum termsEnum = content.iterator(null);
				BytesRef term;
				while ((term = termsEnum.next()) != null) {
					String s = term.utf8ToString();
					DocsAndPositionsEnum positionsEnum = termsEnum.docsAndPositions(null, null);
					if (positionsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
						int i = 0;
						int position = -1;
						while (i < positionsEnum.freq() && (position = positionsEnum.nextPosition()) != -1) {
							if (position >= start && position <= end) {
								if (!(position >= spans.start() && position < spans.end())){
									curTerms.add(s);
								}
							}
							i++;
						}
					}
				}
			}
			
			tw++;
			for(String curS:curTerms){
				if(contexTermsMap.containsKey(curS)) {
					double val = contexTermsMap.get(curS);
					contexTermsMap.put(curS,val+1);
				}
				else
					contexTermsMap.put(curS,(double) 1);
			}
			
		}
		contexTermsMap = MapUtils.sortByValueDesc(contexTermsMap);
		m_contexTermsMap = new HashMap<String, Double>();
		counter = 0;
		BufferedWriter w = new BufferedWriter(new FileWriter("F:\\ScoredFileTest\\TerminologyExtraction\\NCvalueExploration.txt"));
		for(String curS:contexTermsMap.keySet()) {
			counter++;
			m_contexTermsMap.put(curS, (double)contexTermsMap.get(curS)/top);
//			if(counter>=contextWordNum)
//				break;
			
			w.write(curS + " "+(double)contexTermsMap.get(curS) + "\n");
		}
		w.close();
		
	}
	
	private SpanQuery generateSpanQueryFromTerm(String term){
		List<Set<String>> queryList = StringUtils.String2ListSet(term);
		SpanQuery query;
		if (queryList.size() == 1){
			if(queryList.get(0).size() == 1)
				query = new SpanTermQuery(new Term(Constants.field,term));
			else {
				query = new SpanOrQuery();
				for (String t:queryList.get(0))
					((SpanOrQuery) query).addClause(new SpanTermQuery(new Term(Constants.field,t)));
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
		return query;
		
	}
	
	
	
	public double getContextWordWeight(String contextWord){
		double weight = 0;
		if(m_contexTermsMap.containsKey(contextWord))
			weight = m_contexTermsMap.get(contextWord);
		return weight;
	}
	
	public Set<String> getAllContextWords(){
		return m_contexTermsMap.keySet();
	}
	
	
	
	private IndexSearcher m_searcher=null;
	private Map<String, Double> m_contexTermsMap = null;
 
}
