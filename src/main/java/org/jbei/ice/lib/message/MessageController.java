package org.jbei.ice.lib.message;

import java.util.ArrayList;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.shared.dto.AccountType;
import org.jbei.ice.shared.dto.MessageInfo;

/**
 * @author Hector Plahar
 */
public class MessageController {

    private final MessageDAO dao;

    public MessageController() {
        dao = new MessageDAO();
    }

    public MessageInfo retrieveMessage(long id) throws ControllerException {
        try {
            Message message = dao.retrieveMessage(id);
            return Message.toDTO(message);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public void createMessage(long accountId, String title, String details, String toEmail) throws ControllerException {
        Message message = new Message();
        try {
            dao.save(message);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public void deleteMessage(long messageId) throws ControllerException {
    }

    public ArrayList<MessageInfo> retrieveMessages(Account requestor, Account owner, int start, int count)
            throws ControllerException {
        Logger.info(requestor.getEmail() + ": retrieving messages for " + owner.getEmail());
        if (!owner.equals(requestor) || requestor.getType() != AccountType.ADMIN)
            throw new ControllerException("Cannot retrieve messages for another user if non an admin");
        try {
            ArrayList<Message> results = dao.retrieveMessages(owner.getEmail(), start, count);
            ArrayList<MessageInfo> messages = new ArrayList<MessageInfo>();
            for (Message message : results) {
                messages.add(Message.toDTO(message));
            }
            return messages;
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public int getNewMessageCount(Account account, String userId) throws ControllerException {
        try {
            return dao.retrieveNewMessageCount(userId);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }
}
