package org.jbei.ice.lib.dao.hibernate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dto.sample.SampleRequestStatus;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.model.Request;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * Data accessor object for Sample Request objects
 *
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class RequestDAO extends HibernateRepository<Request> {

    public Request get(long id) throws DAOException {
        return super.get(Request.class, id);
    }

    public ArrayList<Request> getSampleRequestByStatus(Account account, Entry entry, SampleRequestStatus status)
            throws DAOException {
        String sql = "from " + Request.class.getName() + " where status=:status and entry=:entry and account=:account";
        Query query = currentSession().createQuery(sql);
        query.setParameter("status", status);
        query.setParameter("entry", entry);
        query.setParameter("account", account);

        List list = query.list();
        if (list == null)
            return new ArrayList<>();
        return new ArrayList<Request>(list);
    }

    public Request getSampleRequestInCart(Account account, Entry entry) throws DAOException {
        String sql = "from " + Request.class.getName() + " where status=:status and entry=:entry and account=:account";
        Query query = currentSession().createQuery(sql);
        query.setParameter("status", SampleRequestStatus.IN_CART);
        query.setParameter("entry", entry);
        query.setParameter("account", account);

        try {
            List list = query.list();
            if (list.isEmpty())
                return null;

            HashSet<Request> inCart = new HashSet<Request>(list);
            if (inCart.size() > 1) {
                Logger.error("Multiple sample requests found for entry " + entry.getId());
            }
            return (Request) inCart.toArray()[0];
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getCount(Account account) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(Request.class.getName());
            if (account != null)
                criteria.add(Restrictions.eq("account", account));
            criteria.setProjection(Projections.rowCount());
            return ((Number) criteria.uniqueResult()).intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Request> get(int start, int limit, String sort, boolean asc) throws DAOException {
        String sql = "from " + Request.class.getName() + " request order by " + sort;
        sql += asc ? " asc" : " desc";
        Query query = currentSession().createQuery(sql);
        query.setMaxResults(limit);
        query.setFirstResult(start);

        try {
            return new ArrayList<Request>(query.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Request> getAccountRequests(Account account, int start, int limit, String sort, boolean asc)
            throws DAOException {
        String sql = "from " + Request.class.getName() + " request where account=:account order by " + sort;
        sql += asc ? " asc" : " desc";
        Query query = currentSession().createQuery(sql);
        query.setParameter("account", account);
        query.setMaxResults(limit);
        query.setFirstResult(start);

        try {
            return new ArrayList<Request>(query.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
