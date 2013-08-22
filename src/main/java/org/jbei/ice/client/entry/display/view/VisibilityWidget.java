package org.jbei.ice.client.entry.display.view;

import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

import static org.jbei.ice.client.entry.display.view.VisibilityWidgetPresenter.IVisibilityView;

/**
 * @author Hector Plahar
 */
public class VisibilityWidget extends Composite implements IVisibilityView {

    private final FlexTable layout;
    private final Label tooltip;
    private final Label label;
    private final VisibilityWidgetPresenter presenter;
    private HandlerRegistration registration;

    public VisibilityWidget() {

        tooltip = new Label("?");
        tooltip.setStyleName("entry_pending_visibility_tooltip");
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
