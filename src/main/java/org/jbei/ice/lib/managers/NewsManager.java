package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.News;

public class NewsManager {

    public static News get(long id) throws ManagerException {
        News news = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + News.class.getName() + " where id = :id");
            query.setParameter("id", id);

            news = (News) query.uniqueResult();
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve news by id: " + String.valueOf(id), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return news;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<News> retrieveAll() throws ManagerException {
        Session session = DAO.newSession();
        try {
            ArrayList<News> results = new ArrayList<News>();

            Query query = session.createQuery("from " + News.class.getName()
                    + " order by creationTime DESC ");

            results.addAll(query.list());
            return results;

        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve news", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public static News update(News news) throws ManagerException {
        try {
            news.setModificationTime(new Date(System.currentTimeMillis()));
            DAO.save(news);
        } catch (DAOException e) {
            String msg = "Could not save news: " + news.getTitle() + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        }

        return news;
    }

    public static News save(News news) throws ManagerException {
        news.setCreationTime(new Date(System.currentTimeMillis()));
        return update(news);
    }
}
