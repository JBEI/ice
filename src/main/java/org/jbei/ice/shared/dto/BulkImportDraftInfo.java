package org.jbei.ice.shared.dto;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BulkImportDraftInfo implements IsSerializable {

    private long id;
    private String name;
    private EntryAddType type;
    private int count;
    private Date created;

    // when retrieving the data for menu
    // these are empty
    private ArrayList<EntryInfo> primary;
    private ArrayList<EntryInfo> secondary;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public ArrayList<EntryInfo> getPrimary() {
        return primary;
    }

    public void setPrimary(ArrayList<EntryInfo> primary) {
        this.primary = primary;
    }

    public ArrayList<EntryInfo> getSecondary() {
        return secondary;
    }

    public void setSecondary(ArrayList<EntryInfo> secondary) {
        this.secondary = secondary;
    }
}
