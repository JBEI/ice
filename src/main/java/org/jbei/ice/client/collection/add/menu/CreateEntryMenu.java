package org.jbei.ice.client.collection.add.menu;

import java.util.Arrays;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Menu widget for creating new entry
 *
 * @author Hector Plahar
 */

public class CreateEntryMenu implements IsWidget {

    interface Style extends CellList.Style {

        String subMenuCreateNew();
    }

    interface ExportAsResource extends CellList.Resources {

        static ExportAsResource INSTANCE = GWT.create(ExportAsResource.class);

        @Source("org/jbei/ice/client/resource/css/CreateEntry.css")
        Style cellListStyle();
    }

    private final Button createEntry;
    private final SingleSelectionModel<EntryAddType> optionSelection;

    public CreateEntryMenu(String label) {
        ExportAsResource.INSTANCE.cellListStyle().ensureInjected();
        label += " <i class=\"" + FAIconType.CARET_DOWN.getStyleName() + "\"></i>";
        createEntry = new Button(label);
        createEntry.setStyleName(ExportAsResource.INSTANCE.cellListStyle().subMenuCreateNew());

        // renderer for options list
        CellList<EntryAddType> options = new CellList<EntryAddType>(new AbstractCell<EntryAddType>() {

            @Override
            public void render(Context context, EntryAddType type, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span>" + type.toString() + "</span>");
            }
        }, ExportAsResource.INSTANCE);

        options.setRowData(Arrays.asList(EntryAddType.values()));
        final PopupHandler clickHandler = new PopupHandler(options, createEntry.getElement(), false);

        createEntry.addClickHandler(clickHandler);
        optionSelection = new SingleSelectionModel<EntryAddType>();
        optionSelection.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                clickHandler.hidePopup();
            }
        });

        options.setSelectionModel(optionSelection);
    }

    public SingleSelectionModel<EntryAddType> getSelectionModel() {
        return this.optionSelection;
    }

    @Override
    public Widget asWidget() {
        return createEntry;
    }
}
