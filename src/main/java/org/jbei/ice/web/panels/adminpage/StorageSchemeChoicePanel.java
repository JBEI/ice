package org.jbei.ice.web.panels.adminpage;

import java.util.List;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class StorageSchemeChoicePanel extends Panel {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private List<AjaxLink> storageLinks;

    public StorageSchemeChoicePanel(String id) {
        super(id);

        @SuppressWarnings("rawtypes")
        ListView<AjaxLink> storageSchemeLinks = new ListView<AjaxLink>("storageSchemeListView",
                new PropertyModel<List<AjaxLink>>(this, "storageLinks")) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<AjaxLink> item) {
                item.add(item.getModelObject());
            }

        };
        storageSchemeLinks.setOutputMarkupId(true);

    }

    @SuppressWarnings("rawtypes")
    public void setStorageLinks(List<AjaxLink> storageLinks) {
        this.storageLinks = storageLinks;
    }

    @SuppressWarnings("rawtypes")
    public List<AjaxLink> getStorageLinks() {
        return storageLinks;
    }
}
