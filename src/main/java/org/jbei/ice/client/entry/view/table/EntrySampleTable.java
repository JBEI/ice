package org.jbei.ice.client.entry.view.table;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class EntrySampleTable extends Composite {

    private final FlexTable table;
    private int row;

    public EntrySampleTable() {
        table = new FlexTable();
        initWidget(table);

        table.setWidth("100%");
    }

    public void setData(ArrayList<SampleStorage> data) {
        table.clear();
        row = 0;

        for (SampleStorage datum : data) {
            addLabelCol(row, datum.getSample());
            addLocationCol(row, datum.getStorageList());
            addDepositor(row, datum.getSample());

            // add/edit
            row += 1;
        }
    }

    private void addDepositor(int row, SampleInfo sampleInfo) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<span>");
        sb.appendEscaped(DateUtilities.formatDate(sampleInfo.getCreationTime()));

        Hyperlink link = new Hyperlink(sampleInfo.getDepositor(), Page.PROFILE.getLink() + ";id="
                + sampleInfo.getDepositor());

        sb.appendHtmlConstant("</span><br /><span>");
        sb.appendHtmlConstant("by " + link.getElement().getInnerHTML());
        sb.appendHtmlConstant("</span>");
        table.setHTML(row, 2, sb.toSafeHtml().asString());
        table.getFlexCellFormatter().setVerticalAlignment(row, 2, HasAlignment.ALIGN_TOP);
    }

    private void addLabelCol(int row, SampleInfo sampleInfo) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<span class=\"font-bold\">");
        sb.appendEscaped(sampleInfo.getLabel());
        sb.appendHtmlConstant("</span><br><span style=\"color: #999\" class=\"font-85em\">");
        sb.appendEscaped(sampleInfo.getNotes() == null ? "" : sampleInfo.getNotes());
        sb.appendHtmlConstant("</span>");
        table.setHTML(row, 0, sb.toSafeHtml().asString());
        table.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
    }

    private void addLocationCol(int row, LinkedList<StorageInfo> list) {
        Tree tree = new Tree();
        addTreeHandler(tree);
        Hyperlink rootLink = new Hyperlink(list.get(0).getDisplay(), Page.STORAGE.getLink()
                + ";id=" + list.get(0).getId());
        TreeItem root = new TreeItem(rootLink);
        tree.addItem(root);
        TreeItem tmp;

        if (list.size() > 1) {
            for (int i = 1; i < list.size(); i += 1) {
                StorageInfo info = list.get(i);
                Hyperlink infoLink = new Hyperlink(info.getDisplay(), Page.STORAGE.getLink()
                        + ";id=" + info.getId());
                tmp = new TreeItem(infoLink);
                root.addItem(tmp);
                root = tmp;
            }
        }
        table.setWidget(row, 1, tree);
        table.getFlexCellFormatter().setWidth(row, 1, "300px");
    }

    private void addTreeHandler(Tree tree) {
        tree.addOpenHandler(new OpenHandler<TreeItem>() {

            @Override
            public void onOpen(OpenEvent<TreeItem> event) {
                TreeItem item = event.getTarget();

                // open everything underneath
                item = item.getChild(0);
                while (item != null) {
                    item.setState(true, false);
                    item = item.getChild(0);
                }
            }
        });

        tree.addCloseHandler(new CloseHandler<TreeItem>() {

            @Override
            public void onClose(CloseEvent<TreeItem> event) {
                // close everything below it
                TreeItem item = event.getTarget();

                item = item.getChild(0);
                while (item != null) {
                    item.setState(false, false);
                    item = item.getChild(0);
                }
            }
        });
    }
}
