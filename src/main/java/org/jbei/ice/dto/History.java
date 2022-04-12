package org.jbei.ice.dto;

import org.jbei.ice.account.Account;
import org.jbei.ice.dto.entry.EntryFieldLabel;
import org.jbei.ice.dto.web.RegistryPartner;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class History implements IDataTransferModel {

    private long id;
    private String action;
    private String userId;
    private Account account;
    private RegistryPartner partner;
    private EntryFieldLabel entryFieldLabel;
    private String oldValue;
    private long time;
    private boolean isCustom;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public RegistryPartner getPartner() {
        return partner;
    }

    public void setPartner(RegistryPartner partner) {
        this.partner = partner;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public EntryFieldLabel getEntryFieldLabel() {
        return entryFieldLabel;
    }

    public void setEntryFieldLabel(EntryFieldLabel entryFieldLabel) {
        this.entryFieldLabel = entryFieldLabel;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }
}
