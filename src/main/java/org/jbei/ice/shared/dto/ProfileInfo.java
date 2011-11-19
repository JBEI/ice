package org.jbei.ice.shared.dto;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProfileInfo implements IsSerializable {
    private AccountInfo accountInfo;
    private ArrayList<Long> userEntries;
    private ArrayList<Long> userSamples;

    public ProfileInfo() {
        userEntries = new ArrayList<Long>();
        userSamples = new ArrayList<Long>();
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public ArrayList<Long> getUserEntries() {
        return userEntries;
    }

    public void setUserEntries(ArrayList<Long> userEntries) {
        this.userEntries.clear();
        if (userEntries == null)
            return;

        this.userEntries.addAll(userEntries);
    }

    public ArrayList<Long> getUserSamples() {
        return userSamples;
    }

    public void setUserSamples(ArrayList<Long> userSamples) {
        this.userSamples.clear();
        if (userSamples == null)
            return;

        this.userSamples.addAll(userSamples);
    }
}
