package org.jbei.ice.web.panels;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.web.forms.SequenceNewFormPanel;
import org.jbei.ice.web.forms.SequenceUpdateFormPanel;

public class SequenceViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Sequence sequence;
    private Entry entry;

    private AjaxFallbackLink<Object> addSequenceLink;
    private AjaxFallbackLink<Object> editSequenceLink;
    private String sequenceUser;
    private EmptyPanel emptySequenceFormPanel;

    public SequenceViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        try {
            sequence = SequenceManager.getByEntry(entry);
        } catch (ManagerException e) {
            e.printStackTrace();
        }

        initializeControls();

        setOutputMarkupId(true);

        updateStatus(sequence);
    }

    private void initializeControls() {
        addSequenceLink = new AjaxFallbackLink<Object>("actionSequenceLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                SequenceNewFormPanel addNewSequence = new SequenceNewFormPanel("sequenceFormPanel",
                        (SequenceViewPanel) getParent(), entry);
                addNewSequence.setOutputMarkupId(true);

                getParent().addOrReplace(addNewSequence);
                target.addComponent(addNewSequence);
            }
        };
        addSequenceLink.add(new Label("actionLinkLabel", "Add sequence"));

        editSequenceLink = new AjaxFallbackLink<Object>("actionSequenceLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                SequenceUpdateFormPanel updateSequence = new SequenceUpdateFormPanel(
                        "sequenceFormPanel", (SequenceViewPanel) getParent(), entry);
                updateSequence.setOutputMarkupId(true);

                getParent().addOrReplace(updateSequence);
                target.addComponent(updateSequence);
            }
        };
        editSequenceLink.add(new Label("actionLinkLabel", "Edit sequence"));

        emptySequenceFormPanel = new EmptyPanel("sequenceFormPanel");
        emptySequenceFormPanel.setOutputMarkupId(true);
        add(emptySequenceFormPanel);
    }

    private void updateStatus(Sequence sequence) {
        if (sequence == null) {
            addOrReplace(addSequenceLink);
            addOrReplace(createNoSequenceFragment());
        } else {
            this.sequence = sequence;
            addOrReplace(editSequenceLink);
            addOrReplace(createSequenceFragment());
            sequenceUser = sequence.getSequenceUser();
        }
    }

    private Fragment createNoSequenceFragment() {
        Fragment fragment = new Fragment("sequenceDataPanel", "noSequenceFragment", this);

        fragment.setOutputMarkupPlaceholderTag(true);
        fragment.setOutputMarkupId(true);

        return fragment;
    }

    private Fragment createSequenceFragment() {
        Fragment fragment = new Fragment("sequenceDataPanel", "sequenceFragment", this);

        fragment.add(new TextArea<String>("sequenceTextArea", new PropertyModel<String>(this,
                "sequenceUser")));

        if (sequence != null) {
            File sequenceFile;
            try {
                sequenceFile = File.createTempFile(sequence.getEntry().getPartNumbersAsString()
                        + "-", ".gb");

                FileWriter fstream = new FileWriter(sequenceFile);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(sequence.getSequenceUser());
                out.close();

                fragment.add(new DownloadLink("downloadLink", sequenceFile).add(new Label(
                        "sequenceDownloadFileName", sequence.getEntry().getPartNumbersAsString()
                                + ".gb")));

                fragment.setOutputMarkupPlaceholderTag(true);
                fragment.setOutputMarkupId(true);

                fragment.add(new DeleteSequenceLink("deleteLink"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            fragment.add(new DownloadLink("downloadLink", new File("asdf")));
        }

        return fragment;
    }

    public String getSequenceUser() {
        return sequenceUser;
    }

    public void clearForm() {
        addOrReplace(emptySequenceFormPanel);
    }

    public void updateView(Sequence sequence) {
        clearForm();

        updateStatus(sequence);
    }

    class DeleteSequenceLink extends AjaxFallbackLink<Object> {
        private static final long serialVersionUID = 1L;

        public DeleteSequenceLink(String id) {
            super(id);
            this.add(new SimpleAttributeModifier("onclick",
                    "return confirm('Delete this sequence?');"));
        }

        public void onClick(AjaxRequestTarget target) {
            try {
                entry.setSequence(null);

                SequenceManager.delete(sequence);

                updateView(null);
            } catch (ManagerException e) {
                e.printStackTrace();
            }
        }
    }
}
