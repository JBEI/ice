package org.jbei.ice.client.common.search;

import java.util.Arrays;
import java.util.List;

import org.jbei.ice.client.common.FilterWidget;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class SearchFilterWidget extends FilterWidget {

    private final ListBox list;
    private final TextBox box;
    private final boolean showOperator;

    public SearchFilterWidget(SearchFilterType type, boolean showOperator) {
        super(type);
        this.showOperator = showOperator;

        box = new TextBox();
        box.setStyleName("input_box");
        list = new ListBox();

        String listId = "_" + type.getShortName();
        String boxId = "_" + type.getShortName();
        HTMLPanel layout = new HTMLPanel("<span id=\"" + listId + "\"></span><span id=\"" + boxId + "\"></span>");

        if (showOperator) {
            for (QueryOperator operator : getOperatorList()) {
                list.addItem(operator.operator(), operator.symbol());
            }
            layout.add(list, listId);
        }

        layout.add(box, boxId);
        initWidget(layout);
    }

    public void setInputPlaceholder(String value) {
        box.getElement().setAttribute("placeholder", value);
    }

    @Override
    public void setWidth(String width) {
        this.box.setVisibleLength(32);
        this.box.setMaxLength(32);
        this.box.setWidth(width);
    }

    @Override
    public QueryOperator getSelectedOperator() {
        int indx = this.list.getSelectedIndex();
        return getOperatorList().get(indx);
    }

    @Override
    public List<QueryOperator> getOperatorList() {
        return Arrays.asList(QueryOperator.CONTAINS, QueryOperator.DOES_NOT_CONTAIN, QueryOperator.BEGINS_WITH,
                             QueryOperator.ENDS_WITH, QueryOperator.IS_NOT, QueryOperator.IS);
    }

    @Override
    public String getSelectedOperand() {
        return this.box.getText();
    }

    public boolean isShowOperator() {
        return showOperator;
    }
}
