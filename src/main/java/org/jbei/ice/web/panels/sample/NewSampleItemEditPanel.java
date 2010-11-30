package org.jbei.ice.web.panels.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.CustomChoice;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.EntryViewPage;

public class NewSampleItemEditPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Sample sample = null;

    public NewSampleItemEditPanel(String id, Sample passedSample, boolean isEditAction) {
        super(id);

        sample = passedSample;

        class SampleEditForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;

            private String label;
            private String depositor;
            private String notes;
            private CustomChoice schemeChoice;
            private Storage currentScheme;
            private ArrayList<CustomChoice> schemeChoices = new ArrayList<CustomChoice>();
            private ArrayList<SchemeValue> schemeValues = new ArrayList<SchemeValue>();
            private NewSampleItemEditPanel controllingPanel = null;
            private Panel alertPanel = null;

            public SampleEditForm(String id, boolean isEditAction) {
                super(id);
                setOutputMarkupId(true);
                setModel(new CompoundPropertyModel<Object>(this));
                setControllingPanel((NewSampleItemEditPanel) getParent());
                setLabel(sample.getLabel());
                setNotes(sample.getNotes());
                add(renderSchemeChoices("schemeChoices"));
                addOrReplace(renderSchemeFieldsListView("schemeValueListView"));
                alertPanel = new EmptyPanel("alertPanel");

                try {
                    currentScheme = StorageManager.get(Long.valueOf(schemeChoice.getValue()));
                } catch (NumberFormatException e) {
                    // ok to pass
                } catch (ManagerException e) {
                    //log and pass
                    Logger.error(e);
                }

                if (isEditAction) {
                    if (sample.getDepositor() == null || sample.getDepositor().isEmpty()) {
                        setDepositor(sample.getEntry().getOwnerEmail());
                    } else {
                        setDepositor(sample.getDepositor());
                    }

                    // populate storage info
                    if (sample.getStorage() != null) {
                        Storage currentStorage = sample.getStorage();
                        Storage currentScheme = StorageManager
                                .getSchemeContainingParentStorage(currentStorage);
                        for (CustomChoice schemeChoice : schemeChoices) {
                            if (Long.parseLong(schemeChoice.getValue()) == currentScheme.getId()) {
                                this.schemeChoice = schemeChoice;
                                this.currentScheme = currentScheme;
                            }
                        }

                        ArrayList<SchemeValue> tempSchemeValues = new ArrayList<SchemeValue>();
                        // compare expected vs found scheme
                        if (StorageManager.isStorageSchemeInAgreement(currentStorage)) {
                            while (StorageType.SCHEME != currentStorage.getStorageType()) {
                                tempSchemeValues.add(new SchemeValue(currentStorage.getName(),
                                        currentStorage.getIndex()));
                                currentStorage = currentStorage.getParent();
                            }
                            Collections.reverse(tempSchemeValues);
                            schemeValues = tempSchemeValues;
                        } else {
                            // unknown scheme
                            alertPanel = new UnknownStorageSchemePanel("alertPanel",
                                    sample.getStorage());
                        }
                    }
                } else {
                    setDepositor(IceSession.get().getAccount().getEmail());
                }
                // end populate storage info

                AjaxButton clearButton = new AjaxButton("clearButton") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                        SampleEditForm parentForm = (SampleEditForm) form;
                        for (SchemeValue schemeValue : parentForm.getSchemeValues()) {
                            schemeValue.setIndex(null);
                        }
                        ListView<SchemeValue> newListView = renderSchemeFieldsListView("schemeValueListView");
                        parentForm.addOrReplace(newListView);
                        target.addComponent(parentForm);
                    }

                };
                clearButton.setDefaultFormProcessing(false);
                add(clearButton);

                Button cancelButton = new Button("cancelButton", new Model<String>("Cancel")) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onSubmit() {
                        setRedirect(true);
                        setResponsePage(EntryViewPage.class, new PageParameters("0="
                                + sample.getEntry().getId() + ",1=samples"));
                    }
                };
                cancelButton.setDefaultFormProcessing(false);
                add(cancelButton);

                add(new TextField<String>("label").setRequired(true).setLabel(
                    new Model<String>("Label")));
                add(new TextField<String>("depositor").setRequired(true).setLabel(
                    new Model<String>("Depositor")));
                add(new TextArea<String>("notes"));
                add(alertPanel);
                add(new Button("saveSampleButton", new Model<String>("Save")));
            }

            @Override
            protected void onSubmit() {
                NewSampleItemEditPanel sampleItemEditPanel = (NewSampleItemEditPanel) getParent();
                Sample sample = sampleItemEditPanel.getSample();
                sample.setLabel(getLabel());
                sample.setDepositor(getDepositor());
                sample.setNotes(getNotes());
                // get scheme
                Storage scheme = null;
                long schemeId = Long.parseLong(getSchemeChoice().getValue());
                try {
                    scheme = StorageManager.get(schemeId);
                } catch (ManagerException e1) {
                    throw new ViewException(e1);
                }
                // get values for scheme.
                // If all the values are null, then no location, whereas if partially 
                // null, prompt user for reinput.
                int nullCounter = 0;
                ArrayList<String> valueHolder = new ArrayList<String>();
                for (SchemeValue schemeValue : getSchemeValues()) {
                    if (schemeValue.getIndex() == null) {
                        nullCounter += 1;
                    }
                    valueHolder.add(schemeValue.getIndex());
                }
                Storage storage = null;
                if (nullCounter == 0) {
                    if (scheme != null) {
                        try {
                            storage = StorageManager.getLocation(scheme,
                                valueHolder.toArray(new String[valueHolder.size()]));
                        } catch (ManagerException e) {
                            throw new ViewException(e);
                        }
                    }
                } else if (nullCounter == getSchemeValues().size()) {
                    //User did not want to enter storage. set Storage to null
                } else {
                    error("Location cannot be partially filled");
                    return;
                }
                // create/get location with scheme + values

                sample.setStorage(storage);

                // assign location to sample

                SampleController sampleController = new SampleController(IceSession.get()
                        .getAccount());

                try {
                    sampleController.saveSample(sampleItemEditPanel.getSample());

                    setRedirect(true);
                    setResponsePage(EntryViewPage.class, new PageParameters("0="
                            + sample.getEntry().getId() + ",1=samples"));
                } catch (PermissionException e) {
                    throw new ViewPermissionException("No permissions to save sample!", e);
                } catch (ControllerException e) {
                    throw new ViewException(e);
                }
            }

            protected DropDownChoice<CustomChoice> renderSchemeChoices(String id) {
                DropDownChoice<CustomChoice> dropDownChoice = null;

                List<Storage> schemes = StorageManager.getStorageSchemesForEntryType(sample
                        .getEntry().getRecordType());

                for (Storage scheme : schemes) {
                    CustomChoice schemeChoice1 = new CustomChoice(scheme.getName(),
                            String.valueOf(scheme.getId()));
                    schemeChoices.add(schemeChoice1);
                    if (PopulateInitialDatabase.DEFAULT_PLASMID_STORAGE_SCHEME_NAME.equals(scheme
                            .getName())) {
                        setSchemeChoice(schemeChoice1);
                        currentScheme = scheme;

                    } else if (PopulateInitialDatabase.DEFAULT_STRAIN_STORAGE_SCHEME_NAME
                            .equals(scheme.getName())) {
                        setSchemeChoice(schemeChoice1);
                        currentScheme = scheme;

                    } else if (PopulateInitialDatabase.DEFAULT_PART_STORAGE_SCHEME_NAME
                            .equals(scheme.getName())) {
                        setSchemeChoice(schemeChoice1);
                        currentScheme = scheme;

                    } else if (PopulateInitialDatabase.DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME
                            .equals(scheme.getName())) {
                        setSchemeChoice(schemeChoice1);
                        currentScheme = scheme;
                    }
                }

                if (schemeChoices.size() == 0) {
                    return null;
                    // render nothing
                }

                dropDownChoice = new DropDownChoice<CustomChoice>(id,
                        new PropertyModel<CustomChoice>(this, "schemeChoice"),
                        new Model<ArrayList<CustomChoice>>(schemeChoices),
                        new ChoiceRenderer<CustomChoice>("name", "value")) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected boolean wantOnSelectionChangedNotifications() {
                        return true;
                    }

                    @Override
                    protected void onSelectionChanged(final CustomChoice choice) {
                        SampleEditForm form = (SampleEditForm) getParent();
                        form.setSchemeChoice(choice);
                        form.getSchemeValues().clear();

                        try {
                            currentScheme = StorageManager
                                    .get(Long.valueOf(schemeChoice.getValue()));
                        } catch (NumberFormatException e) {
                            // ok to pass
                        } catch (ManagerException e) {
                            //log and pass
                            Logger.error(e);
                        }
                        ListView<SchemeValue> listView = renderSchemeFieldsListView("schemeValueListView");

                        form.addOrReplace(listView);
                    }
                };
                return dropDownChoice;

            }

            protected ListView<SchemeValue> renderSchemeFieldsListView(String listViewId) {

                List<Storage> schemeList = currentScheme.getSchemes();
                if (schemeValues.size() == 0) {
                    for (Storage item : schemeList) {
                        schemeValues.add(new SchemeValue(item.getName(), null));
                    }
                }
                ListView<SchemeValue> listView = new ListView<SchemeValue>(listViewId,
                        new PropertyModel<ArrayList<SchemeValue>>(this, "schemeValues")) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void populateItem(ListItem<SchemeValue> item) {
                        SchemeValue schemeValue = item.getModelObject();
                        item.add(new Label("itemLabel", schemeValue.getName()));
                        item.add(new TextField<String>("itemValue", new PropertyModel<String>(
                                schemeValue, "index")));
                    }

                };
                listView.setOutputMarkupId(true);
                listView.setOutputMarkupPlaceholderTag(true);
                return listView;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public String getDepositor() {
                return depositor;
            }

            public void setDepositor(String depositor) {
                this.depositor = depositor;
            }

            public String getNotes() {
                return notes;
            }

            public void setNotes(String notes) {
                this.notes = notes;
            }

            public void setSchemeChoice(CustomChoice schemeChoice) {
                this.schemeChoice = schemeChoice;
            }

            public CustomChoice getSchemeChoice() {
                return schemeChoice;
            }

            public void setControllingPanel(NewSampleItemEditPanel controllingPanel) {
                this.controllingPanel = controllingPanel;
            }

            @SuppressWarnings("unused")
            public NewSampleItemEditPanel getControllingPanel() {
                return controllingPanel;
            }

            @SuppressWarnings("unused")
            public void setSchemeValues(ArrayList<SchemeValue> schemeValues) {
                this.schemeValues = schemeValues;
            }

            public List<SchemeValue> getSchemeValues() {
                return schemeValues;
            }

            @SuppressWarnings("unused")
            public void setSchemeChoices(ArrayList<CustomChoice> schemeChoices) {
                this.schemeChoices = schemeChoices;
            }

            @SuppressWarnings("unused")
            public List<CustomChoice> getSchemeChoices() {
                return schemeChoices;
            }

        }

        SampleEditForm sampleEditForm = new SampleEditForm("sampleEditForm", isEditAction);
        sampleEditForm.setOutputMarkupId(true);
        add(sampleEditForm);
        add(new FeedbackPanel("feedback"));
        setOutputMarkupId(true);
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Sample getSample() {
        return sample;
    }
}
