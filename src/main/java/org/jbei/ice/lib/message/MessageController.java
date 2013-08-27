package org.jbei.ice.lib.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.message.MessageInfo;
import org.jbei.ice.lib.shared.dto.message.MessageList;
import org.jbei.ice.lib.shared.dto.user.AccountType;
import org.jbei.ice.lib.shared.dto.user.User;

/**
 * @author Hector Plahar
 */
public class MessageController {

    private final MessageDAO dao;

    public MessageController() {
        dao = new MessageDAO();
    }

    /**
     * Marks a message as read and returns the number of unread messages
     *
     * @param account account for user making the request.
     * @param id      identifier for message to be marked as read
     * @return number of unread messages after marking message as read
     * @throws ControllerException
     */
    public int markMessageAsRead(Account account, long id) throws ControllerException {
        try {
            Message message = dao.retrieveMessage(id);
            message.setRead(true);
            message.setDateRead(new Date(System.currentTimeMillis()));
            dao.update(message);
            return getNewMessageCount(account);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    /**
     * Sends message contained in the MessageInfo to the specified recipients. It some of the
     * recipients do not exist, the routine does its best to deliver as many as possible
     *
     * @param sender account for user sending the message
     * @param info   details of message including recipient(s)
     * @return false if the message fails to be sent to all the intended recipients
     * @throws ControllerException
     */
    public boolean sendMessage(Account sender, MessageInfo info) throws ControllerException {
        if (info == null || info.getAccounts().isEmpty() && info.getUserGroups().isEmpty())
            throw new ControllerException("Cannot send message");
        boolean success = true;

        Message message = new Message();
        message.setDateSent(new Date(System.currentTimeMillis()));
        message.setFromEmail(sender.getEmail());
        message.setMessage(info.getMessage());
        message.setTitle(info.getTitle());

        if (info.getAccounts() != null) {
            for (User user : info.getAccounts()) {
                Account account = ControllerFactory.getAccountController().getByEmail(user.getEmail());
                if (account == null) {
                    success = false;
                    continue;
                }
                message.getDestinationAccounts().add(account);
            }
        }

        if (info.getUserGroups() != null) {
            for (UserGroup userGroup : info.getUserGroups()) {
                Group group = ControllerFactory.getGroupController().getGroupById(userGroup.getId());
                if (group == null) {
                    Logger.warn("Could not retrieve group with id " + userGroup.getId() + " to send message");
                    success = false;
                    continue;
                }

                message.getDestinationGroups().add(group);
            }
        }

        if (message.getDestinationAccounts().isEmpty() && message.getDestinationGroups().isEmpty())
            return false;

        try {
            dao.save(message);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
        return success;
    }

    public MessageList retrieveMessages(Account requester, Account owner, int start, int count)
            throws ControllerException {
        Logger.info(requester.getEmail() + ": retrieving messages for " + owner.getEmail());
        if (!owner.equals(requester) && requester.getType() != AccountType.ADMIN) {
            Logger.error("Cannot retrieve messages for another user if non an admin");
            throw new ControllerException("Cannot retrieve messages for another user if non an admin");
        }

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
