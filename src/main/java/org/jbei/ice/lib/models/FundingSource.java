package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.jbei.ice.lib.dao.IModel;

/**
 * Store Funding Source information.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
@Entity
@Table(name = "funding_source")
@SequenceGenerator(name = "sequence", sequenceName = "funding_source_id_seq", allocationSize = 1)
public class FundingSource implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "funding_source", length = 255, nullable = false)
    private String fundingSource;

    @Column(name = "principal_investigator", length = 255, nullable = false)
    private String principalInvestigator;

    /*@OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "funding_source_id")
    @OrderBy("id")
    private Set<EntryFundingSource> entryFundingSources = new LinkedHashSet<EntryFundingSource>();

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "funding_source_id")
    @OrderBy("id")
    private Set<AccountFundingSource> accountFundingSources = new LinkedHashSet<AccountFundingSource>();*/

    //getters and setters
    @XmlTransient
    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    /*public void setEntryFundingSources(Set<EntryFundingSource> entryFundingSources) {
        // TODO: Tim; Implement setEntryFundingSources method for FundingSource
        //this.entryFundingSources = entryFundingSources;
    }

    public Set<EntryFundingSource> getEntryFundingSources() {
        return entryFundingSources;
    }

    public void setAccountFundingSources(Set<AccountFundingSource> accountFundingSources) {
        // TODO: Tim; Implement setAccountFundingSources method for FundingSource
        //this.accountFundingSources = accountFundingSources;
    }

    public Set<AccountFundingSource> getAccountFundingSources() {
        return accountFundingSources;
    }
    */
}
