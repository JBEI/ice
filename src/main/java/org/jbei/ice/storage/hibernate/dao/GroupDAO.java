package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.group.GroupType;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Group;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manager to manipulate {@link Group} objects.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
@SuppressWarnings("unchecked")
public class GroupDAO extends HibernateRepository<Group> {
    /**
     * Retrieve {@link Group} object from the database by its uuid.
     *
     * @param uuid universally unique identifier for group
     * @return Group object.
     * @throws DAOException
     */
    public Group get(String uuid) throws DAOException {
        try {
            return (Group) currentSession().createCriteria(Group.class)
                    .add(Restrictions.eq("uuid", uuid))
                    .uniqueResult();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public long getMemberCount(String uuid) throws DAOException {
        Number number = (Number) currentSession().createCriteria(Group.class)
                .add(Restrictions.eq("uuid", uuid))
                .createAlias("members", "member")
                .setProjection(Projections.rowCount())
                .uniqueResult();
        return number.longValue();
    }

    /**
     * Retrieve {@link Group} object from the database by its id.
     *
     * @param id group unique identifier
     * @return Group object.
     * @throws DAOException
     */
    public Group get(long id) throws DAOException {
        return super.get(Group.class, id);
    }

    public HashSet<Group> getByIdList(Set<Long> idsSet) throws DAOException {
        Session session = currentSession();

        try {
            Criteria criteria = session.createCriteria(Group.class).add(Restrictions.in("id", idsSet));
            List list = criteria.list();
            return new HashSet<>(list);

        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Set<Group> getMatchingGroups(Account account, String token, int limit) throws DAOException {
        Session session = currentSession();
        Set<Group> userGroups = account.getGroups();

        try {
            token = token.toUpperCase();
            // match the string and group must either be public, owned by user or in user's private groups
            String queryString = "from " + Group.class.getName() + " g where (UPPER(g.label) like '%" + token + "%')";
            Query query = session.createQuery(queryString);

            if (limit > 0)
                query.setMaxResults(limit);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<>(query.list());
            if (result.isEmpty())
                return result;

            HashSet<Group> matches = new HashSet<>();
            for (Group group : result) {
                if (group.getUuid().equals(GroupController.PUBLIC_GROUP_UUID))
                    continue;

                if (group.getType() != GroupType.PUBLIC && (group
                        .getOwner() != account) && (userGroups == null || !userGroups.contains(group)))
                    continue;

                matches.add(group);
            }

            return matches;
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Error retrieving matching groups", e);
        }
    }

    public List<Group> retrieveMemberGroups(Account account) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(Group.class)
                    .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                    .createAlias("members", "member", JoinType.LEFT_OUTER_JOIN)
                    .add(Restrictions.disjunction(
                            Restrictions.eq("owner", account),
                            Restrictions.eq("member.email", account.getEmail())));
            return criteria.list();
        } catch (HibernateException he) {
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
    public Set<String> getMemberGroupUUIDs(Account account) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(Group.class)
                    .createAlias("members", "member", JoinType.LEFT_OUTER_JOIN)
                    .add(Restrictions.disjunction(Restrictions.eq("owner", account),
                            Restrictions.eq("member.email", account.getEmail())))
                    .setProjection(Projections.property("uuid"));
            return new HashSet<>(criteria.list());
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    public List<Group> getGroupsByType(GroupType type, int offset, int limit) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(Group.class)
                    .add(Restrictions.eq("type", type))
                    .setFirstResult(offset)
                    .setMaxResults(limit);
            return criteria.list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public long getGroupsByTypeCount(GroupType type) throws DAOException {
        try {
            Number number = (Number) currentSession().createCriteria(Group.class)
                    .add(Restrictions.eq("type", type))
                    .setProjection(Projections.rowCount()).uniqueResult();
            return number.longValue();
        } catch (HibernateException he) {
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
     * @throws DAOException on HibernateException retrieving groups
     */
    public List<Group> getGroupsBy(GroupType type, boolean isAutoJoin) {
        try {
            return currentSession().createCriteria(Group.class)
                    .add(Restrictions.eq("type", type))
                    .add(Restrictions.eq("autoJoin", isAutoJoin))
                    .list();

        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
