package org.jbei.ice.search;

import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.Search;
import org.jbei.ice.executor.Task;
import org.jbei.ice.logging.Logger;
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
        Session session = HibernateConfiguration.newSession();
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
