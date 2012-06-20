package org.jbei.ice.lib.account.model;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Represents the many-to-many table between {@link org.jbei.ice.lib.entry.model.Entry} and {@link Account}.
 * <p/>
 * This class explicitly spells out the many-to-many representation instead of relying on
 * Hibernate's automatic intermediate table generation due to historical database compatibility with
 * the python version.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "account_entry_relationship")
@SequenceGenerator(name = "sequence", sequenceName = "account_entry_relationship_id_seq", allocationSize = 1)
public class AccountEntryRelationship implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accounts_id")
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id")
    private Entry entry;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

}
