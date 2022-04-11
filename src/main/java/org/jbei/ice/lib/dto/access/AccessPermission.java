package org.jbei.ice.lib.dto.access;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * Data transfer object for permissions
 */
public class AccessPermission implements IDataTransferModel {

    private long id;
    private Type type;        // type of permission (eg. read entry or write folder access privilege)
    private Article article;  // account or group or remote (object being acted on)
    private long typeId;      // id for type of permission (entry or folder or bulk upload)
    private long articleId;   // id for article being acted on (group or account)
    private String display;   // account or group or folder name
    private AccountTransfer account; // account making request
    private UserGroup group;
    private String userId;
    private FolderDetails folderDetails;
    private RegistryPartner partner;
    private String secret;

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

    public AccountTransfer getAccount() {
        return account;
    }

    public void setAccount(AccountTransfer account) {
        this.account = account;
    }

    public UserGroup getGroup() {
        return group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
    }

    public boolean isCanRead() {
        return type == Type.READ_ENTRY || type == Type.READ_FOLDER || type == Type.READ_UPLOAD;
    }

    public boolean isCanWrite() {
        return type == Type.WRITE_ENTRY || type == Type.WRITE_FOLDER || type == Type.WRITE_UPLOAD;
    }

    public boolean isEntry() {
        return type == Type.READ_ENTRY || type == Type.WRITE_ENTRY;
    }

    public boolean isFolder() {
        return type == Type.READ_FOLDER || type == Type.WRITE_FOLDER;
    }

    public boolean isUpload() {
        return type == Type.READ_UPLOAD || type == Type.WRITE_UPLOAD;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public FolderDetails getFolderDetails() {
        return folderDetails;
    }

    public void setFolderDetails(FolderDetails folderDetails) {
        this.folderDetails = folderDetails;
    }

    public RegistryPartner getPartner() {
        return partner;
    }

    public void setPartner(RegistryPartner partner) {
        this.partner = partner;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public enum Type implements IDataTransferModel {
        READ_ENTRY, WRITE_ENTRY, READ_FOLDER, WRITE_FOLDER, READ_UPLOAD, WRITE_UPLOAD
    }

    public enum Article implements IDataTransferModel {
        REMOTE, ACCOUNT, GROUP
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
