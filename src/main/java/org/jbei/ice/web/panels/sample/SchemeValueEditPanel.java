package org.jbei.ice.web.panels.sample;

import java.util.ArrayList;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class SchemeValueEditPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private ArrayList<SchemeValue> schemeValues = null;

    public SchemeValueEditPanel(String id, ArrayList<SchemeValue> schemeValues) {
        super(id);
        this.schemeValues = schemeValues;

        ListView<SchemeValue> listView = new ListView<SchemeValue>("schemeValuesListView",
                new PropertyModel<ArrayList<SchemeValue>>(this, "schemeValues")) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<SchemeValue> item) {
                SchemeValue schemeValue = item.getModelObject();
                item.add(new Label("itemLabel", schemeValue.getName()));
                item.add(new TextField<String>("itemValue", new PropertyModel<String>(schemeValue,
                        "index")));
            }

        };
        listView.setOutputMarkupId(true);
        listView.setOutputMarkupPlaceholderTag(true);
        add(listView);
    }

    public void setSchemeValues(ArrayList<SchemeValue> schemeValues) {
        this.schemeValues = schemeValues;
    }

    public ArrayList<SchemeValue> getSchemeValues() {
        return schemeValues;
    }

}
