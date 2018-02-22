package org.jbei.ice.lib.entry.sample;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.comment.UserComment;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.IDataTransferModel;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Comment;
import org.jbei.ice.storage.model.Sample;
import org.jbei.ice.storage.model.Storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Samples on a plate
 *
 * @author Hector Plahar
 */
public class PlateSamples implements IDataTransferModel {

    // map of well -> sample
    private Map<String, PartSample> sampleMap;
    private final EntryAuthorization authorization;

    public PlateSamples() {
        sampleMap = new HashMap<>();
        for (int i = 0; i <= 95; i += 1) {
            String position = numberToPosition(i);
            sampleMap.put(position, new PartSample());
        }
        authorization = new EntryAuthorization();
    }

    public boolean setSample(String userId, long locationId) {
        Storage storage = DAOFactory.getStorageDAO().get(locationId);
        if (storage == null)
            return false;

        for (Storage well : storage.getChildren()) {
            String position = well.getIndex();
            PartSample partSample = sampleMap.get(position);
            Iterator<Storage> iterator = well.getChildren().iterator();
            if (!iterator.hasNext())
                continue;

            Storage tube = iterator.next();
            partSample.setLocation(tube.toDataTransferObject());
            partSample.getLocation().setName(position); // re-using name for location on plate
            List<Sample> sampleList = DAOFactory.getSampleDAO().getSamplesByStorage(tube);

            Sample sample = sampleList.get(0); // expecting only a single sample associated with the tube
            if (!authorization.canRead(userId, sample.getEntry()))
                continue;

            partSample.setId(sample.getId());
            partSample.setPartId(sample.getEntry().getId());
            partSample.setPartName(sample.getEntry().getName());
            partSample.setLabel(sample.getLabel());
            partSample.setCreationTime(sample.getCreationTime().getTime());

            if (sample.getComments() != null) {
                for (Comment comment : sample.getComments()) {
                    UserComment userComment = new UserComment();
                    userComment.setId(comment.getId());
                    userComment.setMessage(comment.getBody());
                    partSample.getComments().add(userComment);
                }
            }
            setAccountInfo(partSample, sample.getDepositor());

        }

        return true;
    }

    protected PartSample setAccountInfo(PartSample partSample, String email) {
        Account account = DAOFactory.getAccountDAO().getByEmail(email);
        if (account != null)
            partSample.setDepositor(account.toDataTransferObject());
        else {
            AccountTransfer accountTransfer = new AccountTransfer();
            accountTransfer.setEmail(email);
            partSample.setDepositor(accountTransfer);
        }
        return partSample;
    }

    private String numberToPosition(int number) {
        if (number < 0 || number > 95)
            throw new IllegalArgumentException("Invalid number location");
        String row = String.format("%02d", (number % 12) + 1);
        int position = number / 12;

        switch (position) {
            case 0:
                return "A" + row;
            case 1:
                return "B" + row;
            case 2:
                return "C" + row;
            case 3:
                return "D" + row;
            case 4:
                return "E" + row;
            case 5:
                return "F" + row;
            case 6:
                return "G" + row;
            case 7:
                return "H" + row;
        }
        return null;
    }
}
