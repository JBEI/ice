package org.jbei.ice.web.panels.adminpage;

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
import org.jbei.ice.lib.models.StorageScheme;

public class StorageSchemeEditPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private LinkedList<StorageSchemeEditItemPanel> locationItems = new LinkedList<StorageSchemeEditItemPanel>();

    private String schemeName = null;

    public StorageSchemeEditPanel(String id) {
        super(id);
        locationItems.add(new StorageSchemeEditItemPanel("editStorageSchemeItemPanel"));

        initializeComponents();
    }

    public StorageSchemeEditPanel(String id, StorageScheme storageScheme) {
        super(id);
        schemeName = storageScheme.getLabel();
        for (String key : storageScheme.getSchemes().keySet()) {
            StorageSchemeEditItemPanel newSchemeItemPanel = new StorageSchemeEditItemPanel(
                    "editStorageSchemeItemPanel", key, storageScheme.getSchemes().get(key));
            locationItems.add(newSchemeItemPanel);
        }
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

                System.out.println("do something");

            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                // rerender form to update feedback panel
                target.addComponent(form);
            }

        });
        add(editStorageSchemeForm);
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String name) {
        schemeName = name;
    }

    public void setLocationItems(LinkedList<StorageSchemeEditItemPanel> locationItems) {
        this.locationItems = locationItems;
    }

    public LinkedList<StorageSchemeEditItemPanel> getLocationItems() {
        return locationItems;
    }

}
