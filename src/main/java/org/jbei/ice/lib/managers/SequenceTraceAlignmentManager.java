package org.jbei.ice.lib.managers;

import java.util.LinkedHashSet;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.SequenceTraceAlignment;

public class SequenceTraceAlignmentManager extends Manager {
    public static SequenceTraceAlignment create(SequenceTraceAlignment sequenceTraceAlignment)
            throws ManagerException {
        if (sequenceTraceAlignment == null) {
            throw new ManagerException("SequenceTraceAlignment is null!");
        }

        SequenceTraceAlignment result;

        try {
            result = (SequenceTraceAlignment) dbSave(sequenceTraceAlignment);
        } catch (Exception e) {
            throw new ManagerException("Could not create SequenceTraceAlignment in db");
        }

        return result;
    }

    public static void delete(SequenceTraceAlignment sequenceTraceAlignment)
            throws ManagerException {
        if (sequenceTraceAlignment == null) {
            throw new ManagerException("SequenceTraceAlignment is null!");
        }

        try {
            dbDelete(sequenceTraceAlignment);
        } catch (Exception e) {
            throw new ManagerException("Could not delete SequenceTraceAlignment in db: "
                    + e.toString());
        }
    }

    public static SequenceTraceAlignment save(SequenceTraceAlignment sequenceTraceAlignment)
            throws ManagerException {
        if (sequenceTraceAlignment == null) {
            throw new ManagerException("SequenceTraceAlignment is empty!");
        }

        SequenceTraceAlignment result;

        try {
            result = (SequenceTraceAlignment) dbSave(sequenceTraceAlignment);
        } catch (Exception e) {
            throw new ManagerException("Could not update SequenceTraceAlignment in db");
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashSet<SequenceTraceAlignment> get(Entry entry) throws ManagerException {
        if (entry == null) {
            throw new ManagerException("Invalid entry!");
        }

        LinkedHashSet<SequenceTraceAlignment> result = null;

        Session session = getSession();
        try {
            String queryString = "from SequenceTraceAlignment as sequenceTraceAlignment where sequenceTraceAlignment.entry = :entry order by sequenceTraceAlignment.id desc";
            Query query = session.createQuery(queryString);

            query.setEntity("entry", entry);

            result = new LinkedHashSet<SequenceTraceAlignment>(query.list());
        } catch (Exception e) {
            String msg = "Could not get Sample by Entry " + entry.getRecordId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashSet<SequenceTraceAlignment> getByAccount(Account account, int offset,
            int limit) throws ManagerException {
        if (account == null) {
            throw new ManagerException("Invalid account!");
        }

        LinkedHashSet<SequenceTraceAlignment> result = null;

        Session session = getSession();
        try {
            String queryString = "from SequenceTraceAlignment as sequenceTraceAlignment where sequenceTraceAlignment.depositor = :depositor";

            Query query = session.createQuery(queryString);

            query.setParameter("depositor", account.getEmail());
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            result = new LinkedHashSet<SequenceTraceAlignment>(query.list());
        } catch (Exception e) {
            String msg = "Could not retrieve samples by account " + account.getEmail();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }

        return result;
    }

    public static int getByAccountCount(Account account) throws ManagerException {
        if (account == null) {
            throw new ManagerException("Invalid account!");
        }

        Session session = getSession();

        try {
            String queryString = "from SequenceTraceAlignment as sequenceTraceAlignment where sequenceTraceAlignment.depositor = :depositor";

            Query query = session.createQuery(queryString);

            query.setParameter("depositor", account.getEmail());

            return query.list().size();
        } catch (Exception e) {
            String msg = "Could not retrieve SequenceTraceAlignment by account "
                    + account.getEmail();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }
}
