package org.jbei.ice.web.panels;

import java.util.ArrayList;
import java.util.Set;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.permissions.WorkSpace;
import org.jbei.ice.lib.permissions.WorkSpaceItem;

public class WorkSpaceTablePanel extends Panel {

    private static final long serialVersionUID = 1L;

    public WorkSpaceTablePanel(String id) {
        super(id);
    }

    public WorkSpaceTablePanel(String id, WorkSpace workSpace, int limit) {
        super(id);
        @SuppressWarnings("unchecked")
        PageableListView workSpaceView = new PageableListView("itemRow", workSpace.toArrayList(),
                limit) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem panelItem) {
                // TODO Auto-generated method stub
                WorkSpaceItem workSpaceItem = (WorkSpaceItem) panelItem.getModelObject();
                panelItem.add(new CheckBox("checkBox", new Model<Boolean>(false)));
                Entry entry = workSpaceItem.getEntry();

                Set<Name> nameSet = entry.getNames();
                Set<PartNumber> partNumberSet = entry.getPartNumbers();

                PartNumber temp = (PartNumber) partNumberSet.toArray()[0];
                panelItem.add(new Label("partNumber", temp.getPartNumber()));

                Name temp2 = (Name) nameSet.toArray()[0];
                panelItem.add(new Label("name", temp2.getName()));

                panelItem.add(new Label("description", entry.getShortDescription()));
                panelItem.add(new Label("date", entry.getCreationTime().toString()));
            }

        };
        add(workSpaceView);
        add(new PagingNavigator("navigator", workSpaceView));
    }

    public WorkSpaceTablePanel(String id, ArrayList<Entry> entries, int limit) {
        super(id);
        @SuppressWarnings( { "unchecked", "serial" })
        PageableListView<?> listView = new PageableListView("itemRows", entries, limit) {
            @Override
            protected void populateItem(ListItem item) {
                Entry entry = (Entry) item.getModelObject();
                item.add(new CheckBox("checkBox", new Model<Boolean>(false)));
                Set<Name> nameSet = entry.getNames();
                Set<PartNumber> partNumberSet = entry.getPartNumbers();

                PartNumber temp = (PartNumber) partNumberSet.toArray()[0];
                item.add(new Label("partNumber", temp.getPartNumber()));

                Name temp2 = (Name) nameSet.toArray()[0];
                item.add(new Label("name", temp2.getName()));

                item.add(new Label("description", entry.getShortDescription()));

                item.add(new Label("date", entry.getCreationTime().toString()));
            }

        };
        add(listView);
        add(new PagingNavigator("navigator", listView));

    }

}
