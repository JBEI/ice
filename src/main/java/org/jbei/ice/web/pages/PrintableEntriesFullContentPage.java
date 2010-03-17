package org.jbei.ice.web.pages;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.web.panels.PartSimpleViewPanel;
import org.jbei.ice.web.panels.PlasmidSimpleViewPanel;
import org.jbei.ice.web.panels.StrainSimpleViewPanel;

public class PrintableEntriesFullContentPage extends ProtectedPage {
    public PrintableEntriesFullContentPage(ArrayList<Entry> entries) {
        super();

        initialize(entries);
    }

    @Override
    protected void initializeComponents() {
        add(new Label("title", "Printable"));
    }

    private void initialize(ArrayList<Entry> entries) {
        RepeatingView repeatingView = new RepeatingView("entriesRepeatingView");

        int index = 0;

        for (Iterator<Entry> iterator = entries.iterator(); iterator.hasNext();) {
            Entry entry = iterator.next();

            if (entry instanceof Plasmid) {
                repeatingView
                        .add(new PlasmidSimpleViewPanel(String.valueOf(index), (Plasmid) entry));
            } else if (entry instanceof Strain) {
                repeatingView.add(new StrainSimpleViewPanel(String.valueOf(index), (Strain) entry));
            } else if (entry instanceof Part) {
                repeatingView.add(new PartSimpleViewPanel(String.valueOf(index), (Part) entry));
            }

            index++;
        }

        add(repeatingView);
    }
}
