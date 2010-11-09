package org.jbei.ice.web.panels;

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

public class EditLocationSchemeItemPanel extends Panel {
    private static final long serialVersionUID = -1;

    private CustomChoice locationType = null;
    private String locationName = null;

    public EditLocationSchemeItemPanel(String id) {
        super(id);
        ArrayList<CustomChoice> choices = new ArrayList<CustomChoice>();
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
                EditLocationSchemePanel parentPanel = (EditLocationSchemePanel) getParent()
                        .getParent().getParent().getParent().getParent();

                parentPanel.getLocationItems().add(
                    new EditLocationSchemeItemPanel("editLocationSchemeItemPanel"));

                target.addComponent(form);
            }

        }.setDefaultFormProcessing(false));

        add(new AjaxButton("removeButton") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                EditLocationSchemePanel parentPanel = (EditLocationSchemePanel) getParent()
                        .getParent().getParent().getParent().getParent();

                LinkedList<EditLocationSchemeItemPanel> locationItems = parentPanel
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
