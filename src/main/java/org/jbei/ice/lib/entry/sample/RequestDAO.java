package org.jbei.ice.lib.entry.sample;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.model.Request;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestStatus;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * Data accessor object for Sample Request objects
 *
 * @author Hector Plahar
 */
public class RequestDAO extends HibernateRepository<Request> {

    public Request get(long id) throws DAOException {
        return super.get(Request.class, id);
    }

    /**
     * retrieves the number of samples requests that have been submitted by a user
     * with the specified request status
     *
     * @param account user account
     * @param status  request status for request
     * @return number of sample requests with {@link SampleRequestStatus} of pending
     * @throws DAOException
     */
    public int getRequestCount(Account account, SampleRequestStatus status) throws DAOException {
        Criteria criteria = currentSession().createCriteria(Request.class.getName())
                .add(Restrictions.eq("account", account))
                .add(Restrictions.eq("status", status))
                .setProjection(Projections.rowCount());
        try {
            Number number = (Number) criteria.uniqueResult();
            return number.intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Request getSampleRequest(Account account, Entry entry) throws DAOException {
        Criteria criteria = currentSession().createCriteria(Request.class.getName())
                .add(Restrictions.eq("account", account))
                .add(Restrictions.eq("entry", entry));
        try {
            List list = criteria.list();
            if (list.isEmpty())
                return null;

            if (list.size() > 1) {
                Logger.error("Multiple sample requests found for entry " + entry.getId());
            }
            return (Request) list.get(0);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Request> getAccountRequestList(Account account, int start, int count, String sort, boolean asc)
            throws DAOException {
        Criteria criteria = currentSession().createCriteria(Request.class.getName())
                .add(Restrictions.eq("account", account));
        criteria.setMaxResults(count);
        criteria.setFirstResult(start);
        criteria.addOrder(asc ? Order.asc(sort) : Order.desc(sort));
        try {
            return new ArrayList<Request>(criteria.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Request> getAllRequestList() throws DAOException {
        Criteria criteria = currentSession().createCriteria(Request.class.getName());
        criteria.setMaxResults(100);
        criteria.setFirstResult(0);
        boolean asc = true;
        String sort = "requested";
        criteria.addOrder(asc ? Order.asc(sort) : Order.desc(sort));
        try {
            return new ArrayList<Request>(criteria.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Request> getRequestListInCart(Account account) throws DAOException {
        Criteria criteria = currentSession().createCriteria(Request.class.getName())
                .add(Restrictions.eq("account", account))
                .add(Restrictions.eq("status", SampleRequestStatus.IN_CART));

        try {
            return new ArrayList<Request>(criteria.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
