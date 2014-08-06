package org.jbei.ice.lib.dao.hibernate;

import java.util.List;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.experiment.Experiment;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;

/**
 * @author Hector Plahar
 */
public class ExperimentDAO extends HibernateRepository<Experiment> {

    public Experiment get(long id) {
        return super.get(Experiment.class, id);
    }

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
}
