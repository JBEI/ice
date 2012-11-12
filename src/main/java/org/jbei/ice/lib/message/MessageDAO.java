package org.jbei.ice.lib.message;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;

/**
 * @author Hector Plahar
 */
public class MessageDAO extends HibernateRepository<Message> {

    public Message retrieveMessage(long id) throws DAOException {
        return super.get(Message.class, id);
    }


}
