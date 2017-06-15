package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Comment;
import org.jbei.ice.storage.model.Entry;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * DAO for comment objects
 *
 * @author Hector Plahar
 */
public class CommentDAO extends HibernateRepository<Comment> {

    public List<Comment> retrieveComments(Entry entry) {
        try {
            CriteriaQuery<Comment> query = getBuilder().createQuery(Comment.class);
            Root<Comment> from = query.from(Comment.class);
            query.select(from).where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getCommentCount(Entry entry) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Comment> from = query.from(Comment.class);
            query.select(getBuilder().countDistinct(from.get("id"))).where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @Override
    public Comment get(long id) {
        return super.get(Comment.class, id);
    }
}
