package org.jbei.ice.lib.search;

import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.MassIndexer;
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
        Session session = HibernateUtil.newSession();
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        MassIndexer indexer = fullTextSession.createIndexer();
        indexer.idFetchSize(20);
        indexer.progressMonitor(IndexerProgressMonitor.getInstance());

        try {
            indexer.startAndWait();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        fullTextSession.close();
        Logger.info("Lucene rebuild complete");
    }
}
