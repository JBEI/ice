package org.jbei.ice.lib.entry.sample;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.sample.*;
import org.jbei.ice.lib.email.EmailFactory;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.RequestDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Request;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Handler for sample requests
 *
 * @author Hector Plahar
 */
public class RequestRetriever {

    private final RequestDAO dao;
    private final EntryDAO entryDAO;

    public RequestRetriever() {
        this.dao = DAOFactory.getRequestDAO();
        this.entryDAO = DAOFactory.getEntryDAO();
    }

    /**
     * Creates a new sample request for the specified user and specified entry.
     * The default status is "IN CART"
     */
    public boolean placeSampleInCart(String userId, SampleRequest sampleRequest) {
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
            return dao.create(request) != null;
        } catch (DAOException e) {
            Logger.error(e);
            return false;
        }
    }

    public UserSamples getUserSamples(String userId, SampleRequestStatus status, int start, int limit, String sort,
                                      boolean asc) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        UserSamples samples = new UserSamples();
        int count = dao.getCount(account);
        samples.setCount(count);

        List<Request> requestList = dao.getAccountRequests(account, status, start, limit, sort, asc);

        for (Request request : requestList)
            samples.getRequests().add(request.toDataTransferObject());

        return samples;
    }

    public UserSamples getRequests(String userId, int start, int limit, String sort, boolean asc,
                                   SampleRequestStatus status, String filter) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (account.getType() != AccountType.ADMIN)
            return getUserSamples(userId, null, start, limit, sort, asc);

        int count = dao.getCount(status, filter);
        UserSamples samples = new UserSamples();
        samples.setCount(count);

        List<Request> results = dao.get(start, limit, sort, asc, status, filter);
        SampleService sampleService = new SampleService();

        for (Request request : results) {
            SampleRequest sampleRequest = request.toDataTransferObject();
            ArrayList<PartSample> location = sampleService.retrieveEntrySamples(userId, request.getEntry().getId());
            sampleRequest.setLocation(location);
            samples.getRequests().add(sampleRequest);
        }

        return samples;
    }

    public SampleRequest removeSampleFromCart(String userId, long requestId) {
        try {
            Request request = dao.get(requestId);
            if (request == null)
                return null;

            if (!request.getAccount().getEmail().equalsIgnoreCase(userId))
                return null;

            Logger.info(userId + ": Removing sample from cart for entry " + request.getEntry().getId());
            dao.delete(request);
            return request.toDataTransferObject();
        } catch (DAOException de) {
            Logger.error(de);
            return null;
        }
    }

    public SampleRequest updateStatus(String userId, long requestId, SampleRequestStatus newStatus) {
        Request request = dao.get(requestId);
        if (request == null)
            return null;

        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (!request.getAccount().getEmail().equalsIgnoreCase(userId) && account.getType() != AccountType.ADMIN) {
            throw new PermissionException("No permissions for request");
        }

        if (request.getStatus() == newStatus)
            return request.toDataTransferObject();

        request.setStatus(newStatus);
        request.setUpdated(new Date());
        return dao.update(request).toDataTransferObject();
    }

    public boolean setRequestsStatus(String userId, ArrayList<Long> ids, SampleRequestStatus status) {
        boolean sendEmail = status == SampleRequestStatus.PENDING;
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        for (long id : ids) {
            Request request = dao.get(id);
            if (request == null)
                continue;

            if (!request.getAccount().getEmail().equalsIgnoreCase(userId) && account.getType() != AccountType.ADMIN)
                continue;

            request.setStatus(status);
            request.setRequested(new Date());
            dao.update(request);
        }

        // send email to strain archivist
        if (sendEmail) {
            String email = Utils.getConfigValue(ConfigurationKey.BULK_UPLOAD_APPROVER_EMAIL);
            if (email != null && !email.isEmpty()) {
                String subject = "Sample request";
                String body = "A sample request has been received from " + account.getFullName() + " for " + ids.size();
                if (ids.size() == 1)
                    body += " sample";
                else
                    body += " samples";
                body += "\n\nPlease go to the following link to review pending requests.\n\n";
                body += Utils.getConfigValue(ConfigurationKey.URI_PREFIX) + "/admin/samples";
                EmailFactory.getEmail().send(email, subject, body);
            }
        }

        return true;
    }
}
