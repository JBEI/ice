package org.jbei.ice.client.entry.display.panel.sample;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.entry.display.model.SampleStorage;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.lib.shared.dto.PartSample;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * @author Hector Plahar
 */
public class Storage96WellPanel extends Composite {

    public Storage96WellPanel(SampleStorage storage, String wellName, String tubeName, String plateName) {

        FlexTable panel = new FlexTable();
        panel.setCellPadding(2);
        panel.setCellSpacing(2);
        panel.setStyleName("entry_sample_panel");
        initWidget(panel);

        PartSample part = storage.getPartSample();

        panel.setHTML(0, 0, "<b> PLATE " + plateName + "</b>");
        panel.getFlexCellFormatter().setColSpan(0, 0, 12);
        panel.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_CENTER);
        panel.getCellFormatter().setStyleName(0, 0, "bg_cc");

        for (int i = 0; i <= 96; i += 1) {
            char c = (char) (65 + (i / 12));
            int row = (i / 12) + 1;
            int col = i % 12;

            String colStr = (col + 1) + "";
            colStr = colStr.length() == 1 ? ("0" + colStr) : colStr;
            String currentWell = Character.valueOf(c).toString() + colStr;
            String label = Character.valueOf(c).toString()
                    + "<b style=\"vertical-align: sub; font-size: 7px\">"
                    + NumberFormat.getFormat("##").format(col + 1)
                    + "</b>";
            panel.setHTML(row, col, label);

            if (wellName.equalsIgnoreCase(currentWell)) {
                panel.getFlexCellFormatter().setStyleName(row, col, "border-1-selected");
                panel.getCellFormatter().getElement(row, col).setTitle(tubeName);
                if (tubeName.isEmpty())
                    panel.getFlexCellFormatter().addStyleName(row, col, "bg_coral");
                else
                    panel.getFlexCellFormatter().addStyleName(row, col, "bg_green");
            } else {
                panel.getFlexCellFormatter().setStyleName(row, col, "border-1");
            }
        }

        String html = "<b>" + part.getLabel() + "</b> - " + part.getNotes();
        SafeHtmlBuilder sb = addDepositor(part);
        html += ("<br>" + sb.toSafeHtml().asString());

        panel.setHTML(9, 0, html);
        panel.getFlexCellFormatter().setColSpan(9, 0, 12);
        panel.getFlexCellFormatter().setHorizontalAlignment(9, 0, HasAlignment.ALIGN_CENTER);
        panel.getCellFormatter().setStyleName(9, 0, "bg_cc");
    }

    private SafeHtmlBuilder addDepositor(PartSample partSample) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendEscaped(DateUtilities.formatDate(partSample.getCreationTime()));

        Hyperlink link = new Hyperlink(partSample.getDepositor(), Page.PROFILE.getLink() + ";id="
                + partSample.getDepositor());

        sb.appendHtmlConstant(" by " + link.getElement().getInnerHTML());
        return sb;
    }
}
