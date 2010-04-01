package org.jbei.ice.lib.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.AccountFundingSource;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.PermissionManager;

public class PopulateInitialDatabase {
    // This is a global "everyone" uuid
    public static String everyoneGroup = "8746a64b-abd5-4838-a332-02c356bbeac0";

    public static void main(String[] args) {
        /*
        createFirstGroup();
        populatePermissionReadGroup();
         */
        try {
            normalizeAllFundingSources();
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    public static Group createFirstGroup() {
        Group group1 = null;
        try {
            group1 = GroupManager.get(everyoneGroup);

        } catch (ManagerException e) {
            String msg = "Could not get everyone group " + e.toString();
            Logger.info(msg);
        }

        if (group1 == null) {
            Group group = new Group();
            group.setLabel("Everyone");
            group.setDescription("Everyone");
            group.setParent(null);

            group.setUuid(everyoneGroup);
            try {
                GroupManager.save(group);
                Logger.info("Creating everyone group");
                group1 = group;
            } catch (ManagerException e) {
                String msg = "Could not save everyone group: " + e.toString();
                Logger.error(msg, e);
            }
        }
        return group1;

    }

    public static void populatePermissionReadGroup() {
        Group group1 = null;
        try {
            group1 = GroupManager.get(everyoneGroup);
        } catch (ManagerException e) {
            // nothing happens
            Logger.debug(e.toString());
        }
        if (group1 != null) {
            ArrayList<Entry> allEntries = null;
            try {
                allEntries = EntryManager.getAllEntries();
            } catch (ManagerException e1) {
                e1.printStackTrace();
            }
            for (Entry entry : allEntries) {
                try {
                    Set<Group> groups = PermissionManager.getReadGroup(entry);
                    int originalSize = groups.size();
                    groups.add(group1);
                    PermissionManager.setReadGroup(entry, groups);

                    String msg = "updated id:" + entry.getId() + " from " + originalSize + " to "
                            + groups.size() + ".";
                    Logger.info(msg);
                } catch (ManagerException e) {
                    // skip
                    Logger.debug(e.toString());
                }

            }
        }
    }

    public static void normalizeAllFundingSources() throws DAOException {
        ArrayList<Entry> allEntries = null;

        try {
            allEntries = EntryManager.getAllEntries();
        } catch (ManagerException e) {
            e.printStackTrace();
        }

        for (Entry entry : allEntries) {
            Set<EntryFundingSource> entryFundingSources = entry.getEntryFundingSources();

            for (EntryFundingSource entryFundingSource : entryFundingSources) {
                normalizeFundingSources(entryFundingSource.getFundingSource());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void normalizeFundingSources(FundingSource dupeFundingSource) throws DAOException {

        String queryString = "from " + FundingSource.class.getName()
                + " where fundingSource=:fundingSource AND"
                + " principalInvestigator=:principalInvestigator";
        Session session = DAO.newSession();
        Query query = session.createQuery(queryString);
        query.setParameter("fundingSource", dupeFundingSource.getFundingSource());
        query.setParameter("principalInvestigator", dupeFundingSource.getPrincipalInvestigator());
        ArrayList<FundingSource> dupeFundingSources = new ArrayList<FundingSource>();
        try {
            dupeFundingSources = new ArrayList<FundingSource>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not get funding sources " + e.toString(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        FundingSource keepFundingSource = dupeFundingSources.get(0);
        for (int i = 1; i < dupeFundingSources.size(); i++) {
            FundingSource deleteFundingSource = dupeFundingSources.get(i);
            // normalize EntryFundingSources
            queryString = "from " + EntryFundingSource.class.getName()
                    + " where fundingSource=:fundingSource";
            session = DAO.newSession();
            query = session.createQuery(queryString);
            query.setParameter("fundingSource", deleteFundingSource);
            List<EntryFundingSource> entryFundingSources = null;
            try {
                entryFundingSources = (query).list();
            } catch (HibernateException e) {
                Logger.error("Could not get funding sources " + e.toString(), e);
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }

            for (EntryFundingSource entryFundingSource : entryFundingSources) {
                try {
                    entryFundingSource.setFundingSource(keepFundingSource);
                    DAO.save(entryFundingSource);
                } catch (DAOException e) {
                    throw e;
                }
            }

            // normalize AccountFundingSources
            queryString = "from " + AccountFundingSource.class.getName()
                    + " where fundingSource=:fundingSource";
            session = DAO.newSession();
            query = session.createQuery(queryString);
            query.setParameter("fundingSource", deleteFundingSource);
            List<AccountFundingSource> accountFundingSources = null;
            try {
                accountFundingSources = query.list();
            } catch (HibernateException e) {
                Logger.error("Could not get funding sources " + e.toString(), e);
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }

            for (AccountFundingSource accountFundingSource : accountFundingSources) {
                accountFundingSource.setFundingSource(keepFundingSource);
                try {
                    DAO.save(accountFundingSource);
                } catch (DAOException e) {
                    String msg = "Could set normalized entry funding source: " + e.toString();
                    Logger.error(msg, e);
                }
            }
            try {
                String temp = deleteFundingSource.getPrincipalInvestigator() + ":"
                        + deleteFundingSource.getFundingSource();
                DAO.delete(deleteFundingSource);
                Logger.info("Normalized funding source: " + temp);
            } catch (DAOException e) {
                String msg = "Could not delete funding source during normalization: "
                        + e.toString();
                Logger.error(msg, e);
            }
        }
    }
}
