package org.jbei.ice.lib.managers;

import java.util.LinkedHashSet;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.models.TraceSequenceAlignment;

public class TraceSequenceManager extends Manager {
    public static TraceSequence create(TraceSequence traceSequence) throws ManagerException {
        if (traceSequence == null) {
            throw new ManagerException("Couldn't create TraceSequence. TraceSequence is null!");
        }

        TraceSequence result = (TraceSequence) dbSave(traceSequence);

        return result;
    }

    public static void delete(TraceSequence traceSequence) throws ManagerException {
        if (traceSequence == null) {
            throw new ManagerException("Couldn't delete TraceSequence. TraceSequence is null!");
        }

        dbDelete(traceSequence);
    }

    public static TraceSequence save(TraceSequence traceSequence) throws ManagerException {
        if (traceSequence == null) {
            throw new ManagerException("Couldn't save TraceSequence. TraceSequence is null!");
        }

        TraceSequence result = (TraceSequence) dbSave(traceSequence);

        return result;
    }

    public static TraceSequenceAlignment saveAlignment(TraceSequenceAlignment traceSequenceAlignment)
            throws ManagerException {
        if (traceSequenceAlignment == null) {
            throw new ManagerException(
                    "Couldn't save TraceSequenceAlignment. TraceSequenceAlignment is null!");
        }

        TraceSequenceAlignment result = (TraceSequenceAlignment) dbSave(traceSequenceAlignment);

        return result;
    }

    public static void deleteAlignment(TraceSequenceAlignment traceSequenceAlignment)
            throws ManagerException {
        if (traceSequenceAlignment == null) {
            throw new ManagerException(
                    "Couldn't delete TraceSequenceAlignment. TraceSequenceAlignment is null!");
        }

        dbDelete(traceSequenceAlignment);
    }

    public static TraceSequence get(int id) {
        Session session = getSession();
        TraceSequence traceSequence = null;

        try {
            traceSequence = (TraceSequence) session.load(TraceSequence.class, id);
        } catch (HibernateException e) {
            Logger.error("Could not get TraceSequence!", e);
        }

        return traceSequence;
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashSet<TraceSequence> getByEntry(Entry entry) throws ManagerException {
        if (entry == null) {
            throw new ManagerException("Couldn't get TraceSequences by Entry. Entry is null!");
        }

        LinkedHashSet<TraceSequence> result = null;

        Session session = getSession();
        try {
            String queryString = "from TraceSequence as traceSequence where traceSequence.entry = :entry order by traceSequence.creationTime asc";
            Query query = session.createQuery(queryString);

            query.setEntity("entry", entry);

            result = new LinkedHashSet<TraceSequence>(query.list());
        } catch (Exception e) {
            String msg = "Could not get TraceSequence by Entry " + entry.getRecordId();

            Logger.error(msg, e);

            throw new ManagerException(msg, e);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static int getNumberOfTraceSequences(Entry entry) {
        int result = 0;

        Session session = getSession();
        try {
            String queryString = "from " + TraceSequence.class.getName() + " where entry = :entry";
            Query query = session.createQuery(queryString);
            query.setParameter("entry", entry);
            List<TraceSequence> traceSequences = query.list();
            result = traceSequences.size();
        } catch (Exception e) {
            String msg = "Could not determine number of TraceSequences for entry: "
                    + entry.getRecordId();
            Logger.error(msg, e);
        }

        return result;
    }

    public static void main(String args[]) {
        /*Entry entry;
        try {
            entry = EntryManager.get(4107);

            LinkedHashSet<TraceSequence> traces = getByEntry(entry);

            System.out.println(traces.toString());

            TraceSequence traceSequence = new TraceSequence(entry, "bbbbbb", "zdmytriv@gmail.com",
                    "aaaaaaaaattttcccaaaaccgggggg", "aaaaaaaaattttttttttcccccgggggg", null,
                    new Date());

            create(traceSequence);

            TraceSequenceAlignment traceSequenceAlignment = new TraceSequenceAlignment(
                    traceSequence, 310, "1,0,0,0,0,0,0,0,1,1,1,1,1,0,0,1,1,0,1", 200, 240, 3000,
                    3040, "a-aaatatttt-----atatatactctc", "atata-----tgcatgactgacaaa---tggact",
                    new Date());

            saveAlignment(traceSequenceAlignment);

            traceSequence.setAlignment(traceSequenceAlignment);

            save(traceSequence);

            System.out.println("Looks ok!");
        } catch (ManagerException e) {
            e.printStackTrace();
        }*/
    }
}
