package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.web.panels.AttachmentsViewPanel;
import org.jbei.ice.web.panels.PartViewPanel;
import org.jbei.ice.web.panels.PlasmidViewPanel;
import org.jbei.ice.web.panels.SampleViewPanel;
import org.jbei.ice.web.panels.SequenceViewPanel;
import org.jbei.ice.web.panels.StrainViewPanel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.utils.JbeiConstants;

public class EntryViewPage extends ProtectedPage {

	public Entry entry;

	public Component displayPanel;
	public Component generalPanel;
	public Component samplesPanel;
	public Component attachmentsPanel;
	public Component sequencePanel;

	@SuppressWarnings("unchecked")
	public BookmarkablePageLink generalLink;
	public AjaxFallbackLink<?> samplesLink;
	public AjaxFallbackLink<?> attachmentsLink;
	public AjaxFallbackLink<?> sequenceLink;

	@SuppressWarnings("unchecked")
	public EntryViewPage(PageParameters parameters) {
		super(parameters);

		int entryId = parameters.getInt("0");
		try {
			entry = EntryManager.get(entryId);
		} catch (ManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		class SamplesLink extends AjaxFallbackLink {
			private static final long serialVersionUID = 1L;

			public SamplesLink(String id) {
				super(id);
			}

			public void onClick(AjaxRequestTarget target) {
				generalLink.add(
						new SimpleAttributeModifier("class", "inactive"))
						.setOutputMarkupId(true);
				samplesLink.add(new SimpleAttributeModifier("class", "active"))
						.setOutputMarkupId(true);
				attachmentsLink.add(
						new SimpleAttributeModifier("class", "inactive"))
						.setOutputMarkupId(true);
				sequenceLink.add(
						new SimpleAttributeModifier("class", "inactive"))
						.setOutputMarkupId(true);

				refreshTabLinks(getPage(), target);

				if (samplesPanel == null) {
					samplesPanel = makeSamplesPanel(entry);
				}
				displayPanel = samplesPanel;
				getPage().replace(displayPanel);
				target.addComponent(displayPanel);
			}
		}

		class AttachmentsLink extends AjaxFallbackLink {
			private static final long serialVersionUID = 1L;

			public AttachmentsLink(String id) {
				super(id);
			}

			public void onClick(AjaxRequestTarget target) {
				generalLink.add(
						new SimpleAttributeModifier("class", "inactive"))
						.setOutputMarkupId(true);
				samplesLink.add(
						new SimpleAttributeModifier("class", "inactive"))
						.setOutputMarkupId(true);
				attachmentsLink.add(
						new SimpleAttributeModifier("class", "active"))
						.setOutputMarkupId(true);
				sequenceLink.add(
						new SimpleAttributeModifier("class", "inactive"))
						.setOutputMarkupId(true);

				refreshTabLinks(getPage(), target);

				if (attachmentsPanel == null) {
					attachmentsPanel = makeAttachmentsPanel(entry);
				}
				displayPanel = attachmentsPanel;
				getPage().replace(displayPanel);
				target.addComponent(displayPanel);
			}
		}

		class SequenceLink extends AjaxFallbackLink {
			private static final long serialVersionUID = 1L;

			public SequenceLink(String id) {
				super(id);
			}

			public void onClick(AjaxRequestTarget target) {
				generalLink.add(
						new SimpleAttributeModifier("class", "inactive"))
						.setOutputMarkupId(true);
				samplesLink.add(
						new SimpleAttributeModifier("class", "inactive"))
						.setOutputMarkupId(true);
				attachmentsLink.add(
						new SimpleAttributeModifier("class", "inactive"))
						.setOutputMarkupId(true);
				sequenceLink
						.add(new SimpleAttributeModifier("class", "active"))
						.setOutputMarkupId(true);

				refreshTabLinks(getPage(), target);

				if (sequencePanel == null) {
					sequencePanel = makeSequencePanel(entry);
				}
				displayPanel = sequencePanel;
				getPage().replace(displayPanel);
				target.addComponent(displayPanel);
			}
		}

		String recordType = JbeiConstants.getRecordType(entry.getRecordType());
		add(new Label("titleName", recordType + ": " + entry.getNamesAsString()));
		
		generalLink = new BookmarkablePageLink("generalLink",	EntryViewPage.class, 
						new PageParameters("0=" + entry.getId()));
		generalLink.setOutputMarkupId(true);
		samplesLink = new SamplesLink("samplesLink");
		samplesLink.add(new SimpleAttributeModifier("class", "inactive"))
				.setOutputMarkupId(true);
		attachmentsLink = new AttachmentsLink("attachmentsLink");
		attachmentsLink.add(new SimpleAttributeModifier("class", "inactive"))
				.setOutputMarkupId(true);
		sequenceLink = new SequenceLink("sequenceLink");
		sequenceLink.add(new SimpleAttributeModifier("class", "inactive"))
				.setOutputMarkupId(true);

		add(generalLink);
		add(samplesLink);
		add(attachmentsLink);
		add(sequenceLink);

		generalPanel = makeGeneralPanel(entry).setOutputMarkupId(true);
		displayPanel = generalPanel;
		add(displayPanel);

	}

	public Panel makeGeneralPanel(Entry entry) {
		String recordType = entry.getRecordType();
		Panel panel = null;
		if (recordType.equals("strain")) {
			panel = new StrainViewPanel("centerPanel", (Strain) entry);
		} else if (recordType.equals("plasmid")) {
			panel = new PlasmidViewPanel("centerPanel", (Plasmid) entry);
		} else if (recordType.equals("part")) {
			panel = new PartViewPanel("centerPanel", (Part) entry);
		}

		panel.setOutputMarkupId(true);
		return panel;

	}

	public Panel makeSamplesPanel(Entry entry) {
		Panel panel = new SampleViewPanel("centerPanel", entry);
		panel.setOutputMarkupId(true);
		return panel;
	}

	public Panel makeAttachmentsPanel(Entry entry) {
		Panel panel = new AttachmentsViewPanel("centerPanel", entry);
		panel.setOutputMarkupId(true);
		return panel;
	}

	public Panel makeSequencePanel(Entry entry) {
		Panel panel = new SequenceViewPanel("centerPanel", entry);
		panel.setOutputMarkupId(true);
		return panel;
	}

	public void refreshTabLinks(Page page, AjaxRequestTarget target) {
		page.replace(generalLink);
		page.replace(samplesLink);
		page.replace(attachmentsLink);
		page.replace(sequenceLink);
		target.addComponent(generalLink);
		target.addComponent(samplesLink);
		target.addComponent(attachmentsLink);
		target.addComponent(sequenceLink);
	}

}
