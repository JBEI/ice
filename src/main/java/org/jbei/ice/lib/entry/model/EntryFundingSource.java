package org.jbei.ice.lib.entry.model;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.FundingSource;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Many-to-Many representation between {@link Entry} and {@link org.jbei.ice.lib.models.FundingSource}.
 * <p/>
 * This class explicitly spells out the many-to-many representation instead of relying on
 * Hibernate's automatic intermediate table generation due to historical database compatibility with
 * the python version.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "entries_funding_source")
@SequenceGenerator(name = "sequence", sequenceName = "entries_funding_source_id_seq", allocationSize = 1)
public class EntryFundingSource implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "funding_source_id", nullable = false)
    private FundingSource fundingSource;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    @XmlTransient
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public FundingSource getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(FundingSource fundingSource) {
        this.fundingSource = fundingSource;
    }

    @XmlTransient
    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }
}