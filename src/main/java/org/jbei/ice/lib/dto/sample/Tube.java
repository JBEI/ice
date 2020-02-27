package org.jbei.ice.lib.dto.sample;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * String pair POJO for barcode and partId
 *
 * @author Hector Plahar
 */
public class Tube implements IDataTransferModel {

    private String barcode;
    private String partId;
    private boolean barcodeAvailable;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public boolean isBarcodeAvailable() {
        return barcodeAvailable;
    }

    public void setBarcodeAvailable(boolean barcodeAvailable) {
        this.barcodeAvailable = barcodeAvailable;
    }
}
