package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.sample.SampleRequestStatus;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Request;

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

    public List<Request> getSampleRequestByStatus(Account account, Entry entry, SampleRequestStatus status)
            throws DAOException {
        try {
            return currentSession().createCriteria(Request.class.getName())
                    .add(Restrictions.eq("status", status))
                    .add(Restrictions.eq("entry", entry))
                    .add(Restrictions.eq("account", account)).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Request getSampleRequestInCart(Account account, Entry entry) throws DAOException {
        try {
            List list = currentSession().createCriteria(Request.class.getName())
                    .add(Restrictions.eq("status", SampleRequestStatus.IN_CART))
                    .add(Restrictions.eq("entry", entry))
                    .add(Restrictions.eq("account", account))
                    .list();
            if (list.isEmpty())
                return null;

            HashSet<Request> inCart = new HashSet<>(list);
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
            Criteria criteria = currentSession().createCriteria(Request.class.getName());

            if (status != null) {
                criteria.add(Restrictions.eq("status", status));
            }

            if (filter != null) {
                criteria.createAlias("account", "account");
                criteria.add(Restrictions.disjunction()
                        .add(Restrictions.ilike("account.firstName", filter, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("account.lastName", filter, MatchMode.ANYWHERE)));
            }

            Number number = (Number) criteria.setProjection(Projections.rowCount()).uniqueResult();
            return number.intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Request> get(int start, int limit, String sort, boolean asc, SampleRequestStatus status, String filter)
            throws DAOException {

        try {
            Criteria criteria = currentSession().createCriteria(Request.class.getName());
            if (status != null) {
                criteria.add(Restrictions.eq("status", status));
            }

            if (filter != null) {
                criteria.createAlias("account", "account");
                criteria.add(Restrictions.disjunction()
                        .add(Restrictions.ilike("account.firstName", filter, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("account.lastName", filter, MatchMode.ANYWHERE)));
            }

            criteria.addOrder(asc ? Order.asc(sort) : Order.desc(sort));
            criteria.setMaxResults(limit);
            criteria.setFirstResult(start);
            return criteria.list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Request> getAccountRequests(Account account, SampleRequestStatus status, int start, int limit,
                                            String sort, boolean asc) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(Request.class).add(Restrictions.eq("account", account));
            if (status != null) {
                criteria.add(Restrictions.eq("status", status));
            }

            criteria.addOrder(asc ? Order.asc(sort) : Order.desc(sort));
            criteria.setMaxResults(limit);
            criteria.setFirstResult(start);
            return criteria.list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
