package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.model.Model;

public class DeletionMessagePage extends ProtectedPage {
    protected static final long serialVersionUID = 1L;

    private String title = null;

    public DeletionMessagePage(PageParameters parameters) {
        super(parameters);
        add(new StyleSheetReference("stylesheet", DeletionMessagePage.class, "main.css"));
        setTitle("Entry Deleted");
        String part_number = parameters.getString("number");
        String recordId = parameters.getString("recordId");

        String msg1 = "The entry " + part_number + " has been deleted successfully.";
        String msg2 = "Id: " + recordId;
        setMessages(msg1, msg2);
    }


    private void setMessages(String message1, String message2) {
        add(new Label("message1", new Model<String>(message1)));
        add(new Label("message2", new Model<String>(message2)));
    }

    private void setTitle(String title) {
        this.title = title;
    }

    @Override
    protected String getTitle() {
        return title;
    }

}
