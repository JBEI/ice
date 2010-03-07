package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.managers.TraceSequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.panels.AttachmentsViewPanel;
import org.jbei.ice.web.panels.PartViewPanel;
import org.jbei.ice.web.panels.PermissionEditPanel;
import org.jbei.ice.web.panels.PlasmidViewPanel;
import org.jbei.ice.web.panels.SampleViewPanel;
import org.jbei.ice.web.panels.SequenceAnalysisViewPanel;
import org.jbei.ice.web.panels.SequenceViewPanel;
import org.jbei.ice.web.panels.StrainViewPanel;

public class EntryViewPage extends ProtectedPage {
    public Entry entry = null;

    public Component displayPanel;
    public Component generalPanel;
    public Component samplesPanel;
    public Component attachmentsPanel;
    public Component sequencePanel;
    public Component permissionPanel;

    public BookmarkablePageLink<Object> generalLink;
    public BookmarkablePageLink<Object> sequenceLink;
    public BookmarkablePageLink<Object> sequenceAnalysisLink;
    public BookmarkablePageLink<Object> samplesLink;
    public BookmarkablePageLink<Object> attachmentsLink;
    public BookmarkablePageLink<Object> permissionLink;

    public String subPage = null;
    public String entryTitle = "";

    private final String SAMPLES_URL_KEY = "samples";
    private final String SEQUENCE_URL_KEY = "sequence";
    private final String SEQUENCE_ANALYSIS_URL_KEY = "seqanalysis";
    private final String ATTACHMENTS_URL_KEY = "attachments";
    private final String PERMISSIONS_URL_KEY = "permission";

    public EntryViewPage(PageParameters parameters) {
        super(parameters);

        processPageParameters(parameters);

        subPage = parameters.getString("1");

        String recordType = JbeiConstants.getRecordType(entry.getRecordType());
        entryTitle = recordType + ": " + entry.getNamesAsString();
        add(new Label("titleName", entryTitle));

        renderGeneralLink();
        renderSamplesLink();
        renderAttachmentsLink();
        renderSequenceLink();
        renderSequenceAnalysisLink();
        renderPermissionsLink();

        setActiveLink();

        add(generalLink);
        add(sequenceLink);
        add(sequenceAnalysisLink);
        add(samplesLink);
        add(attachmentsLink);
        add(permissionLink);

        // TODO: REMOVE IT LATER
        sequenceAnalysisLink.setVisible(false);

        if (!PermissionManager.hasWritePermission(entry.getId())) {
            permissionLink.setVisible(false);
        }

        generalPanel = makeSubPagePanel(entry);
        displayPanel = generalPanel;
        add(displayPanel);
    }

    @Override
    protected String getTitle() {
        return entryTitle + " - " + super.getTitle();
    }

    private void processPageParameters(PageParameters parameters) {
        int entryId = 0;

        String identifier = parameters.getString("0");

        try {
            entryId = Integer.parseInt(identifier);
            entry = AuthenticatedEntryManager.get(entryId);
        } catch (NumberFormatException e) {
            // Not a number. Perhaps it's a part number or recordId?
            try {
                entry = AuthenticatedEntryManager.getByPartNumber(identifier);
                entryId = entry.getId();
            } catch (PermissionException e1) {
                // entryId is still 0
            } catch (ManagerException e1) {
                Logger.error(e.toString(), e);
                throw new RuntimeException(e);
            }
            if (entryId == 0) {
                try {
                    entry = AuthenticatedEntryManager.getByRecordId(identifier);
                    entryId = entry.getId();
                } catch (PermissionException e1) {
                    // entryId is still 0
                } catch (ManagerException e1) {
                    Logger.error(e.toString(), e);
                    throw new RuntimeException(e);
                }
            }
        } catch (PermissionException e) {
            entryId = 0;
        } catch (ManagerException e) {
            Logger.error(e.toString(), e);
            throw new RuntimeException(e);
        }

        if (entryId == 0) {
            throw new RestartResponseAtInterceptPageException(PermissionDeniedPage.class);
        }
    }

    private void setActiveLink() {
        SimpleAttributeModifier inactiveSimpleAttributeModifier = new SimpleAttributeModifier(
                "class", "inactive");
        SimpleAttributeModifier activeSimpleAttributeModifier = new SimpleAttributeModifier(
                "class", "active");

        generalLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        sequenceLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        sequenceAnalysisLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        samplesLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        attachmentsLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        permissionLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);

        if (subPage == null) {
            generalLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(SAMPLES_URL_KEY)) {
            samplesLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(ATTACHMENTS_URL_KEY)) {
            attachmentsLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(SEQUENCE_URL_KEY)) {
            sequenceLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(SEQUENCE_ANALYSIS_URL_KEY)) {
            sequenceAnalysisLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(PERMISSIONS_URL_KEY)) {
            permissionLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        }
    }

    private Panel makeSubPagePanel(Entry entry) {
        if (subPage == null) {
            return makeGeneralPanel(entry);
        } else if (subPage.equals(SEQUENCE_URL_KEY)) {
            return makeSequencePanel(entry);
        } else if (subPage.equals(SEQUENCE_ANALYSIS_URL_KEY)) {
            return makeSequenceAnalysisPanel(entry);
        } else if (subPage.equals(SAMPLES_URL_KEY)) {
            return makeSamplesPanel(entry);
        } else if (subPage.equals(ATTACHMENTS_URL_KEY)) {
            return makeAttachmentsPanel(entry);
        } else if (subPage.equals(PERMISSIONS_URL_KEY)) {
            return makePermissionPanel(entry);
        } else {
            return makeGeneralPanel(entry);
        }
    }

    private Panel makeGeneralPanel(Entry entry) {
        Panel panel = null;

        if (entry instanceof Strain) {
            panel = new StrainViewPanel("centerPanel", (Strain) entry);
        } else if (entry instanceof Plasmid) {
            panel = new PlasmidViewPanel("centerPanel", (Plasmid) entry);
        } else if (entry instanceof Part) {
            panel = new PartViewPanel("centerPanel", (Part) entry);
        }

        if (panel != null) {
            panel.setOutputMarkupId(true);
        }

        return panel;
    }

    private Panel makeSamplesPanel(Entry entry) {
        Panel panel = new SampleViewPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel makeAttachmentsPanel(Entry entry) {
        Panel panel = new AttachmentsViewPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel makeSequencePanel(Entry entry) {
        Panel panel = new SequenceViewPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel makeSequenceAnalysisPanel(Entry entry) {
        Panel panel = new SequenceAnalysisViewPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel makePermissionPanel(Entry entry) {
        Panel panel = new PermissionEditPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    private void renderGeneralLink() {
        generalLink = new BookmarkablePageLink<Object>("generalLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId()));

        generalLink.setOutputMarkupId(true);
    }

    private void renderSamplesLink() {
        samplesLink = new BookmarkablePageLink<Object>("samplesLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + SAMPLES_URL_KEY));

        samplesLink.setOutputMarkupId(true);

        int numSamples = SampleManager.getNumberOfSamples(entry);
        String samplesLabel = "Samples";
        if (numSamples > 0) {
            samplesLabel = samplesLabel + " (" + numSamples + ")";
        }

        samplesLink.add(new Label("samplesLabel", samplesLabel));
    }

    private void renderSequenceLink() {
        sequenceLink = new BookmarkablePageLink<Object>("sequenceLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + SEQUENCE_URL_KEY));

        sequenceLink.setOutputMarkupId(true);

        String sequenceLabel = "Sequence";
        if (SequenceManager.hasSequence(entry)) {
            sequenceLabel = sequenceLabel + " (1)";
        }

        sequenceLink.add(new Label("sequenceLabel", sequenceLabel));
    }

    private void renderSequenceAnalysisLink() {
        sequenceAnalysisLink = new BookmarkablePageLink<Object>("sequenceAnalysisLink",
                EntryViewPage.class, new PageParameters("0=" + entry.getId() + ",1="
                        + SEQUENCE_ANALYSIS_URL_KEY));

        sequenceAnalysisLink.setOutputMarkupId(true);

        String sequenceAnalysisLabel = "Seq. Analysis";
        int numTraceSequences = TraceSequenceManager.getNumberOfTraceSequences(entry);
        if (numTraceSequences > 0) {
            sequenceAnalysisLabel = sequenceAnalysisLabel + " (" + numTraceSequences + ")";
        }

        sequenceAnalysisLink.add(new Label("sequenceAnalysisLabel", sequenceAnalysisLabel));
    }

    private void renderAttachmentsLink() {
        attachmentsLink = new BookmarkablePageLink<Object>("attachmentsLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + ATTACHMENTS_URL_KEY));

        attachmentsLink.setOutputMarkupId(true);

        int numAttachments = AttachmentManager.getNumberOfAttachments(entry);
        String attachmentsLabel = "Attachments";
        if (numAttachments > 0) {
            attachmentsLabel = attachmentsLabel + " (" + numAttachments + ")";
        }

        attachmentsLink.add(new Label("attachmentsLabel", attachmentsLabel));
    }

    private void renderPermissionsLink() {
        permissionLink = new BookmarkablePageLink<Object>("permissionLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + PERMISSIONS_URL_KEY));

        permissionLink.setOutputMarkupId(true);
    }
}
