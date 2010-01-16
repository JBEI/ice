package org.jbei.ice.web.panels;

import java.io.IOException;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.web.pages.EntryViewPage;

public class AttachmentItemViewPanel extends Panel {
	private static final long serialVersionUID = 1L;
	private Integer index = null;
	private Attachment attachment = null;

	@SuppressWarnings("unchecked")
	public AttachmentItemViewPanel(String id, Integer counter,
			Attachment attachment) {
		super(id);
		this.setAttachment(attachment);
		this.setIndex(counter);

		add(new Label("counter", counter.toString()));
		String descriptionString = attachment.getDescription();
		if (descriptionString.length() > 70) {
			descriptionString = descriptionString.substring(0, 69) + "...";
		}
		
		add(new Label("description", descriptionString));
				
		class DeleteAttachmentLink extends AjaxFallbackLink {
			private static final long serialVersionUID = 1L;

			public DeleteAttachmentLink(String id) {
				super(id);
				this.add(new SimpleAttributeModifier("onclick",
						"return confirm('Delete this attachment?');"));
			}

			public void onClick(AjaxRequestTarget target) {
				AttachmentItemViewPanel thisPanel = (AttachmentItemViewPanel) getParent();
				Attachment attachment = thisPanel.getAttachment();

				try {
					AttachmentManager.delete(attachment);
				} catch (ManagerException e) {
					e.printStackTrace();
				}

				setRedirect(true);
				setResponsePage(EntryViewPage.class, new PageParameters("0="
						+ attachment.getEntry().getId() + ",1=attachments"));
			}
		}

		AjaxFallbackLink deleteAttachmentLink = new DeleteAttachmentLink(
				"deleteAttachmentLink");
		deleteAttachmentLink.setOutputMarkupId(true);
		add(deleteAttachmentLink);
		
		//BookmarkablePageLink downloadLink = new BookmarkablePageLink("downloadAttachmentLink", WorkSpacePage.class);
		DownloadLink downloadLink = null;
		try {
			downloadLink = new DownloadLink("downloadAttachmentLink", AttachmentManager.readFile(attachment), attachment.getFileName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		downloadLink.add(new Label("fileName", attachment.getFileName()));
		add(downloadLink);
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getIndex() {
		return index;
	}
}
