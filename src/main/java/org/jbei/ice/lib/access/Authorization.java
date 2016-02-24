package org.jbei.ice.lib.access;

import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;
import org.jbei.ice.storage.IRepository;
import org.jbei.ice.storage.model.Account;

/**
 * Used in instances where access permissions are to be enforced.
 *
 * @author Hector Plahar
 */
public class Authorization<T extends DataModel> {

    private final IRepository<T> repository;

    public Authorization(IRepository<T> repository) {
        this.repository = repository;
    }

    protected Account getAccount(String userId) {
        if (userId == null)
            return null;

        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (account == null)
            throw new IllegalArgumentException("Could not retrieve account information for user " + userId);
        return account;
    }

    // performs validation based on owner or same group membership
    public IDataTransferModel get(String userId, long id) throws AuthorizationException {
        T object = getObjectById(id);
        if (object == null)
            return null;

        boolean isAdmin = isAdmin(userId);

        if (isAdmin || canRead(userId, object))
            return object.toDataTransferObject();

        throw new AuthorizationException(userId + " does not have authorization for this action");
    }

    protected String getOwner(T object) {
        return null;
    }

    protected T getObjectById(long id) {
        return repository.get(id);
    }

    public boolean isAdmin(String userId) {
        if (userId == null || userId.trim().isEmpty())
            return false;

        Account account = getAccount(userId);
        return account.getType() == AccountType.ADMIN;
    }

    public boolean canRead(String userId, T object) {
        return (isOwner(userId, object) || isAdmin(userId));
    }

    protected boolean isOwner(String userId, T object) {
        String owner = getOwner(object);
        return owner == null || userId.equalsIgnoreCase(owner);
    }

    public void expectRead(String userId, T object) throws PermissionException {
        if (!canRead(userId, object))
            throw new PermissionException(userId + " does not have access to object " + object);
    }

    /**
     * Should either be an administrator or the owner of the object to be able to write
     *
     * @param userId unique user identifier
     * @param object object write ownership is being checked against
     * @return true is user is an admin or the owner of the object
     */
    public boolean canWrite(String userId, T object) {
        if (isAdmin(userId))
            return true;

        String owner = getOwner(object);
        return owner == null || userId.equals(owner);
    }

    public void expectWrite(String userId, T object) throws PermissionException {
        if (!canWrite(userId, object))
            throw new PermissionException(userId + " is lacking write permissions");
    }

    public void expectAdmin(String userId) throws PermissionException {
        if (!isAdmin(userId))
            throw new PermissionException(userId + " attempting to access admin restricted action");
    }
}
