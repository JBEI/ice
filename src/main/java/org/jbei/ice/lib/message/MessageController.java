package org.jbei.ice.lib.message;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.dao.AccountDAO;
import org.jbei.ice.lib.dao.hibernate.dao.MessageDAO;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.dto.message.MessageInfo;
import org.jbei.ice.lib.dto.message.MessageList;
import org.jbei.ice.lib.group.Group;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class MessageController {

    private final MessageDAO dao;
    private final AccountDAO accountDAO;

    public MessageController() {
        dao = DAOFactory.getMessageDAO();
        accountDAO = DAOFactory.getAccountDAO();
    }

    /**
     * Marks a message as read and returns the number of unread messages
     *
     * @param account account for user making the request.
     * @param id      identifier for message to be marked as read
     * @return number of unread messages after marking message as read
     */
    public int markMessageAsRead(Account account, long id) {
        Message message = dao.retrieveMessage(id);
        message.setRead(true);
        message.setDateRead(new Date());
        dao.update(message);
        return getNewMessageCount(account);
    }

    /**
     * Sends message contained in the MessageInfo to the specified recipients. It some of the
     * recipients do not exist, the routine does its best to deliver as many as possible
     *
     * @param sender account for user sending the message
     * @param info   details of message including recipient(s)
     * @return false if the message fails to be sent to all the intended recipients
     */
    public boolean sendMessage(Account sender, MessageInfo info) {
        if (info == null || info.getAccounts().isEmpty() && info.getUserGroups().isEmpty())
            return false;
        boolean success = true;

        Message message = new Message();
        message.setDateSent(new Date(System.currentTimeMillis()));
        message.setFromEmail(sender.getEmail());
        message.setMessage(info.getMessage());
        message.setTitle(info.getTitle());

        if (info.getAccounts() != null) {
            for (AccountTransfer accountTransfer : info.getAccounts()) {
                Account account = accountDAO.getByEmail(accountTransfer.getEmail());
                if (account == null) {
                    success = false;
                    continue;
                }
                message.getDestinationAccounts().add(account);
            }
        }

        if (info.getUserGroups() != null) {
            for (UserGroup userGroup : info.getUserGroups()) {
                Group group = DAOFactory.getGroupDAO().get(userGroup.getId());
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

        dao.create(message);
        return success;
    }

    public MessageList retrieveMessages(String requester, String owner, int start, int count) {
        Account requesterAccount = DAOFactory.getAccountDAO().getByEmail(requester);
        Account account = DAOFactory.getAccountDAO().getByEmail(owner);

        if (!account.equals(requesterAccount) && requesterAccount.getType() != AccountType.ADMIN) {
            Logger.error("Cannot retrieve messages for another user if non an admin");
            return null;
        }

        List<Message> results = new ArrayList<>(dao.retrieveMessages(account, start, count));
        ArrayList<MessageInfo> messages = new ArrayList<>();
        for (Message message : results) {
            Account from = accountDAO.getByEmail(message.getFromEmail());
            if (from == null)
                continue;

            MessageInfo info = message.toDataTransferObject();
            info.setFrom(from.getFullName());
            messages.add(info);
        }
        MessageList messageList = new MessageList();
        messageList.setList(messages);
        int totalSize = dao.retrieveMessageCount(account);
        messageList.setTotalSize(totalSize);
        messageList.setStart(start);
        messageList.setCount(count);
        return messageList;
    }

    public int getNewMessageCount(Account account) {
        return dao.retrieveNewMessageCount(account);
    }
}
