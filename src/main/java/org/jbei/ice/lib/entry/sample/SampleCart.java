package org.jbei.ice.lib.entry.sample;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.sample.SampleRequest;
import org.jbei.ice.lib.dto.sample.SampleRequestStatus;
import org.jbei.ice.lib.dto.sample.SampleRequestType;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.RequestDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Request;

import java.util.Date;
import java.util.List;

/**
 * Cart that handles samples (for specified user) in ICE
 *
 * @author Hector Plahar
 */
public class SampleCart {

    private final String userId;
    private final EntryDAO entryDAO;
    private final RequestDAO dao;

    public SampleCart(String userId) {
        this.userId = userId;
        this.entryDAO = DAOFactory.getEntryDAO();
        this.dao = DAOFactory.getRequestDAO();
    }

    /**
     * Creates a new sample request for the specified user and specified entry.
     * The default status is "IN CART"
     */
    public boolean addRequest(SampleRequest sampleRequest) {
        long partId = sampleRequest.getPartData().getId();
        Entry entry = entryDAO.get(sampleRequest.getPartData().getId());

        if (entry == null)
            throw new IllegalArgumentException("Cannot find entry with id: " + partId);

        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        // check if sample is already in cart with status of "IN CART"
        try {
            List<Request> requests = dao.getSampleRequestByStatus(account, entry, SampleRequestStatus.IN_CART);
            if (requests != null && !requests.isEmpty())
                return true;

            Request request = new Request();
            request.setAccount(account);
            request.setGrowthTemperature(sampleRequest.getGrowthTemperature());
            request.setEntry(entry);
            if (sampleRequest.getRequestType() == null)
                sampleRequest.setRequestType(SampleRequestType.LIQUID_CULTURE);
            request.setType(sampleRequest.getRequestType());
            request.setRequested(new Date(System.currentTimeMillis()));
            request.setUpdated(request.getRequested());
            request.setPlateDescription(sampleRequest.getPlateDescription());
            return dao.create(request) != null;
        } catch (DAOException e) {
            Logger.error(e);
            return false;
        }
    }

    public SampleRequest removeRequest(long requestId) {
        try {
            Request request = dao.get(requestId);
            if (request == null)
                return null;

            if (!request.getAccount().getEmail().equalsIgnoreCase(userId))
                return null;

            dao.delete(request);
            return request.toDataTransferObject();
        } catch (DAOException de) {
            Logger.error(de);
            return null;
        }
    }
}
