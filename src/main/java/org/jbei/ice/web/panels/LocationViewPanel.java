package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Location;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.web.IceSession;

public class LocationViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    Sample sample = null;
    ArrayList<Location> locations = new ArrayList<Location>();
    ArrayList<Panel> panels = new ArrayList<Panel>();

    public LocationViewPanel(String id, Sample sample) {
        super(id);

        this.sample = sample;

        class AddLocationLink extends AjaxFallbackLink<Object> {
            private static final long serialVersionUID = 1L;

            public AddLocationLink(String id) {
                super(id);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                LocationViewPanel thisPanel = (LocationViewPanel) getParent().getParent();
                ArrayList<Panel> thisPanelsPanels = thisPanel.getPanels();
                if (thisPanelsPanels.size() > 0
                        && thisPanelsPanels.get(0) instanceof LocationItemEditPanel) {
                    // If first item is already an edit form, do nothing. 
                } else {
                    Location newLocation = new Location();
                    newLocation.setSample(thisPanel.getSample());
                    Panel newLocationEditPanel = new LocationItemEditPanel("locationItemPanel",
                            newLocation);
                    newLocationEditPanel.setOutputMarkupId(true);
                    panels.add(0, newLocationEditPanel);
                    // I need to get the SampleViewPanel.
                    SampleViewPanel temp = (SampleViewPanel) thisPanel.getParent().getParent()
                            .getParent().getParent();

                    target.getPage().replace(temp);
                    target.addComponent(temp);
                }
            }

        }

        WebMarkupContainer topLinkContainer = new WebMarkupContainer("topLinkContainer");
        topLinkContainer.setVisible(IceSession.get().getAccount().getEmail().equals(
                sample.getDepositor()));
        topLinkContainer.add(new AddLocationLink("addLocationLink"));
        add(topLinkContainer);

        locations.addAll(sample.getLocations());

        Object[] temp = locations.toArray();
        if (temp.length == 0) {
            Panel locationItemPanel = new EmptyMessagePanel("locationItemPanel",
                    "No locations provided");
            locationItemPanel.setOutputMarkupId(true);
            panels.add(locationItemPanel);
        } else {
            populatePanels();
        }

        ListView<Object> locationsList = generateLocationsList("locationsListView");
        locationsList.setOutputMarkupId(true);
        add(locationsList);
    }

    public void populatePanels() {
        Integer counter = 1;
        panels.clear();
        for (Location location : locations) {
            Panel locationItemPanel = new LocationItemViewPanel("locationItemPanel", counter,
                    location);
            locationItemPanel.setOutputMarkupId(true);
            panels.add(locationItemPanel);
            counter = counter + 1;
        }
    }

    public ListView<Object> generateLocationsList(String id) {
        ListView<Object> locationsListView = new ListView<Object>(id, panels) {
            private static final long serialVersionUID = 1L;

            protected void populateItem(ListItem<Object> item) {
                Panel panel = (Panel) item.getModelObject();
                item.add(panel);
            }
        };

        return locationsListView;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Location> locations) {
        this.locations = locations;
    }

    public ArrayList<Panel> getPanels() {
        return panels;
    }

    public void setPanels(ArrayList<Panel> panels) {
        this.panels = panels;
    }
}
