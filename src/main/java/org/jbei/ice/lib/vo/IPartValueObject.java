package org.jbei.ice.lib.vo;

public interface IPartValueObject extends IEntryValueObject {

    public abstract String getPackageFormat();

    public abstract void setPackageFormat(String packageFormat);

    public abstract String getPkgdDnaFwdHash();

    public abstract void setPkgdDnaFwdHash(String pkgdDnaFwdHash);

    public abstract String getPkgdDnaRevHash();

    public abstract void setPkgdDnaRevHash(String pkgdDnaRevHash);

}