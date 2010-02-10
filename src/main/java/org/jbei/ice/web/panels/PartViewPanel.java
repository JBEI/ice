package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.permissions.AuthenticatedSampleManager;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.EntryUpdatePage;
import org.jbei.ice.web.pages.ProfilePage;

public class PartViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public PartViewPanel(String id, Part entry) {
        super(id);

        ArrayList<Component> elements = new ArrayList<Component>();

        elements.add(new Label("partNumber", entry.getOnePartNumber().getPartNumber()));
        elements.add(new Label("names", entry.getNamesAsString()));
        elements.add(new Label("alias", entry.getAlias()));
        elements.add(new Label("packageFormat", JbeiConstants.getPackageFormat(entry
                .getPackageFormat())));

        if (entry.getCreatorEmail() == null || entry.getCreatorEmail().isEmpty()) {
            BookmarkablePageLink<ProfilePage> creatorProfileLink = new BookmarkablePageLink<ProfilePage>(
                    "creatorProfileLink", ProfilePage.class, new PageParameters("0=about,1="
                            + entry.getCreatorEmail()));
            creatorProfileLink.add(new Label("creatorLinked", ""));
            elements.add(creatorProfileLink.setVisible(false));

            elements.add(new Label("creator", entry.getCreator()));
        } else {
            Account creatorAccount = null;
            try {
                creatorAccount = AccountManager.getByEmail(entry.getCreatorEmail());
            } catch (ManagerException e) {
                e.printStackTrace();
            }

            BookmarkablePageLink<ProfilePage> creatorProfileLink = new BookmarkablePageLink<ProfilePage>(
                    "creatorProfileLink", ProfilePage.class, new PageParameters("0=about,1="
                            + entry.getCreatorEmail()));
            String creatorAltText = "Profile "
                    + ((creatorAccount == null) ? entry.getCreator() : creatorAccount.getFullName());
            creatorProfileLink.add(new SimpleAttributeModifier("title", creatorAltText));
            creatorProfileLink.add(new SimpleAttributeModifier("alt", creatorAltText));
            creatorProfileLink.add(new Label("creatorLinked", ((creatorAccount == null) ? entry
                    .getCreator() : creatorAccount.getFullName())));
            elements.add(creatorProfileLink);

            elements.add(new Label("creator", "").setVisible(false));
        }

        elements.add(new Label("status", org.jbei.ice.lib.utils.JbeiConstants.getStatus(entry
                .getStatus())));

        if (entry.getOwnerEmail() == null || entry.getOwnerEmail().isEmpty()) {
            BookmarkablePageLink<ProfilePage> ownerProfileLink = new BookmarkablePageLink<ProfilePage>(
                    "ownerProfileLink", ProfilePage.class, new PageParameters("0=about,1="
                            + entry.getOwnerEmail()));

            ownerProfileLink.add(new Label("ownerLinked", ""));
            elements.add(ownerProfileLink.setVisible(false));

            elements.add(new Label("owner", entry.getOwner()));
        } else {
            Account ownerAccount = null;
            try {
                ownerAccount = AccountManager.getByEmail(entry.getOwnerEmail());
            } catch (ManagerException e) {
                e.printStackTrace();
            }

            BookmarkablePageLink<ProfilePage> ownerProfileLink = new BookmarkablePageLink<ProfilePage>(
                    "ownerProfileLink", ProfilePage.class, new PageParameters("0=about,1="
                            + entry.getOwnerEmail()));
            String ownerAltText = "Profile "
                    + ((ownerAccount == null) ? entry.getOwner() : ownerAccount.getFullName());
            ownerProfileLink.add(new SimpleAttributeModifier("title", ownerAltText));
            ownerProfileLink.add(new SimpleAttributeModifier("alt", ownerAltText));
            ownerProfileLink.add(new Label("ownerLinked", ((ownerAccount == null) ? entry
                    .getOwner() : ownerAccount.getFullName())));
            elements.add(ownerProfileLink);

            elements.add(new Label("owner", "").setVisible(false));
        }

        elements.add(new Label("links", entry.getLinksAsString()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        String creationTime = dateFormat.format(entry.getCreationTime());
        elements.add(new Label("creationTime", creationTime));

        String modificationTime = "";
        Date modificationTimeStamp = entry.getModificationTime();
        if (modificationTimeStamp != null) {
            modificationTime = dateFormat.format(entry.getModificationTime());
        }
        elements.add(new Label("modificationTime", modificationTime));

        elements.add(new Label("keywords", entry.getKeywords()));
        elements.add(new MultiLineLabel("shortDescription", entry.getShortDescription()));

        int numAttachments = AttachmentManager.getNumberOfAttachments(entry);
        String attachmentText = "";
        if (numAttachments == 0) {
            attachmentText = "No attachments provided";
        } else if (numAttachments == 1) {
            attachmentText = "One attachment provided";
        } else {
            attachmentText = String.valueOf(numAttachments) + " attachments provided";
        }
        elements.add(new Label("attachments", attachmentText));
        int numSamples = AuthenticatedSampleManager.getNumberOfSamples(entry);
        String samplesText = "";
        if (numSamples == 0) {
            samplesText = "No samples provided";
        } else if (numSamples == 1) {
            samplesText = "One sample provided";
        } else {
            samplesText = String.valueOf(numSamples) + " samples provided";
        }
        elements.add(new Label("samples", samplesText));
        String sequenceText = (SequenceManager.hasSequence(entry)) ? "Sequence Provided"
                : "No sequence provided";
        elements.add(new Label("sequence", sequenceText));

        elements.add(new MultiLineLabel("references", entry.getReferences()));
        elements.add(new MultiLineLabel("longDescription", entry.getLongDescription()));

        WebMarkupContainer topLinkContainer = new WebMarkupContainer("topLink");
        topLinkContainer.setVisible(PermissionManager.hasWritePermission(entry.getId(), IceSession
                .get().getSessionKey()));
        topLinkContainer.add(new BookmarkablePageLink("updateLink", EntryUpdatePage.class,
                new PageParameters("0=" + entry.getId())));
        elements.add(topLinkContainer);

        String bioSafetyLevel = "";
        if (entry.getBioSafetyLevel() != null) {
            bioSafetyLevel = entry.getBioSafetyLevel().toString();
        }
        elements.add(new Label("bioSafety", bioSafetyLevel));
        String intellectualProperty = "";
        if (entry.getIntellectualProperty() != null) {
            intellectualProperty = entry.getIntellectualProperty();
        }
        elements.add(new Label("intellectualProperty", intellectualProperty));
        Set<EntryFundingSource> entryFundingSources = entry.getEntryFundingSources();
        String principalInvestigator = null;
        String fundingSource = null;
        // TODO: handle multiple funding sources
        for (EntryFundingSource entryFundingSource : entryFundingSources) {
            principalInvestigator = entryFundingSource.getFundingSource()
                    .getPrincipalInvestigator();
            fundingSource = entryFundingSource.getFundingSource().getFundingSource();
        }
        if (principalInvestigator == null) {
            principalInvestigator = "";
        }
        if (fundingSource == null) {
            fundingSource = "";
        }
        elements.add(new Label("principalInvestigator", principalInvestigator));
        elements.add(new Label("fundingSource", fundingSource));

        for (Component item : elements) {
            add(item);
        }

    }
}
