package org.jbei.ice.lib.models;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.IPartValueObject;
import org.jbei.ice.shared.dto.EntryType;

/**
 * Store Part specific fields.
 * <p>
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
 * 
 */
@Entity
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "parts")
public class Part extends Entry implements IPartValueObject, IModel {
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

    public Part(String recordId, String versionId, String recordType, String owner,
            String ownerEmail, String creator, String creatorEmail, String status, String alias,
            String keywords, String shortDescription, String longDescription,
            String longDescriptionType, String references, Date creationTime,
            Date modificationTime, AssemblyStandard packageFormat, String pkgdDnaFwdHash,
            String pkgdDnaRevHash) {
        super(recordId, versionId, recordType, owner, ownerEmail, creator, creatorEmail, status,
                alias, keywords, shortDescription, longDescription, longDescriptionType,
                references, creationTime, modificationTime);
        this.packageFormat = packageFormat;
        this.pkgdDnaFwdHash = pkgdDnaFwdHash;
        this.pkgdDnaRevHash = pkgdDnaRevHash;
    }

    @Override
    public AssemblyStandard getPackageFormat() {
        return packageFormat;
    }

    @Override
    public void setPackageFormat(AssemblyStandard packageFormat) {
        this.packageFormat = packageFormat;
    }

    @Override
    public String getPkgdDnaFwdHash() {
        return pkgdDnaFwdHash;
    }

    @Override
    public void setPkgdDnaFwdHash(String pkgdDnaFwdHash) {
        this.pkgdDnaFwdHash = pkgdDnaFwdHash;
    }

    @Override
    public String getPkgdDnaRevHash() {
        return pkgdDnaRevHash;
    }

    @Override
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
