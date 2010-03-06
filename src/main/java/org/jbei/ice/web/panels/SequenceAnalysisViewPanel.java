package org.jbei.ice.web.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.forms.TraceFileNewFormPanel;

public class SequenceAnalysisViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Entry entry;
    private EmptyPanel traceSequenceFormPanel;

    public SequenceAnalysisViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        renderAddTraceFileLink();
        renderTraceSequenceFormPanel();
    }

    public void removeForm() {
        addOrReplace(traceSequenceFormPanel);
    }

    private void renderTraceSequenceFormPanel() {
        traceSequenceFormPanel = new EmptyPanel("traceSequenceFormPanel");
        traceSequenceFormPanel.setOutputMarkupId(true);

        add(traceSequenceFormPanel);
    }

    private void renderAddTraceFileLink() {
        add(new AjaxFallbackLink<Object>("addTraceFileLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                TraceFileNewFormPanel addTraceFileNewFormPanel = new TraceFileNewFormPanel(
                        "traceSequenceFormPanel", (SequenceAnalysisViewPanel) getParent(), entry);
                addTraceFileNewFormPanel.setOutputMarkupId(true);

                getParent().addOrReplace(addTraceFileNewFormPanel);
                target.addComponent(addTraceFileNewFormPanel);
            }
        });
    }
}
