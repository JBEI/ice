package org.jbei.ice.lib.entry.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.shared.dto.EntryType;

import org.hibernate.search.annotations.Indexed;

/**
 * Store Part specific fields.
 * <p/>
 * <ul>
 * <li><b>packageFormat: </b>Best guess assembly/packaging format for the part. Currentyl accepts
 * Raw, Biobrick, and BglBrick.</li>
 * <li><b>pkgd_dna_fwd_hash: </b> Forward sequence hash of the entire sequence, including complete
 * prefix and suffix, if applicable.</li>
 * <li><b>pkgd_dna_rev_hash: </b> Reverse sequence hash of the entre sequence, including complete
 * prefix and suffix, if applicable.</li>
 * </ul>
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

    @Column(name = "pkgd_dna_fwd_hash", length = 40)
    private String pkgdDnaFwdHash;

    @Column(name = "pkgd_dna_rev_hash", length = 40)
    private String pkgdDnaRevHash;

    public Part() {
        setRecordType(EntryType.PART.getName());
    }

    public AssemblyStandard getPackageFormat() {
        return packageFormat;
    }

    public void setPackageFormat(AssemblyStandard packageFormat) {
        this.packageFormat = packageFormat;
    }

    public String getPkgdDnaFwdHash() {
        return pkgdDnaFwdHash;
    }

    public void setPkgdDnaFwdHash(String pkgdDnaFwdHash) {
        this.pkgdDnaFwdHash = pkgdDnaFwdHash;
    }

    public String getPkgdDnaRevHash() {
        return pkgdDnaRevHash;
    }

    public void setPkgdDnaRevHash(String pkgdDnaRevHash) {
        this.pkgdDnaRevHash = pkgdDnaRevHash;
    }

    /**
     * Generate a map between {@link AssemblyStandard} types and user friendly string.
     *
     * @return Map of types and names.
     */
    public static Map<String, String> getPackageFormatOptionsMap() {
        Map<String, String> resultMap = new LinkedHashMap<String, String>();

        resultMap.put(AssemblyStandard.RAW.toString(), "Raw");
        resultMap.put(AssemblyStandard.BIOBRICKA.toString(), "BioBrick A");
        resultMap.put(AssemblyStandard.BIOBRICKB.toString(), "BglBrick");

        return resultMap;
    }

    public static AssemblyStandard getAssemblyType() {
        AssemblyStandard result = null;

        return result;
    }

    public static List<SequenceFeature> getAssemblyFeatures() {
        List<SequenceFeature> result = null;

        return result;
    }

    public static SequenceFeature getInnerPart() {
        SequenceFeature result = null;

        return result;
    }

}
