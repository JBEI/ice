package org.jbei.ice.web.panels;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.AdminPage;
import org.jbei.ice.web.pages.ProfilePage;

public class AdminAccountUpdatePanel extends Panel {

	private static final long serialVersionUID = 1L;
	private Account account;
	private final boolean isNewRegistration;

	public AdminAccountUpdatePanel(String id) {
		this(id, null);
	}
	
	public AdminAccountUpdatePanel(String id, Account account) {
		super(id);
		this.account = account;
		isNewRegistration = (account == null);
		
		add(new AdminAccountUpdateForm("admin_account_update_form"));
	}
	
	class AdminAccountUpdateForm extends StatelessForm<Object> {

		private static final long serialVersionUID = 1L;
		private String firstName;
        private String lastName;
        private String initials;
        private String email;
        private String institution;
        private String description;
        
		public AdminAccountUpdateForm(String id) {
			super(id);			
			retrieveAccountProperties();
		
			setModel(new CompoundPropertyModel<Object>(this));
			
			TextField<String> firstName = new TextField<String>("firstName", new PropertyModel<String>(this, "firstName"));
			firstName.setRequired(true);
			firstName.add(new StringValidator.MaximumLengthValidator(50));
			add(firstName);

			add(new TextField<String>("lastName", new PropertyModel<String>(this, "lastName"))
	                .setRequired(true).setLabel(new Model<String>("Family name"))
	                .add(new StringValidator.MaximumLengthValidator(50)));
	        add(new TextField<String>("initials", new PropertyModel<String>(this, "initials"))
	                .setLabel(new Model<String>("Initials")).add(
	                    new StringValidator.MaximumLengthValidator(10)));
	        
 	        TextField<String> emailField = new TextField<String>("email", new PropertyModel<String>(this, "email"));
	        emailField.setRequired(true);
	        emailField.add(new StringValidator.MaximumLengthValidator(100));
	        emailField.add(EmailAddressValidator.getInstance());
	        if (account != null && !account.getEmail().isEmpty()) {
	        	emailField.setEnabled(false);
	        }
	        add(emailField);
	        
	        add(new TextField<String>("institution", new PropertyModel<String>(this,
	                "institution")).setLabel(new Model<String>("Institution")));
	        add(new TextArea<String>("description", new PropertyModel<String>(this,
	                "description")).setLabel(new Model<String>("Description")));
	        
	        String buttonText = (isNewRegistration) ? "Save" : "Update";
	        
	        add(new Button("submit_button", new Model<String>(buttonText)));
	        
	        Button cancelButton = new Button("cancel_button", new Model<String>("Cancel")) {			
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit() {
					setResponsePage(AdminPage.class);
				}
			};
			cancelButton.setDefaultFormProcessing(false);
			add(cancelButton);
			
			add(new FeedbackPanel("feedback"));
	    }
		
		protected void retrieveAccountProperties() {
			if (account == null)
				return;
			
			firstName = account.getFirstName();
	        lastName = account.getLastName();
	        initials = account.getInitials();
	        email = account.getEmail();
	        institution = account.getInstitution();
	        description = account.getDescription();
		}
	
	    @Override
	    protected void onSubmit() {
	        if (initials == null) {
	            initials = "";
	        }
	        if (institution == null) {
	            institution = "";
	        }
	        if (description == null) {
	            description = "";
	        }
	
	        if (isNewRegistration) {	        	
	        	createNewAccount();	        	
	        } else {
	        	updateExistingAccount();
	        }
	        	
	        setResponsePage(AdminPage.class);
	    }
	    
	    private void updateExistingAccount() {
	    	try {
				Account account = AccountController.getByEmail(email);
				if (account == null)
					throw new ViewException("Account not found for update: " + email);
				saveAccountInformation();
			} catch (ControllerException e) {
				throw new ViewException(e);
			}
	    }	
	    
	    private void createNewAccount() {
			try {
				account = AccountController.getByEmail(email);
			} catch (ControllerException e) {
				throw new ViewException(e);
			}
			
            if (account != null) {
                error("Account with this email address already registered");
                return;
            }
            
            String newPassword = Utils.generateUUID().substring(24);
            account = new Account();
            account.setIp("");
            account.setPassword(AccountController.encryptPassword(newPassword));
            saveAccountInformation();
            
            sendEmailNotification(newPassword);
	    }
	    
	    private void sendEmailNotification(String newPassword) {
	    	CharSequence resetPasswordPage = WebRequestCycle.get()
            .urlFor(ProfilePage.class,
                new PageParameters("0=password,1=" + account.getEmail()));
		    WebRequestCycle webRequestCycle = (WebRequestCycle) WebRequestCycle.get();
		    HttpServletRequest httpServletRequest = webRequestCycle.getWebRequest()
		            .getHttpServletRequest();
		
		    String urlHeader = (httpServletRequest.isSecure()) ? "https://" : "http://";
		    urlHeader = urlHeader + httpServletRequest.getServerName() + ":"
		            + httpServletRequest.getLocalPort() + "/";
		    String resetPasswordPageUrl = urlHeader + resetPasswordPage;
		
		    String subject = "Account created successfully";
		
		    StringBuilder stringBuilder = new StringBuilder();
		    Formatter formatter = new Formatter(stringBuilder, Locale.US);
		
		    String body = "Dear %1$s, %n%n Thank you for creating a %2$s account. %nBy accessing "
		            + "this site with the password provided at the bottom "
		            + "you agree to the following terms:%n%n%3$s%n%nYour new password is: %4$s%n"
		            + "Please go to the following link and change your password:%n%n"
		            + resetPasswordPageUrl.toString();
		
		    String terms = "Biological Parts IP Disclaimer: \n\n"
		            + "The JBEI Registry of Biological Parts Software is licensed under a standard BSD\n"
		            + "license. Permission or license to use the biological parts registered in\n"
		            + "the JBEI Registry of Biological Parts is not included in the BSD license\n"
		            + "to use the JBEI Registry Software. Berkeley Lab and JBEI make no representation\n"
		            + "that the use of the biological parts registered in the JBEI Registry of\n"
		            + "Biological Parts will not infringe any patent or other proprietary right.";
		    formatter.format(body, email, JbeirSettings.getSetting("PROJECT_NAME"), terms,
		        newPassword);
		    Emailer.send(email, subject, stringBuilder.toString());
	    }
	
		private void saveAccountInformation() {
			account.setIsSubscribed(1);
	        account.setModificationTime(Calendar.getInstance().getTime());
	        account.setEmail(email);
	        account.setFirstName(firstName);
	        account.setLastName(lastName);
	        account.setInitials(initials);
	        account.setInstitution(institution);
	        account.setDescription(description);
	
	        try {
	        	AccountController.save(account);
	        }
	        catch (Exception e) {
	        	throw new ViewException(e);
	        }
		}
	}
}
	
		
