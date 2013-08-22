package org.jbei.ice.lib.shared.dto.comment;

import java.util.Date;

import org.jbei.ice.lib.shared.dto.IDTOModel;
import org.jbei.ice.lib.shared.dto.user.User;

/**
 * DTO for {@link org.jbei.ice.lib.models.Comment}. Comments are tied to specific entries
 * and the entryId field is used to uniquely identify the
 *
 * @author Hector Plahar
 */
public class UserComment implements IDTOModel {

    private User user;
    private String message;
    private Date commentDate;
    private long entryId;

    public UserComment() {
    }

    public UserComment(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Date commentDate) {
        this.commentDate = commentDate;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }
}
