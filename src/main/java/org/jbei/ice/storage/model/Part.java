package org.jbei.ice.storage.model;

import org.hibernate.search.annotations.Indexed;
import org.jbei.ice.lib.dto.entry.EntryType;

import javax.persistence.*;

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
