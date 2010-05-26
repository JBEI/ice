package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.SequenceAnalysisController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.EntryViewPage;

public class EntryTabPanel extends Panel {

    private static final long serialVersionUID = 1L;

    public Entry entry = null;

    public BookmarkablePageLink<Object> generalLink;
    public BookmarkablePageLink<Object> sequenceAnalysisLink;
    public BookmarkablePageLink<Object> samplesLink;

    private final String SAMPLES_URL_KEY = "samples";
    private final String SEQUENCE_ANALYSIS_URL_KEY = "seqanalysis";
    private final String ATTACHMENTS_URL_KEY = "attachments";
    private final String PERMISSIONS_URL_KEY = "permission";

    public String subPage = null;

    public EntryTabPanel(String id, String subPage, Entry entry) {
        super(id);
        this.entry = entry;
        this.subPage = subPage;
        renderGeneralLink();
        renderSamplesLink();
        renderSequenceAnalysisLink();

        add(generalLink);
        add(sequenceAnalysisLink);
        add(samplesLink);
        setActiveLink();
    }

    private void setActiveLink() {
        SimpleAttributeModifier inactiveSimpleAttributeModifier = new SimpleAttributeModifier(
                "class", "inactive");
        SimpleAttributeModifier activeSimpleAttributeModifier = new SimpleAttributeModifier(
                "class", "active");
        generalLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        sequenceAnalysisLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        samplesLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        if (subPage == null) {
            generalLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(SAMPLES_URL_KEY)) {
            samplesLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(ATTACHMENTS_URL_KEY)) {
        } else if (subPage.equals(SEQUENCE_ANALYSIS_URL_KEY)) {
            sequenceAnalysisLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(PERMISSIONS_URL_KEY)) {
        }
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
        SampleController sampleController = new SampleController(IceSession.get().getAccount());
        int numSamples = 0;
        try {
            numSamples = sampleController.getNumberOfSamples(entry);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }
        String samplesLabel = "Samples";
        if (numSamples > 0) {
            samplesLabel = samplesLabel + " (" + numSamples + ")";
        }
        samplesLink.add(new Label("samplesLabel", samplesLabel));
    }

    private void renderSequenceAnalysisLink() {
        sequenceAnalysisLink = new BookmarkablePageLink<Object>("sequenceAnalysisLink",
                EntryViewPage.class, new PageParameters("0=" + entry.getId() + ",1="
                        + SEQUENCE_ANALYSIS_URL_KEY));
        sequenceAnalysisLink.setOutputMarkupId(true);
        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                IceSession.get().getAccount());
        String sequenceAnalysisLabel = "Seq. Analysis";
        int numTraceSequences;
        try {
            numTraceSequences = sequenceAnalysisController.getNumberOfTraceSequences(entry);
            if (numTraceSequences > 0) {
                sequenceAnalysisLabel = sequenceAnalysisLabel + " (" + numTraceSequences + ")";
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }
        sequenceAnalysisLink.add(new Label("sequenceAnalysisLabel", sequenceAnalysisLabel));
    }

}
