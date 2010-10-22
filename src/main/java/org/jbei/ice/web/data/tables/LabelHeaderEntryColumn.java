package org.jbei.ice.web.data.tables;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class LabelHeaderEntryColumn extends AbstractEntryColumn {

    private static final long serialVersionUID = 1L;
    private IModel<String> displayModel;

    public LabelHeaderEntryColumn(String headerLabel, String propertyExpression, String sortProperty) {
        super(propertyExpression, sortProperty);
        this.displayModel = new Model<String>(headerLabel);
    }

    @Override
    public void detach() {
        if (displayModel != null) {
            displayModel.detach();
        }
    }

    public IModel<String> getDisplayModel() {
        if (displayModel == null)
            return new Model<String>("");
        return displayModel;
    }

    @Override
    public Component getHeader(String componentId) {
        return new Label(componentId, getDisplayModel());
    }
}
