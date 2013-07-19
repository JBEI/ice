package org.jbei.ice.client.entry.display.view;

import org.jbei.ice.lib.shared.dto.entry.Visibility;

import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

/**
 * @author Hector Plahar
 */
public class VisibilityWidgetPresenter {

    public interface IVisibilityView {

        void setWidgetVisibility(boolean visible);

        void setLabel(String text);

        void setTooltipMouseOverHandler(MouseOverHandler handler);

        void setTooltip(String text);
    }

    private final IVisibilityView view;
    private Visibility visibility;

    public VisibilityWidgetPresenter(IVisibilityView view) {
        this.view = view;
        this.view.setWidgetVisibility(false);
        this.view.setTooltipMouseOverHandler(new VisibilityHoverHandler());
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;

        switch (visibility) {
            case OK:
                view.setWidgetVisibility(false);
                break;

            case PENDING:
                view.setLabel("Pending");
                view.setWidgetVisibility(true);
                break;

            case DRAFT:
                view.setLabel("Draft");
                view.setWidgetVisibility(true);
                break;
        }
    }

    private class VisibilityHoverHandler implements MouseOverHandler {

        @Override
        public void onMouseOver(MouseOverEvent event) {
            if (visibility == null)
                return;

            switch (visibility) {
                case PENDING:
                    view.setTooltip("This entry is part of a submitted bulk import awaiting approval");
                    break;

                case DRAFT:
                    view.setTooltip("This entry is part of bulk import draft");
                    break;
            }
        }
    }
}
