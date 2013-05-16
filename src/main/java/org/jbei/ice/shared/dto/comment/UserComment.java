package org.jbei.ice.shared.dto.comment;

import java.util.Date;

import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.IDTOModel;

/**
 * @author Hector Plahar
 */
public class UserComment implements IDTOModel {

    private AccountInfo user;
    private String message;
    private Date commentDate;

    public UserComment() {}

    public AccountInfo getUser() {
        return user;
    }

    public void setUser(AccountInfo user) {
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
}
