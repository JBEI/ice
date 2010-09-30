package org.jbei.ice.web.forms;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.CommaSeparatedField;
import org.jbei.ice.web.common.ViewException;

public class PlasmidUpdateFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public PlasmidUpdateFormPanel(String id, Plasmid plasmid) {
        super(id);

        PlasmidForm form = new PlasmidForm("plasmidForm", plasmid);
        AjaxButton deleteButton = new AjaxButton("deletePlasmidButton", form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                @SuppressWarnings("rawtypes")
                Entry entry = ((EntryUpdateForm) getParent()).getEntry();
                EntryController entryController = new EntryController(IceSession.get().getAccount());
                try {
                    entryController.delete(entry);
                } catch (ControllerException e) {
                    throw new ViewException(e);
                } catch (PermissionException e) {
                    throw new ViewException(e);
                }
            }
        };

        deleteButton.add(new JavascriptEventConfirmation("onclick", "Delete this Entry?"));
        form.add(deleteButton);
        form.add(new Button("submitButton"));
        add(form);

        add(new FeedbackPanel("feedback"));

    }

    class PlasmidForm extends EntryUpdateForm<Plasmid> {
        private static final long serialVersionUID = 1L;

        //plasmid only fields
        private String selectionMarkers;
        private String backbone;
        private String originOfReplication;
        private String promoters;
        private boolean circular;

        public PlasmidForm(String id, Plasmid plasmid) {
            super(id, plasmid);
        }

        @Override
        protected void initializeElements() {
            super.initializeElements();

            add(new TextField<String>("selectionMarkers", new PropertyModel<String>(this,
                    "selectionMarkers")));
            add(new TextField<String>("backbone", new PropertyModel<String>(this, "backbone")));
            add(new TextField<String>("originOfReplication", new PropertyModel<String>(this,
                    "originOfReplication")));
            add(new TextField<String>("promoters", new PropertyModel<String>(this, "promoters")));
            add(new CheckBox("circular", new PropertyModel<Boolean>(this, "circular")));
        }

        @Override
        protected void populateFormElements() {
            super.populateFormElements();

            Plasmid plasmid = getEntry();

            setSelectionMarkers(plasmid.getSelectionMarkersAsString());
            setBackbone(plasmid.getBackbone());
            setOriginOfReplication(plasmid.getOriginOfReplication());
            setPromoters(plasmid.getPromoters());
            setCircular(plasmid.getCircular());
        }

        @Override
        protected void populateEntry() {
            super.populateEntry();

            Plasmid plasmid = getEntry();

            CommaSeparatedField<SelectionMarker> selectionMarkersField = new CommaSeparatedField<SelectionMarker>(
                    SelectionMarker.class, "getName", "setName");
            selectionMarkersField.setString(getSelectionMarkers());
            plasmid.setSelectionMarkers(selectionMarkersField.getItemsAsSet());

            plasmid.setBackbone(getBackbone());
            plasmid.setOriginOfReplication(getOriginOfReplication());
            plasmid.setPromoters(getPromoters());
            plasmid.setCircular(getCircular());
        }

        // Getters and setters for PlasmidForm
        public void setSelectionMarkers(String selectionMarkers) {
            this.selectionMarkers = selectionMarkers;
        }

        public String getSelectionMarkers() {
            return selectionMarkers;
        }

        public String getBackbone() {
            return backbone;
        }

        public void setBackbone(String backbone) {
            this.backbone = backbone;
        }

        public String getOriginOfReplication() {
            return originOfReplication;
        }

        public void setOriginOfReplication(String originOfReplication) {
            this.originOfReplication = originOfReplication;
        }

        public String getPromoters() {
            return promoters;
        }

        public void setPromoters(String promoters) {
            this.promoters = promoters;
        }

        public boolean getCircular() {
            return circular;
        }

        public void setCircular(boolean circular) {
            this.circular = circular;
        }
    }
}
