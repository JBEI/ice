package org.jbei.ice.lib.managers;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.models.News;

public class GenericManager extends Manager {
    @SuppressWarnings("unchecked")
    public static Set<News> getNewses() throws ManagerException {
        LinkedHashSet<News> newses = null;
        Session session = getSession();
        try {
            Query query = session.createQuery("from News news");
            newses = new LinkedHashSet<News>(query.list());
        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve News: " + e.toString());
        } finally {

        }
        return newses;

    }

    @SuppressWarnings("unchecked")
    public static Set<News> getPublishedNewses(int offset, int limit) throws ManagerException {
        LinkedHashSet<News> newses = null;
        Session session = getSession();
        try {
            Query query = session
                    .createQuery("from News news where news.isPublished = 1 order by news.publicationTime desc");
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            newses = new LinkedHashSet<News>(query.list());
        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve News: " + e.toString());
        } finally {

        }
        return newses;

    }

    @SuppressWarnings("unchecked")
    public static Set<News> getAllPublishedNewses() throws ManagerException {
        LinkedHashSet<News> newses = null;
        Session session = getSession();
        try {
            Query query = session
                    .createQuery("from News news where news.isPublished = 1 order by news.publicationTime desc");
            newses = new LinkedHashSet<News>(query.list());
        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve News: " + e.toString());
        } finally {

        }
        return newses;

    }

    public static long getTotalEntries() throws ManagerException {
        long result = 0;
        Session session = getSession();
        Query query = session.createQuery("select count(entry.id) from Entry entry");
        try {
            result = (Long) query.uniqueResult();
        } catch (HibernateException e) {
            throw new ManagerException("Couldn't retrieve News: " + e.toString());
        } finally {

        }
        return result;
    }
}
