package org.jbei.ice.client.entry.view.table;

import java.util.ArrayList;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;

public class EntrySequenceTable extends Composite {
    private final FlexTable table;
    private final boolean showAdminFeature = false; // TODO 

    public EntrySequenceTable() {
        table = new FlexTable();
        initWidget(table);
        table.setWidth("100%");
        table.setHTML(0, 0, "No Sequence Trace Files Available");
    }

    public void setData(ArrayList<SequenceAnalysisInfo> data) {
        table.clear();
        int row = 0;

        for (SequenceAnalysisInfo datum : data) {
            addFileName(row, datum.getName(), datum.getFileId());
            addDepositor(row, datum);

            if (showAdminFeature) { // TODO : then add delete link
            }

            // add/edit
            row += 1;
        }
    }

    private void addFileName(int row, String fileName, String fileId) {
        String url = GWT.getHostPageBaseURL() + "download?type=sequence&id=" + fileId;

        Anchor anchor = new Anchor(fileName, url);
        table.setWidget(row, 0, anchor);
    }

    private void addDepositor(int row, SequenceAnalysisInfo value) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<span>");
        sb.appendEscaped(DateUtilities.formatDate(value.getCreated()));
        sb.appendHtmlConstant("</span><br /><span>");

        if (value.getDepositor() == null) {
            sb.appendHtmlConstant("</span>");
        } else {
            String name = value.getDepositor().getFirstName() + " "
                    + value.getDepositor().getLastName();
            String email = value.getDepositor().getEmail();
            if (value.getDepositor().getFirstName() == null || name.trim().isEmpty())
                name = email;
            Hyperlink link = new Hyperlink(name, Page.PROFILE.getLink() + ";id=" + email);
            link.setStyleName("display-inline");
            sb.appendHtmlConstant("by " + link.toString() + "</span>");
        }

        table.setHTML(row, 1, sb.toSafeHtml().asString());
    }
}
