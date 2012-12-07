package org.jbei.ice.lib.search;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;

class SearchDAO extends HibernateRepository {

    public void initHibernateSearch() throws DAOException {
        try {
            Session session = HibernateHelper.newSession();
            FullTextSession fullTextSession = Search.getFullTextSession(session);
            try {
                fullTextSession.createIndexer().startAndWait();
            } catch (InterruptedException e) {
                Thread.interrupted();
                Logger.warn("Re-indexing not complete");
            }
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }
}
