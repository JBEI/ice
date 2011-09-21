package org.jbei.ice.shared;

import org.jbei.ice.shared.dto.EntryInfo;


public class Part extends EntryInfo {
    private static final long serialVersionUID = 1L;

    public enum AssemblyStandard {
        RAW, BIOBRICKA, BIOBRICKB;
    }

    private String pkgdDnaFwdHash;
    private String pkgdDnaRevHash;

    public Part() {
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
