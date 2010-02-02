package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.jbei.ice.lib.value_objects.IPartValueObject;

@Entity
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "parts")
public class Part extends Entry implements IPartValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "package_format", length = 255)
    private String packageFormat;

    @Column(name = "pkgd_dna_fwd_hash", length = 40)
    private String pkgdDnaFwdHash;

    @Column(name = "pkgd_dna_rev_hash", length = 40)
    private String pkgdDnaRevHash;

    public Part() {
    }

    public Part(String recordId, String versionId, String recordType, String owner,
            String ownerEmail, String creator, String creatorEmail, int visibility, String status,
            String alias, String keywords, String shortDescription, String longDescription,
            String references, Date creationTime, Date modificationTime, String packageFormat,
            String pkgdDnaFwdHash, String pkgdDnaRevHash) {
        super(recordId, versionId, recordType, owner, ownerEmail, creator, creatorEmail,
                visibility, status, alias, keywords, shortDescription, longDescription, references,
                creationTime, modificationTime);
        this.packageFormat = packageFormat;
        this.pkgdDnaFwdHash = pkgdDnaFwdHash;
        this.pkgdDnaRevHash = pkgdDnaRevHash;
    }

    public String getPackageFormat() {
        return packageFormat;
    }

    public void setPackageFormat(String packageFormat) {
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
}
