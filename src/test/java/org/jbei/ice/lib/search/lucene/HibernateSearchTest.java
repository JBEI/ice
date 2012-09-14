package org.jbei.ice.lib.search.lucene;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.search.HibernateSearch;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class HibernateSearchTest {

    @Before
    public void setUp() {
        HibernateHelper.initializeMock();
    }

    @Test
    public void testExecuteSearch() throws Exception {
        // TODO : create entries
        ApplicationController.initializeHibernateSearch();
        HibernateSearch.getInstance().executeSearch("Hector");
    }
}
