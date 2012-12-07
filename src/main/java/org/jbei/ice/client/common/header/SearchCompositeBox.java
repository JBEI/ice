package org.jbei.ice.client.common.header;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Composite box for quick search.
 * Combines a text box and a holder for search widgets
 *
 * @author Hector Plahar
 */
public class SearchCompositeBox extends Composite {

    private final TextBox box;
    private final HTMLPanel imagePanel;
    private final Icon pullDownIcon;
    private boolean open;

    public SearchCompositeBox() {
        Grid grid = new Grid(1, 3);
        grid.setCellPadding(0);
        grid.setCellSpacing(0);
        grid.setStyleName("quick_search");
        initWidget(grid);

        FlexTable widgetHolder = new FlexTable();
        widgetHolder.setCellPadding(4);
        widgetHolder.setCellSpacing(0);

        // widget holder
        grid.setWidget(0, 0, widgetHolder);

        // text box
        box = new TextBox();
        box.setWidth("370px");
        box.setStyleName("quick_search_input");
        box.getElement().setAttribute("placeHolder", "Enter search term(s) and/or use the drop down menu");
        grid.setWidget(0, 1, box);

        // search arrow
        pullDownIcon = new Icon(FAIconType.CARET_DOWN);
        imagePanel = new HTMLPanel("<span style=\"position: relative; left: 6px; top: 6px; font-size: 0.9em\"" +
                                           "id=\"search_arrow\"></span>");
        imagePanel.setStyleName("quick_search_arrow_panel");
        imagePanel.add(pullDownIcon, "search_arrow");
        grid.setWidget(0, 2, imagePanel);
        grid.getCellFormatter().setStyleName(0, 2, "search_arrow_td");
    }

    public Element getPullDownAreaElement() {
        return imagePanel.getElement();
    }

    public Element getTextBoxElement() {
        return box.getElement();
    }

    public void addTextBoxKeyDownHandler(KeyDownHandler keyDownHandler) {
        box.addKeyDownHandler(keyDownHandler);
    }

    public void setPullDownClickHandler(final ClickHandler handler) {
        imagePanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (open) {
                    pullDownIcon.setType(FAIconType.CARET_DOWN);
                } else
                    pullDownIcon.setType(FAIconType.CARET_UP);
                open = !open;
                handler.onClick(event);
            }
        }, ClickEvent.getType());
    }

    public CloseHandler<PopupPanel> getCloseHandler() {
        return new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                open = false;
                pullDownIcon.setType(FAIconType.CARET_DOWN);
            }
        };
    }

    public void reset() {
        box.setText("");
        imagePanel.setStyleName("quick_search_arrow_panel");
    }

    public String getQueryString() {
        return box.getText().trim();
    }

    public void setSearch(String box) {
        this.box.setText(box);
    }

    public void advancedWidgetClosed() {
        open = false;
        pullDownIcon.setType(FAIconType.CARET_DOWN);
    }
}
