package org.jbei.ice.lib.search;

import java.io.IOException;


import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;;

public class Lucene {
	public Lucene() {
		String input_file = "/var/lib/jbeiregistry/";
		
		try {
			FSDirectory index = NIOFSDirectory.getDirectory(input_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StandardAnalyzer analyzer = new StandardAnalyzer();
	
	}
	public void rebuildIndex() {
	
	}
	
	public SearchResults query(String query) {
		return query(query, 0, 10);
	}
	
	public SearchResults query(String query, int offset, int limit) {
		return null;
		
	}
	
}