package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.sample.SampleRequest;
import org.jbei.ice.lib.dto.sample.SampleRequestStatus;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Request;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Data accessor object for Sample Request objects
 *
 * @author Hector Plahar
 */
public class RequestDAO extends HibernateRepository<Request> {

    public Request get(long id) {
        return super.get(Request.class, id);
    }

    public List<Request> getSampleRequestByStatus(Account account, Entry entry, SampleRequestStatus status) {
        try {
            CriteriaQuery<Request> query = getBuilder().createQuery(Request.class);
            Root<Request> from = query.from(Request.class);
            query.where(getBuilder().and(
                    getBuilder().equal(from.get("status"), status),
                    getBuilder().equal(from.get("entry"), entry),
                    getBuilder().equal(from.get("account"), account)
            ));
            return currentSession().createQuery(query).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Request getSampleRequestInCart(Account account, Entry entry) {
        try {
            CriteriaQuery<Request> query = getBuilder().createQuery(Request.class);
            Root<Request> from = query.from(Request.class);
            query.where(getBuilder().and(
                    getBuilder().equal(from.get("status"), SampleRequestStatus.IN_CART),
                    getBuilder().equal(from.get("entry"), entry),
                    getBuilder().equal(from.get("account"), account)
            ));

            List<Request> list = currentSession().createQuery(query).list();
            if (list.isEmpty())
                return null;

            HashSet<Request> inCart = new HashSet<>(list);
            if (inCart.size() > 1) {
                Logger.error("Multiple sample requests found for entry " + entry.getId());
            }
            return (Request) inCart.toArray()[0];
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getCount(Account account) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Request> from = query.from(Request.class);
            query.select(getBuilder().countDistinct(from.get("id")));

            if (account != null)
                query.where(getBuilder().equal(from.get("account"), account));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getCount(String filter, List<SampleRequestStatus> status) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Request> from = query.from(Request.class);
            query.select(getBuilder().countDistinct(from.get("id")));
            List<Predicate> predicates = createPredicates(from, filter, status);
            if (!predicates.isEmpty())
                query.where(predicates.toArray(new Predicate[predicates.size()]));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Request> get(int start, int limit, String sort, boolean asc, String filter, List<SampleRequestStatus> status) {
        try {
            CriteriaQuery<Request> query = getBuilder().createQuery(Request.class).distinct(true);
            Root<Request> from = query.from(Request.class);
            List<Predicate> predicates = createPredicates(from, filter, status);
            if (!predicates.isEmpty())
                query.where(predicates.toArray(new Predicate[predicates.size()]));

            query.orderBy(asc ? getBuilder().asc(from.get(sort)) : getBuilder().desc(from.get(sort)));
            return currentSession().createQuery(query).setMaxResults(limit).setFirstResult(start).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    protected List<Predicate> createPredicates(Root<Request> root, String filter, List<SampleRequestStatus> status) {
        List<Predicate> predicates = new ArrayList<>();
        if (status != null && !status.isEmpty())
            predicates.add(root.get("status").in(status));

        if (filter != null) {
            Join<SampleRequest, Account> account = root.join("account");
            filter = filter.toLowerCase();
            predicates.add(getBuilder().or(
                    getBuilder().like(getBuilder().lower(account.get("firstName")), "%" + filter + "%"),
                    getBuilder().like(getBuilder().lower(account.get("lastName")), "%" + filter + "%")));
        }
        return predicates;
    }

    public List<Request> getAccountRequests(Account account, SampleRequestStatus status, int start, int limit,
                                            String sort, boolean asc) {
        try {
            CriteriaQuery<Request> query = getBuilder().createQuery(Request.class);
            Root<Request> from = query.from(Request.class);

            if (status != null) {
                query.where(
                        getBuilder().equal(from.get("account"), account),
                        getBuilder().equal(from.get("status"), status));
            } else {
                query.where(getBuilder().equal(from.get("account"), account));
            }

            query.orderBy(asc ? getBuilder().asc(from.get(sort)) : getBuilder().desc(from.get(sort)));
            return currentSession().createQuery(query).setFirstResult(start).setMaxResults(limit).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
