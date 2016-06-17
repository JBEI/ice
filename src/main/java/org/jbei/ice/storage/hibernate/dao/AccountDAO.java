package org.jbei.ice.storage.hibernate.dao;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.*;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @SuppressWarnings("unchecked")
    public Set<Account> getMatchingAccounts(String token, int limit) {
        try {
            String[] tokens = token.split("\\s+");
            Disjunction disjunction = Restrictions.disjunction();
            for (String tok : tokens) {
                disjunction.add(Restrictions.ilike("firstName", tok, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("lastName", tok, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("email", tok, MatchMode.ANYWHERE));
            }

            Criteria criteria = currentSession().createCriteria(Account.class)
                    .add(disjunction);

            if (limit > 0)
                criteria.setMaxResults(limit);
            return new HashSet<>(criteria.list());
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    /**
     * Retrieve an {@link Account} by the email field.
     *
     * @param email unique email identifier for account
     * @return Account record referenced by email or null if email is null
     */
    public Account getByEmail(String email) {
        if (email == null)
            return null;

        try {
            return (Account) currentSession().createCriteria(Account.class)
                    .add(Restrictions.eq("email", email.trim()).ignoreCase())
                    .uniqueResult();
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
    @SuppressWarnings("unchecked")
    public List<Account> getAccounts(int offset, int limit, String sort, boolean asc, String filter) {
        try {
            Criteria criteria = currentSession().createCriteria(Account.class.getName());
            if (!StringUtils.isEmpty(filter)) {
                criteria.add(Restrictions.disjunction()
                        .add(Restrictions.ilike("firstName", filter, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("lastName", filter, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("email", filter, MatchMode.ANYWHERE)));
            }
            criteria.addOrder(asc ? Order.asc(sort) : Order.desc(sort));
            criteria.setMaxResults(limit);
            criteria.setFirstResult(offset);
            return criteria.list();
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
            Criteria criteria = currentSession().createCriteria(Account.class.getName());
            if (!StringUtils.isEmpty(filter)) {
                criteria.add(Restrictions.disjunction()
                        .add(Restrictions.ilike("firstName", filter, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("lastName", filter, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("email", filter, MatchMode.ANYWHERE)));
            }
            Number itemCount = (Number) criteria.setProjection(Projections.countDistinct("id")).uniqueResult();
            return itemCount.longValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
