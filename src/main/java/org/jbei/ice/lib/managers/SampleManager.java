package org.jbei.ice.lib.managers;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.utils.Utils;

public class SampleManager extends Manager {
    public static Sample get(int id) throws ManagerException {
        Sample result = null;
        try {
            result = (Sample) dbGet(Sample.class, id);
        } catch (Exception e) {
            String msg = "Could not get Sample by id";
            Logger.error(msg, e);

            e.printStackTrace();
            throw new ManagerException(msg, e);

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashSet<Sample> get(Entry entry) throws ManagerException {
        LinkedHashSet<Sample> result = null;
        Session session = getSession();
        try {
            String queryString = "from Sample as sample where sample.entry = :entry order by sample.id desc";
            Query query = session.createQuery(queryString);

            query.setEntity("entry", entry);

            result = new LinkedHashSet<Sample>(query.list());
        } catch (Exception e) {
            String msg = "Could not get Sample by Entry " + entry.getRecordId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        } finally {

        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashSet<Sample> getByAccount(Account account, int offset, int limit)
            throws ManagerException {
        LinkedHashSet<Sample> result = null;
        Session session = getSession();
        try {
            String queryString = "from Sample as sample where sample.depositor = :depositor";

            Query query = session.createQuery(queryString);

            query.setParameter("depositor", account.getEmail());
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            result = new LinkedHashSet<Sample>(query.list());
        } catch (Exception e) {
            String msg = "Could not retrieve samples by account " + account.getEmail();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        } finally {

        }

        return result;
    }

    public static int getByAccountCount(Account account) throws ManagerException {
        Session session = getSession();
        try {
            String queryString = "from Sample as sample where sample.depositor = :depositor";

            Query query = session.createQuery(queryString);

            query.setParameter("depositor", account.getEmail());

            return query.list().size();
        } catch (Exception e) {
            String msg = "Could not retrieve samples by account " + account.getEmail();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        } finally {

        }
    }

    @SuppressWarnings("unchecked")
    public static boolean hasSample(Entry entry) {
        boolean result = false;
        Session session = getSession();
        try {
            String queryString = "from " + Sample.class.getName() + " where entry = :entry";
            Query query = session.createQuery(queryString);
            query.setParameter("entry", entry);
            List samples = query.list();
            if (samples.size() > 0) {
                result = true;
            }
        } catch (Exception e) {
            String msg = "Could not determine if entry has Sample: " + entry.getRecordId();
            Logger.error(msg, e);
        } finally {

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static int getNumberOfSamples(Entry entry) {
        int result = 0;
        Session session = getSession();
        try {
            String queryString = "from " + Sample.class.getName() + " where entry = :entry";
            Query query = session.createQuery(queryString);
            query.setParameter("entry", entry);
            List samples = query.list();
            result = samples.size();
        } catch (Exception e) {
            String msg = "Could not determine if entry has Sample: " + entry.getRecordId();
            Logger.error(msg, e);
        } finally {

        }
        return result;
    }

    public static Sample create(Sample sample) throws ManagerException {

        sample.setCreationTime(Calendar.getInstance().getTime());
        sample.setUuid(Utils.generateUUID());
        return save(sample);

    }

    public static Sample save(Sample sample) throws ManagerException {
        Sample result = null;
        sample.setModificationTime(Calendar.getInstance().getTime());
        if (sample.getUuid() == null || sample.getUuid().equals("")) {
            result = create(sample);
        } else {
            try {
                result = (Sample) dbSave(sample);
            } catch (Exception e) {
                String msg = "Could not save sample " + sample.getLabel();
                Logger.error(msg, e);
                throw new ManagerException(msg, e);
            }
        }
        return result;
    }

    public static void delete(Sample sample) throws ManagerException {
        try {
            dbDelete(sample);
        } catch (Exception e) {
            String msg = "Could not delete sample " + sample.getId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

}
