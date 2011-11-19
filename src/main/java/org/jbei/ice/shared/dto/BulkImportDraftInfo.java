package org.jbei.ice.shared.dto;

import java.util.Date;

import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BulkImportDraftInfo implements IsSerializable {

    private String name;
    private EntryAddType type;
    private int count;
    private Date created;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntryAddType getType() {
        return type;
    }

    public void setType(EntryAddType type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
