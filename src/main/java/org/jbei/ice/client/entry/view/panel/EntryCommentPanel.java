package org.jbei.ice.client.entry.view.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.lib.shared.dto.comment.UserComment;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Panel for comments
 *
 * @author Hector Plahar
 */
public class EntryCommentPanel extends Composite {

    private final FlexTable table;
    private AddCommentPanel commentArea;
    private ServiceDelegate<UserComment> delegate;
    private boolean hasData;
    private Button addCommentButton;

    public EntryCommentPanel() {
        table = new FlexTable();
        initWidget(table);
        table.setWidth("100%");

        addCommentButton = new Button("<i class=\"" + FAIconType.COMMENT_ALT.getStyleName() + "\"></i> Add Comment");
        commentArea = new AddCommentPanel();

        addCommentButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                table.getFlexCellFormatter().setVisible(2, 0, true);
                if (!hasData) {
                    table.getFlexCellFormatter().setVisible(3, 0, false);
                }
            }
        });

        table.setWidget(0, 0, addCommentButton);
        table.setHTML(1, 0, "");
        table.setWidget(2, 0, commentArea);
        table.getFlexCellFormatter().setVisible(2, 0, false);
        table.setHTML(3, 0, "<i class=\"font-75em pad-8\">No comments available</i>");
//        table.setVisible(false);

        setCancelHandler();
        setSubmitHandler();
    }

    public void setSampleOptions(ArrayList<SampleStorage> sampleOptions) {
        commentArea.setSampleOptions(sampleOptions);
    }

    protected void setCancelHandler() {
        commentArea.setCancelHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                commentArea.reset();
                table.getFlexCellFormatter().setVisible(2, 0, false);
                if (!hasData) {
                    table.getFlexCellFormatter().setVisible(3, 0, true);
                }
            }
        });
    }

    protected void setSubmitHandler() {
        commentArea.setSubmitHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (delegate == null)
                    return;
                delegate.execute(commentArea.getUserComment());
            }
        });
    }

    public void setCommentSubmitDelegate(ServiceDelegate<UserComment> delegate) {
        this.delegate = delegate;
    }

    public void setData(ArrayList<UserComment> data) {
        hasData = data != null && !data.isEmpty();
        if (!hasData) {
            table.clear();
            table.setWidget(0, 0, addCommentButton);
            table.setHTML(1, 0, "");
            table.setWidget(2, 0, commentArea);
            table.getFlexCellFormatter().setVisible(2, 0, false);
            table.setHTML(3, 0, "<i class=\"font-75em pad-8\">No comments available</i>");
            return;
        }

        Collections.sort(data, new Comparator<UserComment>() {
            @Override
            public int compare(UserComment o1, UserComment o2) {
                return o1.getCommentDate().compareTo(o2.getCommentDate());
            }
        });

        int row = 3;

        for (UserComment comment : data) {
            // display comment
            FlexTable commentTable = new FlexTable();
            commentTable.setHTML(0, 0, "<br>Submitted by <a href=\"#" + Page.PROFILE.getLink() + ";id="
                    + comment.getUser().getId() + ";s=profile"
                    + "\">" + comment.getUser().getFullName() + "</a> on "
                    + DateUtilities.formatDate(comment.getCommentDate()) + ": ");
            commentTable.getFlexCellFormatter().setStyleName(0, 0, "font-75em");
            commentTable.getFlexCellFormatter().setColSpan(0, 0, 3);

            commentTable.setHTML(1, 0, "<i style=\"font-size: 1em; color: #eee\" class=\""
                    + FAIconType.QUOTE_LEFT.getStyleName() + "\"></i>");
            commentTable.setHTML(2, 1, "<span style=\"padding: 2px\">" + comment.getMessage() + "</span>");
            commentTable.getFlexCellFormatter().setWidth(1, 1, "350px");
            commentTable.setHTML(3, 2, "<i style=\"font-size: 1em; color: #eee\" class=\""
                    + FAIconType.QUOTE_RIGHT.getStyleName() + "\"></i>");
            commentTable.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);

            table.setWidget(row, 0, commentTable);
            table.getFlexCellFormatter().setStyleName(row, 0, "pad-left-40");
            row += 1;
        }
    }

    public void addComment(UserComment comment) {
        table.getFlexCellFormatter().setVisible(2, 0, false);
        int row = table.getRowCount() + 2;
        FlexTable commentTable = new FlexTable();
        commentTable.setHTML(0, 0, "<br>Submitted by <a href=\"#" + Page.PROFILE.getLink() + ";id="
                + comment.getUser().getId() + ";s=profile"
                + "\">" + comment.getUser().getFullName() + "</a> on "
                + DateUtilities.formatDate(comment.getCommentDate()) + ": ");
        commentTable.getFlexCellFormatter().setStyleName(0, 0, "font-75em");
        commentTable.getFlexCellFormatter().setColSpan(0, 0, 3);

        commentTable.setHTML(1, 0, "<i style=\"font-size: 1em; color: #eee\" class=\""
                + FAIconType.QUOTE_LEFT.getStyleName() + "\"></i>");
        commentTable.setHTML(2, 1, "<span style=\"padding: 2px\">" + comment.getMessage() + "</span>");
        commentTable.getFlexCellFormatter().setWidth(1, 1, "350px");
        commentTable.setHTML(3, 2, "<i style=\"font-size: 1em; color: #eee\" class=\""
                + FAIconType.QUOTE_RIGHT.getStyleName() + "\"></i>");
        commentTable.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);

        table.setWidget(row, 0, commentTable);
        table.getFlexCellFormatter().setStyleName(row, 0, "pad-left-40");
    }

    protected class AddCommentPanel extends Composite {

        private final TextArea area;
        private Button submit;
        private HTML cancel;
        private ListBox sampleOptions;

        public AddCommentPanel() {
            area = new TextArea();
            area.getElement().setAttribute("placeHolder", "Enter comment");
            area.setVisibleLines(6);
            area.setCharacterWidth(70);
            area.setStyleName("input_box");
            submit = new Button("Submit");
            cancel = new HTML("Cancel");
            cancel.setStyleName("footer_feedback_widget");
            cancel.addStyleName("font-70em");
            cancel.addStyleName("display-inline");
            sampleOptions = new ListBox();
            sampleOptions.addItem("None");
            sampleOptions.addItem("All");
            sampleOptions.setStyleName("pull_down");

            FlexTable flexTable = new FlexTable();
            flexTable.setWidget(0, 0, area);

            String html = "<b class=\"font-75em\" style=\"vertical-align: top\">Affected Samples</b><br>"
                    + "<span  id=\"sample_selection\"></span>";
            HTMLPanel panel = new HTMLPanel(html);
            flexTable.setWidget(0, 1, panel);
            flexTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);
            panel.add(sampleOptions, "sample_selection");

            flexTable.setWidget(1, 0, submit);
            flexTable.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_RIGHT);
            flexTable.setWidget(1, 1, cancel);
            initWidget(flexTable);
        }

        public void setSampleOptions(ArrayList<SampleStorage> samples) {
            sampleOptions.clear();
            sampleOptions.addItem("None");
            sampleOptions.addItem("All");
            reset();

            if (samples == null || samples.isEmpty()) {
                sampleOptions.setEnabled(false);
                return;
            }

            for (SampleStorage storage : samples) {
                sampleOptions.addItem(storage.getSample().getLabel());
            }
            sampleOptions.setEnabled(true);
            hasData = true;
        }

        public boolean validate() {
            if (area.getText().trim().isEmpty()) {
                area.setStyleName("input_box_error");
                return false;
            }

            area.setStyleName("input_box");
            return true;
        }

        public void reset() {
            area.setText("");
            area.setStyleName("input_box");
            sampleOptions.setSelectedIndex(0);
        }

        public void setSubmitHandler(final ClickHandler handler) {
            submit.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (validate()) {
                        handler.onClick(event);
                        reset();
                    }
                }
            });
        }

        public void setCancelHandler(ClickHandler handler) {
            cancel.addClickHandler(handler);
        }

        public UserComment getUserComment() {
            String selected = sampleOptions.getItemText(sampleOptions.getSelectedIndex());
            if ("None".equalsIgnoreCase(selected)) {
                return new UserComment(area.getText().trim());
            }

            if ("All".equalsIgnoreCase(selected)) {
                String txt = "";
                for (int i = 0; i < sampleOptions.getItemCount() - 1; i += 1) {
                    String text = sampleOptions.getItemText(i);
                    if ("None".equals(text) || "All".equals(text))
                        continue;

                    txt += (text + ",");
                }
                txt += sampleOptions.getItemText(sampleOptions.getItemCount() - 1);
                return new UserComment("<b>Affected Samples: </b>" + txt + "<br><br>" + area.getText().trim());
            }

            String txt = sampleOptions.getItemText(sampleOptions.getSelectedIndex());
            return new UserComment("<b>Affected Sample: </b>" + txt + "<br><br>" + area.getText().trim());
        }
    }
}
