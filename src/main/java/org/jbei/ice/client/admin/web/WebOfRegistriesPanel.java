package org.jbei.ice.client.admin.web;

import java.util.ArrayList;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.web.RegistryPartner;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * View panel for display ui elements and interaction with the Web of registries admin section
 *
 * @author Hector Plahar
 */
public class WebOfRegistriesPanel extends Composite implements IAdminPanel {

    private ServiceDelegate<String> addPartnerDelegate;
    private HTMLPanel addPartnerPanel;
    private final FlexTable layout;
    private FlexTable partnerPanel;
    private Button joinToggle;
    private boolean toggled;
    private ArrayList<String> partnersList;
    private PartnerTable partnerTable;

    public WebOfRegistriesPanel(ServiceDelegate<String> partnerDelegate) {
        layout = new FlexTable();
        layout.setWidth("600px");
        layout.setCellPadding(1);
        layout.setCellSpacing(0);
        layout.setStyleName("pad_top");
        initWidget(layout);

        joinToggle = new Button("<i class=\"blue " + FAIconType.GLOBE.getStyleName() + "\"></i>" +
                                        "<span class=\"font-85em\">Enable</span>");
        joinToggle.setStyleName("sub-button");

        this.addPartnerDelegate = partnerDelegate;
        this.partnersList = new ArrayList<String>();
    }

    public void setData(WebOfRegistries settings, ServiceDelegate<RegistryPartner> partnerStatusDelegate) {
        layout.clear();
        layout.setWidget(0, 0, joinToggle);

        partnerPanel = createRegistryPartnerPanel(settings.getPartners(), partnerStatusDelegate);
        toggled = settings.isWebEnabled();
        toggle();
        partnerPanel.setVisible(settings.isWebEnabled());
        layout.setWidget(1, 0, partnerPanel);
    }

    public void updateRow(RegistryPartner partner) {
        partnerTable.updateRow(partner);
    }

    public void addJoinBoxHandler(final ClickHandler handler) {
        joinToggle.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                toggled = !toggled;
                partnerPanel.setVisible(toggled);
                toggle();
                handler.onClick(event);
            }
        });
    }

    private void toggle() {
        if (toggled) {
            joinToggle.setHTML("<i class=\"red " + FAIconType.GLOBE.getStyleName() + "\"></i> "
                                       + "<span class=\"font-90em\">Disable</span>");
        } else {
            joinToggle.setHTML("<i class=\"green " + FAIconType.GLOBE.getStyleName() + "\"></i> "
                                       + "<span class=\"font-90em\">Enable</span>");
        }
    }

    public boolean isToggled() {
        return toggled;
    }

    public void addPartner(String partner) {
        if (partnersList.isEmpty()) {
            partnerPanel.setHTML(2, 0, "<span style=\"margin-left: 10px; padding: 3px;\" class=\"font-75em\">"
                    + partner + "</span>");
        } else {
            int row = partnerPanel.getRowCount();
            partnerPanel.setHTML(row, 0, "<span style=\"margin-left: 10px; padding: 3px;\" class=\"font-75em\">"
                    + partner + "</span>");
        }
        partnersList.add(partner);
    }

    private FlexTable createRegistryPartnerPanel(ArrayList<RegistryPartner> partners,
            ServiceDelegate<RegistryPartner> partnerStatusDelegate) {
        HTMLPanel headerPanel = new HTMLPanel(
                "<span style=\"color: #233559; "
                        + "font-weight: bold; font-style: italic; font-size: 0.80em; text-transform: uppercase\">"
                        + "Registry Partners</span><span style=\"margin-left: 20px\" id=\"add_partner\"></span>");

        headerPanel.setStyleName("entry_sequence_sub_header");

        addPartnerPanel = new HTMLPanel("<span id=\"add_partner_input\"></span><span id=\"add_partner_submit\"></span>"
                                                + "<span id=\"add_partner_cancel\"></span>");
        partnerTable = new PartnerTable(partnerStatusDelegate);
        final TextBox addInput = new TextBox();
        addInput.getElement().setAttribute("placeholder", "e.g. public-registry.jbei.org");
        addInput.setStyleName("input_box");
        addInput.setWidth("200px");

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

                addPartnerDelegate.execute(text);
                addInput.setStyleName("input_box");
                addInput.setText("");
                addPartnerPanel.setVisible(false);
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
//        HTML addPartnerLabel = new HTML("<i class=\"" + FAIconType.GLOBE.getStyleName()
//                                                + "\"></i><i style=\"vertical-align: text-bottom; font-size: 7px\""
//                                                + "class=\"" + FAIconType.PLUS.getStyleName() + "\"></i>");
//        addPartnerLabel.setStyleName("display-inline");
//        headerPanel.add(addPartnerLabel, "add_partner");
//        addPartnerLabel.addClickHandler(new ClickHandler() {
//
//            @Override
//            public void onClick(ClickEvent event) {
//                addPartnerPanel.setVisible(true);
//            }
//        });

//        CellList<RemotePartner> list = new CellList<RemotePartner>(new AbstractCell<RemotePartner>() {
//            @Override
//            public void render(Context context, RemotePartner value, SafeHtmlBuilder sb) {
//            }
//        });
//
//        DataGrid<RemotePartner> grid = new DataGrid<RemotePartner>();
//        grid.setAutoHeaderRefreshDisabled(false);
//        grid.setEmptyTableWidget(new HTML("No data for you"));
//        grid.setTableBuilder(new AbstractCellTableBuilder<RemotePartner>() {
//            @Override
//            protected void buildRowImpl(RemotePartner rowValue, int absRowIndex) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//        });

        FlexTable table = new FlexTable();
        table.setWidth("100%");
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidget(0, 0, headerPanel);
        table.setWidget(1, 0, addPartnerPanel);
        table.setHTML(2, 0, "&nbsp;");
        table.setWidget(3, 0, partnerTable);

        addPartnerPanel.setVisible(false);
        partnerTable.setData(partners);

        return table;
    }
}
