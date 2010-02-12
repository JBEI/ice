package org.jbei.ice.lib.utils;

import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.HibernateHelper;
import org.jbei.ice.lib.managers.Manager;
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
        normalizeAllFundingSources();
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
                Logger.error(msg);
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
            Set<Entry> allEntries = EntryManager.getAll();
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

    public static void normalizeAllFundingSources() {
        Set<Entry> allEntries = EntryManager.getAll();
        for (Entry entry : allEntries) {
            Set<EntryFundingSource> entryFundingSources = entry.getEntryFundingSources();
            for (EntryFundingSource entryFundingSource : entryFundingSources) {
                normalizeFundingSources(entryFundingSource.getFundingSource());

            }
        }
    }

    public static void normalizeFundingSources(FundingSource dupeFundingSource) {

        String queryString = "from " + FundingSource.class.getName()
                + " where fundingSource=:fundingSource AND"
                + " principalInvestigator=:principalInvestigator";
        Query query = HibernateHelper.getSession().createQuery(queryString);
        query.setParameter("fundingSource", dupeFundingSource.getFundingSource());
        query.setParameter("principalInvestigator", dupeFundingSource.getPrincipalInvestigator());
        @SuppressWarnings("unchecked")
        List<FundingSource> dupeFundingSources = query.list();
        FundingSource keepFundingSource = dupeFundingSources.get(0);
        for (int i = 1; i < dupeFundingSources.size(); i++) {
            FundingSource deleteFundingSource = dupeFundingSources.get(i);
            // normalize EntryFundingSources
            queryString = "from " + EntryFundingSource.class.getName()
                    + " where fundingSource=:fundingSource";
            query = HibernateHelper.getSession().createQuery(queryString);
            query.setParameter("fundingSource", deleteFundingSource);
            @SuppressWarnings("unchecked")
            List<EntryFundingSource> entryFundingSources = (query).list();
            for (EntryFundingSource entryFundingSource : entryFundingSources) {
                try {
                    entryFundingSource.setFundingSource(keepFundingSource);
                    Manager.dbSave(entryFundingSource);
                } catch (ManagerException e) {
                    String msg = "Could set normalized entry funding source: " + e.toString();
                    Logger.error(msg);
                }
            }
            // normalize AccountFundingSources
            queryString = "from " + AccountFundingSource.class.getName()
                    + " where fundingSource=:fundingSource";
            query = HibernateHelper.getSession().createQuery(queryString);
            query.setParameter("fundingSource", deleteFundingSource);
            @SuppressWarnings("unchecked")
            List<AccountFundingSource> accountFundingSources = query.list();
            for (AccountFundingSource accountFundingSource : accountFundingSources) {
                accountFundingSource.setFundingSource(keepFundingSource);
                try {
                    Manager.dbSave(accountFundingSource);
                } catch (ManagerException e) {
                    String msg = "Could set normalized entry funding source: " + e.toString();
                    Logger.error(msg);
                }
            }
            try {
                String temp = deleteFundingSource.getPrincipalInvestigator() + ":"
                        + deleteFundingSource.getFundingSource();
                Manager.dbDelete(deleteFundingSource);
                Logger.info("Normalized funding source: " + temp);
            } catch (ManagerException e) {
                String msg = "Could not delete funding source during normalization: "
                        + e.toString();
                Logger.error(msg);
            }
        }
    }

}
