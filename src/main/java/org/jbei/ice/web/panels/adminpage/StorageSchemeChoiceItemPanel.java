package org.jbei.ice.web.panels.adminpage;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

public class StorageSchemeChoiceItemPanel extends Panel {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private AjaxLink link;

    public StorageSchemeChoiceItemPanel(String id) {
        super(id);

    }

    @SuppressWarnings("rawtypes")
    public void setLink(AjaxLink link) {
        this.link = link;
    }

    @SuppressWarnings("rawtypes")
    public AjaxLink getLink() {
        return link;
    }

}
