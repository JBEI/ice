package org.jbei.ice.web.panels.adminpage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.models.LocationNew;
import org.jbei.ice.lib.models.LocationNew.LocationType;
import org.jbei.ice.web.common.CustomChoice;

public class StorageSchemeEditItemPanel extends Panel {
    private static final long serialVersionUID = -1;

    private CustomChoice locationType = null;
    private String locationName = null;
    private ArrayList<CustomChoice> choices = new ArrayList<CustomChoice>();

    public StorageSchemeEditItemPanel(String id) {
        super(id);

        initializeComponents();
    }

    public StorageSchemeEditItemPanel(String id, String locationName, LocationType locationType) {
        super(id);

        initializeComponents();

        this.locationName = locationName;
        for (CustomChoice choice : choices) {
            if (choice.getValue().equals(locationType.toString())) {
                this.locationType = choice;
            }
        }

    }

    private void initializeComponents() {

        Map<String, String> locations = LocationNew.getLocationTypeOptionsMap();
        for (LocationType locationType : LocationType.values()) {
            choices.add(new CustomChoice(locations.get(locationType.toString()), locationType
                    .toString()));
        }

        DropDownChoice<CustomChoice> dropDownChoice = new DropDownChoice<CustomChoice>(
                "locationTypeDropDownChoice",
                new PropertyModel<CustomChoice>(this, "locationType"),
                new Model<ArrayList<CustomChoice>>(choices), new ChoiceRenderer<CustomChoice>(
                        "name", "value"));

        dropDownChoice.setRequired(true);
        dropDownChoice.setLabel(new Model<String>("Location Type"));
        add(dropDownChoice);

        add(new TextField<String>("locationName", new PropertyModel<String>(this, "locationName"))
                .setRequired(true).setLabel(new Model<String>("Location Name")));

        add(new AjaxButton("addButton") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                StorageSchemeEditPanel parentPanel = (StorageSchemeEditPanel) getParent()
                        .getParent().getParent().getParent().getParent();

                parentPanel.getLocationItems().add(
                    new StorageSchemeEditItemPanel("editStorageSchemeItemPanel"));

                target.addComponent(form);
            }

        }.setDefaultFormProcessing(false));

        add(new AjaxButton("removeButton") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                StorageSchemeEditPanel parentPanel = (StorageSchemeEditPanel) getParent()
                        .getParent().getParent().getParent().getParent();

                LinkedList<StorageSchemeEditItemPanel> locationItems = parentPanel
                        .getLocationItems();
                if (locationItems.size() == 1) {
                    return; // keep at least one panel
                }

                locationItems.remove(locationItems.indexOf(getParent()));

                target.addComponent(form);

            }

        }.setDefaultFormProcessing(false));
    }

    public CustomChoice getLocationType() {
        return locationType;
    }

    public void setLocationType(CustomChoice locationType) {
        this.locationType = locationType;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationName() {
        return locationName;
    }
}
