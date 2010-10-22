package org.jbei.ice.web.data.tables;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;

public class RegistryTable<T extends Object> extends DataTable<T> {

	private static final long serialVersionUID = 1L;

	public RegistryTable(String id, IColumn<T>[] columns, SortableDataProvider<T> dataProvider, int rowsPerPage) {
		super(id, columns, dataProvider, rowsPerPage);
	
		setOutputMarkupId(true);
		setVersioned(false);
		
		addTopToolbar(new AjaxFallbackHeadersToolbar(this, dataProvider));
		addBottomToolbar(new RegistryTableNavigationToolbar(this));
	}

	@Override
	protected Item<T> newRowItem(String id, int index, IModel<T> model)	{
		return new OddEvenItem<T>(id, index, model);
	}
}
