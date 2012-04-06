package org.jbei.ice.client.entry.view.detail;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Dialog box for using vector editor
 * 
 * @author Hector Plahar
 */
public class VectorEditorDialog extends DialogBox {

    private FlowPanel container, controls;
    private Anchor close;
    private final Label label;

    public VectorEditorDialog(String title) {

        setText(title);
        setAnimationEnabled(true);
        setGlassEnabled(true);

        label = new Label();
        label.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                center();
            }
        });
        container = new FlowPanel();
        addWindowListener();

        container.addStyleName("dialogContainer");
        container.setWidth((Window.getClientWidth() - 50) + "px");
        container.setHeight((Window.getClientHeight() - 50) + "px");

        close = new Anchor();
        close.setStyleName("x");
        close.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onCloseClick(event);
            }
        });

        controls = new FlowPanel();
        controls.setStyleName("dialogControls");
        controls.add(close);
    }

    public Label getLabel(String text) {
        label.setText(text);
        return label;
    }

    private void addWindowListener() {
        Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent event) {
                int height = event.getHeight();
                int width = event.getWidth();
                container.setWidth((width - 30) + "px");
                container.setHeight((height - 30) + "px");
            }
        });
    }

    /**
     * Called when the close icon is clicked. The default
     * implementation hides dialog box.
     */
    protected void onCloseClick(ClickEvent event) {
        hide();
    }

    @Override
    public void setWidget(Widget widget) {
        if (container.getWidgetCount() == 0) {
            // setup
            container.add(controls);
            super.setWidget(container);
        } else {
            // remove the old one
            while (container.getWidgetCount() > 1) {
                container.remove(1);
            }
        }

        // add the new widget
        container.add(widget);
    }

    public void setCloseIconVisible(boolean visible) {
        close.setVisible(visible);
    }

    /**
     * Returns the FlowPanel that contains the controls. More controls
     * can be added directly to this.
     */
    public FlowPanel getControlPanel() {
        return controls;
    }
}
