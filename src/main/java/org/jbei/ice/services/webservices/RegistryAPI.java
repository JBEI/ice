package org.jbei.ice.services.webservices;

import java.util.ArrayList;

import javax.jws.WebService;

import org.hibernate.SessionException;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SearchController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.search.blast.BlastResult;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.lib.search.lucene.SearchResult;

@WebService
public class RegistryAPI {
    public String login(String login, String password) throws SessionException, ServiceException {
        String sessionId = null;

        try {
            SessionData sessionData = AccountController.authenticate(login, password);

            sessionId = sessionData.getSessionKey();
        } catch (InvalidCredentialsException e) {
            Logger.warn("Invalid credentials provided by user: " + login);

            throw new SessionException("Invalid credentials!");
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }

        return sessionId;
    }

    public void logout(String sessionId) throws ServiceException {
        try {
            AccountController.deauthenticate(sessionId);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }
    }

    public boolean isAuthenticated(String sessionId) throws ServiceException {
        boolean authenticated = false;

        try {
            authenticated = AccountController.isAuthenticated(sessionId);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }

        return authenticated;
    }

    public int getNumberOfPublicEntries() throws ServiceException {
        int result = 0;

        try {
            result = EntryManager.getNumberOfVisibleEntries();
        } catch (ManagerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }

        return result;
    }

    public ArrayList<SearchResult> search(String sessionId, String query) throws ServiceException {
        SearchController searchController = getSearchController(sessionId);

        ArrayList<SearchResult> results = null;

        try {
            results = searchController.find(query);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }

        return results;
    }

    public ArrayList<BlastResult> blastn(String sessionId, String querySequence)
            throws SessionException, ServiceException {
        SearchController searchController = getSearchController(sessionId);

        ArrayList<BlastResult> results = null;

        try {
            results = searchController.blastn(querySequence);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        } catch (ProgramTookTooLongException e) {
            Logger.error(e);

            throw new ServiceException(
                    "It took to long to search for sequence, try shorter sequence.");
        }

        return results;
    }

    public ArrayList<BlastResult> tblastx(String sessionId, String querySequence)
            throws SessionException, ServiceException {
        SearchController searchController = getSearchController(sessionId);

        ArrayList<BlastResult> results = null;

        try {
            results = searchController.tblastx(querySequence);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        } catch (ProgramTookTooLongException e) {
            Logger.error(e);

            throw new ServiceException(
                    "It took to long to search for sequence, try shorter sequence.");
        }

        return results;
    }

    public Entry getByRecordId(String sessionId, String entryId) throws SessionException,
            ServiceException, ServicePermissionException {
        EntryController entryController = getEntryController(sessionId);

        Entry entry = null;

        try {
            entry = entryController.getByRecordId(entryId);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        } catch (PermissionException e) {
            throw new ServicePermissionException("No permissions to read this entry by entryId: "
                    + entryId);
        }

        return entry;
    }

    public Entry getByPartNumber(String sessionId, String partNumber) throws SessionException,
            ServiceException, ServicePermissionException {
        EntryController entryController = getEntryController(sessionId);

        Entry entry = null;

        try {
            entry = entryController.getByPartNumber(partNumber);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        } catch (PermissionException e) {
            throw new ServicePermissionException(
                    "No permissions to read this entry by partNumber: " + partNumber);
        }

        return entry;
    }

    public boolean hasReadPermissions(String sessionId, String entryId) throws SessionException,
            ServiceException, ServicePermissionException {
        boolean result = false;

        EntryController entryController = getEntryController(sessionId);

        try {
            result = entryController.hasReadPermissionByRecordId(entryId);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }

        return result;
    }

    public boolean hasWritePermissions(String sessionId, String entryId) throws SessionException,
            ServiceException {
        boolean result = false;

        EntryController entryController = getEntryController(sessionId);

        try {
            result = entryController.hasWritePermissionByRecordId(entryId);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }

        return result;
    }

    public Plasmid createPlasmid(String sessionId, Plasmid plasmid) throws SessionException,
            ServiceException {
        EntryController entryController = getEntryController(sessionId);

        Entry remoteEntry = createEntry(sessionId, plasmid);

        Entry newEntry = null;
        try {
            newEntry = entryController.createEntry(remoteEntry);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }

        return (Plasmid) newEntry;
    }

    public Strain createStrain(String sessionId, Strain strain) throws SessionException,
            ServiceException {
        EntryController entryController = getEntryController(sessionId);

        Entry remoteEntry = createEntry(sessionId, strain);

        Entry newEntry = null;
        try {
            newEntry = entryController.createEntry(remoteEntry);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }

        return (Strain) newEntry;
    }

    public Part createPart(String sessionId, Part part) throws SessionException, ServiceException {
        EntryController entryController = getEntryController(sessionId);

        Entry remoteEntry = createEntry(sessionId, part);

        Entry newEntry = null;
        try {
            newEntry = entryController.createEntry(remoteEntry);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }

        return (Part) newEntry;
    }

    public Plasmid updatePlasmid(String sessionId, Plasmid plasmid) throws SessionException,
            ServiceException, ServicePermissionException {
        EntryController entryController = getEntryController(sessionId);

        Entry savedEntry = null;

        try {
            savedEntry = entryController.save(updateEntry(sessionId, plasmid));
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        } catch (PermissionException e) {
            throw new ServicePermissionException("No permissions to save this entry!");
        }

        return (Plasmid) savedEntry;
    }

    public Strain updateStrain(String sessionId, Strain strain) throws SessionException,
            ServiceException, ServicePermissionException {
        EntryController entryController = getEntryController(sessionId);

        Entry savedEntry = null;

        try {
            savedEntry = entryController.save(updateEntry(sessionId, strain));
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        } catch (PermissionException e) {
            throw new ServicePermissionException("No permissions to save this entry!");
        }

        return (Strain) savedEntry;
    }

    public Part updatePart(String sessionId, Part part) throws SessionException, ServiceException,
            ServicePermissionException {
        EntryController entryController = getEntryController(sessionId);

        Entry savedEntry = null;

        try {
            savedEntry = entryController.save(updateEntry(sessionId, part));
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        } catch (PermissionException e) {
            throw new ServicePermissionException("No permissions to save this entry!");
        }

        return (Part) savedEntry;
    }

    protected Entry createEntry(String sessionId, Entry entry) throws SessionException,
            ServiceException {
        if (entry == null) {
            throw new ServiceException("Failed to create null Entry!");
        }

        // Validate recordType
        if (entry instanceof Plasmid) {
            entry.setRecordType("plasmid");
        } else if (entry instanceof Strain) {
            entry.setRecordType("strain");
        } else if (entry instanceof Part) {
            entry.setRecordType("part");
        } else {
            throw new ServiceException(
                    "Invalid entry class! Accepted entries with classes Plasmid, Strain and Part.");
        }

        // Validate creator
        if (entry.getCreator() == null || entry.getCreator().isEmpty()) {
            throw new ServiceException("Creator is mandatory field!");
        }

        // Validate owner and ownerEmail
        if (entry.getOwner() == null || entry.getOwner().isEmpty() || entry.getOwnerEmail() == null
                || entry.getOwnerEmail().isEmpty()) {
            throw new ServiceException("Owner and OwnerEmail are mandatory fields!");
        }

        // Validate short description
        if (entry.getShortDescription() == null || entry.getShortDescription().isEmpty()) {
            throw new ServiceException("Short Description is mandatory field!");
        }

        // Validate status
        if (entry.getStatus() == null) {
            throw new ServiceException(
                    "Invalid status! Expected type: 'complete', 'in progress' or 'planned'.");
        } else if (!entry.getStatus().equals("complete")
                && !entry.getStatus().equals("in progress") && !entry.getStatus().equals("planned")) {
            throw new ServiceException(
                    "Invalid status! Expected type: 'complete', 'in progress' or 'planned'.");
        }

        // Validate bioSafetyLevel
        if (entry.getBioSafetyLevel() != 1 && entry.getBioSafetyLevel() != 2) {
            throw new ServiceException("Invalid bio safety level! Expected: '1' or '2'");
        }

        // Validate name
        if (entry.getNames() == null || entry.getNames().size() == 0) {
            throw new ServiceException("Name is mandatory! Expected at least one name.");
        } else {
            for (Name name : entry.getNames()) {
                if (name.getName() == null || name.getName().isEmpty()) {
                    throw new ServiceException("Name can't be null or empty!");
                }

                name.setEntry(entry);
            }
        }

        // Validate selection markers
        if (entry.getSelectionMarkers() != null && entry.getSelectionMarkers().size() > 0) {
            for (SelectionMarker selectionMarker : entry.getSelectionMarkers()) {
                if (selectionMarker.getName() == null || selectionMarker.getName().isEmpty()) {
                    throw new ServiceException("Selection Marker can't be null or empty!");
                }

                selectionMarker.setEntry(entry);
            }
        }

        // Validate links
        if (entry.getLinks() != null && entry.getLinks().size() > 0) {
            for (Link link : entry.getLinks()) {
                if (link.getLink() == null || link.getLink().isEmpty()) {
                    throw new ServiceException("Link can't be null or empty!");
                }

                link.setEntry(entry);
            }
        }

        // Validate entry funding sources
        if (entry.getEntryFundingSources() == null || entry.getEntryFundingSources().size() == 0) {
            throw new ServiceException(
                    "FundingSource is mandatory! Expected at least one FundingSource.");
        } else {
            for (EntryFundingSource entryFundingSource : entry.getEntryFundingSources()) {
                if (entryFundingSource.getFundingSource() == null) {
                    throw new ServiceException("FundingSource can't be null!");
                }

                if (entryFundingSource.getFundingSource().getFundingSource() == null
                        || entryFundingSource.getFundingSource().getFundingSource().isEmpty()) {
                    throw new ServiceException("FundingSource can't be null or empty!");
                }

                if (entryFundingSource.getFundingSource().getPrincipalInvestigator() == null
                        || entryFundingSource.getFundingSource().getPrincipalInvestigator()
                                .isEmpty()) {
                    throw new ServiceException("PrincipalInvestigator can't be null or empty!");
                }

                entryFundingSource.setEntry(entry);
            }
        }

        return entry;
    }

    protected Entry updateEntry(String sessionId, Entry entry) throws SessionException,
            ServiceException, ServicePermissionException {
        EntryController entryController = getEntryController(sessionId);

        Entry currentEntry = null;

        try {
            try {
                currentEntry = entryController.getByRecordId(entry.getRecordId());
            } catch (PermissionException e) {
                throw new ServicePermissionException("No permissions to read this entry!");
            }

            if (currentEntry == null) {
                throw new ServiceException("Invalid recordId for entry!");
            }

            if (!entryController.hasWritePermission(currentEntry)) {
                throw new ServicePermissionException("No permissions to change this entry!");
            }
        } catch (ControllerException e) {
            throw new ServiceException(e);
        }

        // Validate and set creator
        if (entry.getCreator() == null || entry.getCreator().isEmpty()) {
            throw new ServiceException("Creator is mandatory field!");
        } else {
            currentEntry.setCreator(entry.getCreator());
            currentEntry.setCreatorEmail(entry.getCreatorEmail());
        }

        // Validate and set owner
        if (entry.getOwner() == null || entry.getOwner().isEmpty()) {
            throw new ServiceException("Owner is mandatory field!");
        } else {
            currentEntry.setOwner(entry.getOwner());
        }

        // Validate and set ownerEmail
        if (entry.getOwnerEmail() == null || entry.getOwnerEmail().isEmpty()) {
            throw new ServiceException("OwnerEmail is mandatory field!");
        } else {
            currentEntry.setOwnerEmail(entry.getOwnerEmail());
        }

        // Validate and set short description
        if (entry.getShortDescription() == null || entry.getShortDescription().isEmpty()) {
            throw new ServiceException("Short Description is mandatory field!");
        } else {
            currentEntry.setShortDescription(entry.getShortDescription());
        }

        // Validate status
        if (entry.getStatus() == null) {
            throw new ServiceException(
                    "Invalid status! Expected type: 'complete', 'in progress' or 'planned'.");
        } else if (!entry.getStatus().equals("complete")
                && !entry.getStatus().equals("in progress") && !entry.getStatus().equals("planned")) {
            throw new ServiceException(
                    "Invalid status! Expected type: 'complete', 'in progress' or 'planned'.");
        } else {
            currentEntry.setStatus(entry.getStatus());
        }

        // Validate bioSafetyLevel
        if (entry.getBioSafetyLevel() != 1 && entry.getBioSafetyLevel() != 2) {
            throw new ServiceException("Invalid bio safety level! Expected: '1' or '2'");
        } else {
            currentEntry.setBioSafetyLevel(entry.getBioSafetyLevel());
        }

        currentEntry.setAlias(entry.getAlias());
        currentEntry.setKeywords(entry.getKeywords());
        currentEntry.setLongDescription(entry.getLongDescription());
        currentEntry.setReferences(entry.getReferences());
        currentEntry.setIntellectualProperty(entry.getIntellectualProperty());

        if (entry instanceof Plasmid) {
            ((Plasmid) currentEntry).setBackbone(((Plasmid) entry).getBackbone());
            ((Plasmid) currentEntry).setCircular(((Plasmid) entry).getCircular());
            ((Plasmid) currentEntry).setOriginOfReplication(((Plasmid) entry)
                    .getOriginOfReplication());
            ((Plasmid) currentEntry).setPromoters(((Plasmid) entry).getPromoters());
        } else if (entry instanceof Strain) {
            ((Strain) currentEntry).setHost(((Strain) entry).getHost());
            ((Strain) currentEntry).setPlasmids(((Strain) entry).getPlasmids());
            ((Strain) currentEntry).setGenotypePhenotype(((Strain) entry).getGenotypePhenotype());
        } else if (entry instanceof Part) {
            ((Part) currentEntry).setPackageFormat(((Part) entry).getPackageFormat());
        }

        // Validate and set name
        if (entry.getNames() == null || entry.getNames().size() == 0) {
            throw new ServiceException("Name is mandatory! Expected at least one name.");
        } else {
            for (Name name : entry.getNames()) {
                if (name.getName() == null || name.getName().isEmpty()) {
                    throw new ServiceException("Name can't be null or empty!");
                }

                boolean existName = false;
                for (Name currentEntryName : currentEntry.getNames()) {
                    if (currentEntryName.getName().equals(name.getName())) {
                        existName = true;

                        break;
                    }
                }

                if (!existName) {
                    name.setEntry(currentEntry);

                    currentEntry.getNames().add(name);
                }
            }
        }

        // Validate and set selection markers
        if (entry.getSelectionMarkers() != null && entry.getSelectionMarkers().size() > 0) {
            for (SelectionMarker selectionMarker : entry.getSelectionMarkers()) {
                if (selectionMarker.getName() == null || selectionMarker.getName().isEmpty()) {
                    throw new ServiceException("Selection Marker can't be null or empty!");
                }

                boolean existSelectionMarker = false;
                for (SelectionMarker currentEntrySelectionMarker : currentEntry
                        .getSelectionMarkers()) {
                    if (currentEntrySelectionMarker.getName().equals(selectionMarker.getName())) {
                        existSelectionMarker = true;

                        break;
                    }
                }

                if (!existSelectionMarker) {
                    selectionMarker.setEntry(currentEntry);

                    currentEntry.getSelectionMarkers().add(selectionMarker);
                }
            }
        } else {
            currentEntry.setSelectionMarkers(null);
        }

        if (entry.getLinks() != null && entry.getLinks().size() > 0) {
            for (Link link : entry.getLinks()) {
                if (link.getLink() == null || link.getLink().isEmpty()) {
                    throw new ServiceException("Link can't be null or empty!");
                }

                boolean existLink = false;
                for (Link currentEntryLink : currentEntry.getLinks()) {
                    if (currentEntryLink.getUrl().equals(link.getUrl())
                            && currentEntryLink.getLink().equals(link.getLink())) {
                        existLink = true;

                        break;
                    }
                }

                if (!existLink) {
                    link.setEntry(currentEntry);

                    currentEntry.getLinks().add(link);
                }
            }
        } else {
            currentEntry.setLinks(null);
        }

        // Validate and set entry funding sources
        if (entry.getEntryFundingSources() == null || entry.getEntryFundingSources().size() == 0) {
            throw new ServiceException(
                    "FundingSource is mandatory! Expected at least one FundingSource.");
        } else {
            for (EntryFundingSource entryFundingSource : entry.getEntryFundingSources()) {
                if (entryFundingSource.getFundingSource() == null) {
                    throw new ServiceException("FundingSource can't be null!");
                }

                if (entryFundingSource.getFundingSource().getFundingSource() == null
                        || entryFundingSource.getFundingSource().getFundingSource().isEmpty()) {
                    throw new ServiceException("FundingSource can't be null or empty!");
                }

                if (entryFundingSource.getFundingSource().getPrincipalInvestigator() == null
                        || entryFundingSource.getFundingSource().getPrincipalInvestigator()
                                .isEmpty()) {
                    throw new ServiceException("PrincipalInvestigator can't be null or empty!");
                }

                boolean existEntryFundingSource = false;
                for (EntryFundingSource currentEntryEntryFundingSource : currentEntry
                        .getEntryFundingSources()) {

                    if (currentEntryEntryFundingSource.getFundingSource().getFundingSource()
                            .equals(entryFundingSource.getFundingSource().getFundingSource())
                            && currentEntryEntryFundingSource.getFundingSource()
                                    .getPrincipalInvestigator().equals(
                                            entryFundingSource.getFundingSource()
                                                    .getPrincipalInvestigator())) {
                        existEntryFundingSource = true;

                        break;
                    }

                }

                if (!existEntryFundingSource) {
                    entryFundingSource.setEntry(currentEntry);

                    currentEntry.getEntryFundingSources().add(entryFundingSource);
                }

                entryFundingSource.setEntry(entry);
            }
        }

        return currentEntry;
    }

    public void removeEntry(String sessionId, String entryId) throws SessionException,
            ServiceException, ServicePermissionException {
        EntryController entryController = getEntryController(sessionId);

        try {
            Entry entry;
            entry = entryController.getByRecordId(entryId);

            entryController.delete(entry);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        } catch (PermissionException e) {
            throw new ServicePermissionException("No permissions to delete this entry!");
        }
    }

    public Sequence getSequence(String sessionId, String entryId) throws SessionException,
            ServiceException, ServicePermissionException {

        SequenceController sequenceController = getSequenceController(sessionId);
        EntryController entryController = getEntryController(sessionId);

        Sequence sequence = null;

        try {
            Entry entry = entryController.getByRecordId(entryId);

            sequence = sequenceController.getByEntry(entry);
        } catch (PermissionException e) {
            throw new ServicePermissionException("No permission to read this entry");
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }

        return sequence;
    }

    // --------------------------------------------

    /*public Sequence createSequence(String sessionId, String entryId, Sequence sequence)
            throws SessionException, ServiceException, ServicePermissionException {
        return null;
    }

    public void removeSequence(String sessionId, Sequence sequence) throws SessionException,
            ServiceException, ServicePermissionException {
    }

    public Sequence uploadSequence(String sessionId, String entryId, String Sequence)
            throws SessionException, ServiceException, ServicePermissionException {
        return null;
    }*/

    // --------------------------------------------

    protected EntryController getEntryController(String sessionId) throws SessionException,
            ServiceException {
        Account account = validateAccount(sessionId);

        return new EntryController(account);
    }

    protected SequenceController getSequenceController(String sessionId) throws ServiceException {
        Account account = validateAccount(sessionId);

        return new SequenceController(account);
    }

    protected SearchController getSearchController(String sessionId) throws SessionException,
            ServiceException {
        return new SearchController(validateAccount(sessionId));
    }

    protected Account validateAccount(String sessionId) throws ServiceException {
        if (!isAuthenticated(sessionId)) {
            throw new SessionException("Not uauthorized access! Autorize first!");
        }

        Account account = null;

        try {
            account = AccountController.getAccountBySessionKey(sessionId);
        } catch (ControllerException e) {
            Logger.error(e);

            throw new ServiceException("Registry Service Internal Error!");
        }

        if (account == null) {
            Logger.error("Failed to lookup account!");

            throw new ServiceException("Registry Service Internal Error!");
        }

        return account;
    }
}
