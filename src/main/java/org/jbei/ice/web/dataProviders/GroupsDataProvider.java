package org.jbei.ice.web.dataProviders;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.web.common.ViewException;

public class GroupsDataProvider extends SortableDataProvider<Group>  {

	private static final long serialVersionUID = 1L;

	@Override
	public Iterator<? extends Group> iterator(int first, int count) {
		try {
			List<Group> groups = new LinkedList<Group>( GroupManager.getAll() );
			return groups.subList(first, first+count).iterator();
		} catch (ManagerException e) {
			throw new ViewException(e);
		}
	}

	@Override
	public int size() {
		try {
			return GroupManager.getAll().size();
		} catch (ManagerException e) {
			throw new ViewException(e);
		}
	}

	@Override
	public IModel<Group> model(Group object) {		
		return new Model<Group>(object);
	}
}
