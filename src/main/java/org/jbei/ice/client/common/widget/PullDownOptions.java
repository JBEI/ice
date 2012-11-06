package org.jbei.ice.client.common.widget;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * @author Hector Plahar
 */
public abstract class PullDownOptions<T> extends Composite {

    private final CellList<T> options;
    private final PopupHandler popupHandler;
    protected final SingleSelectionModel<T> optionSelection;

    interface PullDownListResource extends CellList.Resources {

        static PullDownListResource INSTANCE = GWT.create(PullDownListResource.class);

        @Source("org/jbei/ice/client/resource/css/PullDownOptions.css")
        CellList.Style cellListStyle();
    }

    public PullDownOptions(String labelString) {
        PullDownListResource.INSTANCE.cellListStyle().ensureInjected();

        options = new CellList<T>(new AbstractCell<T>() {

            @Override
            public void render(Context context, T value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant(renderCell(value));
            }
        }, PullDownListResource.INSTANCE);
        options.setWidth("110px");

        HTMLPanel panel = new HTMLPanel("<span id=\"header_user_label\"> <span id=\"header_caret\"></span></span>");
        panel.setStyleName("pull_down_as_link");

        Icon caret = new Icon(FAIconType.CARET_DOWN);
        caret.addStyleName("font-75em");

        HTML label = new HTML(labelString);
        panel.add(label, "header_user_label");
        panel.add(caret, "header_caret");
        initWidget(panel);

        addClickHandler(caret);
        optionSelection = new SingleSelectionModel<T>();
        options.setSelectionModel(optionSelection);

        popupHandler = new PopupHandler(options, label.getElement(), false);
    }

    public SingleSelectionModel<T> getSelectionModel() {
        return this.optionSelection;
    }

    protected void addClickHandler(HasClickHandlers label) {
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popupHandler.onClick(event);
                event.stopPropagation();
            }
        });
    }

    public void setOptions(List<T> values) {
        options.setRowData(values);
    }

    protected abstract String renderCell(T value);
}
