package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.EntryUpdatePage;
import org.jbei.ice.web.pages.ProfilePage;
import org.jbei.ice.web.panels.sample.BriefSampleViewPanel;
import org.jbei.ice.web.utils.WebUtils;

public class AbstractEntryViewPanel<T extends Entry> extends Panel {
    private static final long serialVersionUID = 1L;

    private final Model<T> entryModel;

    public AbstractEntryViewPanel(String id, Model<T> entryModel) {
        super(id);

        this.entryModel = entryModel;

        renderPartNumber();
        renderNames();
        renderAlias();
        renderCreator();
        renderOwner();
        renderLinks();
        renderStatus();
        renderCreationTime();
        renderModificationTime();
        renderKeywords();
        renderSummary();
        //renderNotes();
        renderReferences();
        renderBioSafetyLevel();
        renderIntellectualProperty();
        renderFundingSource();
        renderPrincipalInvestigator();
        renderParameters();
        renderBriefSampleView();
    }

    protected T getEntry() {
        return entryModel.getObject();
    }

    protected void renderTopLink() {
        EntryController entryController = new EntryController(IceSession.get().getAccount());

        WebMarkupContainer topLinkContainer = new WebMarkupContainer("topLink");

        try {
            topLinkContainer.setOutputMarkupId(true);
            topLinkContainer.setOutputMarkupPlaceholderTag(true);
            topLinkContainer.setVisible(entryController.hasWritePermission(getEntry()));
            topLinkContainer.add(new BookmarkablePageLink<WebPage>("updateLink",
                    EntryUpdatePage.class, new PageParameters("0=" + getEntry().getId())));
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        add(topLinkContainer);
    }

    protected void renderPartNumber() {
        add(new Label("partNumber", getEntry().getOnePartNumber().getPartNumber()));
    }

    protected void renderSelectionMarkers() {
        add(new Label("selectionMarkers", getEntry().getSelectionMarkersAsString()));
    }

    protected void renderNames() {
        add(new Label("names", getEntry().getNamesAsString()));
    }

    protected void renderAlias() {
        add(new Label("alias", getEntry().getAlias()));
    }

    protected void renderCreator() {
        BookmarkablePageLink<ProfilePage> creatorProfileLink = new BookmarkablePageLink<ProfilePage>(
                "creatorProfileLink", ProfilePage.class, new PageParameters("0=about,1="
                        + getEntry().getCreatorEmail()));

        Account creatorAccount;
        try {
            creatorAccount = AccountController.getByEmail(getEntry().getCreatorEmail());
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        if (creatorAccount == null) {
            creatorProfileLink.add(new Label("creator", getEntry().getCreator() == null ? ""
                    : getEntry().getCreator()));

            creatorProfileLink.setEnabled(false);
        } else {
            creatorProfileLink.add(new Label("creator", creatorAccount.getFullName()));
        }
        add(creatorProfileLink);
    }

    protected void renderOwner() {
        BookmarkablePageLink<ProfilePage> ownerProfileLink = new BookmarkablePageLink<ProfilePage>(
                "ownerProfileLink", ProfilePage.class, new PageParameters("0=about,1="
                        + getEntry().getOwnerEmail()));

        Account ownerAccount;
        try {
            ownerAccount = AccountController.getByEmail(getEntry().getOwnerEmail());
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        if (ownerAccount == null) {
            ownerProfileLink.add(new Label("owner", getEntry().getOwner() == null ? "" : getEntry()
                    .getOwner()));

            ownerProfileLink.setEnabled(false);
        } else {
            ownerProfileLink.add(new Label("owner", ownerAccount.getFullName()));
        }

        add(ownerProfileLink);
    }

    protected void renderStatus() {
        add(new Label("status", org.jbei.ice.lib.utils.JbeiConstants.getStatus(getEntry()
                .getStatus())));
    }

    protected void renderLinks() {
        add(new Label("links", WebUtils.linkifyText(getEntry().getLinksAsString()))
                .setEscapeModelStrings(false));
    }

    protected void renderCreationTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        String creationTime = dateFormat.format(getEntry().getCreationTime());

        add(new Label("creationTime", creationTime));
    }

    protected void renderModificationTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");

        String modificationTime = "";

        Date modificationTimeStamp = getEntry().getModificationTime();
        if (modificationTimeStamp != null) {
            modificationTime = dateFormat.format(getEntry().getModificationTime());
        }

        add(new Label("modificationTime", modificationTime));
    }

    protected void renderKeywords() {
        add(new Label("keywords", getEntry().getKeywords()));
    }

    protected void renderSummary() {
        add(new MultiLineLabel("shortDescription", WebUtils.linkifyText(getEntry()
                .getShortDescription())).setEscapeModelStrings(false));
    }

    protected void renderNotes() {
        add(new MultiLineLabel("longDescription", WebUtils.linkifyText(getEntry()
                .getLongDescription())).setEscapeModelStrings(false));
    }

    protected void renderReferences() {
        add(new MultiLineLabel("references", WebUtils.linkifyText(getEntry().getReferences()))
                .setEscapeModelStrings(false));
    }

    protected void renderBioSafetyLevel() {
        String bioSafetyLevel = "";

        if (getEntry().getBioSafetyLevel() != null) {
            bioSafetyLevel = getEntry().getBioSafetyLevel().toString();
        }

        add(new Label("bioSafety", bioSafetyLevel));
    }

    protected void renderIntellectualProperty() {
        String intellectualProperty = "";

        if (getEntry().getIntellectualProperty() != null) {
            intellectualProperty = getEntry().getIntellectualProperty();
        }

        add(new Label("intellectualProperty", WebUtils.linkifyText(intellectualProperty))
                .setEscapeModelStrings(false));
    }

    protected void renderPrincipalInvestigator() {
        add(new Label("principalInvestigator", getEntry().principalInvestigatorToString()));
    }

    protected void renderFundingSource() {
        add(new Label("fundingSource", getEntry().fundingSourceToString()));
    }

    protected void renderParameters() {
        Panel parameterViewPanel = null;
        if (getEntry().getParameters().size() == 0) {
            parameterViewPanel = new EmptyMessagePanel("parameters", "");
        } else {
            parameterViewPanel = new ParameterViewPanel("parameters", getEntry().getParameters());
        }
        add(parameterViewPanel);
    }

    protected void renderBriefSampleView() {
        ArrayList<Sample> samples = null;
        Panel sampleViewPanel = null;
        try {
            samples = SampleManager.getSamplesByEntry(getEntry());
        } catch (ManagerException e) {
            // it's ok. show blank. Log and continue
            Logger.error(e.toString());
        }
        if (samples == null) {
            sampleViewPanel = new EmptyMessagePanel("sampleLocation", "");
        } else if (samples.size() == 0) {
            sampleViewPanel = new EmptyMessagePanel("sampleLocation", "");
        } else {
            sampleViewPanel = new BriefSampleViewPanel("sampleLocation", samples);
        }
        add(sampleViewPanel);
    }

    protected String trimLongField(String value, int maxLength) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        if (value.length() > maxLength) {
            return value.substring(0, maxLength) + "...";
        } else {
            return value;
        }
    }
}
