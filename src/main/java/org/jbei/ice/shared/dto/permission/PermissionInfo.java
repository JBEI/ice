package org.jbei.ice.shared.dto.permission;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PermissionInfo implements IsSerializable {

    private Type type;
    private Article article;
    private long typeId;      // id for type of permission (entry or folder)
    private long articleId;   // id for article being acted on (group or account)
    private String display;   // account or group name

    public PermissionInfo() {}

    public PermissionInfo(Article article, long articleId, Type type, long typeId, String display) {
        this.article = article;
        this.type = type;
        this.typeId = typeId;
        this.articleId = articleId;
        this.display = display;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public long getArticleId() {
        return articleId;
    }

    public void setArticleId(long id) {
        this.articleId = id;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public boolean isCanRead() {
        return type == Type.READ_ENTRY || type == Type.READ_FOLDER;
    }

    public boolean isCanWrite() {
        return type == Type.WRITE_ENTRY || type == Type.WRITE_FOLDER;
    }

    public boolean isEntry() {
        return type == Type.READ_ENTRY || type == Type.WRITE_ENTRY;
    }

    public boolean isFolder() {
        return type == Type.READ_FOLDER || type == Type.WRITE_FOLDER;
    }

    public enum Type implements IsSerializable {
        READ_ENTRY, WRITE_ENTRY, READ_FOLDER, WRITE_FOLDER;
    }

    public enum Article implements IsSerializable {
        ACCOUNT, GROUP;
    }

    @Override
    public String toString() {
        String typeName = type == null ? "" : type.name();
        return typeName + " (" + typeId + ") for " + article.name() + "(" + articleId + ")";
    }

    public boolean equals(PermissionInfo info) {
        return info != null
                && info.getType() == type
                && info.getTypeId() == typeId
                && info.getArticle() == article
                && info.getArticleId() == articleId
                && info.getDisplay().equalsIgnoreCase(display);
    }
}
