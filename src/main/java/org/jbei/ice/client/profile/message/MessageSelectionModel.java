package org.jbei.ice.client.profile.message;

import org.jbei.ice.lib.shared.dto.message.MessageInfo;

import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

/**
 * @author Hector Plahar
 */
public class MessageSelectionModel extends MultiSelectionModel<MessageInfo> {

    private boolean allSelected;

    public MessageSelectionModel() {
        super(new ProvidesKey<MessageInfo>() {

            @Override
            public Long getKey(MessageInfo item) {
                return item.getId();
            }
        });
    }

    public void setAllSelected(boolean b) {
        allSelected = b;
    }

    public boolean isAllSelected() {
        return this.allSelected;
    }

    @Override
    public boolean isSelected(MessageInfo object) {
        if (allSelected) {
            setSelected(object, true);
        }

        return super.isSelected(object);
    }
}
