package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.forms.JavascriptEventConfirmation;
import org.jbei.ice.web.pages.AdminPage;

public class GroupsDataView extends DataView<Group> {
	
	private static final long serialVersionUID = 1L;
	private static final int PAGE_RECORD_COUNT = 50;

	protected GroupsDataView(String id, IDataProvider<Group> dataProvider) {
		super(id, dataProvider, PAGE_RECORD_COUNT);
	}
	
	@Override
	protected void populateItem(Item<Group> item) {
		
		renderGroupId(item);
		renderLabel(item);
		renderDescription(item);
		renderParent(item);
		renderEditLink(item);
		renderDeleteLink(item);
	}
	
	protected void renderGroupId(Item<Group> item ) {
		item.add(new Label("group_id", item.getModelObject().getUuid()));
	}
	
	protected void renderParent(Item<Group> item) {
		Group parent = item.getModelObject().getParent();
		String parentLabel = (parent == null) ? "null" : parent.getLabel();
		item.add(new Label("parent", parentLabel));
	}
	
	protected void renderLabel(Item<Group> item ) {
		item.add( new Label("label", item.getModelObject().getLabel() ));
	}
	
	protected void renderDescription(Item<Group> item ) {
		item.add(new Label("description", item.getModelObject().getDescription()));
	}
	
	protected void renderEditLink(final Item<Group> item) {
		
		AjaxFallbackLink editLink = new AjaxFallbackLink("edit_link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				setResponsePage(AdminPage.class, new PageParameters("0=groups,1=" + item.getModelObject().getUuid()));
			}
		};
		editLink.add(new Label("edit", "edit"));
		
		item.add(editLink);
	}
	
	protected void renderDeleteLink(final Item<Group> item) {
		AjaxFallbackLink deleteLink = new AjaxFallbackLink("delete_link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					GroupManager.delete(item.getModelObject());
				} catch (ManagerException e) {
					throw new ViewException(e);
				}
				setResponsePage(AdminPage.class, new PageParameters("0=groups"));
			}
        };

        deleteLink.add(new JavascriptEventConfirmation("onclick", "Delete this Group?"));
        deleteLink.add(new Label("delete","delete"));
        item.add( deleteLink );   
	}
}
