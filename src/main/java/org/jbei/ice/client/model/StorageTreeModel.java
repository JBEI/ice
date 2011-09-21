package org.jbei.ice.client.model;

import java.util.ArrayList;

import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;

public class StorageTreeModel implements TreeViewModel {

    private final ArrayList<StorageInfo> root;

    public StorageTreeModel(ArrayList<StorageInfo> root) {
        this.root = root;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            // root only
            Cell<StorageInfo> cell = new AbstractCell<StorageInfo>() {

                @Override
                public void render(com.google.gwt.cell.client.Cell.Context context,
                        StorageInfo value, SafeHtmlBuilder sb) {
                    sb.appendEscaped(value.getDisplay());
                }
            };

            ListDataProvider<StorageInfo> provider = new ListDataProvider<StorageInfo>(this.root);
            return new DefaultNodeInfo<StorageInfo>(provider, cell);

        } else {
            if (value instanceof StorageInfo) {

            } else {
                Window.alert("Unknown type");
            }
            //            // FAKE
            //            /*
            //             * Create some data in a data provider. Use the parent value as a prefix
            //             * for the next level.
            //             */
            //            ListDataProvider<String> dataProvider = new ListDataProvider<String>();
            //            for (int i = 0; i < 2; i++) {
            //                dataProvider.getList().add(value + "." + String.valueOf(i));
            //            }
            //
            //            // Return a node info that pairs the data with a cell.
            //            return new DefaultNodeInfo<String>(dataProvider, new TextCell());
            //        }
            return null;
        }
    }

    @Override
    public boolean isLeaf(Object value) {
        if (value == null || !(value instanceof StorageInfo))
            return false;

        StorageInfo info = (StorageInfo) value;
        return (info.getChildCount() == 0);

    }
}
