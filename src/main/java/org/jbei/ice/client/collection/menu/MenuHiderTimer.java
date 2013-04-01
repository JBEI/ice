package org.jbei.ice.client.collection.menu;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * Timer used to hide a deleted folder menu
 *
 * @author hplahar
 */
public class MenuHiderTimer extends Timer {

    private final FlexTable table;
    private final int row;

    public MenuHiderTimer(FlexTable table, int row) {
        this.table = table;
        this.row = row;
    }

    @Override
    public void run() {
        if (table == null || table.getRowCount() <= row)
            return;

        table.removeRow(row);
    }
}
