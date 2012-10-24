package org.jbei.ice.lib.news;

import java.util.ArrayList;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * DAO for managing {@link News}
 *
 * @author Hector Plahar
 */
class NewsDAO extends HibernateRepository<News> {

    public News get(long id) throws DAOException {
        return super.get(News.class, id);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<News> retrieveAll() throws DAOException {
        Session session = newSession();
        try {
            ArrayList<News> results = new ArrayList<News>();
            Query query = session.createQuery("from " + News.class.getName() + " order by creationTime DESC ");
            results.addAll(query.list());
            return results;

        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve news", e);
        } finally {
            closeSession(session);
        }
    }

    public News save(News news) throws DAOException {
        return super.saveOrUpdate(news);
    }

    public void update(News news) throws DAOException {
        super.saveOrUpdate(news);
    }
}
