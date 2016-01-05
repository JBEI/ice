package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sample;
import org.jbei.ice.storage.model.Storage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class SampleDAO extends HibernateRepository<Sample> {

    public Sample get(long id) throws DAOException {
        return super.get(Sample.class, id);
    }

    public boolean hasSample(Entry entry) throws DAOException {
        Session session = currentSession();
        try {
            Number itemCount = (Number) session.createCriteria(Sample.class)
                                               .setProjection(Projections.countDistinct("id"))
                                               .add(Restrictions.eq("entry", entry)).uniqueResult();

            return itemCount.intValue() > 0;
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve sample by entry: " + entry.getId(), e);
        }
    }

    public int getSampleCount(Entry entry) {
        Number itemCount = (Number) currentSession().createCriteria(Sample.class)
                .setProjection(Projections.countDistinct("id"))
                .add(Restrictions.eq("entry", entry)).uniqueResult();
        return itemCount.intValue();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Sample> getSamplesByEntry(Entry entry) throws DAOException {
        Query query = currentSession().createQuery("from " + Sample.class.getName() + " where entry=:entry");
        query.setParameter("entry", entry);
        try {
            return new ArrayList<Sample>(query.list());
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve sample by entry: " + entry.getId(), e);
        }
    }

    /**
     * Retrieve {@link Sample} objects associated with the given {@link Storage} object.
     *
     * @param storage
     * @return ArrayList of Sample objects.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Sample> getSamplesByStorage(Storage storage) throws DAOException {
        ArrayList<Sample> samples = null;
        Session session = currentSession();
        try {
            String queryString = "from " + Sample.class.getName() + " as sample where sample.storage = :storage";
            Query query = session.createQuery(queryString);
            query.setEntity("storage", storage);

            @SuppressWarnings("rawtypes")
            List list = query.list();
            if (list != null) {
                samples = (ArrayList<Sample>) list;
            }

        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve sample by storage id: " + storage.getId(), e);
        }
        return samples;
    }
}
