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
        setMessage("The entry has been deleted successfully");
    }


    private void setMessage(String message) {
        add(new Label("message", new Model<String>(message)));
    }

    private void setTitle(String title) {
        this.title = title;
    }

    @Override
    protected String getTitle() {
        return title;
    }

}
