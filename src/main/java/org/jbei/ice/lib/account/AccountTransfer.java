package org.jbei.ice.lib.account;

import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * Data transfer object for user account
 *
 * @author Hector Plahar
 */
public class AccountTransfer implements IDataTransferModel {

    private long id;
    private String sessionId;
    private String email;
    private String password;
    private String initials;
    private String firstName;
    private String lastName;
    private String institution;
    private String description;
    private long lastLogin;
    private long registerDate;
    private long userEntryCount;
    private long visibleEntryCount;
    private boolean isAdmin;
    private int newMessageCount;
    private AccountType accountType;
    private ArrayList<AccessPermission> defaultPermissions;

    public AccountTransfer() {
        institution = "";
        description = "";
        initials = "";
        defaultPermissions = new ArrayList<>();
    }

    public AccountTransfer(String email, String password) {
        this.email = email;
        this.password = password;
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

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        if (firstName == null && lastName == null)
            return null;
        return this.firstName + " " + this.lastName;
    }

    public String getInstitution() {
        return institution;
    }

    public String getDescription() {
        return description;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getUserEntryCount() {
        return userEntryCount;
    }

    public void setUserEntryCount(long ownerEntryCount) {
        this.userEntryCount = ownerEntryCount;
    }

    public long getVisibleEntryCount() {
        return visibleEntryCount;
    }

    public void setVisibleEntryCount(long visibleEntryCount) {
        this.visibleEntryCount = visibleEntryCount;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long date) {
        this.lastLogin = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNewMessageCount() {
        return newMessageCount;
    }

    public void setNewMessageCount(int newMessageCount) {
        this.newMessageCount = newMessageCount;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public ArrayList<AccessPermission> getDefaultPermissions() {
        return defaultPermissions;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(long registerDate) {
        this.registerDate = registerDate;
    }

    @Override
    public int hashCode() {
        return this.email.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != AccountTransfer.class)
            return false;

        return ((AccountTransfer) o).getEmail().equalsIgnoreCase(this.email);
    }
}
