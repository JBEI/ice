package org.jbei.ice.web.dataProviders;

import java.util.Iterator;

import org.jbei.ice.lib.models.Entry;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public abstract class RegistryTableDataProvider<T extends Entry> implements ISortableDataProvider<T> {

	private static final long serialVersionUID = 1L;

	@Override
	public Iterator<? extends T> iterator(int first, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IModel<T> model(T object) {
		return new Model<T>( object );
	}

	@Override
	public void detach() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ISortState getSortState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSortState(ISortState state) {
		// TODO Auto-generated method stub
		
	}


}
