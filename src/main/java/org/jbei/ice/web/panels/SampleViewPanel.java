package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class SampleViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Entry entry = null;
    private ArrayList<Sample> samples = new ArrayList<Sample>();
    private ArrayList<Panel> panels = new ArrayList<Panel>();

    public SampleViewPanel(String id, Entry entry) {
        super(id);

        SampleController sampleController = new SampleController(IceSession.get().getAccount());

        this.entry = entry;
        class AddSampleLink extends AjaxFallbackLink<Object> {
            private static final long serialVersionUID = 1L;

            public AddSampleLink(String id) {
                super(id);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                SampleViewPanel thisPanel = (SampleViewPanel) getParent();
                ArrayList<Panel> thisPanelsPanels = thisPanel.getPanels();
                if (thisPanelsPanels.size() > 0
                        && thisPanelsPanels.get(0) instanceof SampleItemEditPanel) {
                    // If the first item is already an edit form, do nothing.
                } else {
                    Sample newSample = new Sample();
                    newSample.setEntry(thisPanel.getEntry());
                    Panel newSampleEditPanel = new SampleItemEditPanel("sampleItemPanel",
                            newSample, false);
                    newSampleEditPanel.setOutputMarkupId(true);

                    panels.add(0, newSampleEditPanel);

                    target.getPage().replace(thisPanel);
                    target.addComponent(thisPanel);
                }
            }
        }

        add(new AddSampleLink("addSampleLink"));

        try {
            samples.addAll(sampleController.getSamples(entry));
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        Object[] temp = samples.toArray();
        if (temp.length == 0) {
            Panel sampleItemPanel = new EmptyMessagePanel("sampleItemPanel", "No samples provided");
            sampleItemPanel.setOutputMarkupId(true);
            panels.add(sampleItemPanel);
        } else {
            populatePanels();
        }

        ListView<Object> samplesList = generateSamplesList("samplesListView");
        samplesList.setOutputMarkupId(true);
        add(samplesList);
    }

    public void populatePanels() {
        Integer counter = 1;
        panels.clear();
        for (Sample sample : samples) {
            Panel sampleItemPanel = new SampleItemViewPanel("sampleItemPanel", counter, sample);
            sampleItemPanel.setOutputMarkupId(true);
            panels.add(sampleItemPanel);
            counter = counter + 1;
        }
    }

    public ListView<Object> generateSamplesList(String id) {
        ListView<Object> samplesListView = new ListView<Object>(id, panels) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Object> item) {
                Panel panel = (Panel) item.getModelObject();
                item.add(panel);
            }
        };

        return samplesListView;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public ArrayList<Panel> getPanels() {
        return panels;
    }

    public ArrayList<Sample> getSamples() {
        return samples;
    }
}
