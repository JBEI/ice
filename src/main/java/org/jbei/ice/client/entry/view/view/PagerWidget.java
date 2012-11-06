package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

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
        goBack = new Icon(FAIconType.SHARE_ALT); //ImageUtil.getPrevIcon();
        goBack.setTitle("Back");
        goBack.addStyleName("cursor_pointer");

        leftBtn = new Icon(FAIconType.CHEVRON_LEFT);
        leftBtn.removeStyleName("font-awesome");
        leftBtn.addStyleName("nav-left");

        rightBtn = new Icon(FAIconType.CHEVRON_RIGHT);
        rightBtn.removeStyleName("font-awesome");
        rightBtn.addStyleName("nav-right");

        navText = new Label();
        navText.setStyleName("display-inline");
        navText.addStyleName("font-80em");
        navText.addStyleName("pad-6");

        HTMLPanel panel = new HTMLPanel(
                "<span id=\"leftBtn\"></span> <span id=\"navText\" class=\"font-bold\"></span><span " +
                        "id=\"rightBtn\"></span>");
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
            leftBtn.addStyleName("nav");
        } else {
            leftBtn.removeStyleName("nav");
            leftBtn.addStyleName("nav_disabled");
        }
    }

    public void enableNext(boolean enabled) {
        if (enabled) {
            rightBtn.removeStyleName("nav_disabled");
            rightBtn.addStyleName("nav");
        } else {
            rightBtn.removeStyleName("nav");
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
