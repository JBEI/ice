package org.jbei.ice.web.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.forms.PartUpdateFormPanel;
import org.jbei.ice.web.forms.PlasmidUpdateFormPanel;
import org.jbei.ice.web.forms.StrainUpdateFormPanel;

public class AdminEditPartsFormPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public AdminEditPartsFormPanel(String id) {
		super(id);
		
		add(new EditEntryForm("edit_part_form"));
		add(new EmptyMessagePanel("content_panel", "").setOutputMarkupId(true));
	}
	
	class EditEntryForm extends StatelessForm<Object> {

		private static final long serialVersionUID = 1L;
		private String entryId;

		public EditEntryForm(String id) {
			super(id);
			setModel(new CompoundPropertyModel<Object>(this));
            add(new TextField<String>("entryId"));
            add(new EditPartButton("editButton"));
		}
		
		public String getEntryId(){
			return entryId;
		}    	
    }
	
	class EditPartButton extends AjaxButton {
    	private static final long serialVersionUID = 1L;
    	
    	public EditPartButton(String id) {
    		super(id);
    	}

        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

        	String entryId = ((EditEntryForm)getParent()).getEntryId();
        	
            EntryController entryController = new EntryController(IceSession.get().getAccount());

            Entry entry = null;
			try {
				entry = entryController.getByIdentifier(entryId);
			} catch (ControllerException e) {
				throw new ViewException(e);
			} catch (PermissionException e) {
				throw new ViewException(e);
			}

            if (entry == null) {
                Panel answer = new EmptyMessagePanel("content_panel", "invalid entryId");
                answer.setOutputMarkupId(true);
                getParent().getParent().replace(answer);
                target.addComponent(answer);
            } else {
                Panel answer = null;
                if (entry instanceof Plasmid) {
                    answer = new PlasmidUpdateFormPanel("content_panel", (Plasmid) entry);
                } else if (entry instanceof Strain) {
                    answer = new StrainUpdateFormPanel("content_panel", (Strain) entry);
                } else if (entry instanceof Part) {
                    answer = new PartUpdateFormPanel("content_panel", (Part) entry);
                }

                if (answer != null) {
                    answer.setOutputMarkupId(true);
                    getParent().getParent().replace(answer);
                    target.addComponent(answer);
                }
            }

        }
    }
	
}
