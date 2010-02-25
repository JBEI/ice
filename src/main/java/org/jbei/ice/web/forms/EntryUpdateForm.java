package org.jbei.ice.web.forms;

import java.util.Set;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.EntryViewPage;

public class EntryUpdateForm<T extends Entry> extends EntrySubmitForm<T> {
    private static final long serialVersionUID = 1L;

    public EntryUpdateForm(String id, T entry) {
        super(id);

        setEntry(entry);

        populateFormElements();
    }

    protected void populateFormElements() {
        Entry entry = getEntry();

        setNames(entry.getNamesAsString());
        setCreator(entry.getCreator());
        setCreatorEmail(entry.getCreatorEmail());
        setStatus(lookupCustomChoice(customChoicesList(Entry.getStatusOptionsMap()), entry
                .getStatus()));
        setAlias(entry.getAlias());
        setLinks(entry.getLinksAsString());
        setKeywords(entry.getKeywords());
        setSummary(entry.getShortDescription());
        setNotes(entry.getLongDescription());
        setReferences(entry.getReferences());
        setIntellectualProperty(entry.getIntellectualProperty());
        setBioSafetyLevel(lookupCustomChoice(
                customChoicesList(Entry.getBioSafetyLevelOptionsMap()), String.valueOf(entry
                        .getBioSafetyLevel())));

        Set<EntryFundingSource> entryFundingSources = entry.getEntryFundingSources();
        if (entryFundingSources != null && entryFundingSources.size() > 0) {
            for (EntryFundingSource entryFundingSource : entryFundingSources) {
                FundingSource fundingSource = entryFundingSource.getFundingSource();

                setFundingSource(fundingSource.getFundingSource());
                setPrincipalInvestigator(fundingSource.getPrincipalInvestigator());
            }
        }
    }

    @Override
    protected void populateEntryOwner() {
    }

    @Override
    protected void submitEntry() {
        Entry entry = getEntry();
        if (PermissionManager.hasWritePermission(entry.getId(), IceSession.get().getSessionKey())) {
            try {
                AuthenticatedEntryManager.save(entry, IceSession.get().getSessionKey());

                JobCue.getInstance().addJob(Job.REBUILD_BLAST_INDEX);
                JobCue.getInstance().addJob(Job.REBUILD_SEARCH_INDEX);

                setResponsePage(EntryViewPage.class, new PageParameters("0=" + entry.getId()));
            } catch (ManagerException e) {
                String msg = "System Error: Could not save! ";
                Logger.error(msg + e.getMessage());
                error(msg);
                e.printStackTrace();
            } catch (PermissionException e) {
                error(e.getMessage());
            }
        } else {
            try {
                EntryManager.save(entry);
                info("Save as admin successful!");
            } catch (ManagerException e) {
                String msg = "System Error: Could not save! ";
                Logger.error(msg + e.getMessage());
                error(msg);
                e.printStackTrace();
            }
        }
    }
}
