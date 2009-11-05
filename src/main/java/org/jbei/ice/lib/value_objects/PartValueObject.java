package org.jbei.ice.lib.value_objects;

public interface PartValueObject extends EntryValueObject {

	public abstract String getPackageFormat();

	public abstract void setPackageFormat(String packageFormat);

	public abstract String getPkgdDnaFwdHash();

	public abstract void setPkgdDnaFwdHash(String pkgdDnaFwdHash);

	public abstract String getPkgdDnaRevHash();

	public abstract void setPkgdDnaRevHash(String pkgdDnaRevHash);

}