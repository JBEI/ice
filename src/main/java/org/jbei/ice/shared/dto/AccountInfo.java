package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AccountInfo implements IsSerializable {

    private String sessionId;
    private String email;
    private String firstName;
    private String lastName;
    private String institution;
    private String description;
    private String since;
    private int entriesAvailable;
    private int userEntryCount;
    private int userSampleCount;
    private long visibleEntryCount;

    public AccountInfo() {
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public String getInstitution() {
        return institution;
    }

    public String getDescription() {
        return description;
    }

    public String getSince() {
        return since;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getEntriesAvailable() {
        return entriesAvailable;
    }

    public void setEntriesAvailable(int entriesAvailable) {
        this.entriesAvailable = entriesAvailable;
    }

    public int getUserEntryCount() {
        return userEntryCount;
    }

    public void setUserEntryCount(int userEntryCount) {
        this.userEntryCount = userEntryCount;
    }

    public int getUserSampleCount() {
        return userSampleCount;
    }

    public void setUserSampleCount(int userSampleCount) {
        this.userSampleCount = userSampleCount;
    }

    public long getVisibleEntryCount() {
        return visibleEntryCount;
    }

    public void setVisibleEntryCount(long visibleEntryCount) {
        this.visibleEntryCount = visibleEntryCount;
    }
}
