package org.jbei.ice.client.entry.display.detail;

import java.util.ArrayList;

import org.jbei.ice.client.Page;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Widget for display the list of parts associated with another part
 * in an orderly fashion
 *
 * @author Hector Plahar
 */
public class LinkedEntriesWidget extends Composite {

    public LinkedEntriesWidget(ArrayList<PartData> parts) {
        if (parts == null || parts.isEmpty()) {
            initWidget(new HTML());
            return;
        }

        VerticalPanel panel = new VerticalPanel();
        initWidget(panel);

        for (PartData part : parts) {
            panel.add(new Hyperlink(part.getPartId(), Page.ENTRY_VIEW.getLink() + ";id=" + part.getId()));
        }
    }
}
