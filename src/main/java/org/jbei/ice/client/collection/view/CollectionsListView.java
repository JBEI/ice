package org.jbei.ice.client.collection.view;

import org.jbei.ice.client.collection.CollectionListPager;
import org.jbei.ice.client.collection.presenter.CollectionsListPresenter;
import org.jbei.ice.client.collection.table.CollectionListTable;
import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.common.HeaderView;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CollectionsListView extends Composite implements CollectionsListPresenter.Display {

    private CollectionListTable systemCollectionTable;
    private CollectionListTable userCollectionTable;
    private Button addCollectionButton;
    private Button submitCollectionButton;
    private Button cancelAddCollectionButton;
    private final Widget addCollectionWidget;
    private final VerticalPanel contentPanel;
    private int addCollectionIndex;
    private TextBox nameInput;
    private TextBox descriptionInput;

    public CollectionsListView() {

        HeaderPanel panel = new HeaderPanel();
        initWidget(panel);

        systemCollectionTable = new CollectionListTable();
        userCollectionTable = new CollectionListTable();
        addCollectionButton = new Button("Add");
        submitCollectionButton = new Button("Submit");
        cancelAddCollectionButton = new Button("Cancel");
        addCollectionWidget = createAddWidget();
        contentPanel = new VerticalPanel();
        contentPanel.setWidth("100%");

        panel.setWidth("100%");
        panel.setHeight("100%");

        panel.setHeaderWidget(createHeader());
        panel.setContentWidget(createContent());
        panel.setFooterWidget(Footer.getInstance());
    }

    @Override
    public String getCollectionName() {
        return nameInput.getText();
    }

    @Override
    public String getCollectionDescription() {
        return descriptionInput.getText();
    }

    @Override
    public Button getAddCollectionButton() {
        return this.addCollectionButton;
    }

    @Override
    public Button getCancelSubmitCollectionButton() {
        return this.cancelAddCollectionButton;
    }

    public Button getSubmitCollectionButton() {
        return this.submitCollectionButton;
    }

    protected Widget createContent() {

        HorizontalPanel systemHeader = new HorizontalPanel();
        systemHeader.setWidth("100%");

        // label
        HTML add = new HTML("<b>System Collections</b>");
        systemHeader.add(add);
        systemHeader.setCellHorizontalAlignment(add, HasAlignment.ALIGN_LEFT);

        // pager
        CollectionListPager pager = new CollectionListPager();
        pager.setDisplay(systemCollectionTable);
        systemHeader.add(pager);
        systemHeader.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_RIGHT);

        contentPanel.add(systemHeader);
        contentPanel.add(systemCollectionTable);

        final HorizontalPanel userHeader = new HorizontalPanel();
        userHeader.setWidth("100%");

        // label
        HTML userLabel = new HTML("<b>User Collections</b>");
        userHeader.add(userLabel);
        userHeader.setCellHorizontalAlignment(userLabel, HasHorizontalAlignment.ALIGN_LEFT);

        // add 
        userHeader.add(addCollectionButton);
        userHeader.setCellHorizontalAlignment(addCollectionButton,
            HasHorizontalAlignment.ALIGN_LEFT);

        // pager
        CollectionListPager userPage = new CollectionListPager();
        userPage.setDisplay(userCollectionTable);
        userHeader.add(userPage);
        userHeader.setCellHorizontalAlignment(userPage, HasHorizontalAlignment.ALIGN_RIGHT);

        contentPanel.add(userHeader);
        addCollectionIndex = contentPanel.getWidgetIndex(userHeader);
        contentPanel.add(userCollectionTable);

        return contentPanel;
    }

    @Override
    public void showAddCollectionWidget() {
        if (contentPanel.getWidgetIndex(this.addCollectionWidget) != -1)
            return;

        contentPanel.insert(addCollectionWidget, addCollectionIndex + 1);
    }

    @Override
    public void hideAddCollectionWidget() {
        contentPanel.remove(this.addCollectionWidget);
    }

    /**
     * @return widget for creating a new collection
     */
    protected Widget createAddWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(new Label("Name"));
        nameInput = new TextBox();
        panel.add(nameInput);
        panel.add(new Label("Description"));
        descriptionInput = new TextBox();
        panel.add(descriptionInput);
        panel.add(submitCollectionButton);
        panel.add(cancelAddCollectionButton);
        return panel;
    }

    protected Widget createHeader() {
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.add(new HeaderView());
        panel.add(new HeaderMenu());
        return panel;
    }

    @Override
    public CollectionListTable getDataTable() {
        return this.systemCollectionTable;
    }

    @Override
    public CollectionListTable getUserDataTable() {
        return this.userCollectionTable;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

}
