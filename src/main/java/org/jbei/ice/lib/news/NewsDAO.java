package org.jbei.ice.lib.news;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.News;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import java.util.ArrayList;

/**
 * DAO for managing {@link News}
 *
 * @author Hector Plahar
 */
class NewsDAO extends HibernateRepository {

    public News get(long id) throws DAOException {
        return (News) super.get(News.class, id);
    }

    public ArrayList<News> retrieveAll() throws DAOException {
        Session session = newSession();
        try {
            ArrayList<News> results = new ArrayList<News>();

            Query query = session.createQuery("from " + News.class.getName()
                                                      + " order by creationTime DESC ");

            results.addAll(query.list());
            return results;

        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve news", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }
}
