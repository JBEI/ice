package org.jbei.ice.web.panels.adminpage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.managers.ConfigurationManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.models.Configuration.ConfigurationKey;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;
import org.jbei.ice.web.common.CustomChoice;
import org.jbei.ice.web.common.ViewException;

public class StorageSchemeEditPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private LinkedList<StorageSchemeEditItemPanel> locationItems = new LinkedList<StorageSchemeEditItemPanel>();
    private Storage scheme = null;
    private String schemeName = null;
    private CustomChoice rootTypeChoice = null;
    private Panel parentPanel = null;

    public StorageSchemeEditPanel(String id, StorageSchemeChoicePanel parentPanel) {
        super(id);
        locationItems.add(new StorageSchemeEditItemPanel("editStorageSchemeItemPanel"));
        setParentPanel(parentPanel);
        initializeComponents();
    }

    public StorageSchemeEditPanel(String id, Storage storageScheme,
            StorageSchemeChoicePanel parentPanel) {
        super(id);
        scheme = storageScheme;
        schemeName = storageScheme.getName();
        setParentPanel(parentPanel);

        List<Storage> schemes = storageScheme.getSchemes();
        if (schemes != null) {

            for (Storage storage : schemes) {
                StorageSchemeEditItemPanel newSchemeItemPanel = new StorageSchemeEditItemPanel(
                        "editStorageSchemeItemPanel", storage.getName(), storage.getStorageType());
                locationItems.add(newSchemeItemPanel);
            }
        } else {
            locationItems.add(new StorageSchemeEditItemPanel("editStorageSchemeItemPanel"));

        }
        initializeComponents();
    }

    private void initializeComponents() {
        Form<Object> editStorageSchemeForm = new Form<Object>("editStorageSchemeForm");

        ListView<StorageSchemeEditItemPanel> storageSchemeListView = new ListView<StorageSchemeEditItemPanel>(
                "storageSchemeListView", new PropertyModel<List<StorageSchemeEditItemPanel>>(this,
                        "locationItems")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<StorageSchemeEditItemPanel> item) {
                item.add(item.getModelObject());
            }

        };
        storageSchemeListView.setOutputMarkupId(true);

        editStorageSchemeForm.add(new TextField<String>("schemeName", new PropertyModel<String>(
                this, "schemeName")).setRequired(true).setLabel(new Model<String>("Scheme Name")));
        editStorageSchemeForm.add(storageSchemeListView);
        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        editStorageSchemeForm.add(feedbackPanel);

        DropDownChoice<CustomChoice> rootTypeDropdown = renderRootTypeChoices("rootTypeDropDown");

        editStorageSchemeForm.add(rootTypeDropdown);

        editStorageSchemeForm.add(new AjaxButton("evaluateButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                StorageSchemeEditPanel thisPanel = (StorageSchemeEditPanel) getParent().getParent();
                ArrayList<Storage> schemes = new ArrayList<Storage>();
                for (StorageSchemeEditItemPanel item : thisPanel.getLocationItems()) {

                    StorageType itemLocationType = StorageType.valueOf(item.getLocationType()
                            .getValue());
                    schemes.add(new Storage(item.getLocationName(), "", itemLocationType, "", null));
                }
                StorageSchemeChoicePanel originalPanel = (StorageSchemeChoicePanel) thisPanel
                        .getParentPanel();
                Storage scheme = thisPanel.getScheme();
                scheme.setName(getSchemeName());
                scheme.setSchemes(schemes);
                scheme.setOwnerEmail(PopulateInitialDatabase.systemAccountEmail);
                scheme.setStorageType(StorageType.SCHEME);
                int i = Integer.valueOf(thisPanel.getRootTypeChoice().getValue());
                Storage root;
                try {
                    root = StorageManager.get(Integer.valueOf(i));
                } catch (ManagerException e) {
                    throw new ViewException(e);
                }
                scheme.setParent(root);
                originalPanel.saveScheme(scheme);

                target.addComponent(originalPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                // rerender form to update feedback panel
                target.addComponent(form);
            }

        });
        add(editStorageSchemeForm);
    }

    protected DropDownChoice<CustomChoice> renderRootTypeChoices(String id) {
        DropDownChoice<CustomChoice> dropDownChoice = null;
        ArrayList<CustomChoice> choicesArray = new ArrayList<CustomChoice>();
        Storage storage;
        try {
            storage = StorageManager.get(ConfigurationManager.get(
                ConfigurationKey.STRAIN_STORAGE_ROOT).getValue());
            choicesArray.add(new CustomChoice(storage.getName(), String.valueOf(storage.getId())));
            storage = StorageManager.get(ConfigurationManager.get(
                ConfigurationKey.PLASMID_STORAGE_ROOT).getValue());
            choicesArray.add(new CustomChoice(storage.getName(), String.valueOf(storage.getId())));
            storage = StorageManager.get(ConfigurationManager.get(
                ConfigurationKey.PART_STORAGE_ROOT).getValue());
            choicesArray.add(new CustomChoice(storage.getName(), String.valueOf(storage.getId())));
            storage = StorageManager.get(ConfigurationManager.get(
                ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT).getValue());
            choicesArray.add(new CustomChoice(storage.getName(), String.valueOf(storage.getId())));

        } catch (ManagerException e) {
            throw new ViewException(e);
        }

        // set existing
        if (getScheme().getParent() != null) {
            long existingId = getScheme().getParent().getId();
            for (CustomChoice choice : choicesArray) {
                if (Long.valueOf(choice.getValue()) == existingId) {
                    setRootTypeChoice(choice);
                    break;
                }
            }
        }

        dropDownChoice = new DropDownChoice<CustomChoice>(id, new PropertyModel<CustomChoice>(this,
                "rootTypeChoice"), new Model<ArrayList<CustomChoice>>(choicesArray),
                new ChoiceRenderer<CustomChoice>("name", "value"));
        dropDownChoice.setRequired(true);
        dropDownChoice.setLabel(new Model<String>("Storage Type Root"));
        return dropDownChoice;

    }

    public void setParentPanel(Panel parentPanel) {
        this.parentPanel = parentPanel;
    }

    public Panel getParentPanel() {
        return parentPanel;
    }

    public void setLocationItems(LinkedList<StorageSchemeEditItemPanel> locationItems) {
        this.locationItems = locationItems;
    }

    public LinkedList<StorageSchemeEditItemPanel> getLocationItems() {
        return locationItems;
    }

    public void setScheme(Storage scheme) {
        this.scheme = scheme;
    }

    public Storage getScheme() {
        return scheme;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setRootTypeChoice(CustomChoice rootTypeChoice) {
        this.rootTypeChoice = rootTypeChoice;
    }

    public CustomChoice getRootTypeChoice() {
        return rootTypeChoice;
    }

}
