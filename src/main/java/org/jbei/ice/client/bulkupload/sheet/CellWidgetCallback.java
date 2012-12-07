package org.jbei.ice.client.bulkupload.sheet;

/**
 * @author Hector Plahar
 */
public interface CellWidgetCallback {

    void onMouseDown(int row, int col);

    void onMouseUp(int row, int col);

    void onMouseOver(int row, int col);
}
