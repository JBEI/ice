package org.jbei.ice.lib.dao.hibernate;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.IDataModel;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;

public class SearchDAO extends HibernateRepository {

    public void initHibernateSearch() throws DAOException {
        try {
            Session session = HibernateHelper.newSession();
            FullTextSession fullTextSession = Search.getFullTextSession(session);
            try {
                fullTextSession.createIndexer().idFetchSize(100);
                fullTextSession.createIndexer().startAndWait();
            } catch (InterruptedException e) {
                Thread.interrupted();
                Logger.warn("Re-indexing not complete");
            }
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    public void reIndexInbackground() throws DAOException {
        try {
            Session session = HibernateHelper.newSession();
            FullTextSession fullTextSession = Search.getFullTextSession(session);
            fullTextSession.createIndexer().idFetchSize(100);
            fullTextSession.createIndexer().start();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    @Override
    public IDataModel get(long id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
