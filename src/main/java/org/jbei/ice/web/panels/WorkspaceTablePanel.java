package org.jbei.ice.web.panels;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.dataProviders.WorkspaceDataProvider;
import org.jbei.ice.web.pages.EntriesAllFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesCurrentFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesXMLExportPage;
import org.jbei.ice.web.pages.PrintableEntriesFullContentPage;
import org.jbei.ice.web.pages.PrintableEntriesTablePage;
import org.jbei.ice.web.pages.UnprotectedPage;

public class WorkspaceTablePanel extends Panel {
    private static final long serialVersionUID = 1L;

    private WorkspaceDataProvider workspaceDataProvider;
    private WorkspaceDataView workspaceDataView;

    public WorkspaceTablePanel(String id) {
        super(id);

        workspaceDataProvider = new WorkspaceDataProvider(IceSession.get().getAccount());

        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.cluetip.js"));
        add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.cluetip.css"));

        workspaceDataView = new WorkspaceDataView("workspaceDataView", workspaceDataProvider, 15);

        add(workspaceDataView);
        add(new JbeiPagingNavigator("navigator", workspaceDataView));

        renderExportLinks();
    }

    private void renderExportLinks() {
        add(new Link<Page>("printableCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesTablePage(workspaceDataProvider.getEntries(),
                        true));
            }
        });
        add(new Link<Page>("printableAllLink") {
            private static final long serialVersionUID = 2L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesFullContentPage(workspaceDataProvider
                        .getEntries()));
            }
        });
        add(new Link<Page>("excelCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesCurrentFieldsExcelExportPage(workspaceDataProvider
                        .getEntries()));
            }
        });
        add(new Link<Page>("excelAllLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesAllFieldsExcelExportPage(workspaceDataProvider
                        .getEntries()));
            }
        });
        add(new Link<Page>("xmlLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesXMLExportPage(workspaceDataProvider.getEntries()));
            }
        });
    }

}
