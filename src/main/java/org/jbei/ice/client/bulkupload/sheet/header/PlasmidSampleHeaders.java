package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;

/**
 * @author Hector Plahar
 */
public class PlasmidSampleHeaders extends SampleHeaders {

    public PlasmidSampleHeaders(ArrayList<String> locationList) {
        super(locationList, null);
    }

    @Override
    public SheetCellData extractValue(EntryField headerType, EntryInfo info) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
