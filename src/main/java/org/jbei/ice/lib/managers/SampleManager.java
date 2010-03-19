package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;

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
}
