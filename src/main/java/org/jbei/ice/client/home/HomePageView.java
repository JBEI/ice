package org.jbei.ice.client.home;

import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

public class HomePageView extends AbstractLayout implements IHomePageView {

    private FlexTable contentTable;

    @Override
    protected void initComponents() {
        super.initComponents();

        contentTable = new FlexTable();
    }

    @Override
    protected Widget createContents() {

        contentTable.setWidth("100%");
        contentTable.setHTML(0, 0,
            "<div style=\"padding: 10px; font-size: 0.95em;\"><b>Home</b><p style=\"line-height: 1.6\">"
                    + "The Joint BioEnergy Institute (JBEI) is a San Francisco Bay Area "
                    + "scientific partnership led by Lawrence Berkeley National Laboratory "
                    + "(Berkeley Lab) and including the Sandia National Laboratories "
                    + "(Sandia), the University of California (UC) campuses of Berkeley and "
                    + "Davis, the Carnegie Institution for Science, Lawrence Livermore "
                    + "National Laboratory (LLNL), and Pacific Northwest National Laboratory "
                    + "(PNNL). JBEI's primary scientific mission is to advance the "
                    + "development of the next generation of biofuels - drop-in liquid fuels "
                    + "derived from the solar energy stored in plant biomass. JBEI is one of "
                    + "three U.S. Department of Energy (DOE) Bioenergy Research Centers"
                    + "(BRCs).</div>");
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        return contentTable;
    }
}
