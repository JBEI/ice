package org.jbei.ice.shared.dto.entry;

public class PartInfo extends EntryInfo {

    private static final long serialVersionUID = 1L;
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
