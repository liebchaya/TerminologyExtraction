package scorers;

import java.io.IOException;

/**
 * 
 * @author Admin
 *
 */
public class ATFScorer implements TermScorer{

	/**
	 * atf = tf/df
	 */
	public double score(String term) throws IOException {
		TFScorer tf = new TFScorer();
		DFScorer df = new DFScorer();
		return tf.score(term)/df.score(term);
	}
	

}
