package org.jbei.ice.client;

/**
 * A (currently) generic callback for reporting success/failure of some action
 *
 * @author hplahar
 */
public abstract class Callback<T> {

    public abstract void onSuccess(T t);

    public abstract void onFailure();
}
