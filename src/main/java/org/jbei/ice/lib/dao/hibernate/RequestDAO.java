package org.jbei.ice.lib.dao.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dto.sample.SampleRequestStatus;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.model.Request;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

    public int getCount(Account account) {
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

    public int getCount(SampleRequestStatus status, String filter) throws DAOException {
        try {
            String sql = "select count(*) from " + Request.class.getName() + " request";
            if (filter != null) {
                filter = filter.toUpperCase();
                sql += " where UPPER(account.firstName) like '" + filter + "%' OR UPPER(account.lastName) like '" +
                        filter + "%'";
            }

            Number number = (Number) currentSession().createQuery(sql).uniqueResult();
            return number.intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Request> get(int start, int limit, String sort, boolean asc, SampleRequestStatus status, String filter)
            throws DAOException {
        String sql = "from " + Request.class.getName() + " request";
        if (filter != null) {
            filter = filter.toUpperCase();
            sql += " where UPPER(account.firstName) like '" + filter + "%' OR UPPER(account.lastName) like '" +
                    filter + "%'";
        }

        sql += " order by " + sort;
        sql += asc ? " asc" : " desc";

        Query query = currentSession().createQuery(sql);
        query.setMaxResults(limit);
        query.setFirstResult(start);

        try {
            return new ArrayList<>(query.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Request> getAccountRequests(Account account, SampleRequestStatus status, int start, int limit,
            String sort, boolean asc) throws DAOException {
        String sql = "from " + Request.class.getName() + " request where account=:account";
        if (status != null) {
            sql += " and status=:status";
        }

        sql += " order by " + sort;
        sql += asc ? " asc" : " desc";

        Query query = currentSession().createQuery(sql);
        query.setParameter("account", account);
        if (status != null)
            query.setParameter("status", status);
        query.setMaxResults(limit);
        query.setFirstResult(start);

        try {
            return new ArrayList<>(query.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
