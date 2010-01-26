package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.EntryUpdatePage;

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
        elements.add(new Label("creator", entry.getCreator()));
        elements.add(new Label("status", org.jbei.ice.lib.utils.JbeiConstants.getStatus(entry
                .getStatus())));

        elements.add(new Label("linkToOwner", entry.getOwner()));
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
        elements.add(new Label("shortDescription", entry.getShortDescription()));

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
        int numSamples = SampleManager.getNumberOfSamples(entry);
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

        elements.add(new Label("references", entry.getReferences()));
        elements.add(new Label("longDescription", entry.getLongDescription()));
        BookmarkablePageLink updateLink = new BookmarkablePageLink("updateLink",
                EntryUpdatePage.class, new PageParameters("0=" + entry.getId()));
        updateLink.setVisible(PermissionManager.hasWritePermission(entry.getId(), IceSession.get()
                .getSessionKey()));
        elements.add(updateLink);

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
            principalInvestigator = entryFundingSource.getFundingSource().getFundingSource();
            fundingSource = entryFundingSource.getFundingSource().getPrincipalInvestigator();
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
