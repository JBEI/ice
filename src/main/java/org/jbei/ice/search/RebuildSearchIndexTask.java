package org.jbei.ice.search;

import org.hibernate.Session;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.jbei.ice.executor.Task;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;

/**
 * Task to rebuild lucene index
 *
 * @author Hector Plahar
 */
public class RebuildSearchIndexTask extends Task {

    @Override
    public void execute() {
        Logger.info("Rebuilding search index in background");
        Session session = HibernateConfiguration.newSession();
        SearchSession searchSession = Search.session(session);
        MassIndexer massIndexer = searchSession.massIndexer();
        massIndexer.idFetchSize(20).monitor(IndexerProgressMonitor.getInstance());

        try {
            massIndexer.startAndWait();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        session.close();
        Logger.info("Lucene rebuild complete");
    }
}
