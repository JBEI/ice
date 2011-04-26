package org.jbei.ice.web.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;

public class TextMarkupPanel extends AbstractMarkupPanel {
    private static final long serialVersionUID = 1L;

    private TextArea<String> notesTextArea;

    public TextMarkupPanel(String id) {
        super(id);

        Form<Void> form = new Form<Void>("form");
        add(form);

        notesTextArea = new TextArea<String>("notes", new Model<String>(""));
        notesTextArea.setEscapeModelStrings(false);

        OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                markupData = notesTextArea.getDefaultModelObjectAsString();
            }
        };

        notesTextArea.add(onChangeAjaxBehavior);

        form.add(notesTextArea);

        add(form);
    }

    public final TextArea<String> getNotesTextArea() {
        return notesTextArea;
    }

    @Override
    public void setData(String data) {
        markupData = data;

        notesTextArea.setDefaultModel(new Model<String>(markupData));
    }
}