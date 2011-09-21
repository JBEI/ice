package org.jbei.ice.shared.dto;

import java.io.Serializable;

public class AccountInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String email;
    private String firstName;
    private String lastName;
    private String institution;
    private String description;
    private String since;

    public AccountInfo() {
    }

    //    public AccountInfo(String email, String firstName, String lastName, String institution,
    //            String description, String since) {
    //        super();
    //        this.email = email;
    //        this.firstName = firstName;
    //        this.lastName = lastName;
    //        this.institution = institution;
    //        this.description = description;
    //        this.since = since;
    //    }

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

}
