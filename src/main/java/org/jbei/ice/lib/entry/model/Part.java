package org.jbei.ice.lib.entry.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.jbei.ice.lib.shared.dto.entry.EntryType;

import org.hibernate.search.annotations.Indexed;

/**
 * Store Part specific fields.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Indexed
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "parts")
public class Part extends Entry {
    private static final long serialVersionUID = 1L;

    public enum AssemblyStandard {
        RAW, BIOBRICKA, BIOBRICKB;
    }

    @Column(name = "package_format", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssemblyStandard packageFormat;

    public Part() {
        super();
        setRecordType(EntryType.PART.getName());
        this.packageFormat = Part.AssemblyStandard.RAW;
    }
}
