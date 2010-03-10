package org.jbei.ice.lib.managers;

import java.util.ArrayList;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Workspace;
import org.jbei.ice.lib.query.SortField;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.IceSession;

import edu.emory.mathcs.backport.java.util.Arrays;

public class WorkspaceManager extends Manager {

    public static Workspace create(Account account, Entry entry) throws ManagerException {
        Workspace result = new Workspace(account, entry);
        try {
            dbSave(result);
        } catch (ManagerException e) {
            throw new ManagerException("Could not create Workspace", e);
        }
        return result;
    }

    public static Workspace addOrUpdate(Workspace workspace) throws ManagerException {

        Workspace result = get(workspace.getAccount(), workspace.getEntry());
        if (result == null) {
            result = workspace;
        }
        result.setInWorkspace(true);
        try {
            result = (Workspace) dbSave(result);
        } catch (Exception e) {
            new ManagerException("Could not add workspace ", e);
        }
        return result;
    }

    public static boolean hasEntry(Account account, Entry entry) {

        boolean result = false;
        String queryString = "from Workspace workspace where entry=:entry and account=:account";
        Session session = getSession();
        Query query = session.createQuery(queryString);
        query.setParameter("entry", entry);
        query.setParameter("account", account);
        try {
            Workspace queryResult = (Workspace) query.uniqueResult();
            if (queryResult != null) {
                if (queryResult.isInWorkspace()) {
                    result = true;
                }
            }
        } catch (Exception e) {
            Logger.error("Could not determine if account's entry is in workspace", e);
        }
        return result;
    }

    public static boolean hasEntry(Entry entry) throws ManagerException {
        Account account = IceSession.get().getAccount();
        return hasEntry(account, entry);
    }

    public static void setVisited(Account account, Entry entry) {

        try {
            Workspace queryResult = get(account, entry);
            if (queryResult == null) {
                queryResult = create(account, entry);
            }

            long numberVisited = queryResult.getNumberVisited() + 1;
            queryResult.setNumberVisited(numberVisited);
            queryResult.setDateVisited(System.currentTimeMillis());
            dbSave(queryResult);

        } catch (Exception e) {
            new ManagerException("Could not set visited number", e);
        }
    }

    public static void setVisited(Entry entry) {
        Account account = IceSession.get().getAccount();
        setVisited(account, entry);
    }

    public static ArrayList<Workspace> get() {
        ArrayList<Workspace> result = null;
        Account account = IceSession.get().getAccount();
        String queryString = "from Workspace workspace where account=:account order by workspace.dateAdded desc";
        Session session = getSession();
        Query query = session.createQuery(queryString);
        query.setParameter("account", account);
        try {
            @SuppressWarnings("unchecked")
            ArrayList<Workspace> temp = new ArrayList<Workspace>(query.list());
            result = temp;
        } catch (Exception e) {
            new ManagerException("Could not get workspace for account " + account.getEmail(), e);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Workspace> getByAccount(Account account, int offset, int limit,
            SortField[] sortFields) {
        String sortQuerySuffix = "";

        if (sortFields != null && sortFields.length > 0) {
            sortQuerySuffix = Utils.join(", ", Arrays.asList(sortFields));
        }

        String queryString = "from Workspace where account=:account and inWorkspace = true"
                + (!sortQuerySuffix.isEmpty() ? (" ORDER BY " + sortQuerySuffix) : "");
        Session session = getSession();
        Query query = session.createQuery(queryString);
        query.setParameter("account", account);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        ArrayList<Workspace> result = null;
        try {

            result = new ArrayList<Workspace>(query.list());

        } catch (Exception e) {
            Logger.error("Could not get workspace by account ", e);
        }

        return result;
    }

    public static Workspace get(Account account, Entry entry) throws ManagerException {
        String queryString = "from Workspace workspace where entry=:entry and account=:account";
        Session session = getSession();
        Query query = session.createQuery(queryString);
        query.setParameter("entry", entry);
        query.setParameter("account", account);
        Workspace result = null;
        try {
            result = (Workspace) query.uniqueResult();
        } catch (Exception e) {
            throw new ManagerException("Could not get by account and entry", e);
        }
        return result;
    }

    public static int getCountByAccount(Account account) {
        String queryString = "from Workspace workspace where account=:account and inWorkspace = true order by workspace.dateAdded desc";
        Session session = getSession();
        Query query = session.createQuery(queryString);
        query.setParameter("account", account);

        return query.list().size();
    }

    public static Workspace save(Workspace workspace) throws ManagerException {
        Workspace result = (Workspace) dbSave(workspace);
        return result;
    }
}
