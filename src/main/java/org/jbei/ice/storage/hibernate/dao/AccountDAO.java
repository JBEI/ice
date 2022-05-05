package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Data accessor object to manipulate {@link Account} objects in the database.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class AccountDAO extends HibernateRepository<Account> {

    /**
     * Retrieve {@link Account} by id from the database.
     *
     * @param id unique local identifier for object
     * @return Account
     * @throws DAOException
     */
    public Account get(long id) throws DAOException {
        return super.get(Account.class, id);
    }

    /**
     * Retrieves accounts whose firstName, lastName, or email fields match the specified
     * token up to the specified limit.
     *
     * @param token filter for the account fields
     * @param limit maximum number of matching accounts to return; 0 to return all
     * @return list of matching accounts
     */
    public List<Account> getMatchingAccounts(String token, int limit) {
        try {
            CriteriaQuery<Account> query = getBuilder().createQuery(Account.class);
            Root<Account> from = query.from(Account.class);

            String[] tokens = token.split("\\s+");
            List<Predicate> predicates = new ArrayList<>();
            for (String tok : tokens) {
                tok = tok.toLowerCase();
                predicates.add(
                        getBuilder().or(
                                getBuilder().like(getBuilder().lower(from.get("firstName")), "%" + tok + "%"),
                                getBuilder().like(getBuilder().lower(from.get("lastName")), "%" + tok + "%"),
                                getBuilder().like(getBuilder().lower(from.get("email")), "%" + tok + "%"))
                );
            }
            query.where(predicates.toArray(new Predicate[predicates.size()])).distinct(true);
            return currentSession().createQuery(query).setMaxResults(limit).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    /**
     * Retrieve an {@link Account} by the email field.
     *
     * @param email unique email identifier for account
     * @return Account record referenced by email
     */
    public Account getByEmail(String email) {
        if (email == null)
            return null;

        try {
            CriteriaQuery<Account> query = getBuilder().createQuery(Account.class);
            Root<Account> from = query.from(Account.class);
            query.where(getBuilder().equal(getBuilder().lower(from.get("email")), email.trim().toLowerCase()));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve Account by email: " + email, e);
        }
    }

    /**
     * Retrieves list of pageable accounts, matching the parameter values
     *
     * @param offset offset to start retrieving matching accounts
     * @param limit  maximum number of accounts to retrieve
     * @param sort   sort order for retrieval
     * @param asc    whether to sort in ascending or descending order
     * @param filter optional filter to for matching text against firstName, lastName or email fields of accounts
     * @return list of matching accounts
     * @throws DAOException on {@link HibernateException} retrieving accounts
     */
    public List<Account> getAccounts(int offset, int limit, String sort, boolean asc, String filter) {
        try {
            CriteriaQuery<Account> query = getBuilder().createQuery(Account.class);
            Root<Account> from = query.from(Account.class);

            if (filter != null && !filter.isEmpty()) {
                filter = filter.toLowerCase();
                query.where(getBuilder().or(
                                getBuilder().like(getBuilder().lower(from.get("firstName")), "%" + filter + "%"),
                                getBuilder().like(getBuilder().lower(from.get("lastName")), "%" + filter + "%"),
                                getBuilder().like(getBuilder().lower(from.get("email")), "%" + filter + "%"))
                );
            }
            query.distinct(true).orderBy(asc ? getBuilder().asc(from.get(sort)) : getBuilder().desc(from.get(sort)));
            return currentSession().createQuery(query).setMaxResults(limit).setFirstResult(offset).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves maximum number of distinct accounts available and, if specified, whose firstName, lastName and email
     * fields match the filter token. This is intended to be used for paging.
     *
     * @param filter optional token used to match against the firstName, lastName and email fields of accounts
     * @return number of accounts that match the optional filter.
     * @throws DAOException on {@link HibernateException} retrieving the number
     */
    public long getAccountsCount(String filter) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Account> from = query.from(Account.class);

            if (filter != null && !filter.isEmpty()) {
                filter = filter.toLowerCase();
                query.where(getBuilder().or(
                                getBuilder().like(getBuilder().lower(from.get("firstName")), "%" + filter + "%"),
                                getBuilder().like(getBuilder().lower(from.get("lastName")), "%" + filter + "%"),
                                getBuilder().like(getBuilder().lower(from.get("email")), "%" + filter + "%"))
                );
            }
            query.select(getBuilder().countDistinct(from.get("id")));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
