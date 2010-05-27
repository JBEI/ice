package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.EntryViewPage;

public class MiniSamplesViewPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private static final String SAMPLES_URL_KEY = "samples";
    private static final int SHORT_SAMPLENAME_LENGTH = 20;

    private Entry entry = null;
    private ArrayList<Sample> samples = new ArrayList<Sample>();

    public MiniSamplesViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        add(new BookmarkablePageLink<Object>("samplesPageLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + SAMPLES_URL_KEY)));
        SampleController sampleController = new SampleController(IceSession.get().getAccount());

        int numSamples = 0;
        try {
            numSamples = sampleController.getNumberOfSamples(entry);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        this.entry = entry;

        try {
            samples.addAll(sampleController.getSamples(entry));
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        int showLimit = 4;
        if (samples.size() > showLimit) {
            Panel moreLinkPanel = new MoreSamplesLinkPanel("moreSamplesLinkPanel", entry);
            add(moreLinkPanel);
            samples = new ArrayList<Sample>(samples.subList(0, showLimit));
        } else {
            add(new EmptyPanel("moreSamplesLinkPanel"));
        }

        ListView<Sample> samplesList = new ListView<Sample>("samplesList", samples) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Sample> item) {
                String itemLabel = item.getModelObject().getLabel();
                String shortItemLabel = null;
                if (itemLabel.length() > SHORT_SAMPLENAME_LENGTH) {
                    shortItemLabel = itemLabel.substring(0, SHORT_SAMPLENAME_LENGTH) + "...";
                } else {
                    shortItemLabel = itemLabel;
                }
                item.add(new Label("sampleItem", shortItemLabel));
            }

        };

        ArrayList<String> emptySamplesArray = new ArrayList<String>();
        emptySamplesArray.add("No Samples");
        ListView<String> emptySamplesList = new ListView<String>("samplesList", emptySamplesArray) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("sampleItem", item.getModelObject()));
            }
        };

        if (numSamples != 0) {
            add(new Label("samplesCount", "(" + numSamples + ")"));
            add(samplesList);
        } else {
            add(new Label("samplesCount", ""));
            add(emptySamplesList);
        }

    }

    public Entry getEntry() {
        return entry;
    }

}
