package org.jbei.ice.lib.models;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.IModel;

/**
 * Tag an account as a moderator.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 */

@Deprecated
@Entity
@Table(name = "moderator")
@SequenceGenerator(name = "sequence", sequenceName = "moderator_id_seq", allocationSize = 1)
public class Moderator implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

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
}
