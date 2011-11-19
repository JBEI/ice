package org.jbei.ice.client.entry.view.detail;

import org.jbei.ice.shared.dto.PartInfo;

public class PartDetailView extends EntryDetailView<PartInfo> {

    public PartDetailView(PartInfo partInfo) {
        super(partInfo);
    }

    @Override
    protected void addShortFieldValues() {
        addShortField("Packaging Format", info.getPackageFormat(), ValueType.SHORT_TEXT);
    }

    @Override
    protected void addLongFields() {
        // TODO Auto-generated method stub

    }
}
