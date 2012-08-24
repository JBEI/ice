package org.jbei.ice.lib.search.lucene;

import org.jbei.ice.lib.search.LuceneSearch;

import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class LuceneSearchTest {
    @Test
    public void testExecuteSearch() throws Exception {

        LuceneSearch.getInstance().executeSearch("Adrienne");
    }
}
