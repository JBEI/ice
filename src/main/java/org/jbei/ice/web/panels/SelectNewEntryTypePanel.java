package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.web.forms.PartNewFormPanel;
import org.jbei.ice.web.forms.PlasmidNewFormPanel;
import org.jbei.ice.web.forms.PlasmidStrainNewFormPanel;
import org.jbei.ice.web.forms.StrainNewFormPanel;

public class SelectNewEntryTypePanel extends Panel {

    private static final long serialVersionUID = 1L;
    private String typeSelection;

    public SelectNewEntryTypePanel(String id) {

        super(id);

        ArrayList<String> partTypes = new ArrayList<String>();
        partTypes.add("Plasmid");
        partTypes.add("Strain");
        partTypes.add("Part");
        partTypes.add("Strain with One Plasmid");

        RadioChoice<String> partTypeChoice = new RadioChoice<String>("partTypes",
                new PropertyModel<String>(this, "typeSelection"), partTypes);
        partTypeChoice.add(new AjaxFormChoiceComponentUpdatingBehavior() {
            private static final long serialVersionUID = 1L;

            Panel formPanel = null;

            public void onUpdate(AjaxRequestTarget target) {
                System.out.println("I'm here with " + getTypeSelection());

                if (getTypeSelection().equals("Plasmid")) {
                    formPanel = new PlasmidNewFormPanel("formPanel");
                } else if (getTypeSelection().equals("Strain")) {
                    formPanel = new StrainNewFormPanel("formPanel");
                } else if (getTypeSelection().equals("Part")) {
                    formPanel = new PartNewFormPanel("formPanel");
                } else if (getTypeSelection().equals("Strain with One Plasmid")) {
                    formPanel = new PlasmidStrainNewFormPanel("formPanel");
                }

                formPanel.setOutputMarkupId(true);
                target.getPage().replace(formPanel);
                target.addComponent(formPanel);

            }
        });

        add(partTypeChoice);

    }

    public void setTypeSelection(String typeSelection) {
        this.typeSelection = typeSelection;
    }

    public String getTypeSelection() {
        return typeSelection;
    }

}
