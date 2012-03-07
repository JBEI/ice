package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.client.collection.view.OptionSelect;

// attachment menu item
public class AttachmentItem extends OptionSelect {

    private final String description;

    public AttachmentItem(long id, String name, String desc) {
        super(id, name);
        this.description = desc;
    }

    public String getDescription() {
        return description;
    }
}
