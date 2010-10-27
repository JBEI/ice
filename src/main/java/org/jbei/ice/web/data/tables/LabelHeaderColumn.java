package org.jbei.ice.web.data.tables;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * AbstractSortableColumn helper implementation for columns that have labels as the headers.
 * This is opposed to columns that have images as the headers.
 * 
 * @author Hector Plahar
 * 
 * @param <T>
 *            Data Type for column
 * 
 */
public class LabelHeaderColumn<T> extends AbstractSortableColumn<T> {
    private static final long serialVersionUID = 1L;
    private final IModel<String> headerModel;

    /**
     * Creates a column that is sortable
     * 
     * @param headerLabel
     *            plain text for display as the label header
     * @param sortProperty
     *            name of property to be used for sort
     * @param propertyExpression
     *            property expression for cell content
     */
    public LabelHeaderColumn(String headerLabel, String sortProperty, String propertyExpression) {
        super(sortProperty, propertyExpression);
        headerModel = new Model<String>(headerLabel);
    }

    /**
     * Creates a column that is sortable but whose contents have to be provided
     * by overriding evaluate(id:String, object:T):Component
     * 
     * @param headerLabel
     *            plain text for use as column header
     * @param sortProperty
     *            name of property to be used for sort
     */
    public LabelHeaderColumn(String headerLabel, String sortProperty) {
        this(headerLabel, sortProperty, null);
    }

    /**
     * Creates a column that is not sortable and whose contents have to be provided
     * by overriding evaluate(id:String, object:T):Component
     * 
     * @param headerLabel
     */
    public LabelHeaderColumn(String headerLabel) {
        this(headerLabel, null, null);
    }

    @Override
    public Component getHeader(String componentId) {
        return new Label(componentId, headerModel);
    }

    @Override
    public void detach() {
        if (headerModel != null)
            headerModel.detach();
    }
}
