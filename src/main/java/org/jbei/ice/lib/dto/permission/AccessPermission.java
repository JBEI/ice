package org.jbei.ice.lib.dto.permission;

import org.jbei.ice.lib.dao.IDataTransferModel;

public class AccessPermission implements IDataTransferModel {

    private Type type;
    private Article article;
    private long typeId;      // id for type of permission (entry or folder)
    private long articleId;   // id for article being acted on (group or account)
    private String display;   // account or group name

    public AccessPermission() {
    }

    public AccessPermission(Article article, long articleId, Type type, long typeId, String display) {
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

    public enum Type implements IDataTransferModel {
        READ_ENTRY, WRITE_ENTRY, READ_FOLDER, WRITE_FOLDER;
    }

    public enum Article implements IDataTransferModel {
        ACCOUNT, GROUP
    }

    @Override
    public String toString() {
        String typeName = type == null ? "" : type.name();
        return typeName + " (" + typeId + ") for " + article.name() + "(" + articleId + ")";
    }

    public boolean equals(AccessPermission access) {
        return access != null
                && access.getType() == type
                && access.getTypeId() == typeId
                && access.getArticle() == article
                && access.getArticleId() == articleId
                && access.getDisplay().equalsIgnoreCase(display);
    }
}
