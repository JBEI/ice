package org.jbei.ice.lib.entry.sample;

import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.StorageLocation;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

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
            request.setPlateDescription(sampleRequest.getPlateDescription());
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
                                   List<SampleRequestStatus> status, String filter) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (account.getType() != AccountType.ADMIN)
            return getUserSamples(userId, null, start, limit, sort, asc);

        int count = dao.getCount(filter, status);
        UserSamples samples = new UserSamples();
        samples.setCount(count);

        List<Request> results = dao.get(start, limit, sort, asc, filter, status);
        SampleService sampleService = new SampleService();

        for (Request request : results) {
            SampleRequest sampleRequest = request.toDataTransferObject();
            List<PartSample> location = sampleService.retrieveEntrySamples(userId, Long.toString(request.getEntry().getId()));
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

    public ByteArrayOutputStream generateCSVFile(String userId, ArrayList<Long> ids) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = new OutputStreamWriter(out);
        try (CSVWriter writer = new CSVWriter(streamWriter)) {
            SampleService sampleService = new SampleService();

            Set<Long> idSet = new HashSet<>(ids);
            for (long id : idSet) {
                Request request = dao.get(id);
                if (request == null)
                    continue;

                String[] line = new String[3];
                Entry entry = request.getEntry();
                line[0] = entry.getName();

                List<PartSample> samples = sampleService.retrieveEntrySamples(userId, Long.toString(request.getEntry().getId()));
                String plate = null;
                String well = null;

                if (samples.size() == 1) {
                    if (samples.get(0).getLocation().getType() == SampleType.GENERIC) {
                        plate = "generic";
                        well = "";
                    }
                } else {
                    for (PartSample partSample : samples) {
                        if (partSample.getLabel().contains("backup"))
                            continue;

                        // get plate
                        StorageLocation location = partSample.getLocation();
                        if (location == null)
                            continue;

                        if (location.getType() == SampleType.PLATE96) {
                            plate = location.getDisplay().replaceFirst("^0+(?!$)", "");
                        }

                        StorageLocation child = location.getChild();
                        while (child != null) {
                            if (child.getType() == SampleType.WELL) {
                                well = child.getDisplay();
                                break;
                            }
                            child = child.getChild();
                        }

                        if (!StringUtils.isEmpty(well) && !StringUtils.isEmpty(plate))
                            break;
                    }
                }

                if (plate == null || well == null)
                    continue;

                String email = request.getAccount().getEmail();
                int index = email.indexOf('@');
                char typeChar = request.getType() == SampleRequestType.LIQUID_CULTURE ? 'L' : 'A';

                line[1] = typeChar + " " + plate + " " + well + " " + email.substring(0, index);
                line[1] = line[1].trim().replaceAll(" +", " ");
                line[2] = request.getPlateDescription().trim().replaceAll(" +", " ");
                if (request.getGrowthTemperature() != null)
                    line[2] += " " + request.getGrowthTemperature();

                writer.writeNext(line);
            }
        }
        return out;
    }
}
