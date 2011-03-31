package org.jbei.ice.web.panels;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Parameter;

public class ParameterViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public ParameterViewPanel(String id) {
        super(id);

    }

    public ParameterViewPanel(String id, List<Parameter> parameters) {
        super(id);

        if (parameters == null) {
            return;
        } else if (parameters.size() == 0) {
            return;
        }
        ListView<Parameter> listView = new ListView<Parameter>("parameterList", parameters) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Parameter> item) {
                Parameter parameter = item.getModelObject();
                String valueString = null;
                if (parameter.getParameterType() == Parameter.ParameterType.TEXT) {
                    valueString = "\"" + parameter.getValue() + "\"";
                } else {
                    valueString = parameter.getValue();
                }
                item.add(new Label("key", parameter.getKey()));
                item.add(new Label("value", valueString));
                item.add(new Label("separator", ";"));
            }
        };
        add(listView);
    }
}
