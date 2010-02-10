package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.utils.JbeiConstants;

public class PartSimpleViewPanel extends SimpleEntryViewPanel<Part> {
    private static final long serialVersionUID = 1L;

    public PartSimpleViewPanel(String id, Part entry) {
        super(id, new Model<Part>(entry));

        renderPackageFormat();
    }

    protected void renderPackageFormat() {
        add(new Label("packageFormat", JbeiConstants
                .getPackageFormat(getEntry().getPackageFormat())));
    }
}
