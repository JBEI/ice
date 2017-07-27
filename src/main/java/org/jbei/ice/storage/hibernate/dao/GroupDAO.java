package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.group.GroupType;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Group;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Data Accessor object to manipulate {@link Group} objects.
 *
 * @author Hector Plahar
 */
public class GroupDAO extends HibernateRepository<Group> {

    /**
     * Retrieve {@link Group} object from the database by its id.
     *
     * @param id group unique identifier
     * @return Group object.
     * @throws DAOException on Exception
     */
    public Group get(long id) {
        return super.get(Group.class, id);
    }

    /**
     * Retrieve {@link Group} object from the database by its uuid.
     *
     * @param uuid universally unique identifier for group
     * @return Group object.
     * @throws DAOException
     */
    public Group getByUUID(String uuid) {
        try {
            CriteriaQuery<Group> query = getBuilder().createQuery(Group.class);
            Root<Group> from = query.from(Group.class);
            query.where(getBuilder().equal(from.get("uuid"), uuid));
            return currentSession().createQuery(query).uniqueResult();
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public long getMemberCount(String uuid) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Group> from = query.from(Group.class);
            Join<Group, Account> member = from.join("members");
            query.select(getBuilder().countDistinct(member.get("id")));
            query.where(getBuilder().equal(from.get("uuid"), uuid));
            return currentSession().createQuery(query).uniqueResult();
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public List<Group> getMatchingGroups(Account account, String token, int limit) {
        Set<Group> userGroups = account.getGroups();

        try {
            token = token.toUpperCase();
            CriteriaQuery<Group> query = getBuilder().createQuery(Group.class).distinct(true);
            Root<Group> from = query.from(Group.class);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(getBuilder().like(getBuilder().upper(from.get("label")), "%" + token + "%"));
            predicates.add(getBuilder().notEqual(from.get("uuid"), GroupController.PUBLIC_GROUP_UUID));
            Predicate predicate = getBuilder().or(
                    getBuilder().equal(from.get("type"), GroupType.PUBLIC),
                    getBuilder().equal(from.get("owner"), account)
            );
            if (userGroups != null && !userGroups.isEmpty()) {
                predicate.getExpressions().add(from.in(userGroups));
            }
            predicates.add(predicate);
            query.where(predicates.toArray(new Predicate[predicates.size()]));
            return currentSession().createQuery(query).setMaxResults(limit).list();
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException("Error retrieving matching groups", e);
        }
    }

    public List<Group> retrieveMemberGroups(Account account) {
        try {
            CriteriaQuery<Group> query = getBuilder().createQuery(Group.class).distinct(true);
            Root<Group> from = query.from(Group.class);
            Join<Group, Account> members = from.join("members", JoinType.LEFT);
            query.where(getBuilder().or(
                    getBuilder().equal(from.get("owner"), account),
                    getBuilder().equal(members.get("email"), account.getEmail())
            ));
            return currentSession().createQuery(query).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves all UUIDs that the specified account either owns or is a member of
     *
     * @param account account
     * @return list of UUIDs matching the query
     * @throws DAOException on hibernate exception
     */
    public List<String> getMemberGroupUUIDs(Account account) {
        try {
            CriteriaQuery<String> query = getBuilder().createQuery(String.class).distinct(true);
            Root<Group> from = query.from(Group.class);
            Join<Group, Account> members = from.join("members", JoinType.LEFT);
            query.select(from.get("uuid")).where(getBuilder().or(
                    getBuilder().equal(from.get("owner"), account),
                    getBuilder().equal(members.get("email"), account.getEmail())
            ));
            return currentSession().createQuery(query).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Group> getGroupsByType(GroupType type, int offset, int limit) {
        try {
            CriteriaQuery<Group> query = getBuilder().createQuery(Group.class).distinct(true);
            Root<Group> from = query.from(Group.class);
            query.where(getBuilder().equal(from.get("type"), type));
            return currentSession().createQuery(query).setFirstResult(offset).setMaxResults(limit).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public long getGroupsByTypeCount(GroupType type) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class).distinct(true);
            Root<Group> from = query.from(Group.class);
            query.select(getBuilder().countDistinct(from.get("id")));
            query.where(getBuilder().equal(from.get("type"), type));
            return currentSession().createQuery(query).uniqueResult();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves groups by type and <code>autoJoin</code> value
     *
     * @param type       type of groups to retrieve
     * @param isAutoJoin auto join status
     * @return list of groups found that match the parameters
     * @throws DAOException on Exception retrieving groups
     */
    public List<Group> getGroupsBy(GroupType type, boolean isAutoJoin) {
        try {
            CriteriaQuery<Group> query = getBuilder().createQuery(Group.class).distinct(true);
            Root<Group> from = query.from(Group.class);
            query.where(
                    getBuilder().equal(from.get("type"), type),
                    getBuilder().equal((from.get("autoJoin")), isAutoJoin));
            return currentSession().createQuery(query).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
