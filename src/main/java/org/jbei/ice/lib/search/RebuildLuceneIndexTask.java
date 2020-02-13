package org.jbei.ice.lib.search;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.Search;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;

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
            Session session = HibernateConfiguration.newSession();
            FullTextSession fullTextSession = Search.getFullTextSession(session);
            MassIndexer indexer = fullTextSession.createIndexer();
            indexer.idFetchSize(20);
            indexer.progressMonitor(IndexerProgressMonitor.getInstance());
            indexer.startAndWait();
        } catch (HibernateException he) {
            Logger.error(he);
        } catch (InterruptedException e) {
            Thread.interrupted();
            Logger.warn("Indexing incomplete");
        }
    }
}
