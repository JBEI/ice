package org.jbei.ice.storage.model;

import org.jbei.ice.lib.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;

/**
 * Entity for storing user preferences. It serves a dual role in terms of storing the default user values
 * for certain supported entry fields and also the user entered boost values for searching. The list of supported
 * fields for "boosting" can be found in {@link org.jbei.ice.lib.dto.search.SearchBoostField}.
 * <p>
 * Boost field keys are prefixed with "BOOST_". e.g. "BOOST_PRINCIPAL_INVESTIGATOR" will be the boost key for
 * the principal investigator field while the default value key will be "PRINCIPAL_INVESTIGATOR"
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "PREFERENCE")
@SequenceGenerator(name = "sequence", sequenceName = "preferences_id_seq", allocationSize = 1)
public class Preference implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "preference_key")
    private String key;

    @Column(name = "preference_value")
    private String value;

    public Preference() {
    }

    public Preference(Account account, String key, String value) {
        this.account = account;
        this.key = key;
        this.value = value;
    }

    public long getId() {
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

    @Override
    public PreferenceInfo toDataTransferObject() {
        PreferenceInfo info = new PreferenceInfo();
        info.setKey(getKey());
        info.setValue(getValue());
        return info;
    }
}
