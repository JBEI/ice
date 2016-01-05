package org.jbei.ice.lib.search;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.storage.hibernate.HibernateUtil;

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
            Session session = HibernateUtil.newSession();
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
