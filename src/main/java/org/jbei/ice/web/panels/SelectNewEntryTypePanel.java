package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.Component;
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
                new PropertyModel<String>(this, "typeSelection"), partTypes) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSelectionChanged(Object newSelection) {
                Component formPanel = getPage().get("formPanel");

                String tSelection = getTypeSelection();

                if (tSelection == null) {
                    return;
                }

                if (tSelection.equals("Plasmid")) {
                    formPanel = new PlasmidNewFormPanel("formPanel");
                } else if (tSelection.equals("Strain")) {
                    formPanel = new StrainNewFormPanel("formPanel");
                } else if (tSelection.equals("Part")) {
                    formPanel = new PartNewFormPanel("formPanel");
                } else if (tSelection.equals("Strain with One Plasmid")) {
                    formPanel = new PlasmidStrainNewFormPanel("formPanel");
                }

                formPanel.setOutputMarkupId(true);
                formPanel.setOutputMarkupPlaceholderTag(true);
                getPage().replace(formPanel);
                getPage().addOrReplace(formPanel);
            }

            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }
        };

        add(partTypeChoice);
    }

    public void setTypeSelection(String typeSelection) {
        this.typeSelection = typeSelection;
    }

    public String getTypeSelection() {
        return typeSelection;
    }
}
