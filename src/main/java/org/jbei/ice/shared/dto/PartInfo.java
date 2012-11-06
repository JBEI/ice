package org.jbei.ice.shared.dto;

public class PartInfo extends EntryInfo {

    private String packageFormat;

    public PartInfo() {
        super(EntryType.PART);
    }

    public String getPackageFormat() {
        return packageFormat;
    }

    public void setPackageFormat(String packageFormat) {
        this.packageFormat = packageFormat;
    }
}
