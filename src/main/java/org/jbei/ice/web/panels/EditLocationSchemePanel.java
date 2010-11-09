package org.jbei.ice.web.panels;

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

public class EditLocationSchemePanel extends Panel {

    private static final long serialVersionUID = 1L;
    private LinkedList<EditLocationSchemeItemPanel> locationItems = new LinkedList<EditLocationSchemeItemPanel>();

    private String schemeName = null;

    public EditLocationSchemePanel(String id) {
        super(id);
        Form<Object> editLocationSchemeForm = new Form<Object>("editLocationSchemeForm");

        ListView<EditLocationSchemeItemPanel> locationSchemeListView = new ListView<EditLocationSchemeItemPanel>(
                "locationSchemeListView", new PropertyModel<List<EditLocationSchemeItemPanel>>(
                        this, "locationItems")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<EditLocationSchemeItemPanel> item) {
                item.add(item.getModelObject());
            }

        };
        locationSchemeListView.setOutputMarkupId(true);
        locationItems.add(new EditLocationSchemeItemPanel("editLocationSchemeItemPanel"));

        editLocationSchemeForm.add(new TextField<String>("schemeName", new PropertyModel<String>(
                this, "schemeName")).setRequired(true).setLabel(new Model<String>("Scheme Name")));
        editLocationSchemeForm.add(locationSchemeListView);
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        editLocationSchemeForm.add(feedbackPanel);

        editLocationSchemeForm.add(new AjaxButton("evaluateButton") {
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
        add(editLocationSchemeForm);
        add(new FeedbackPanel("feedback2"));
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String name) {
        schemeName = name;
    }

    public void setLocationItems(LinkedList<EditLocationSchemeItemPanel> locationItems) {
        this.locationItems = locationItems;
    }

    public LinkedList<EditLocationSchemeItemPanel> getLocationItems() {
        return locationItems;
    }

}
