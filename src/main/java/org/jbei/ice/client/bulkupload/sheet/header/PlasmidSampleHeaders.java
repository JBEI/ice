package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.shared.dto.EntryInfo;

/**
 * @author Hector Plahar
 */
public class PlasmidSampleHeaders extends SampleHeaders {

    public PlasmidSampleHeaders(ArrayList<String> locationList) {
        super(locationList);
    }

    @Override
    public SheetCellData extractValue(Header headerType, EntryInfo info) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
