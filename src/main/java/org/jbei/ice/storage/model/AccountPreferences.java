package org.jbei.ice.storage.model;

import org.hibernate.annotations.Type;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

import jakarta.persistence.*;

/**
 * Store preferences for a user for an {@link AccountModel} object.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
@Entity
@Table(name = "account_preferences")
@SequenceGenerator(name = "account_preferences_id", sequenceName = "account_preferences_id_seq", allocationSize = 1)

public class AccountPreferences implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "account_preferences_id")
    private long id;

    @Column(name = "preferences")
    @Lob
    private String preferences;

    @Column(name = "restriction_enzymes")
    @Lob
    private String restrictionEnzymes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accounts_id", unique = true, nullable = false)
    private AccountModel account;

    public AccountPreferences() {
        super();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public AccountModel getAccount() {
        return account;
    }

    public void setAccount(AccountModel account) {
        this.account = account;
    }

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
