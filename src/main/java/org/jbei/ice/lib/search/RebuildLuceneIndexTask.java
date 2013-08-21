package org.jbei.ice.lib.search;

import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;

/**
 * Task to rebuild lucene index
 *
 * @author Hector Plahar
 */
public class RebuildLuceneIndexTask extends Task {

    @Override
    public void execute() {
        Logger.info("Rebuilding lucene index in background");
        try {
            Session session = HibernateHelper.newSession();
            FullTextSession fullTextSession = Search.getFullTextSession(session);
            fullTextSession.createIndexer().idFetchSize(20);
            fullTextSession.createIndexer().startAndWait();
        } catch (HibernateException he) {
            Logger.error(he);
        } catch (InterruptedException e) {
            Thread.interrupted();
            Logger.warn("Indexing incomplete");
        }
    }
}
