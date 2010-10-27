package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.pages.EntriesAllFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesCurrentFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesXMLExportPage;
import org.jbei.ice.web.pages.PrintableEntriesFullContentPage;
import org.jbei.ice.web.pages.PrintableEntriesTablePage;

/**
 * Panel that displays links for exporting data (usually in a table).
 * Currently the data is specified as a list of {@link  org.jbei.ice.lib.models.Entry}s
 * This will be refactored in the coming weeks to make it more generic
 * 
 * @author Hector Plahar
 * @param <T>
 *           Data type being exported
 */
public class DataTableExportOptionsPanel<T> extends Panel {

    private static final long serialVersionUID = 1L;

    public DataTableExportOptionsPanel(String id, final ArrayList<Entry> entries) {
        super(id);

        add(new Link<Page>("printableCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesTablePage(entries, true));
            }
        });

        add(new Link<Page>("printableAllLink") {
            private static final long serialVersionUID = 2L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesFullContentPage(entries));
            }
        });

        add(new Link<Page>("excelCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesCurrentFieldsExcelExportPage(entries));
            }
        });

        add(new Link<Page>("excelAllLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesAllFieldsExcelExportPage(entries));
            }
        });

        add(new Link<Page>("xmlLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesXMLExportPage(entries));
            }
        });
    }

}
