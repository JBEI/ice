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

@Entity
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "parts")
public class Part extends Entry implements IPartValueObject, IModel {
    private static final long serialVersionUID = 1L;

    @Column(name = "package_format", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssemblyStandard packageFormat;

    @Column(name = "pkgd_dna_fwd_hash", length = 40)
    private String pkgdDnaFwdHash;

    @Column(name = "pkgd_dna_rev_hash", length = 40)
    private String pkgdDnaRevHash;

    public Part() {
    }

    public Part(String recordId, String versionId, String recordType, String owner,
            String ownerEmail, String creator, String creatorEmail, String status, String alias,
            String keywords, String shortDescription, String longDescription, String references,
            Date creationTime, Date modificationTime, AssemblyStandard packageFormat,
            String pkgdDnaFwdHash, String pkgdDnaRevHash) {
        super(recordId, versionId, recordType, owner, ownerEmail, creator, creatorEmail, status,
                alias, keywords, shortDescription, longDescription, references, creationTime,
                modificationTime);
        this.packageFormat = packageFormat;
        this.pkgdDnaFwdHash = pkgdDnaFwdHash;
        this.pkgdDnaRevHash = pkgdDnaRevHash;
    }

    public enum AssemblyStandard {
        RAW, BIOBRICKA, BIOBRICKB;
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

    public static Map<String, String> getPackageFormatOptionsMap() {
        Map<String, String> resultMap = new LinkedHashMap<String, String>();

        resultMap.put(AssemblyStandard.RAW.toString(), "Raw");
        resultMap.put(AssemblyStandard.BIOBRICKA.toString(), "Biobrick A");
        resultMap.put(AssemblyStandard.BIOBRICKB.toString(), "BioBrick Berkeley");

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
