package org.jbei.ice.lib.entry.sample;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sample.model.Request;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestType;

/**
 * Handler for sample requests
 *
 * @author Hector Plahar
 */
public class SampleRequests {

    private final RequestDAO dao;

    public SampleRequests() {
        this.dao = new RequestDAO();
    }

    public SampleRequest getSampleRequest(Account account, long entryId) {
        Entry entry;
        try {
            entry = ControllerFactory.getEntryController().get(account, entryId);
        } catch (ControllerException e) {
            Logger.error(e);
            return null;
        }

        try {
            Request request = dao.getSampleRequest(account, entry);
            return Request.toDTO(request);
        } catch (DAOException e) {
            return null;
        }
    }

    /**
     * Creates a new sample request for the specified user and specified entry.
     *
     * @param account user account
     * @param entryID unique identifier for the entry
     * @param type    type of sample request
     */
    public void requestSample(Account account, long entryID, SampleRequestType type) {
        Entry entry;
        try {
            entry = ControllerFactory.getEntryController().get(account, entryID);
        } catch (ControllerException e) {
            Logger.error(e);
            return;
        }

        if (entry == null)
            throw new IllegalArgumentException("Cannot find entry with id: " + entryID);

        Request request = new Request();
        request.setAccount(account);
        request.setEntry(entry);
        request.setRequestType(type);
        request.setRequested(new Date(System.currentTimeMillis()));
        request.setUpdated(request.getRequested());
        try {
            dao.save(request);
        } catch (DAOException e) {
            Logger.error(e);
        }
    }

    public ArrayList<SampleRequest> getSampleRequestsInCart(Account account) {
        if (account == null)
            return null;

        try {
            List<Request> requestList = dao.getRequestListInCart(account);
            if (requestList == null)
                return null;

            ArrayList<SampleRequest> requests = new ArrayList<>();
            for (Request request : requestList)
                requests.add(Request.toDTO(request));
            return requests;
        } catch (DAOException e) {
            return null;
        }
    }
}
