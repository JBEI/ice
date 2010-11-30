package org.jbei.ice.web.panels.adminpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;

public class EditPartsPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private Panel contentPanel;
    private Label editPartsHeader;
    private AjaxFallbackLink<Object> headerLink;
    private Label headerLabel;

    public EditPartsPanel(String id) {

        super(id);
        contentPanel = new AdminPartsDataViewPanel("parts_content_panel");
        contentPanel.setOutputMarkupId(true);

        editPartsHeader = new Label("edit_parts_header", "Deleted parts");
        editPartsHeader.setOutputMarkupId(true);

        headerLabel = new Label("edit_link_text", "Edit Part");
        headerLabel.setOutputMarkupId(true);
        headerLink = new EditEntryLink<Object>("edit_link");
        headerLink.add(headerLabel);

        add(editPartsHeader);
        add(contentPanel);
        add(headerLink);

        AjaxButton rebuildBlastButton = new AjaxButton("rebuildBlastButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                JobCue.getInstance().addJob(Job.REBUILD_BLAST_INDEX);
                JobCue.getInstance().processIn(5000L);
            }
        };

        AjaxButton rebuildSearchButton = new AjaxButton("rebuildSearchButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                JobCue.getInstance().addJob(Job.REBUILD_SEARCH_INDEX);
                JobCue.getInstance().processIn(5000L);
            }
        };

        RebuildIndexesForm form = new RebuildIndexesForm("rebuild_indexes_form");
        form.add(rebuildBlastButton);
        form.add(rebuildSearchButton);

        add(form);
    }

    class RebuildIndexesForm extends StatelessForm<Object> {

        private static final long serialVersionUID = 1L;

        public RebuildIndexesForm(String id) {
            super(id);
        }
    }

    class EditEntryLink<T> extends AjaxFallbackLink<T> {

        private static final long serialVersionUID = 1L;

        public EditEntryLink(String id) {
            super(id);
        }

        @Override
        public void onClick(AjaxRequestTarget target) {

            Panel panel = new AdminEditPartsFormPanel("parts_content_panel");
            panel.setOutputMarkupId(true);
            contentPanel.replaceWith(panel);
            contentPanel = panel;

            Label newHeader = new Label("edit_parts_header", "Edit Party Entry");
            newHeader.setOutputMarkupId(true);
            editPartsHeader.replaceWith(newHeader);
            editPartsHeader = newHeader;

            Label newLabel = new Label("edit_link_text", "Show Deleted Parts");
            newLabel.setOutputMarkupId(true);
            headerLabel.replaceWith(newLabel);
            headerLabel = newLabel;

            ShowDeletedPartsLink<Object> link = new ShowDeletedPartsLink<Object>("edit_link");
            link.setOutputMarkupId(true);
            link.add(newLabel);
            headerLink.replaceWith(link);
            headerLink = link;

            target.addComponent(newHeader);
            target.addComponent(link);
            target.addComponent(newLabel);
            target.addComponent(panel);
        }
    }

    class ShowDeletedPartsLink<T> extends AjaxFallbackLink<T> {

        private static final long serialVersionUID = 1L;

        public ShowDeletedPartsLink(String id) {
            super(id);
        }

        @Override
        public void onClick(AjaxRequestTarget target) {

            Panel panel = new AdminPartsDataViewPanel("parts_content_panel");
            panel.setOutputMarkupId(true);
            contentPanel.replaceWith(panel);
            contentPanel = panel;

            Label newHeader = new Label("edit_parts_header", "Deleted Parts");
            newHeader.setOutputMarkupId(true);
            editPartsHeader.replaceWith(newHeader);
            editPartsHeader = newHeader;

            Label newLabel = new Label("edit_link_text", "Edit Part");
            newLabel.setOutputMarkupId(true);
            headerLabel.replaceWith(newLabel);
            headerLabel = newLabel;

            EditEntryLink<Object> link = new EditEntryLink<Object>("edit_link");
            link.setOutputMarkupId(true);
            link.add(newLabel);
            headerLink.replaceWith(link);
            headerLink = link;

            target.addComponent(panel);
            target.addComponent(newHeader);
            target.addComponent(link);
            target.addComponent(newLabel);
        }
    }
}
