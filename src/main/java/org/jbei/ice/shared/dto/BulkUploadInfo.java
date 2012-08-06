package org.jbei.ice.shared.dto;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BulkUploadInfo implements IsSerializable {

    private long id;
    private String name;
    private EntryAddType type;
    private int count;
    private Date created;
    private Date lastUpdate;
    private AccountInfo account;
    private GroupInfo group;
    private ArrayList<EntryInfo> entryList;

    public BulkUploadInfo() {
        entryList = new ArrayList<EntryInfo>();
    }

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

    public AccountInfo getAccount() {
        return account;
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }

    public GroupInfo getGroupInfo() {
        return this.group;
    }

    public void setGroupInfo(GroupInfo info) {
        this.group = info;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public ArrayList<EntryInfo> getEntryList() {
        return entryList;
    }

    public void setEntryList(ArrayList<EntryInfo> entryList) {
        this.entryList.clear();
        this.entryList.addAll(entryList);
    }
}
