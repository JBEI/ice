package org.jbei.ice.lib.message;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.logging.Logger;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class MessageDAO extends HibernateRepository<Message> {

    public Message retrieveMessage(long id) throws DAOException {
        return super.get(Message.class, id);
    }

    public int retrieveNewMessageCount(String userId) throws DAOException {
        try {
            Session session = currentSession();
            Criteria criteria = session.createCriteria(Message.class)
                                       .add(Restrictions.eq("isRead", false))
                                       .add(Restrictions.eq("toEmail", userId));
            criteria.setProjection(Projections.rowCount());
            Number result = (Number) criteria.uniqueResult();
            return result.intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public ArrayList<Message> retrieveMessages(String toEmail, int start, int count) throws DAOException {
        Criteria criteria = currentSession().createCriteria(Message.class);
        criteria.add(Restrictions.eq("toEmail", toEmail));
        criteria.setFirstResult(start);
        criteria.setMaxResults(count);
        List list = criteria.list();
        return new ArrayList<Message>(list);
    }
}
