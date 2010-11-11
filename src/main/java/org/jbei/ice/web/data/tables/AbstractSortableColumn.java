package org.jbei.ice.web.data.tables;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.PropertyResolver;

public abstract class AbstractSortableColumn<T> implements IStyledColumn<T> {

    private static final long serialVersionUID = 1L;
    private final String sortProperty;
    private final String propertyExpression;

    public AbstractSortableColumn(String sortProperty, String propertyExpression) {
        this.sortProperty = sortProperty;
        this.propertyExpression = propertyExpression;
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {

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

        // custom component created by sub-classes
        @SuppressWarnings("rawtypes")
        Component component = evaluate(componentId, rowModel.getObject(), ((Item)item.getParent().getParent()).getIndex());
        item.add(component);
    }

    /**
     * override if a custom component is to be used instead of a
     * property expression
     * 
     * @param componentId
     * @param object
     * @param index
     *            the index assigned to item
     * @return component for rendering in the cell
     */
    protected Component evaluate(String componentId, T object, int index) {
        return new Label(componentId, "");
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
    public String getCssClass() {
        return null;
    }
}
