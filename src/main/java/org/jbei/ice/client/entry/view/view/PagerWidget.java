package org.jbei.ice.client.entry.view.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;

/**
 * Widget for context navigation
 *
 * @author Hector Plahar
 */
public class PagerWidget extends Composite {

    private final Icon goBack;
    private final Icon leftBtn;
    private final Label navText;
    private final Icon rightBtn;

    public PagerWidget() {
        goBack = new Icon(FAIconType.CIRCLE_ARROW_LEFT);
        goBack.setTitle("Back");
        goBack.addStyleName("entry_go_back");

        leftBtn = new Icon(FAIconType.CHEVRON_SIGN_LEFT);
        leftBtn.removeStyleName("font-awesome");
        leftBtn.addStyleName("context_nav_pager");

        rightBtn = new Icon(FAIconType.CHEVRON_SIGN_RIGHT);
        rightBtn.removeStyleName("font-awesome");
        rightBtn.addStyleName("context_nav_pager");

        navText = new Label();
        navText.setStyleName("display-inline");
        navText.addStyleName("font-80em");
        navText.addStyleName("pad-6");

        HTMLPanel panel = new HTMLPanel("<span id=\"leftBtn\"></span>"
                + "<span id=\"navText\" style=\"position:relative; top: -3px\"></span>"
                + "<span id=\"rightBtn\"></span>");
        initWidget(panel);
        panel.setStyleName("pad-6");

        panel.add(leftBtn, "leftBtn");
        panel.add(navText, "navText");
        panel.add(rightBtn, "rightBtn");
    }

    public Icon getGoBack() {
        return this.goBack;
    }

    public void setNextHandler(ClickHandler handler) {
        rightBtn.addDomHandler(handler, ClickEvent.getType());
    }

    public void setGoBackHandler(ClickHandler handler) {
        goBack.addDomHandler(handler, ClickEvent.getType());
    }

    public void setPrevHandler(ClickHandler handler) {
        leftBtn.addClickHandler(handler);
    }

    public void enablePrev(boolean enabled) {
        if (enabled) {
            leftBtn.removeStyleName("nav_disabled");
        } else {
            leftBtn.addStyleName("nav_disabled");
        }
    }

    public void enableNext(boolean enabled) {
        if (enabled) {
            rightBtn.removeStyleName("nav_disabled");
        } else {
            rightBtn.addStyleName("nav_disabled");
        }
    }

    public void setNavText(String text) {
        this.navText.setText(text);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        goBack.setVisible(visible);
    }
}
