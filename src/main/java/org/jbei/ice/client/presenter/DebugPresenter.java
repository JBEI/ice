package org.jbei.ice.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.CollectionEntriesDataTable;
import org.jbei.ice.client.collection.SamplesDataTable;
import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.Header;
import org.jbei.ice.client.component.EntryDataViewDataProvider;
import org.jbei.ice.client.component.ExportAsPanel;
import org.jbei.ice.client.component.table.EntryDataTable;
import org.jbei.ice.client.component.table.EntryTablePager;
import org.jbei.ice.client.component.table.HasEntryDataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryData;
import org.jbei.ice.shared.PlasmidTipView;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DebugPresenter implements Presenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private EntryDataTable<EntryData> table;
    private HasEntryDataTable<SampleInfo> table2;
    private EntryDataViewDataProvider provider;
    private final List<Long> list;

    public DebugPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
        table = new CollectionEntriesDataTable();
        table.addColumnSortHandler(new AsyncHandler(table));
        table.getColumnSortList().push(table.getColumn(ColumnField.CREATED));

        // fake data
        int size = Random.nextInt(1000);
        list = new ArrayList<Long>(size);
        for (long i = 1012; i < size + 1012; i += 1) {
            list.add(i);
        }

        provider = new EntryDataViewDataProvider(table, list, service) {
            @Override
            protected void fetchEntryData(ColumnField field, boolean ascending,
                    final int rangeStart, final int rangeEnd) {

                if (values == null || values.isEmpty())
                    return;

                int factor = (rangeEnd - rangeStart) * 9;
                factor = (factor + rangeEnd) > values.size() ? values.size() : (factor + rangeEnd);
                List<Long> subList = values.subList(rangeStart, factor);
                final ArrayList<Long> realValues = new ArrayList<Long>(subList);

                for (long value : realValues) {
                    EntryData view = new PlasmidTipView();
                    if (Random.nextBoolean())
                        view.setType("Plasmid");
                    else
                        view.setType("Strain");
                    view.setRecordId(value);
                    view.setAlias("JBX-" + value);
                    view.setCreated(System.currentTimeMillis() + value + 1000);
                    results.add(view);
                }

                updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
            }
        };

        provider.setValues(list);

        //
        // TABLE 2
        // 
        table2 = new SamplesDataTable();
        table.addColumnSortHandler(new AsyncHandler(table));
        table.getColumnSortList().push(table.getColumn(ColumnField.CREATED));

        // fake data for table 2
        size = Random.nextInt(1000);
        ArrayList<Long> list2 = new ArrayList<Long>(size);
        for (long i = 1012; i < size + 1012; i += 1) {
            list2.add(i);
        }
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(new View());
    }

    private class View extends Composite {

        private final HeaderPanel layout;

        public View() {

            layout = new HeaderPanel();
            layout.setWidth("100%");
            layout.setHeight("100%");
            initWidget(layout);

            layout.setHeaderWidget(new Header());
            layout.setContentWidget(getTables());
            layout.setFooterWidget(Footer.getInstance().asWidget());
        }

        private Widget getTables() {
            HorizontalPanel panel = new HorizontalPanel();
            panel.add(getTable1());
            panel.add(getTable2());
            return panel;
        }

        private VerticalPanel getTable1() {
            VerticalPanel panel = new VerticalPanel();
            panel.add(table);
            panel.setWidth("100%");
            table.addStyleName("gray_border");
            EntryTablePager tablePager = new EntryTablePager();
            tablePager.setDisplay(table);
            panel.add(tablePager);
            //
            //            // Export as
            ExportAsPanel export = new ExportAsPanel();
            panel.add(export);

            return panel;
        }

        private VerticalPanel getTable2() {
            VerticalPanel panel = new VerticalPanel();
            panel.add(table2);
            panel.setWidth("100%");
            table2.addStyleName("gray_border");
            EntryTablePager tablePager = new EntryTablePager();
            tablePager.setDisplay(table2);
            return panel;

        }
    }
}
