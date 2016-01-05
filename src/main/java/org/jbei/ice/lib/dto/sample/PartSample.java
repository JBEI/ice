package org.jbei.ice.lib.dto.sample;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.StorageLocation;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * Parent class for the different types of samples
 *
 * @author Hector Plahar
 */
public class PartSample implements IDataTransferModel {

    private long id;
    private AccountTransfer depositor;
    private String label;
    private long creationTime;
    private boolean inCart;
    private StorageLocation location;
    private long partId;
    private boolean canEdit;
    private ArrayList<UserComment> comments;

    public PartSample() {
        this.comments = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public AccountTransfer getDepositor() {
        return depositor;
    }

    public void setDepositor(AccountTransfer depositor) {
        this.depositor = depositor;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isInCart() {
        return inCart;
    }

    public void setInCart(boolean inCart) {
        this.inCart = inCart;
    }

    public StorageLocation getLocation() {
        return location;
    }

    public void setLocation(StorageLocation location) {
        this.location = location;
    }

    public long getPartId() {
        return partId;
    }

    public void setPartId(long partId) {
        this.partId = partId;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public ArrayList<UserComment> getComments() {
        return comments;
    }
}
