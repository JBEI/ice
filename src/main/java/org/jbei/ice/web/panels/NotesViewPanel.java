package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.RichTextRenderer;

public class NotesViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public NotesViewPanel(String id, Entry entry) {
        super(id);

        add(new MultiLineLabel("notes", RichTextRenderer.richTextToHtml(
            entry.getLongDescriptionType(), entry.getLongDescription()))
                .setEscapeModelStrings(false));
    }
}