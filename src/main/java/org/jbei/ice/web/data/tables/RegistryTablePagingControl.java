package org.jbei.ice.web.data.tables;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

class RegistryTablePagingControl extends Panel {

    private static final long serialVersionUID = 1L;
    private static final String PAGE_SIZE_CONTROL_ID = "page_size_controls";
    private final DataTable<?> table;

    private final AjaxLink<String> firstLink;
    private final AjaxLink<String> secondLink;
    private final AjaxLink<String> thirdLink;

    public RegistryTablePagingControl(final DataTable<?> table) {
        super(PAGE_SIZE_CONTROL_ID);
        this.table = table;

        firstLink = generatePageSizeLink("15PerPageLink", "15PerPageLabel", 15);
        secondLink = generatePageSizeLink("50PerPageLink", "50PerPageLabel", 50);
        thirdLink = generatePageSizeLink("100PerPageLink", "100PerPageLabel", 100);

        add(firstLink);
        add(secondLink);
        add(thirdLink);
    }

    protected AjaxLink<String> generatePageSizeLink(String id, String labelId, final int pageSize) {
        AjaxLink<String> link = new AjaxLink<String>(id) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                RegistryTablePagingControl.this.table.setRowsPerPage(pageSize);
                target.addComponent(RegistryTablePagingControl.this.table);
                modifyLinksCSS();
            }
        };

        link.add(new Label(labelId, String.valueOf(pageSize)));

        if (currentRowSize() == pageSize) {
            link.add(new SimpleAttributeModifier("class", "disabled"));
        } else {
            link.add(new SimpleAttributeModifier("class", ""));
        }

        return link;
    }

    protected int currentRowSize() {
        return this.table.getRowsPerPage();
    }

    protected void modifyLinksCSS() {
        switch (currentRowSize()) {
        case 15:
            firstLink.add(new SimpleAttributeModifier("class", "disabled"));
            secondLink.add(new SimpleAttributeModifier("class", ""));
            thirdLink.add(new SimpleAttributeModifier("class", ""));
            break;

        case 50:
            firstLink.add(new SimpleAttributeModifier("class", ""));
            secondLink.add(new SimpleAttributeModifier("class", "disabled"));
            thirdLink.add(new SimpleAttributeModifier("class", ""));
            break;

        case 100:
            firstLink.add(new SimpleAttributeModifier("class", ""));
            secondLink.add(new SimpleAttributeModifier("class", ""));
            thirdLink.add(new SimpleAttributeModifier("class", "disabled"));
            break;

        default:
            firstLink.add(new SimpleAttributeModifier("class", ""));
            secondLink.add(new SimpleAttributeModifier("class", ""));
            thirdLink.add(new SimpleAttributeModifier("class", ""));
        }
    }
}
