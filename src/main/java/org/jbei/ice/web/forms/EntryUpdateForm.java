package org.jbei.ice.web.forms;

import java.util.Set;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.ParameterGeneratorParser;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.DeletionMessagePage;
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
        setStatus(lookupCustomChoice(customChoicesList(Entry.getStatusOptionsMap()),
            entry.getStatus()));
        setAlias(entry.getAlias());
        setLinks(entry.getLinksAsString());
        setKeywords(entry.getKeywords());
        setSummary(entry.getShortDescription());
        setReferences(entry.getReferences());
        setIntellectualProperty(entry.getIntellectualProperty());
        setBioSafetyLevel(lookupCustomChoice(
            customChoicesList(Entry.getBioSafetyLevelOptionsMap()),
            String.valueOf(entry.getBioSafetyLevel())));
        setParameters(ParameterGeneratorParser.generateParametersString(entry.getParameters()));
        setNotesMarkupType(lookupCustomChoice(customChoicesList(Entry.getMarkupTypeMap()),
            String.valueOf(entry.getLongDescriptionType())));

        updateNotesMarkupEditor(entry.getLongDescriptionType());

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

    protected AjaxButton createDeleteButton(Form<?> form) {
        AjaxButton deleteButton = new AjaxButton("deleteButton", form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Entry entry = getEntry();
                EntryController entryController = new EntryController(IceSession.get().getAccount());
                try {
                    entryController.delete(entry);
                } catch (ControllerException e) {
                    throw new ViewException(e);
                } catch (PermissionException e) {
                    throw new ViewException(e);
                }
                setResponsePage(DeletionMessagePage.class,
                    new PageParameters("number=" + entry.getOnePartNumber().getPartNumber()
                            + ",recordId=" + entry.getRecordId()));
            }
        };

        deleteButton.add(new JavascriptEventConfirmation("onclick", "Delete this Entry?"));
        return deleteButton;
    }

}
