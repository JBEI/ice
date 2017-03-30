package org.jbei.ice.lib.dto.web;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.net.RemoteContact;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.RemotePartner;

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
}
