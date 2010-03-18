package org.jbei.ice.web.forms;

import java.util.Set;

import org.apache.wicket.PageParameters;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.EntryViewPage;

public class EntryUpdateForm<T extends Entry> extends EntrySubmitForm<T> {
    private static final long serialVersionUID = 1L;

    public EntryUpdateForm(String id, T entry) {
        super(id);

        EntryController entryController = new EntryController(IceSession.get().getAccount());
        try {
            if (!entryController.hasWritePermission(entry)) {
                throw new ViewPermissionException("No write permissions for this entry!");
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

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
        EntryController entryController = new EntryController(IceSession.get().getAccount());

        Entry entry = getEntry();
        try {
            if (entryController.hasWritePermission(entry)) {
                entryController.save(entry);

                setResponsePage(EntryViewPage.class, new PageParameters("0=" + entry.getId()));
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        } catch (PermissionException e) {
            throw new ViewPermissionException("No permissions to save entry!", e);
        }
    }
}
