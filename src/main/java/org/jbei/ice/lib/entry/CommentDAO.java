package org.jbei.ice.lib.entry;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.models.Comment;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 * DAO for comment objects
 *
 * @author Hector Plahar
 */
public class CommentDAO extends HibernateRepository<Comment> {

    @SuppressWarnings("unchecked")
    public ArrayList<Comment> retrieveComments(Entry entry) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Comment.class.getName());
        criteria.add(Restrictions.eq("entry", entry));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List list = criteria.list();
        return new ArrayList<Comment>(list);
    }
}
