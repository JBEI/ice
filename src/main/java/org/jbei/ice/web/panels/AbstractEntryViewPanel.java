package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.permissions.AuthenticatedSampleManager;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.EntryUpdatePage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.ProfilePage;
import org.jbei.ice.web.utils.WebUtils;

public class AbstractEntryViewPanel<T extends Entry> extends Panel {
    private static final long serialVersionUID = 1L;

    private Model<T> entryModel;

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
        renderAttachments();
        renderSamples();
        renderSequence();
        renderNotes();
        renderReferences();
        renderBioSafetyLevel();
        renderIntellectualProperty();
        renderFundingSource();
        renderPrincipalInvestigator();
    }

    protected T getEntry() {
        return entryModel.getObject();
    }

    protected void renderTopLink() {
        WebMarkupContainer topLinkContainer = new WebMarkupContainer("topLink");

        topLinkContainer.setVisible(PermissionManager.hasWritePermission(getEntry().getId(),
                IceSession.get().getSessionKey()));
        topLinkContainer.add(new BookmarkablePageLink<WebPage>("updateLink", EntryUpdatePage.class,
                new PageParameters("0=" + getEntry().getId())));

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
            creatorAccount = AccountManager.getByEmail(getEntry().getCreatorEmail());
        } catch (ManagerException e) {
            creatorAccount = null;
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
            ownerAccount = AccountManager.getByEmail(getEntry().getOwnerEmail());
        } catch (ManagerException e) {
            ownerAccount = null;
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
        add(new Label("links", WebUtils.jbeiLinkifyText(getEntry().getLinksAsString()))
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
        add(new MultiLineLabel("shortDescription", WebUtils.jbeiLinkifyText(getEntry()
                .getShortDescription())).setEscapeModelStrings(false));
    }

    protected void renderAttachments() {
        int numAttachments = AttachmentManager.getNumberOfAttachments(getEntry());

        String attachmentText = "";
        if (numAttachments == 0) {
            attachmentText = "";
        } else if (numAttachments == 1) {
            attachmentText = "One attachment provided";
        } else {
            attachmentText = String.valueOf(numAttachments) + " attachments provided";
        }

        add(new Label("attachments", attachmentText));
    }

    protected void renderSamples() {
        int numSamples = AuthenticatedSampleManager.getNumberOfSamples(getEntry());

        String samplesText = "";
        if (numSamples == 0) {
            samplesText = "";
        } else if (numSamples == 1) {
            samplesText = "One sample provided";
        } else {
            samplesText = String.valueOf(numSamples) + " samples provided";
        }

        add(new Label("samples", samplesText));
    }

    protected void renderSequence() {
        WebMarkupContainer sequenceLinksContainer = new WebMarkupContainer("sequenceLinksContainer");

        Link<Object> downloadLink = new Link<Object>("downloadLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                Entry entry = entryModel.getObject();

                if (SequenceManager.hasSequence(entry)) {
                    IResourceStream resourceStream = null;
                    try {
                        Sequence sequence = SequenceManager.getByEntry(entry);

                        if (sequence != null) {
                            resourceStream = new StringResourceStream(sequence.getSequenceUser(),
                                    "application/genbank");
                            getRequestCycle().setRequestTarget(
                                    new ResourceStreamRequestTarget(resourceStream, entryModel
                                            .getObject().getPartNumbersAsString()
                                            + ".gb"));
                        }
                    } catch (ManagerException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        BookmarkablePageLink<Object> viewLink = new BookmarkablePageLink<Object>("viewLink",
                EntryViewPage.class, new PageParameters("0=" + entryModel.getObject().getId()
                        + ",1=sequence"));

        sequenceLinksContainer.add(downloadLink);
        sequenceLinksContainer.add(viewLink);

        String sequenceText = "";

        if (!SequenceManager.hasSequence(entryModel.getObject())) {
            sequenceLinksContainer.setVisible(false);
        } else {
            sequenceText = "Sequence provided";
        }

        add(sequenceLinksContainer);
        add(new Label("sequence", sequenceText));
    }

    protected void renderNotes() {
        add(new MultiLineLabel("longDescription", WebUtils.jbeiLinkifyText(getEntry()
                .getLongDescription())).setEscapeModelStrings(false));
    }

    protected void renderReferences() {
        add(new MultiLineLabel("references", WebUtils.jbeiLinkifyText(getEntry().getReferences()))
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

        add(new Label("intellectualProperty", intellectualProperty));
    }

    protected void renderPrincipalInvestigator() {
        add(new Label("principalInvestigator", getEntry().principalInvestigatorToString()));
    }

    protected void renderFundingSource() {
        add(new Label("fundingSource", getEntry().fundingSourceToString()));
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
