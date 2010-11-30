package org.jbei.ice.web.panels.sample;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Sample;

public class BriefSampleViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public BriefSampleViewPanel(String id, List<Sample> samples) {
        super(id);

        ArrayList<Panel> samplePanels = new ArrayList<Panel>();
        int index = 1;
        for (Sample sample : samples) {
            samplePanels.add(new BriefSampleViewItemPanel("sampleItem", sample, index));
            index++;
        }

        ListView<Panel> listView = new ListView<Panel>("samplesList", samplePanels) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Panel> item) {
                item.add(item.getModelObject());
            }

        };
        add(listView);

    }
}
