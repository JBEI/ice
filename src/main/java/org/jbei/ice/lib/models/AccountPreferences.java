package org.jbei.ice.lib.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.permissions.WorkSpace;

@Entity
@Table(name = "account_preferences")
@SequenceGenerator(name = "sequence", sequenceName = "account_preferences_id_seq", allocationSize = 1)
public class AccountPreferences implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @Column(name = "preferences")
    @Lob
    private String preferences;

    @Column(name = "restriction_enzymes")
    @Lob
    private String restrictionEnzymes;

    @ManyToOne
    @JoinColumn(name = "accounts_id", unique = true, nullable = false)
    private Account account;

    @Column(name = "work_space", nullable = true)
    private WorkSpace workSpace;

    public AccountPreferences() {
        super();
    }

    public AccountPreferences(Account account, String preferences, String restrictionEnzymes) {
        super();
        this.preferences = preferences;
        this.restrictionEnzymes = restrictionEnzymes;
        this.account = account;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public String getRestrictionEnzymes() {
        return restrictionEnzymes;
    }

    public void setRestrictionEnzymes(String restrictionEnzymes) {
        this.restrictionEnzymes = restrictionEnzymes;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setWorkSpace(WorkSpace workSpace) {
        this.workSpace = workSpace;
    }

    public WorkSpace getWorkSpace() {
        return workSpace;
    }

}