package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.utils.JbeiConstants;

public class PartSimpleViewPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private static final int MAX_LONG_FIELD_LENGTH = 200;

    public PartSimpleViewPanel(String id, Part entry, boolean trimLongText) {
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

        elements.add(new MultiLineLabel("shortDescription", trimLongText ? trimLongField(entry
                .getShortDescription(), MAX_LONG_FIELD_LENGTH) : entry.getShortDescription()));
        elements.add(new MultiLineLabel("references", trimLongText ? trimLongField(entry
                .getReferences(), MAX_LONG_FIELD_LENGTH) : entry.getReferences()));
        elements.add(new MultiLineLabel("longDescription", trimLongText ? trimLongField(entry
                .getLongDescription(), MAX_LONG_FIELD_LENGTH) : entry.getLongDescription()));

        ResourceReference hasAttachmentImage = new ResourceReference(UserEntriesViewPanel.class,
                "attachment.gif");
        ResourceReference hasSequenceImage = new ResourceReference(UserEntriesViewPanel.class,
                "sequence.gif");
        ResourceReference hasSampleImage = new ResourceReference(UserEntriesViewPanel.class,
                "sample.png");
        elements.add(new Image("hasAttachment", hasAttachmentImage).setVisible(AttachmentManager
                .hasAttachment(entry)));
        elements.add(new Image("hasSequence", hasSequenceImage).setVisible(SequenceManager
                .hasSequence(entry)));
        elements.add(new Image("hasSample", hasSampleImage).setVisible(SampleManager
                .hasSample(entry)));

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

    private String trimLongField(String value, int maxLength) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        if (value.length() > maxLength) {
            return value.substring(0, maxLength);
        } else {
            return value;
        }
    }
}
