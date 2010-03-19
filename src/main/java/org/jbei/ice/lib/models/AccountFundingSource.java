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

import org.jbei.ice.lib.dao.IModel;

@Entity
@Table(name = "accounts_funding_source")
@SequenceGenerator(name = "sequence", sequenceName = "accounts_funding_source_id_seq", allocationSize = 1)
public class AccountFundingSource implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "funding_source_id", nullable = false)
    private FundingSource fundingSource;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accounts_id", nullable = false)
    private Account account;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public FundingSource getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(FundingSource fundingSource) {
        this.fundingSource = fundingSource;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

}