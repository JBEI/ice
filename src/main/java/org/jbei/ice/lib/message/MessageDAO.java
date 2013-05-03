package org.jbei.ice.lib.message;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.logging.Logger;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * Hibernate DAO for {@link Message}
 *
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class MessageDAO extends HibernateRepository<Message> {

    public Message retrieveMessage(long id) throws DAOException {
        return super.get(Message.class, id);
    }

    public Message saveMessage(Message message) throws DAOException {
        return super.save(message);
    }

    public int retrieveNewMessageCount(Account account) throws DAOException {
        try {
            Session session = currentSession();

            String sql = "select count(id) from message m where m.id in "
                    + "(select message_id from message_destination_accounts where account_id = "
                    + account.getId() + ")";

            if (!account.getGroups().isEmpty()) {
                String conjunction = " OR m.id in (select message_id from message_destination_groups where group_id in"
                        + " (";

                int size = account.getGroups().size();
                int i = 0;
                for (Group group : account.getGroups()) {
                    conjunction += group.getId();
                    if (i < size - 1)
                        conjunction += ", ";
                }

                sql += (conjunction + "))");
            }
            Query query = session.createSQLQuery(sql);
            Number number = (Number) query.uniqueResult();
            return number.intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Set<Message> retrieveMessages(Account account, int start, int count) throws DAOException {
        try {
            Session session = currentSession();
            String sql = "select id from message m where m.id in "
                    + "(select message_id from message_destination_accounts where account_id = "
                    + account.getId() + ")";

            if (!account.getGroups().isEmpty()) {
                String conjuction = " OR m.id in (select message_id from message_destination_groups where group_id in"
                        + " (";

                int size = account.getGroups().size();
                int i = 0;
                for (Group group : account.getGroups()) {
                    conjuction += group.getId();
                    if (i < size - 1)
                        conjuction += ", ";
                }

                sql += (conjuction + "))");
            }

            Query query = session.createSQLQuery(sql);
            query.setFirstResult(start);
            query.setMaxResults(count);
            List list = query.list();
            Set<Long> set = new HashSet<>();
            for (Object object : list) {
                Number number = (Number) object;
                set.add(number.longValue());
            }

            Criteria criteria = session.createCriteria(Message.class).add(Restrictions.in("id", set));
            criteria.addOrder(Order.desc("dateSent"));
            Set<Message> results = new HashSet<Message>(criteria.list());
            return results;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int retrieveMessageCount(Account account) throws DAOException {
        try {
            Session session = currentSession();
            String sql = "select count(id) from message m where m.id in "
                    + "(select message_id from message_destination_accounts where account_id = "
                    + account.getId() + ")";

            if (!account.getGroups().isEmpty()) {
                String conjuction = " OR m.id in (select message_id from message_destination_groups where group_id in"
                        + " (";

                int size = account.getGroups().size();
                int i = 0;
                for (Group group : account.getGroups()) {
                    conjuction += group.getId();
                    if (i < size - 1)
                        conjuction += ", ";
                }

                sql += (conjuction + "))");
            }

            Query query = session.createSQLQuery(sql);
            Number number = (Number) query.uniqueResult();
            return number.intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException();
        }
    }
}
