package org.jbei.ice.client.bulkimport.panel;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;

public class SheetHeaderPanel extends Composite {

    private TextBox draftInput;
    private Button draftSave;
    private Button submit;
    private Button reset;
    private final HTMLPanel panel;

    public SheetHeaderPanel() {
        String html = "<span style=\"width: 50%; text-align: left; display: inline-block;\"><span style=\"display: inline-block;\" id=\"input_draft_name\"></span> &nbsp; <span id=\"btn_save_draft\"></span></span>"
                + "<span style=\"width: 47%; text-align: right; float:right; display: inline-block;\"><span id=\"btn_reset\"></span><span id=\"btn_submit\"></span></span>";
        panel = new HTMLPanel(html);

        init();

        panel.add(draftInput, "input_draft_name");
        panel.add(draftSave, "btn_save_draft");
        panel.add(reset, "btn_reset");
        panel.add(submit, "btn_submit");
        initWidget(panel);
    }

    private void init() {
        draftInput = new TextBox();
        draftInput.setWidth("300px");
        draftInput.setStylePrimaryName("bulk_import_draft_input");

        draftSave = new Button("Save Draft");
        draftSave.setStyleName("bulk_import_draft_save_button");

        reset = new Button("Reset");
        reset.setStyleName("bulk_import_reset_button");

        submit = new Button("Submit");
        submit.setStyleName("bulk_import_submit_button");
    }

    public Button getSubmit() {
        return this.submit;
    }

    public Button getDraftSave() {
        return draftSave;
    }

    public Button getReset() {
        return this.reset;
    }

    public TextBox getDraftInput() {
        return this.draftInput;
    }

    public void setDraftName(String name) {
        panel.remove(draftInput);
        HTML label = new HTML("<span>" + name + "</span>");
        panel.add(label, "input_draft_name");
    }
}
