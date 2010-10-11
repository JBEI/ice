package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.web.forms.ArabidopsisSeedNewFormPanel;
import org.jbei.ice.web.forms.PartNewFormPanel;
import org.jbei.ice.web.forms.PlasmidNewFormPanel;
import org.jbei.ice.web.forms.PlasmidStrainNewFormPanel;
import org.jbei.ice.web.forms.StrainNewFormPanel;

public class SelectNewEntryTypePanel extends Panel {
    private static final long serialVersionUID = 1L;

    private static final String PLASMID_TYPE = "Plasmid";
    private static final String STRAIN_TYPE = "Strain";
    private static final String PART_TYPE = "Part";
    private static final String STRAIN_WITH_PLASMID_TYPE = "Strain with One Plasmid";
    private static final String ARABIDOPSIS_SEED_TYPE = "Arabidopsis Seed";

    private String typeSelection;

    public SelectNewEntryTypePanel(String id) {
        super(id);

        ArrayList<String> partTypes = new ArrayList<String>();
        partTypes.add(PLASMID_TYPE);
        partTypes.add(STRAIN_TYPE);
        partTypes.add(PART_TYPE);
        partTypes.add(STRAIN_WITH_PLASMID_TYPE);
        partTypes.add(ARABIDOPSIS_SEED_TYPE);

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

                if (tSelection.equals(PLASMID_TYPE)) {
                    formPanel = new PlasmidNewFormPanel("formPanel");
                } else if (tSelection.equals(STRAIN_TYPE)) {
                    formPanel = new StrainNewFormPanel("formPanel");
                } else if (tSelection.equals(PART_TYPE)) {
                    formPanel = new PartNewFormPanel("formPanel");
                } else if (tSelection.equals(STRAIN_WITH_PLASMID_TYPE)) {
                    formPanel = new PlasmidStrainNewFormPanel("formPanel");
                } else if (tSelection.equals(ARABIDOPSIS_SEED_TYPE)) {
                    formPanel = new ArabidopsisSeedNewFormPanel("formPanel");
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
