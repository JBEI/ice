package org.jbei.ice.web.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.utils.RichTextRenderer;

public class WikiMarkupPanel extends AbstractMarkupPanel {
    private static final long serialVersionUID = 1L;

    private Fragment markupFragment;
    private Fragment previewFragment;

    @SuppressWarnings("rawtypes")
    private AjaxLink wikiMarkupLink;
    @SuppressWarnings("rawtypes")
    private AjaxLink previewMarkupLink;

    @SuppressWarnings("rawtypes")
    private TextArea markupTextArea;
    private MultiLineLabel renderedLabel;

    @SuppressWarnings("rawtypes")
    public WikiMarkupPanel(String id) {
        super(id);

        wikiMarkupLink = new AjaxLink("wikiMarkupLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                wikiMarkupLink.add(new SimpleAttributeModifier("class", "active"));
                previewMarkupLink.add(new SimpleAttributeModifier("class", "inactive"));

                previewFragment.setVisible(false);
                markupFragment.setVisible(true);

                target.addComponent(wikiMarkupLink);
                target.addComponent(previewMarkupLink);
                target.addComponent(markupFragment);
                target.addComponent(previewFragment);
            }
        };

        previewMarkupLink = new AjaxLink("previewLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                wikiMarkupLink.add(new SimpleAttributeModifier("class", "inactive"));
                previewMarkupLink.add(new SimpleAttributeModifier("class", "active"));

                previewFragment.setVisible(true);
                markupFragment.setVisible(false);

                target.addComponent(wikiMarkupLink);
                target.addComponent(previewMarkupLink);
                target.addComponent(previewFragment);
                target.addComponent(markupFragment);
            }
        };

        wikiMarkupLink.setOutputMarkupId(true);
        previewMarkupLink.setOutputMarkupId(true);
        wikiMarkupLink.add(new SimpleAttributeModifier("class", "active"));
        previewMarkupLink.add(new SimpleAttributeModifier("class", "inactive"));

        add(wikiMarkupLink);
        add(previewMarkupLink);

        markupFragment = createMarkupFragment();
        previewFragment = createPreviewFragment();

        previewFragment.setVisible(false);
        markupFragment.setVisible(true);

        add(markupFragment);
        add(previewFragment);
    }

    private Fragment createMarkupFragment() {
        Fragment fragment = new Fragment("markupPlaceholderFragment", "markupFragment", this);

        fragment.setOutputMarkupPlaceholderTag(true);
        fragment.setOutputMarkupId(true);

        Form<Void> form = new Form<Void>("form");
        add(form);

        markupTextArea = new TextArea<String>("notes", new Model<String>(""));
        markupTextArea.setEscapeModelStrings(false);

        OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                markupData = markupTextArea.getDefaultModelObjectAsString();
                String renderedData = "";

                if (markupData != null) {
                    renderedData = RichTextRenderer.wikiToHtml(markupData);
                }

                renderedLabel.setDefaultModelObject(renderedData);
            }
        };

        markupTextArea.add(onChangeAjaxBehavior);

        form.add(markupTextArea);

        fragment.add(form);

        return fragment;
    }

    private Fragment createPreviewFragment() {
        Fragment fragment = new Fragment("previewPlaceholderFragment", "previewFragment", this);

        fragment.setOutputMarkupPlaceholderTag(true);
        fragment.setOutputMarkupId(true);

        renderedLabel = new MultiLineLabel("preview", new Model<String>(""));
        renderedLabel.setEscapeModelStrings(false);

        fragment.add(renderedLabel);

        return fragment;
    }

    @SuppressWarnings("unchecked")
    public final TextArea<String> getMarkupTextArea() {
        return markupTextArea;
    }

    @Override
    public void setData(String data) {
        markupTextArea.setDefaultModel(new Model<String>(data));
        markupData = data;

        String renderedData = "";

        if (markupData != null) {
            renderedData = RichTextRenderer.wikiToHtml(markupData);
        }

        renderedLabel.setDefaultModelObject(renderedData);
    }
}