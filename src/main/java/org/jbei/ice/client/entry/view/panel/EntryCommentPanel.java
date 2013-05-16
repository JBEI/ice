package org.jbei.ice.client.entry.view.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.comment.UserComment;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Panel for comments
 *
 * @author Hector Plahar
 */
public class EntryCommentPanel extends Composite {

    private final FlexTable table;
    private AddCommentPanel commentArea;

    public EntryCommentPanel() {
        table = new FlexTable();
        initWidget(table);
        table.setWidth("100%");

        Button addCommentButton = new Button(
                "<i class=\"" + FAIconType.COMMENT_ALT.getStyleName() + "\"></i> Add Comment");
        commentArea = new AddCommentPanel();

        addCommentButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                table.getFlexCellFormatter().setVisible(2, 0, true);
            }
        });

        table.setWidget(0, 0, addCommentButton);
        table.setHTML(1, 0, "");
        table.setWidget(2, 0, commentArea);
        table.getFlexCellFormatter().setVisible(2, 0, false);
        table.setHTML(3, 0, "<i class=\"font-75em pad-8\">No comments available</i>");

        setCancelHandler();
    }

    protected void setCancelHandler() {
        commentArea.setCancelHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                commentArea.reset();
                table.getFlexCellFormatter().setVisible(2, 0, false);
            }
        });
    }

    public void setCommentSubmitHandler(ClickHandler handler) {
        commentArea.setSubmitHandler(handler);
    }

    public String getComment() {
        return commentArea.getCommentString();
    }

    public void setData(ArrayList<UserComment> data) {
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
            commentTable.setHTML(0, 0, "Submitted by <a href=\"#" + Page.PROFILE.getLink() + ";id="
                    + comment.getUser().getId() + ";s=profile"
                    + "\">" + comment.getUser().getFullName() + "</a> on "
                    + DateUtilities.formatDate(comment.getCommentDate()) + ": ");
            commentTable.getFlexCellFormatter().setStyleName(0, 0, "font-75em");
            commentTable.getFlexCellFormatter().setColSpan(0, 0, 3);

            commentTable.setHTML(1, 0, "<i style=\"font-size: 2em; color: #dedede\" class=\""
                    + FAIconType.QUOTE_LEFT.getStyleName() + "\"></i>");
            commentTable.setHTML(2, 1, comment.getMessage());
            commentTable.getFlexCellFormatter().setWidth(1, 1, "300px");
            commentTable.setHTML(3, 2, "<i style=\"font-size: 2em; color: #dedede\" class=\""
                    + FAIconType.QUOTE_RIGHT.getStyleName() + "\"></i>");
            commentTable.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);

            table.setWidget(row, 0, commentTable);
            table.getFlexCellFormatter().setStyleName(row, 0, "pad-left-40");
            row += 1;
        }
    }

    public void addComment(UserComment comment) {
        int row = table.getRowCount() + 3;
        FlexTable commentTable = new FlexTable();
        commentTable.setHTML(0, 0, "Submitted by <a href=\"#" + Page.PROFILE.getLink() + ";id="
                + comment.getUser().getId() + ";s=profile"
                + "\">" + comment.getUser().getFullName() + "</a> on "
                + DateUtilities.formatDate(comment.getCommentDate()) + ": ");
        commentTable.getFlexCellFormatter().setStyleName(0, 0, "font-75em");
        commentTable.getFlexCellFormatter().setColSpan(0, 0, 3);

        commentTable.setHTML(1, 0, "<i style=\"font-size: 2em; color: #dedede\" class=\""
                + FAIconType.QUOTE_LEFT.getStyleName() + "\"></i>");
        commentTable.setHTML(2, 1, comment.getMessage());
        commentTable.getFlexCellFormatter().setWidth(1, 1, "300px");
        commentTable.setHTML(3, 2, "<i style=\"font-size: 2em; color: #dedede\" class=\""
                + FAIconType.QUOTE_RIGHT.getStyleName() + "\"></i>");
        commentTable.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);

        table.setWidget(row, 0, commentTable);
        table.getFlexCellFormatter().setStyleName(row, 0, "pad-left-40");
    }

    protected class AddCommentPanel extends Composite {

        private final TextArea area;
        private Button submit;
        private HTML cancel;

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

            String html = "<span id=\"enter_comment_area\"></span>"
                    + "<div><span id=\"comment_submit_widget\"></span><span id=\"comment_submit_cancel\"></span></div>";
            HTMLPanel panel = new HTMLPanel(html);
            panel.add(area, "enter_comment_area");
            panel.add(submit, "comment_submit_widget");
            panel.add(cancel, "comment_submit_cancel");
            initWidget(panel);
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
        }

        public void setSubmitHandler(final ClickHandler handler) {
            submit.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (validate()) {
                        handler.onClick(event);
                    }
                }
            });
        }

        public void setCancelHandler(ClickHandler handler) {
            cancel.addClickHandler(handler);
        }

        public String getCommentString() {
            return area.getText().trim();
        }
    }
}
