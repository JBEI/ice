package org.jbei.ice.lib.dto.web;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartSource;
import org.jbei.ice.lib.dto.entry.PartWithSequence;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.net.RemoteContact;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents entries that are available for the web (public)
 * These entries can reside on this instance or on other instances
 *
 * @author Hector Plahar
 */
public class WebEntries {

    private final RemotePartnerDAO remotePartnerDAO;
    private final RemoteContact remoteContact;
    private final EntryDAO entryDAO;

    public WebEntries() {
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        this.entryDAO = DAOFactory.getEntryDAO();
        this.remoteContact = new RemoteContact();
    }

    public static Iterator<PartWithSequence> iterator() {
        return new WebEntriesIterator();
    }

    /**
     * Checks the local database for the entry with id <code>recordId</code>
     * If it exists locally and is public, it returns it. Otherwise it checks the
     * other ICE instances that it partners with, in turn, to see if it exists on there
     *
     * @param recordId unique record identifier for the desired entry
     * @return entry details if found, else null
     * @throws PermissionException if the entry exists locally but is not a public entry
     */
    public PartData getPart(String recordId) {
        // check local first
        Entry entry = this.entryDAO.getByRecordId(recordId);
        if (entry != null && entry.getVisibility() != Visibility.REMOTE.getValue()) {
            PermissionsController permissionsController = new PermissionsController();
            if (permissionsController.isPubliclyVisible(entry))
                return ModelToInfoFactory.getInfo(entry);
        }

        List<RemotePartner> partners = this.remotePartnerDAO.getRegistryPartners();
        for (RemotePartner partner : partners) {
            if (partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
                continue;

            PartData partData = this.remoteContact.getPublicEntry(partner.getUrl(), recordId, partner.getApiKey());

            // if the part is just a remote then the main one is on some other ICE instance
            if (partData == null || partData.getVisibility() == Visibility.REMOTE)
                continue;

            return partData;
        }
        return null;
    }

    public FeaturedDNASequence getSequence(String entryId) {
        String recordId;
        try {
            long id = Long.decode(entryId);
            Entry entry = this.entryDAO.get(id);
            if (entry == null)
                recordId = entryId;
            else
                recordId = entry.getRecordId();

        } catch (NumberFormatException ex) {
            recordId = entryId;
        }

        List<RemotePartner> partners = this.remotePartnerDAO.getRegistryPartners();
        for (RemotePartner partner : partners) {
            if (partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
                continue;

            FeaturedDNASequence sequence = this.remoteContact.getPublicEntrySequence(partner.getUrl(), recordId, partner.getApiKey());
            if (sequence != null)
                return sequence;
        }
        return null;
    }

    private static class WebEntriesIterator implements Iterator<PartWithSequence> {
        private List<RemotePartner> partners;
        private List<PartWithSequence> parts;
        private RemotePartner nextPartner;
        private int start;
        private final int fetchCount = 30;

        WebEntriesIterator() {
            partners = DAOFactory.getRemotePartnerDAO().getRegistryPartners();
            parts = new ArrayList<>();
            nextPartner = partners.remove(0);
            fetchMore();
        }

        @Override
        public boolean hasNext() {
            return !parts.isEmpty();
        }

        @Override
        public PartWithSequence next() {
            if (!hasNext())
                return null;

            PartWithSequence sequence = parts.remove(0);
            if (parts.isEmpty()) {
                fetchMore();
            }

            return sequence;
        }

        private void fetchMore() {
            if (nextPartner == null)
                return;

            int retrieved = fetchPartnerEntries(nextPartner);

            if (retrieved == -1) {
                nextPartner = partners.isEmpty() ? null : partners.remove(0);
            }

            // set next start
            if (retrieved < fetchCount) {
                start = 0;
                nextPartner = partners.isEmpty() ? null : partners.remove(0);
            } else {
                start += retrieved;
            }
        }

        private int fetchPartnerEntries(RemotePartner partner) {
            if (partner == null || StringUtils.isEmpty(partner.getUrl()))
                return -1;

            String url = partner.getUrl();
            String registryName = StringUtils.isEmpty(partner.getName()) ? url : partner.getName();

            // fetch "limit" number of entries
            PartnerEntries results = getPartnerEntries(partner);
            if (results == null || results.getEntries().getResultCount() == 0) {
                Logger.error("No results from partner: " + partner.getUrl());
                return 0;
            }

            PartSource source = new PartSource(url, registryName, Long.toString(partner.getId()));

            // index each part and sequence (if available)
            for (PartData result : results.getEntries().getData()) {
                PartWithSequence partSequence = new PartWithSequence();
                partSequence.setPartSource(source);

                if (result.isHasSequence()) {
                    // retrieve sequence
                    IceRestClient client = new IceRestClient(partner.getUrl(), partner.getApiKey());
                    String path = "/rest/web/" + partner.getId() + "/entries/" + result.getRecordId() + "/sequence";
                    FeaturedDNASequence sequence = client.get(path, FeaturedDNASequence.class);
                    if (sequence != null) {
                        partSequence.setSequence(sequence);
                    }
                }

                parts.add(partSequence);
            }

            return results.getEntries().getData().size();
        }

        private PartnerEntries getPartnerEntries(RemotePartner partner) {
            IceRestClient client = new IceRestClient(partner.getUrl(), partner.getApiKey());
            client.queryParam("offset", start);
            client.queryParam("limit", fetchCount);
            String path = "/rest/partners/" + partner.getId() + "/entries";
            return client.get(path, PartnerEntries.class);
        }
    }
}
