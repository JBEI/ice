package org.jbei.ice.client.bulkupload.sheet.header;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;

/**
 * @author Hector Plahar
 */
public abstract class SampleHeaders {

    protected ArrayList<CellColumnHeader> headers = new ArrayList<CellColumnHeader>();

    public SampleHeaders(ArrayList<String> locationList) {
        for (String location : locationList) {
            try {
                Header header = Header.valueOf("SAMPLE_" + location.toUpperCase());
                headers.add(new CellColumnHeader(header));
            } catch (IllegalArgumentException ila) {
                headers.clear();
                return;
            }
        }
    }

    public ArrayList<CellColumnHeader> getHeaders() {
        return headers;
    }

    public int getHeaderSize() {
        return this.headers.size();
    }
}
