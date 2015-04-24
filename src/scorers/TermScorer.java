/**
 * 
 */
package scorers;

import java.io.IOException;

/**
 * @author Admin
 *
 */
public interface TermScorer {
	double score(String term) throws IOException;
}
