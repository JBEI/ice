package org.jbei.ice.lib.managers;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.models.News;

/**
 * Manager to manipulate miscellaneous objects in the database.
 * 
 * @author Timothy Ham, ZInovii Dmytriv
 * 
 */
public class GenericManager {
    /**
     * Retrieve all {@link News} objects from the database.
     * 
     * @return Set of News objects.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static Set<News> getNewses() throws ManagerException {
        LinkedHashSet<News> newses = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from News news");
            newses = new LinkedHashSet<News>(query.list());
        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve News: " + e.toString());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return newses;

    }

    /**
     * Retrieve {@link News} objects that are marked published, offset and limited.
     * 
     * @param offset
     * @param limit
     * @return Set of News objects with published flag set.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static Set<News> getPublishedNewses(int offset, int limit) throws ManagerException {
        LinkedHashSet<News> newses = null;
        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("from News news where news.isPublished = 1 order by news.publicationTime desc");
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            newses = new LinkedHashSet<News>(query.list());
        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve News: " + e.toString());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return newses;

    }

    /**
     * Retrieve all {@link News} objects that are marked published.
     * 
     * @return Set of News objects with published flag set.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static Set<News> getAllPublishedNewses() throws ManagerException {
        LinkedHashSet<News> newses = null;
        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("from News news where news.isPublished = 1 order by news.publicationTime desc");
            newses = new LinkedHashSet<News>(query.list());
        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve News: " + e.toString());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return newses;

    }
}
