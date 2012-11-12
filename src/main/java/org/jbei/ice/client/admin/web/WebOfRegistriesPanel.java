package org.jbei.ice.client.admin.web;

import java.util.HashMap;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.shared.dto.ConfigurationKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Hector Plahar
 */
public class WebOfRegistriesPanel extends Composite implements AdminPanel {

    private ServiceDelegate<String> addPartnerDelegate;
    private HTMLPanel addPartnerPanel;
    private final FlexTable layout;

    public WebOfRegistriesPanel() {
        layout = new FlexTable();
        initWidget(layout);
    }

    public void setAddPartnerDelegate(ServiceDelegate<String> partnerDelegate) {
        this.addPartnerDelegate = partnerDelegate;
    }

    public void setData(HashMap<String, String> settings) {
        layout.clear();
        int row = 0;

        layout.setWidget(row, 0, createRegistryPartnerPanel(settings));
        layout.getFlexCellFormatter().setColSpan(row, 0, 3);
        layout.getFlexCellFormatter().setStyleName(row, 0, "pad_top");
    }

    private Widget createRegistryPartnerPanel(HashMap<String, String> settings) {

        HTMLPanel headerPanel = new HTMLPanel(
                "<span style=\"color: #233559; "
                        + "font-weight: bold; font-style: italic; font-size: 0.80em; text-transform: uppercase\">"
                        + ConfigurationKey.WEB_PARTNERS.toString()
                        + "</span><div style=\"float: right\"><span id=\"add_partner\"></span></div>");

        headerPanel.setStyleName("entry_sequence_sub_header");

        addPartnerPanel = new HTMLPanel("<span id=\"add_partner_input\"></span><span id=\"add_partner_submit\"></span>"
                                                + "<span id=\"add_partner_cancel\"></span>");
        final TextBox addInput = new TextBox();
        addInput.getElement().setAttribute("placeholder", "Enter partner url");
        addInput.setStyleName("input_box");
        addPartnerPanel.add(addInput, "add_partner_input");
        Button addPartnerSubmit = new Button("<i class=\"" + FAIconType.OK.getStyleName() + "\"></i>");
        addPartnerSubmit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String text = addInput.getText().trim();

                if (text.isEmpty()) {
                    addInput.setStyleName("input_box_error");
                    return;
                }

                if (addPartnerDelegate == null)
                    return;

                addPartnerDelegate.execute(text);
                addInput.setStyleName("input_box");
                addInput.setText("");
            }
        });

        addPartnerPanel.add(addPartnerSubmit, "add_partner_submit");
        Button addPartnerCancel = new Button("<i class=\"" + FAIconType.REMOVE.getStyleName() + "\"></i>");
        addPartnerCancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addPartnerPanel.setVisible(false);
            }
        });
        addPartnerPanel.add(addPartnerCancel, "add_partner_cancel");

        HTML addPartnerLabel = new HTML("<i class=\"" + FAIconType.PLUS.getStyleName() + "\"></i> Add");
        addPartnerLabel.setStyleName("open_sequence_sub_link");
        headerPanel.add(addPartnerLabel, "add_partner");
        addPartnerLabel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                addPartnerPanel.setVisible(true);
            }
        });

        FlexTable table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidget(0, 0, headerPanel);
        table.setWidget(1, 0, addPartnerPanel);
        addPartnerPanel.setVisible(false);

        // display partners
        String partners = settings.remove(ConfigurationKey.WEB_PARTNERS.name());
        if (partners == null) {
            table.setHTML(2, 0, "No partners added");
        } else {
            int row = 2;
            for (String partner : partners.split(",")) {
                table.setHTML(row, 0, "<span style=\"margin-left: 10px; padding: 3px;\" class=\"font-75em\">"
                        + partner + "</span>");
                row += 1;
            }
        }
        return table;
    }
}
