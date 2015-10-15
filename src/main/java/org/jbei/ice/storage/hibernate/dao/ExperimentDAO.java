package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Experiment;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class ExperimentDAO extends HibernateRepository<Experiment> {

    public Experiment get(long id) {
        return super.get(Experiment.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Experiment> getExperimentList(long entryId) {
        try {
            String sql = "SELECT DISTINCT e FROM Experiment e JOIN e.subjects s WHERE s.id=:id";
            Query query = currentSession().createQuery(sql);
            query.setLong("id", entryId);
            return query.list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getExperimentCount(long entryId) throws DAOException {
        try {
            String sql = "SELECT count(*) FROM experiment_entry WHERE entry_id=:id";
            SQLQuery query = currentSession().createSQLQuery(sql);
            query.setLong("id", entryId);
            Number result = (Number) query.uniqueResult();
            return result.intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Experiment getByUrl(String url) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(Experiment.class.getName())
                    .add(Restrictions.eq("url", url));
            return (Experiment) criteria.uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
