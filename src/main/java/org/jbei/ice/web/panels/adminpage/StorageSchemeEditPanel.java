package org.jbei.ice.web.panels.adminpage;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.models.Storage.StorageType;
import org.jbei.ice.lib.models.StorageScheme;

public class StorageSchemeEditPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private LinkedList<StorageSchemeEditItemPanel> locationItems = new LinkedList<StorageSchemeEditItemPanel>();
    private StorageScheme scheme = null;
    private String schemeName = null;
    private Panel parentPanel = null;

    public StorageSchemeEditPanel(String id, StorageSchemeChoicePanel parentPanel) {
        super(id);
        locationItems.add(new StorageSchemeEditItemPanel("editStorageSchemeItemPanel"));
        setParentPanel(parentPanel);
        initializeComponents();
    }

    public StorageSchemeEditPanel(String id, StorageScheme storageScheme,
            StorageSchemeChoicePanel parentPanel) {
        super(id);
        scheme = storageScheme;
        schemeName = storageScheme.getLabel();
        setParentPanel(parentPanel);

        LinkedHashMap<String, StorageType> schemes = storageScheme.getSchemes();
        if (schemes != null) {

            for (String key : schemes.keySet()) {
                StorageSchemeEditItemPanel newSchemeItemPanel = new StorageSchemeEditItemPanel(
                        "editStorageSchemeItemPanel", key, storageScheme.getSchemes().get(key));
                locationItems.add(newSchemeItemPanel);
            }
        } else {
            locationItems.add(new StorageSchemeEditItemPanel("editStorageSchemeItemPanel"));

        }
        initializeComponents();
    }

    private void initializeComponents() {
        Form<Object> editStorageSchemeForm = new Form<Object>("editStorageSchemeForm");

        ListView<StorageSchemeEditItemPanel> storageSchemeListView = new ListView<StorageSchemeEditItemPanel>(
                "storageSchemeListView", new PropertyModel<List<StorageSchemeEditItemPanel>>(this,
                        "locationItems")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<StorageSchemeEditItemPanel> item) {
                item.add(item.getModelObject());
            }

        };
        storageSchemeListView.setOutputMarkupId(true);

        editStorageSchemeForm.add(new TextField<String>("schemeName", new PropertyModel<String>(
                this, "schemeName")).setRequired(true).setLabel(new Model<String>("Scheme Name")));
        editStorageSchemeForm.add(storageSchemeListView);
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        editStorageSchemeForm.add(feedbackPanel);

        editStorageSchemeForm.add(new AjaxButton("evaluateButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                StorageSchemeEditPanel thisPanel = (StorageSchemeEditPanel) getParent().getParent();
                LinkedHashMap<String, StorageType> schemes = new LinkedHashMap<String, StorageType>();
                for (StorageSchemeEditItemPanel item : thisPanel.getLocationItems()) {

                    StorageType itemLocationType = StorageType.valueOf(item.getLocationType()
                            .getValue());
                    schemes.put(item.getLocationName(), itemLocationType);
                }
                StorageSchemeChoicePanel originalPanel = (StorageSchemeChoicePanel) thisPanel
                        .getParentPanel();
                StorageScheme scheme = thisPanel.getScheme();
                scheme.setLabel(getSchemeName());
                scheme.setSchemes(schemes);
                originalPanel.saveScheme(scheme);

                target.addComponent(originalPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                // rerender form to update feedback panel
                target.addComponent(form);
            }

        });
        add(editStorageSchemeForm);
    }

    public void setParentPanel(Panel parentPanel) {
        this.parentPanel = parentPanel;
    }

    public Panel getParentPanel() {
        return parentPanel;
    }

    public void setLocationItems(LinkedList<StorageSchemeEditItemPanel> locationItems) {
        this.locationItems = locationItems;
    }

    public LinkedList<StorageSchemeEditItemPanel> getLocationItems() {
        return locationItems;
    }

    public void setScheme(StorageScheme scheme) {
        this.scheme = scheme;
    }

    public StorageScheme getScheme() {
        return scheme;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getSchemeName() {
        return schemeName;
    }

}
