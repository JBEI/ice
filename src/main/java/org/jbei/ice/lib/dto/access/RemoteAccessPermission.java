package org.jbei.ice.lib.dto.access;

import org.jbei.ice.lib.access.AccessType;
import org.jbei.ice.lib.access.Article;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class RemoteAccessPermission implements IDataTransferModel {

    private String userId;
    private AccessType accessType;
    private Article article;
    private String secret;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
