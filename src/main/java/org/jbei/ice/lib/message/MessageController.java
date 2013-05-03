package org.jbei.ice.lib.message;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.shared.dto.AccountType;
import org.jbei.ice.shared.dto.MessageInfo;
import org.jbei.ice.shared.dto.message.MessageList;

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

    public MessageList retrieveMessages(Account requestor, Account owner, int start, int count)
            throws ControllerException {
        Logger.info(requestor.getEmail() + ": retrieving messages for " + owner.getEmail());
        if (!owner.equals(requestor) || requestor.getType() != AccountType.ADMIN)
            throw new ControllerException("Cannot retrieve messages for another user if non an admin");

        try {
            List<Message> results = new ArrayList<>(dao.retrieveMessages(owner, start, count));
            ArrayList<MessageInfo> messages = new ArrayList<>();
            for (Message message : results) {
                Account from = ControllerFactory.getAccountController().getByEmail(message.getFromEmail());
                if (from == null)
                    continue;

                MessageInfo info = Message.toDTO(message);
                info.setFrom(from.getFullName());
                messages.add(info);
            }
            MessageList messageList = new MessageList();
            messageList.setList(messages);
            int totalSize = dao.retrieveMessageCount(owner);
            messageList.setTotalSize(totalSize);
            messageList.setStart(start);
            messageList.setCount(count);
            return messageList;
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public int getNewMessageCount(Account account) throws ControllerException {
        try {
            return dao.retrieveNewMessageCount(account);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }
}
