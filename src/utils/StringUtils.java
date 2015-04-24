package utils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class StringUtils {
	public static LinkedList<String> String2PhraseList(String queryLine){
		LinkedList<String> phraseList = new LinkedList<String>();
		for(String query:queryLine.split("\t"))
			phraseList.add(query);
		return phraseList;
	}
	
	public static List<Set<String>> String2ListSet(String queryLine){
		// build query list for other scorers
		List<Set<String>> queryList = new LinkedList<Set<String>>();
		for(String t:queryLine.split("\t")){
			for(int pos=0; pos< t.split(" ").length; pos++){
				if (queryList.size()-1 < pos){
					Set<String> queries = new HashSet<String>();
					queries.add( t.split(" ")[pos]);
					queryList.add(pos, queries);
				} else {
					queryList.get(pos).add(t.split(" ")[pos]);
				}
			}
			
		}
		return queryList;
	}
}
