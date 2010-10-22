package org.jbei.ice.web.panels;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.data.tables.AbstractEntryColumn;
import org.jbei.ice.web.data.tables.RegistryTable;
import org.jbei.ice.web.dataProviders.AbstractEntriesDataProvider;
import org.jbei.ice.web.pages.EntriesAllFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesCurrentFieldsExcelExportPage;
import org.jbei.ice.web.pages.EntriesXMLExportPage;
import org.jbei.ice.web.pages.PrintableEntriesFullContentPage;
import org.jbei.ice.web.pages.PrintableEntriesTablePage;

public class EntryDataTablePanel<T extends Entry> extends Panel {

	private static final long serialVersionUID = 1L;
	private final AbstractEntriesDataProvider dataProvider;
	private final int DEFAULT_ROW_COUNT = 50;

	public EntryDataTablePanel(String id, AbstractEntriesDataProvider dataProvider, List<AbstractEntryColumn> registryColumns, boolean renderExportLinks) {
		super(id);
		this.dataProvider = dataProvider;
		
		RegistryTable<Entry> table = new RegistryTable<Entry>("data_table", registryColumns.toArray(new IColumn[registryColumns.size()]), dataProvider, DEFAULT_ROW_COUNT);
		add(table);
		renderExportLinks();
	}
	
	private void renderExportLinks() {
		
        add(new Link<Page>("printableCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesTablePage(dataProvider.getEntries(), true));
            }
        });
        
        add(new Link<Page>("printableAllLink") {
            private static final long serialVersionUID = 2L;

            @Override
            public void onClick() {
                setResponsePage(new PrintableEntriesFullContentPage(dataProvider.getEntries()));
            }
        });
        
        add(new Link<Page>("excelCurrentLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesCurrentFieldsExcelExportPage(dataProvider.getEntries()));
            }
        });
        
        add(new Link<Page>("excelAllLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesAllFieldsExcelExportPage(dataProvider.getEntries()));
            }
        });
        
        add(new Link<Page>("xmlLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EntriesXMLExportPage(dataProvider.getEntries()));
            }
        });
    }

}
