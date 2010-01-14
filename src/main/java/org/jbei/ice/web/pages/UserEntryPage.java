package org.jbei.ice.web.pages;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.panels.EntryPagingPanel;
import org.jbei.ice.web.panels.SamplePagingPanel;

public class UserEntryPage extends ProtectedPage {
	Panel userPanel;
	Component entriesLink;
	Component samplesLink;

	@SuppressWarnings("unchecked")
	public UserEntryPage(PageParameters parameters) {
		super(parameters);

		Account account = IceSession.get().getAccount();
		LinkedHashSet<Entry> entries = EntryManager.getByAccount(account, 0,
				1000);
		ArrayList<Entry> entriesList = new ArrayList<Entry>(entries);
		userPanel = new EntryPagingPanel("userPanel", entriesList, 50);
		userPanel.setOutputMarkupId(true);

		class UserEntriesLink extends AjaxFallbackLink {

			private static final long serialVersionUID = 1L;

			public UserEntriesLink(String id) {
				super(id);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				Account account = IceSession.get().getAccount();
				LinkedHashSet<Entry> entries = EntryManager.getByAccount(
						account, 0, 1000);
				ArrayList<Entry> entriesList = new ArrayList<Entry>(entries);
				userPanel = new EntryPagingPanel("userPanel", entriesList, 50);
				userPanel.setOutputMarkupId(true);
				getPage().replace(userPanel);
				target.addComponent(userPanel);
				samplesLink.add(new SimpleAttributeModifier("class", "inactive"))
						.setOutputMarkupId(true);
				entriesLink.add(new SimpleAttributeModifier("class", "active"))
						.setOutputMarkupId(true);

				getPage().replace(samplesLink);
				target.addComponent(samplesLink);
				getPage().replace(entriesLink);
				target.addComponent(entriesLink);

			}
		};

		class UserSamplesLink extends AjaxFallbackLink {
			private static final long serialVersionUID = 1L;

			public UserSamplesLink(String id) {
				super(id);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				Account account = IceSession.get().getAccount();
				Set<Sample> samples = null;

				try {
					samples = SampleManager.getByAccount(account);
				} catch (ManagerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ArrayList<Sample> samplesList = new ArrayList<Sample>(samples);
				userPanel = new SamplePagingPanel("userPanel", samplesList, 50);
				userPanel.setOutputMarkupId(true);
				getPage().replace(userPanel);
				target.addComponent(userPanel);

				samplesLink.add(new SimpleAttributeModifier("class", "active"))
						.setOutputMarkupId(true);
				getPage().replace(samplesLink);
				target.addComponent(samplesLink);
				entriesLink.add(new SimpleAttributeModifier("class", "inactive"))
						.setOutputMarkupId(true);
				getPage().replace(entriesLink);
				target.addComponent(entriesLink);
			}
		};

		entriesLink = new UserEntriesLink("userEntriesLink").add(
				new SimpleAttributeModifier("class", "active"))
				.setOutputMarkupId(true);
		samplesLink = new UserSamplesLink("userSamplesLink").add(
				new SimpleAttributeModifier("class", "inactive"))
				.setOutputMarkupId(true);

		add(entriesLink);
		add(samplesLink);
		add(userPanel);
	}
}
