package org.jbei.ice.web.panels.adminpage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.managers.LocationManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.StorageScheme;
import org.jbei.ice.web.panels.EmptyMessagePanel;

public class StorageSchemeChoicePanel extends Panel {
    private static final long serialVersionUID = 1L;

    private List<ChoicePanelItem> storageChoiceItems;
    private List<StorageScheme> storageSchemes;
    private ListView<ChoicePanelItem> storageSchemeLinks;
    private Panel editPanel;

    public StorageSchemeChoicePanel(String id) {
        super(id);
        setOutputMarkupId(true);
        storageSchemes = new ArrayList<StorageScheme>();
        try {
            storageSchemes = LocationManager.getAllStorageSchemes();
        } catch (ManagerException e) {
            // it's ok. Just render empty.
        }
        renderStorageChoiceItems();

        renderListView();

        add(storageSchemeLinks);
        editPanel = new EmptyMessagePanel("editPanel", "");
        editPanel.setOutputMarkupId(true);

        add(editPanel);

        AjaxLink<Object> addNewLink = new AddNewLink<Object>("addNewLink", this);
        add(addNewLink);
        setOutputMarkupId(true);
    }

    private void renderListView() {
        storageSchemeLinks = new ListView<ChoicePanelItem>("storageSchemeListView",
                new PropertyModel<List<ChoicePanelItem>>(this, "storageChoiceItems")) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<ChoicePanelItem> item) {
                ChoicePanelItem schemeItem = item.getModelObject();
                item.add(schemeItem.getEditLink());
                item.add(schemeItem.getDeleteLink());
            }

        };
        storageSchemeLinks.setOutputMarkupId(true);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void renderStorageChoiceItems() {
        if (storageSchemes == null) {
            return;
        }

        ArrayList<ChoicePanelItem> panelItems = new ArrayList<ChoicePanelItem>();

        for (StorageScheme scheme : storageSchemes) {
            ChoicePanelItem panelItem = new ChoicePanelItem();
            StorageSchemeChoicePanel parentPanel = this;
            panelItem.setStorageSchemePanel(parentPanel);
            panelItem.setDeleteLink(new StorageDeleteLink("deleteLink", scheme, parentPanel));
            StorageEditLink storageEditLink = new StorageEditLink("editLink", scheme, parentPanel);

            panelItem.setEditLink(storageEditLink);
            panelItems.add(panelItem);
        }
        storageChoiceItems = panelItems;
    }

    public void removeStorageScheme(StorageScheme storageScheme) {

        try {
            LocationManager.delete(storageScheme);
            storageSchemes.remove(storageScheme);
        } catch (ManagerException e) {
            error("Could not delete scheme!");
        }
        renderStorageChoiceItems();
    }

    public void editStorageScheme(StorageScheme storageScheme) {
        StorageScheme selectedScheme = storageSchemes.get(storageSchemes.indexOf(storageScheme));
        editPanel = new StorageSchemeEditPanel("editPanel", selectedScheme, this);
        editPanel.setOutputMarkupId(true);
        replace(editPanel);
    }

    public void addNewScheme() {
        editPanel = new StorageSchemeEditPanel("editPanel", new StorageScheme(), this);
        editPanel.setOutputMarkupId(true);
        replace(editPanel);
    }

    public void saveScheme(StorageScheme scheme) {
        try {
            if (scheme.getId() == 0) {
                // this is a new scheme. Add to list of schemes after getting id
                scheme = LocationManager.update(scheme);
                storageSchemes.add(scheme);
            } else {
                LocationManager.update(scheme);
            }
            renderStorageChoiceItems();

            renderListView();
            replace(storageSchemeLinks);

            editPanel = new EmptyPanel("editPanel");
            editPanel.setOutputMarkupId(true);
            replace(editPanel);

        } catch (ManagerException e) {
            error("Could not save scheme!");
        }
    }

    public void setStorageLinks(List<ChoicePanelItem> storageChoiceItems) {
        this.storageChoiceItems = storageChoiceItems;
    }

    public List<ChoicePanelItem> getStorageLinks() {
        return storageChoiceItems;
    }

    public void setStorageSchemes(List<StorageScheme> storageSchemes) {
        this.storageSchemes = storageSchemes;
    }

    public List<StorageScheme> getStorageSchemes() {
        return storageSchemes;
    }

    public void setEditPanel(Panel editPanel) {
        this.editPanel = editPanel;
    }

    public Panel getEditPanel() {
        return editPanel;
    }

    public void setStorageSchemeLinks(ListView<ChoicePanelItem> storageSchemeLinks) {
        this.storageSchemeLinks = storageSchemeLinks;
    }

    public ListView<ChoicePanelItem> getStorageSchemeLinks() {
        return storageSchemeLinks;
    }

    class StorageDeleteLink<T> extends AjaxLink<T> {

        private static final long serialVersionUID = 1L;
        private StorageScheme scheme;
        private StorageSchemeChoicePanel parentPanel;

        public StorageDeleteLink(String id, StorageScheme scheme,
                StorageSchemeChoicePanel parentPanel) {
            super(id);
            this.setScheme(scheme);
            this.setParentPanel(parentPanel);
            setOutputMarkupId(true);
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            parentPanel.removeStorageScheme(scheme);
            target.addComponent(parentPanel);
        }

        public void setScheme(StorageScheme scheme) {
            this.scheme = scheme;
        }

        public StorageScheme getScheme() {
            return scheme;
        }

        public void setParentPanel(StorageSchemeChoicePanel parentPanel) {
            this.parentPanel = parentPanel;
        }

        public StorageSchemeChoicePanel getParentPanel() {
            return parentPanel;
        }

    }

    class StorageEditLink<T> extends AjaxLink<T> {
        private static final long serialVersionUID = 1L;
        private StorageScheme scheme;
        private StorageSchemeChoicePanel parentPanel;

        public StorageEditLink(String id, StorageScheme scheme, StorageSchemeChoicePanel parentPanel) {
            super(id);
            this.setScheme(scheme);
            this.setParentPanel(parentPanel);
            this.add(new Label("schemeLabel", scheme.getLabel()));
            setOutputMarkupId(true);
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            parentPanel.editStorageScheme(scheme);
            target.addComponent(parentPanel);
        }

        public void setScheme(StorageScheme scheme) {
            this.scheme = scheme;
        }

        public StorageScheme getScheme() {
            return scheme;
        }

        public void setParentPanel(StorageSchemeChoicePanel parentPanel) {
            this.parentPanel = parentPanel;
        }

        public StorageSchemeChoicePanel getParentPanel() {
            return parentPanel;
        }

    }

    class AddNewLink<T> extends AjaxLink<T> {
        private static final long serialVersionUID = 1L;
        private StorageSchemeChoicePanel parentPanel;

        public AddNewLink(String id, StorageSchemeChoicePanel parentPanel) {
            super(id);
            setParentPanel(parentPanel);
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            parentPanel.addNewScheme();
            target.addComponent(parentPanel);
        }

        public void setParentPanel(StorageSchemeChoicePanel parentPanel) {
            this.parentPanel = parentPanel;
        }

        public StorageSchemeChoicePanel getParentPanel() {
            return parentPanel;
        }
    }

    class ChoicePanelItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private StorageSchemeChoicePanel storageSchemePanel;

        private AjaxLink<Object> editLink;
        private AjaxLink<Object> deleteLink;

        public void setStorageSchemePanel(StorageSchemeChoicePanel storageSchemePanel) {
            this.storageSchemePanel = storageSchemePanel;
        }

        public StorageSchemeChoicePanel getStorageSchemePanel() {
            return storageSchemePanel;
        }

        public void setEditLink(AjaxLink<Object> editLink) {
            this.editLink = editLink;
        }

        public AjaxLink<Object> getEditLink() {
            return editLink;
        }

        public void setDeleteLink(AjaxLink<Object> deleteLink) {
            this.deleteLink = deleteLink;
        }

        public AjaxLink<Object> getDeleteLink() {
            return deleteLink;
        }

    };
}
