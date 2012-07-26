package org.jbei.ice.client.bulkupload.sheet;

import org.jbei.ice.shared.dto.EntryInfo;

public class PartValueExtractor extends InfoValueExtractor {

    public String extractValue(Header header, EntryInfo info, int index) {
        return super.extractCommon(header, info, index);
    }
}
