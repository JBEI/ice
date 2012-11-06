package org.jbei.ice.client.collection.menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * @author Hector Plahar
 */
public class QuickAddWidgetPresenter {

    public interface View {

        void addCancelHandler(ClickHandler handler);

        void setVisible(boolean visible);
    }

    private final View view;

    public QuickAddWidgetPresenter(final View view) {
        this.view = view;

        this.view.addCancelHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                view.setVisible(false);
            }
        });
    }
}
