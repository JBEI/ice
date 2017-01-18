package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.hibernate.query.NativeQuery;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Experiment;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class ExperimentDAO extends HibernateRepository<Experiment> {

    public Experiment get(long id) {
        return super.get(Experiment.class, id);
    }

    public List<Experiment> getExperimentList(long entryId) {
        try {
            CriteriaQuery<Experiment> query = getBuilder().createQuery(Experiment.class);
            Root<Experiment> from = query.from(Experiment.class);
            Join<Experiment, Entry> entryJoin = from.join("subjects");
            query.distinct(true).where(getBuilder().equal(entryJoin.get("id"), entryId));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getExperimentCount(long entryId) throws DAOException {
        try {
            String sql = "SELECT count(*) FROM experiment_entry WHERE entry_id=:id";
            NativeQuery query = currentSession().createNativeQuery(sql);
            query.setParameter("id", entryId);
            Number result = (Number) query.uniqueResult();
            return result.intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Experiment getByUrl(String url) throws DAOException {
        try {
            CriteriaQuery<Experiment> query = getBuilder().createQuery(Experiment.class);
            Root<Experiment> from = query.from(Experiment.class);
            query.where(getBuilder().equal(from.get("url"), url));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
