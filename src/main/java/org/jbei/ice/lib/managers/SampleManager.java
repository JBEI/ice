package org.jbei.ice.lib.managers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.utils.Utils;

public class SampleManager {
    public static Sample saveSample(Sample sample) throws ManagerException {
        if (sample == null) {
            throw new ManagerException("Failed to save null sample!");
        }

        if (sample.getEntry() == null) {
            throw new ManagerException("Failed to save sample without entry!");
        }

        try {
            sample = (Sample) DAO.save(sample);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save sample!", e);
        }

        return sample;
    }

    public static void deleteSample(Sample sample) throws ManagerException {
        if (sample == null) {
            throw new ManagerException("Failed to delete null sample!");
        }

        try {
            DAO.delete(sample);
        } catch (DAOException e) {
            throw new ManagerException("Failed to delete sample!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Sample> getSamplesByEntry(Entry entry) throws ManagerException {
        ArrayList<Sample> samples = null;

        Session session = DAO.newSession();
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
            throw new ManagerException("Failed to retrieve sample by entry: " + entry.getId(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return samples;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Sample> getSamplesByStorage(Storage storage) throws ManagerException {
        ArrayList<Sample> samples = null;
        Session session = DAO.newSession();
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
            throw new ManagerException("Failed to retrieve sample by storage: " + storage.getId(),
                    e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return samples;
    }

    public static ArrayList<Sample> retrieveSamplesByIndex(String code) throws ManagerException {
        Session session = DAO.newSession();
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
            throw new ManagerException(msg);
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Sample> getSamplesByDepositor(String depositor, int offset, int limit)
            throws ManagerException {
        ArrayList<Sample> samples = null;

        Session session = DAO.newSession();
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
            throw new ManagerException("Failed to retrieve sample by depositor: " + depositor, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return samples;
    }

    public static int getSampleCountBy(String depositor) throws ManagerException {
        Session session = DAO.newSession();
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
    public static LinkedList<Long> retrieveSamplesByDepositorSortByCreated(String depositor,
            boolean ascending) throws ManagerException {
        Session session = DAO.newSession();
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
    public static LinkedList<Sample> getSamplesByIdSet(LinkedList<Long> ids, boolean asc)
            throws ManagerException {
        LinkedList<Sample> samples = new LinkedList<Sample>();

        if (ids.size() == 0) {
            return samples;
        }

        String filter = Utils.join(", ", ids);
        String suffix = "ORDER BY id " + (asc ? "ASC" : "DESC");

        Session session = DAO.newSession();
        try {

            Query query = session.createQuery("from " + Sample.class.getName() + " e WHERE id in ("
                    + filter + ") " + suffix);

            @SuppressWarnings("rawtypes")
            ArrayList list = (ArrayList) query.list();

            if (list != null) {
                samples.addAll(list);
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve samples!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return samples;
    }
}
