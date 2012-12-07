package org.jbei.ice.lib.account.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.shared.dto.bulkupload.PreferenceInfo;

/**
 * Entity for storing user preferences
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "PREFERENCE")
@SequenceGenerator(name = "sequence", sequenceName = "preferences_id_seq", allocationSize = 1)
public class Preference implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private int id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "preference_key")
    private String key;

    @Column(name = "preference_value")
    private String value;

    public Preference(Account account, String key, String value) {
        this.account = account;
        this.key = key;
        this.value = value;
    }

    public Preference() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static PreferenceInfo toDTO(Preference preference) {
        PreferenceInfo info = new PreferenceInfo();
        info.setKey(preference.getKey());
        info.setValue(preference.getValue());
        return info;
    }
}
