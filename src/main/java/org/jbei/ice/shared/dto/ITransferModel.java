package org.jbei.ice.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.jbei.ice.lib.dao.IModel;

/**
 * interface for data transfer objects
 *
 * @author Hector Plahar
 */
public interface ITransferModel<T extends IModel> extends IsSerializable {

    void toInfo(T model);
}
