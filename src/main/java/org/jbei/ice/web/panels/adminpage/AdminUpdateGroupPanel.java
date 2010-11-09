package org.jbei.ice.web.panels.adminpage;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.AdminPage;

public class AdminUpdateGroupPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final boolean isNewGroup;

	public AdminUpdateGroupPanel(String id) {
		this(id, null);
	}
	
	public AdminUpdateGroupPanel(String id, Group group) {
		super(id);
		this.isNewGroup = (group == null);
		add(new AddGroupForm("add_group_form", group));
	}
	
	private class AddGroupForm extends StatelessForm<Object> {

		private static final long serialVersionUID = 1L;
		private Group group;
		
		public AddGroupForm(String id, Group group) {
			super(id);
			if (group != null) {
				this.group = group;
			} 
			else {
				this.group = new Group();
			}
			
			CompoundPropertyModel<Object> model = new CompoundPropertyModel<Object>(this);
			setModel(model);
			
			// add form components 
			TextField<String> labelField = new RequiredTextField<String>("label", new PropertyModel<String>(this.group, "label" ));
			add(labelField);
			
			TextField<String> descriptionField = new RequiredTextField<String>("description", new PropertyModel<String>(this.group, "description"));
			add(descriptionField);
			
			List<Group> allGroups;
			try {
				allGroups = new LinkedList<Group>(GroupManager.getAll());
			} catch (ManagerException e) {
				throw new ViewException(e);
			}
			
			DropDownChoice<Group> choice = new DropDownChoice<Group>("parent", new PropertyModel<Group>(this.group, "parent"), allGroups, new ChoiceRenderer<Group>("label"));
			add(choice);	
			
			// buttons 
			Button submitButton = new Button("submit_button", new Model<String>("Save"));
			add(submitButton);
			
			Button cancelButton = new Button("cancel_button", new Model<String>("Cancel")) {			
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit() {
					setResponsePage(AdminPage.class, new PageParameters("0=groups"));
				}
			};
			cancelButton.setDefaultFormProcessing(false);
			add(cancelButton);
		}
		
		@Override
		protected void onSubmit() {	
			try {
				if (isNewGroup){
					String uuid = java.util.UUID.randomUUID().toString();
					group.setUuid(uuid);
				}
				
				GroupManager.save(group);
			} catch (ManagerException e) {
				throw new ViewException(e);
			}
			setResponsePage(AdminPage.class, new PageParameters("0=groups"));
		}
	}
}
