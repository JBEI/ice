package org.jbei.ice.lib.models.interfaces;

import org.jbei.ice.lib.models.Part.AssemblyStandard;

public interface IPartValueObject extends IEntryValueObject {
    AssemblyStandard getPackageFormat();

    void setPackageFormat(AssemblyStandard packageFormat);

    String getPkgdDnaFwdHash();

    void setPkgdDnaFwdHash(String pkgdDnaFwdHash);

    String getPkgdDnaRevHash();

    void setPkgdDnaRevHash(String pkgdDnaRevHash);
}