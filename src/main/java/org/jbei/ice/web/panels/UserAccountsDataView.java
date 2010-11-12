package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.forms.JavascriptEventConfirmation;
import org.jbei.ice.web.pages.AdminPage;
import org.jbei.ice.web.pages.ProfilePage;

public class UserAccountsDataView extends DataView<Account> {
	
	private static final long serialVersionUID = 1L;
	private static final int PAGE_RECORD_COUNT = 50;
	
	public UserAccountsDataView(String id, IDataProvider<Account> dataProvider, ModalWindow window) {
        super(id, dataProvider, PAGE_RECORD_COUNT);
    }

	@Override
	protected void populateItem(Item<Account> item) {
		
		renderOwnerLink(item);
		renderEmail(item);
		renderDescription(item);
		renderEditLink(item);
		renderDeleteLink(item);
	}
	
	protected void renderOwnerLink(Item<Account> item) {

        Account ownerAccount = item.getModelObject();

        BookmarkablePageLink<ProfilePage> ownerProfileLink = new BookmarkablePageLink<ProfilePage>(
                "accountProfileLink", ProfilePage.class, new PageParameters("0=about,1="
                        + ownerAccount.getEmail()));
        ownerProfileLink.add(new Label("account", ownerAccount.getFullName()));

        String ownerAltText = "Profile " + ownerAccount.getFullName();
        ownerProfileLink.add(new SimpleAttributeModifier("title", ownerAltText));
        ownerProfileLink.add(new SimpleAttributeModifier("alt", ownerAltText));

        item.add(ownerProfileLink);
    }
	
	protected void renderDescription(Item<Account> item) {
        item.add(new Label("description", item.getModelObject().getDescription()));
	}
	
	protected void renderEmail(Item<Account> item) {
        item.add(new Label("email", item.getModelObject().getEmail()));
	}
	
	protected void renderEditLink(final Item<Account> item) {
		
		AjaxLink<Object> editLink = new AjaxLink<Object>("edit_link") {

            private static final long serialVersionUID = 1L;

            @Override
			public void onClick(AjaxRequestTarget target) {
				setResponsePage(AdminPage.class, new PageParameters("0=users,1=" + item.getModelObject().getEmail()));
			}
		};
		
		editLink.add(new Label("edit", "edit"));
		item.add(editLink);
	}
	
	protected void renderDeleteLink(final Item<Account> item) {
		AjaxLink<Object> deleteLink = new AjaxLink<Object>("delete_link") {
            private static final long serialVersionUID = 1L;

            @Override
			public void onClick(AjaxRequestTarget target) {

				try {
					AccountManager.delete(item.getModelObject());
				} catch (ManagerException e) {
					throw new ViewException(e);
				}
				
				setResponsePage(AdminPage.class);
			}
        };

        deleteLink.add(new JavascriptEventConfirmation("onclick", "Delete account \\'" + item.getModelObject().getEmail() + "\\'?"));
        deleteLink.add(new Label("delete","delete"));
        item.add( deleteLink );   
	}
}
