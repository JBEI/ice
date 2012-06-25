package org.jbei.ice.client.entry.view.view;

import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import static org.jbei.ice.client.entry.view.view.VisibilityWidgetPresenter.IVisibilityView;

/**
 * @author Hector Plahar
 */
public class VisibilityWidget extends Composite implements IVisibilityView {

    private final FlexTable layout;
    private final HTML tooltip;
    private final Label label;
    private final VisibilityWidgetPresenter presenter;
    private HandlerRegistration registration;

    public VisibilityWidget() {

        tooltip = new HTML("<span style=\"position: relative; top: -2px; left: -3px; float: right; font-size: 11px; "
                                   + "border-radius: 1em 1em 1em 1em; color: #333; padding: 0 5px; "
                                   + "font-weight: bold; background-color: #f8f8f0; cursor: pointer\">?</span>");
        label = new Label();

        layout = new FlexTable();
        layout.setStyleName("entry_pending_visibility");
        layout.setWidget(0, 0, label);
        layout.setWidget(0, 1, tooltip);

        initWidget(layout);

        presenter = new VisibilityWidgetPresenter(this);
    }

    public VisibilityWidgetPresenter getPresenter() {
        return this.presenter;
    }

    @Override
    public void setWidgetVisibility(boolean visible) {
        this.setVisible(visible);
    }

    @Override
    public void setLabel(String text) {
        this.label.setText(text);
    }

    @Override
    public void setTooltipMouseOverHandler(MouseOverHandler handler) {
        if (registration != null)
            registration.removeHandler();
        registration = tooltip.addMouseOverHandler(handler);
    }

    @Override
    public void setTooltip(String text) {
        tooltip.setTitle(text);
    }
}
