package org.jbei.ice.web.panels;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.UnprotectedPage;
import org.jbei.ice.web.utils.WebUtils;

public class SimpleEntryViewPanel<T extends Entry> extends AbstractEntryViewPanel<T> {
    private static final long serialVersionUID = 1L;
    private static final int MAX_LONG_FIELD_LENGTH = 100;

    public SimpleEntryViewPanel(String id, Model<T> entryModel) {
        super(id, entryModel);

        renderIcons();
    }

    @Override
    protected void renderSummary() {
        add(new MultiLineLabel("shortDescription", trimLongField(WebUtils.linkifyText(getEntry()
                .getShortDescription()), MAX_LONG_FIELD_LENGTH)).setEscapeModelStrings(false));
    }

    @Override
    protected void renderNotes() {
        add(new MultiLineLabel("longDescription", trimLongField(WebUtils.linkifyText(getEntry()
                .getLongDescription()), MAX_LONG_FIELD_LENGTH)).setEscapeModelStrings(false));
    }

    @Override
    protected void renderReferences() {
        add(new MultiLineLabel("references", trimLongField(WebUtils.linkifyText(getEntry()
                .getReferences()), MAX_LONG_FIELD_LENGTH)).setEscapeModelStrings(false));
    }

    protected void renderIcons() {
        ResourceReference hasAttachmentImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "attachment.gif");
        ResourceReference hasSampleImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sample.png");
        ResourceReference hasSequenceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sequence.gif");

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            add(new Image("attachments", hasAttachmentImage).setVisible(entryController
                    .hasAttachments(getEntry())));
            add(new Image("samples", hasSampleImage).setVisible(entryController
                    .hasSamples(getEntry())));
            add(new Image("sequence", hasSequenceImage).setVisible(entryController
                    .hasSequence(getEntry())));
        } catch (ControllerException e) {
            throw new ViewException(e);
        }
    }
}
