package org.jbei.ice.lib.entry.sample;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sample.model.Storage;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class SampleDAO extends HibernateRepository {

    /**
     * Save the given {@link Sample} object in the database.
     *
     * @param sample
     * @return Saved Sample object.
     * @throws DAOException
     */
    public Sample save(Sample sample) throws DAOException {
        return (Sample) super.saveOrUpdate(sample);
    }

    public boolean hasSample(Entry entry) throws DAOException {
        Session session = newSession();
        try {

            Integer itemCount = (Integer) session.createCriteria(Sample.class)
                                                 .setProjection(Projections.countDistinct("id"))
                                                 .add(Restrictions.eq("entry", entry)).uniqueResult();

            return itemCount.intValue() > 0;
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve sample by entry: " + entry.getId(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Delete the give {@link Sample} object in the database.
     *
     * @param sample
     * @throws ManagerException
     */
    public void deleteSample(Sample sample) throws DAOException {
        super.delete(sample);
    }

    /**
     * Retrieve {@link Sample} object associated with the given {@link Entry} object.
     *
     * @return ArrayList of Sample objects.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Long> getSampleIdsByOwner(String ownerId) throws DAOException {

        ArrayList<Long> results = null;
        Session session = newSession();

        try {

            String queryString = "select id from " + Sample.class.getName()
                    + " where depositor = :depositor";

            Query query = session.createQuery(queryString);

            query.setParameter("depositor", ownerId);

            @SuppressWarnings("rawtypes")
            List list = query.list();

            if (list != null) {
                results = (ArrayList<Long>) list;
            }
        } catch (HibernateException he) {
            throw new DAOException("Failed to retrieve samples for owner " + ownerId, he);
        } finally {
            if (session.isOpen())
                session.close();
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Sample> getSamplesByEntry(Entry entry) throws DAOException {
        ArrayList<Sample> samples = null;

        Session session = newSession();
        try {
            String queryString = "from " + Sample.class.getName()
                    + " as sample where sample.entry = :entry order by sample.id desc";

            Query query = session.createQuery(queryString);

            query.setEntity("entry", entry);

            @SuppressWarnings("rawtypes")
            List list = query.list();

            if (list != null) {
                samples = (ArrayList<Sample>) list;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve sample by entry: " + entry.getId(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return samples;
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
        Session session = newSession();
        try {
            String queryString = "from " + Sample.class.getName()
                    + " as sample where sample.storage = :storage";

            Query query = session.createQuery(queryString);

            query.setEntity("storage", storage);

            @SuppressWarnings("rawtypes")
            List list = query.list();
            if (list != null) {
                samples = (ArrayList<Sample>) list;
            }

        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve sample by storage: " + storage.getId(),
                                   e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return samples;
    }

    /**
     * Retrieve {@link Sample} objects by its index field.
     *
     * @param code
     * @return ArrayList of Sample objects.
     * @throws DAOException
     */
    public ArrayList<Sample> retrieveSamplesByIndex(String code) throws DAOException {
        Session session = newSession();
        String queryString = "from " + Storage.class.getName()
                + " as storage where storage.index = :code";
        try {
            Query query = session.createQuery(queryString);
            query.setParameter("code", code);
            Storage storage = (Storage) query.uniqueResult();
            return getSamplesByStorage(storage);
        } catch (Exception e) {
            String msg = "Could not get Storage by code: " + code + " " + e.toString();
            Logger.error(msg, e);
            throw new DAOException(msg);
        }
    }

    /**
     * Retrieve {@link Sample} objects by its depositor field.
     *
     * @param depositor
     * @param offset
     * @param limit
     * @return ArrayList of Sample objects.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Sample> getSamplesByDepositor(String depositor, int offset, int limit)
            throws DAOException {
        ArrayList<Sample> samples = null;

        Session session = newSession();
        try {
            String queryString = "from " + Sample.class.getName()
                    + " as sample where sample.depositor = :depositor order by sample.id desc";

            Query query = session.createQuery(queryString);

            query.setParameter("depositor", depositor);
            query.setFirstResult(offset);
            if (limit >= 0) {
                query.setMaxResults(limit);
            }

            @SuppressWarnings("rawtypes")
            List list = query.list();

            if (list != null) {
                samples = (ArrayList<Sample>) list;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve sample by depositor: " + depositor, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return samples;
    }

    /**
     * Retrieve the number of samples associated the given depositor (email) string.
     *
     * @param depositor
     * @return Number of samples.
     * @throws DAOException
     */
    public int getSampleCountBy(String depositor) throws DAOException {
        Session session = newSession();
        try {
            SQLQuery query = session
                    .createSQLQuery("SELECT COUNT(id) FROM samples WHERE depositor = :depositor ");
            query.setString("depositor", depositor);
            return ((BigInteger) query.uniqueResult()).intValue();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Long> retrieveSamplesByDepositorSortByCreated(String depositor,
            boolean ascending) throws DAOException {
        Session session = newSession();
        LinkedList<Long> results = null;

        try {
            String queryString = "SELECT id FROM " + Sample.class.getName()
                    + " WHERE depositor = :depositor ORDER BY creationTime "
                    + ((ascending ? "ASC" : "DESC"));
            Query query = session.createQuery(queryString);
            query.setParameter("depositor", depositor);

            @SuppressWarnings("rawtypes")
            List list = query.list();

            if (list != null)
                results = new LinkedList<Long>(list);
            return results;

        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Sample> getSamplesByIdSet(LinkedList<Long> ids, boolean asc)
            throws DAOException {
        LinkedList<Sample> samples = new LinkedList<Sample>();

        if (ids.size() == 0) {
            return samples;
        }

        String filter = Utils.join(", ", ids);
        String suffix = "ORDER BY id " + (asc ? "ASC" : "DESC");

        Session session = newSession();
        try {

            Query query = session.createQuery("from " + Sample.class.getName() + " e WHERE id in ("
                                                      + filter + ") " + suffix);

            @SuppressWarnings("rawtypes")
            ArrayList list = (ArrayList) query.list();

            if (list != null) {
                samples.addAll(list);
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve samples!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return samples;
    }
}
