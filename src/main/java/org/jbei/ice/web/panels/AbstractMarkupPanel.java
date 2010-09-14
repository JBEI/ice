package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.panel.Panel;

public class AbstractMarkupPanel extends Panel {
    private static final long serialVersionUID = 1L;

    protected String markupData;

    public AbstractMarkupPanel(String id) {
        super(id);
    }

    public String getData() {
        return markupData;
    }

    public void setData(String data) {
        markupData = data;
    }
}