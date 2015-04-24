package test;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is for demonstration purposes only. No warranty, guarantee, etc.
 * is implied.
 * <p/>
 * This is not production quality code!
 */
public class TermVectorFun {
	public static String[] DOCS = {
			"The quick red fox jumped over the lazy brown dogs.",
			"Mary had a little lamb whose fleece was white as snow.",
			"Moby Dick is a story of a whale and a man obsessed.",
			"The robber wore a black fleece jacket and a baseball cap.",
			"The English Springer Spaniel is the best of all dogs.",
			"The fleece was green and red",
			"History looks fondly upon the story of the golden fleece, but most people don’t agree" };

	public static void main(String[] args) throws IOException {
		RAMDirectory ramDir = new RAMDirectory();
		//Index some made up content
		IndexWriter writer = new IndexWriter(ramDir, new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(Version.LUCENE_48)));
		//Store both position and offset information
		FieldType type = new FieldType();
		type.setStoreTermVectors(true);
		type.setStoreTermVectorOffsets(true);
		type.setStoreTermVectorPositions(true);
		type.setIndexed(true);
		type.setTokenized(true);
		for (int i = 0; i < DOCS.length; i++) {
		Document doc = new Document();
		Field id = new StringField("id", "doc_" + i, Field.Store.YES);
		doc.add(id);
		Field text = new Field("content", DOCS[i], type);
		doc.add(text);
		writer.addDocument(doc);
		}
		writer.close();
		//Get a searcher
		IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(ramDir));
		// Do a search using SpanQuery
		SpanTermQuery fleeceQ = new SpanTermQuery(new Term("content", "fleece"));
		SpanTermQuery jacketQ = new SpanTermQuery(new Term("content", "jacket"));
		SpanNearQuery andQ = new SpanNearQuery(new SpanTermQuery[]{fleeceQ,jacketQ},0,true );
		TopDocs results = searcher.search(fleeceQ, 10);
		for (int i = 0; i < results.scoreDocs.length; i++) {
			ScoreDoc scoreDoc = results.scoreDocs[i];
			System.out.println("Score Doc: " + scoreDoc);
		}
		IndexReader reader = searcher.getIndexReader();
		//this is not the best way of doing this, but it works for the example. See http://www.slideshare.net/lucenerevolution/is-your-index-reader-really-atomic-or-maybe-slow for higher performance approaches
		AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(reader);
		Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
		Spans spans = andQ.getSpans(wrapper.getContext(), new Bits.MatchAllBits(reader.numDocs()), termContexts);
		int window = 3;//get the words within two of the match
		while (spans.next() == true) {
			Map<Integer, String> entries = new TreeMap<Integer, String>();
			System.out.println("Doc: " + spans.doc() + " Start: " + spans.start() + " End: " + spans.end());
			int start = spans.start() - window;
			int end = spans.end() + window;
			Terms content = reader.getTermVector(spans.doc(), "content");
			TermsEnum termsEnum = content.iterator(null);
			BytesRef term;
			while ((term = termsEnum.next()) != null) {
			//could store the BytesRef here, but String is easier for this example
			String s = new String(term.bytes, term.offset, term.length);
			DocsAndPositionsEnum positionsEnum = termsEnum.docsAndPositions(null, null);
			if (positionsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
				int i = 0;
				int position = -1;
				while (i < positionsEnum.freq() && (position = positionsEnum.nextPosition()) != -1) {
					if (position >= start && position <= end) {
						if (!(position >= spans.start() && position < spans.end()))
							entries.put(position, s);
					}
					i++;
				}
			}
			}
			System.out.println("Entries:" + entries);
		}
		}
}