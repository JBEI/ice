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
import org.hibernate.Query;
import org.hibernate.criterion.Order;
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
        Criteria criteria = currentSession().createCriteria(Request.class.getName());
        criteria.add(Restrictions.eq("account", account));
        criteria.add(Restrictions.eq("entry", entry));
        criteria.add(Restrictions.eq("status", status));
        List list = criteria.list();
        if (list == null)
            return new ArrayList<>();
        return new ArrayList<Request>(list);
    }

    public Request getSampleRequestInCart(Account account, Entry entry) throws DAOException {
        Criteria criteria = currentSession().createCriteria(Request.class.getName())
                .add(Restrictions.eq("account", account))
                .add(Restrictions.eq("entry", entry))
                .add(Restrictions.eq("status", SampleRequestStatus.IN_CART));
        try {
            List list = criteria.list();
            if (list.isEmpty())
                return null;

            if (list.size() > 1) {
                Logger.error("Multiple sample requests found for entry " + entry.getId());
            }
            return (Request) list.get(0);
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    public List<Request> getAccountRequestList(Account account, int start, int count, String sort, boolean asc)
            throws DAOException {
        Criteria criteria = currentSession().createCriteria(Request.class.getName())
                .add(Restrictions.eq("account", account))
                .add(Restrictions.eq("status", SampleRequestStatus.PENDING));
        criteria.setMaxResults(count);
        criteria.setFirstResult(start);
        criteria.addOrder(asc ? Order.asc(sort) : Order.desc(sort));
        try {
            return new ArrayList<Request>(criteria.list());
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    // returns all pending requests
    public List<Request> getAllRequestList() throws DAOException {
        String sql = "from " + Request.class.getName() + " request where status=:status order by request.id desc";
        Query query = currentSession().createQuery(sql);
        query.setParameter("status", SampleRequestStatus.PENDING);

        try {
            List list = query.list();
            return new ArrayList<Request>(list);
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    public List<Request> getRequestListInCart(Account account) throws DAOException {
        Criteria criteria = currentSession().createCriteria(Request.class.getName())
                .add(Restrictions.eq("account", account))
                .add(Restrictions.eq("status", SampleRequestStatus.IN_CART));

        try {
            return new ArrayList<Request>(criteria.list());
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }
}
