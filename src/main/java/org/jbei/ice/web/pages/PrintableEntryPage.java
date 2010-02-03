package org.jbei.ice.web.pages;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.web.panels.PartViewPanel;
import org.jbei.ice.web.panels.PlasmidViewPanel;
import org.jbei.ice.web.panels.StrainViewPanel;

public class PrintableEntryPage extends ProtectedPage {
    public PrintableEntryPage(ArrayList<Entry> entries) {
        super();

        RepeatingView repeatingView = new RepeatingView("entriesRepeatingView");

        int index = 0;

        for (Iterator<Entry> iterator = entries.iterator(); iterator.hasNext();) {
            Entry entry = iterator.next();

            if (entry instanceof Plasmid) {
                repeatingView.add(new PlasmidViewPanel(String.valueOf(index), (Plasmid) entry));
            } else if (entry instanceof Strain) {
                repeatingView.add(new StrainViewPanel(String.valueOf(index), (Strain) entry));
            } else if (entry instanceof Part) {
                repeatingView.add(new PartViewPanel(String.valueOf(index), (Part) entry));
            }

            index++;
        }

        add(repeatingView);
    }

    @Override
    protected void initializeComponents() {
        add(new Label("title", "Printable"));
    }
}
