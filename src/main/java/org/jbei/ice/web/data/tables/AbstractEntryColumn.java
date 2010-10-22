package org.jbei.ice.web.data.tables;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.PropertyResolver;
import org.jbei.ice.lib.models.Entry;

public abstract class AbstractEntryColumn implements IStyledColumn<Entry> {

    private static final long serialVersionUID = 1L;
    private final String propertyExpression;
    private final String sortProperty;

    public AbstractEntryColumn(String propertyExpression) {
        this(propertyExpression, null);
    }

    public AbstractEntryColumn(String propertyExpression, String sortProperty) {
        this.propertyExpression = propertyExpression;
        this.sortProperty = sortProperty;
    }

    @Override
    public String getSortProperty() {
        return this.sortProperty;
    }

    @Override
    public boolean isSortable() {
        return (this.sortProperty != null);
    }

    @Override
    public void populateItem(Item<ICellPopulator<Entry>> item, String componentId,
            IModel<Entry> rowModel) {

        if (propertyExpression != null && !propertyExpression.isEmpty()) {
            try {
                Object object = PropertyResolver.getValue(propertyExpression, rowModel.getObject());
                if (object == null)
                    item.add(new Label(componentId, ""));
                else {
                    item.add(new Label(componentId, object.toString()));
                }
            } catch (Exception ex) {
                item.add(new Label(componentId, ""));
            }
            return;
        }

        // custom component created by subclasses
        Component component = evaluate(componentId, rowModel.getObject());
        item.add(component);
    }

    protected Component evaluate(String componentId, Entry entry) {
        return new Label(componentId, "");
    }

    @Override
    public void detach() {
    }

    @Override
    public String getCssClass() {
        return null;
    }

}
