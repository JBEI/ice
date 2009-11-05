package org.jbei.ice.lib.managers;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.jbei.ice.lib.models.News;

public class GenericManager extends Manager {
	public static Set<News> getNewses () throws ManagerException {
		LinkedHashSet<News> newses = null;
		try {
			Query query = HibernateHelper.getSession().createQuery(
				"from News news");
			newses = new LinkedHashSet<News> (query.list());
		} catch (HibernateException e) {
			throw new ManagerException(
				"Couldn't retrieve News: " + e.toString());
		}
		return newses;
		
	}
	
	public static Set<News> getPublishedNewses(int offset, int limit) throws ManagerException {
		LinkedHashSet<News> newses = null;
		try {
			Query query = HibernateHelper.getSession().createQuery(
				"from News news where news.isPublished = 1 order by news.publicationTime desc");
			query.setFirstResult(offset);
			query.setMaxResults(limit);
			newses = new LinkedHashSet<News> (query.list());
		} catch (HibernateException e) {
			throw new ManagerException(
				"Couldn't retrieve News: " + e.toString());
		}
		return newses;
		
	}
	
	public static Set<News> getAllPublishedNewses() throws ManagerException {
		LinkedHashSet<News> newses = null;
		try {
			Query query = HibernateHelper.getSession().createQuery(
				"from News news where news.isPublished = 1 order by news.publicationTime desc");
			newses = new LinkedHashSet<News> (query.list());
		} catch (HibernateException e) {
			throw new ManagerException(
				"Couldn't retrieve News: " + e.toString());
		}
		return newses;
		
	}
	
	public static long getTotalEntries() {
		long result = 0;
		Query query = HibernateHelper.getSession().createQuery(
			"select count(entry.id) from Entry entry");
		result = (Long) query.uniqueResult();
		return result;
	}
	
}
