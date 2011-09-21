package org.jbei.ice.client.view;

import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.Header;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.presenter.BulkImportPresenter;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

public class BulkImportView extends Composite implements BulkImportPresenter.Display {

    private FlexTable layout;

    public BulkImportView() {

        // page layout
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidth("100%");
        layout.setHeight("98%");
        initWidget(layout);

        bind();
    }

    protected void bind() {

        // headers
        layout.setWidget(0, 0, new Header());
        layout.setWidget(1, 0, new HeaderMenu());

        // contents
        layout.setWidget(2, 0, createSWFImport());
        layout.getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
        layout.getCellFormatter().setHeight(2, 0, "100%");
        layout.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);

        // footer
        layout.setWidget(3, 0, Footer.getInstance());
    }

    protected Widget createSWFImport() {
        String html = "<object id=\"EntryBulkImport\" width=\"100%\" height=\"100%\" codebase=\"https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab\"><param name=\"movie\" value=\"EntryBulkImport.swf\" /> <param name=\"quality\" value=\"high\" /><param name=\"bgcolor\" value=\"#869ca7\" /><param name=\"wmode\" value=\"window\" /><param name=\"allowScriptAccess\" value=\"sameDomain\" /><embed src=\"../resources/org.jbei.ice.web.pages.UnprotectedPage/static/bi/EntryBulkImport.swf?sessionId=81cbf5c4989ea58f6cddaa1c4e3187a113ca7bcc\" quality=\"high\" bgcolor=\"#869ca7\" width=\"100%\" height=\"100%\" name=\"EntryBulkImport\" align=\"middle\" play=\"true\" loop=\"false\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.adobe.com/go/getflashplayer\"/></object>";
        HTML widget = new HTML(html, true);
        widget.setWidth("100%");
        return widget;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

}
