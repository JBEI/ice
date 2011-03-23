package org.jbei.ice.web.data.tables;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.model.Model;

public class RegistryTableNavigationToolbar extends AbstractToolbar {

    private static final long serialVersionUID = 1L;

    public RegistryTableNavigationToolbar(DataTable<?> table) {
        super(table);

        WebMarkupContainer span = new WebMarkupContainer("span");
        add(span);
        span.add(new AttributeModifier("colspan", true, new Model<String>(String.valueOf(table
                .getColumns().length))));

        span.add(newPagingNavigator("navigator", table));
        span.add(newNavigatorLabel("navigatorLabel", table));
        span.add(new RegistryTablePagingControl(table));
    }

    /**
     * Factory method used to create the paging navigator that will be used by the datatable.
     * 
     * @param navigatorId
     *            component id the navigator should be created with
     * @param table
     *            dataview used by datatable
     * @return paging navigator that will be used to navigate the data table
     */
    protected PagingNavigator newPagingNavigator(String navigatorId, final DataTable<?> table) {
        return new AjaxPagingNavigator(navigatorId, table) {
            private static final long serialVersionUID = 1L;

            /**
             * Implement our own ajax event handling in order to update the data table itself, as
             * the
             * default implementation doesn't support DataViews.
             * 
             * @see AjaxPagingNavigator#onAjaxEvent(AjaxRequestTarget)
             */
            @Override
            protected void onAjaxEvent(AjaxRequestTarget target) {
                target.addComponent(table);
                target.appendJavascript("try {	assignClueTips();	} catch (err) {	alert(err);	};");
            }
        };
    }

    /**
     * Factory method used to create the navigator label that will be used by the datatable
     * 
     * @param navigatorId
     *            component id navigator label should be created with
     * @param table
     *            dataview used by datatable
     * @return navigator label that will be used to navigate the data table
     * 
     */
    protected WebComponent newNavigatorLabel(String navigatorId, final DataTable<?> table) {
        return new NavigatorLabel(navigatorId, table);
    }

    /**
     * @see org.apache.wicket.Component#callOnBeforeRenderIfNotVisible()
     */
    @Override
    protected boolean callOnBeforeRenderIfNotVisible() {
        return true;
    }
}
