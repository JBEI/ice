package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "funding_source")
@SequenceGenerator(name = "sequence", sequenceName = "funding_source_id_seq", allocationSize = 1)
public class FundingSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @Column(name = "funding_source", length = 255, nullable = false)
    private String fundingSource;

    @Column(name = "principal_investigator", length = 255, nullable = false)
    private String principalInvestigator;

    @OneToMany
    @JoinColumn(name = "funding_source_id")
    @OrderBy("id")
    private Set<EntryFundingSource> entryFundingSources = new LinkedHashSet<EntryFundingSource>();

    @OneToMany
    @JoinColumn(name = "funding_source_id")
    @OrderBy("id")
    private Set<AccountFundingSource> accountFundingSources = new LinkedHashSet<AccountFundingSource>();

    //getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    public void setEntryFundingSources(Set<EntryFundingSource> entryFundingSources) {
        //TODO
        //this.entryFundingSources = entryFundingSources;
    }

    public Set<EntryFundingSource> getEntryFundingSources() {
        return entryFundingSources;
    }

    public void setAccountFundingSources(Set<AccountFundingSource> accountFundingSources) {
        //TODO
        //this.accountFundingSources = accountFundingSources;
    }

    public Set<AccountFundingSource> getAccountFundingSources() {
        return accountFundingSources;
    }

}
