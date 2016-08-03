package org.jbei.ice.lib.message;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.group.UserGroup;
import org.jbei.ice.lib.dto.message.MessageInfo;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.MessageDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.storage.model.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class Messages {

    private final MessageDAO dao;
    private final AccountDAO accountDAO;
    private final String userId;

    public Messages(String userId) {
        dao = DAOFactory.getMessageDAO();
        accountDAO = DAOFactory.getAccountDAO();
        this.userId = userId;
    }

    /**
     * Marks a message as read and returns the number of unread messages
     *
     * @param id identifier for message to be marked as read
     * @return number of unread messages after marking message as read
     */
    public int markMessageAsRead(long id) {
        Message message = dao.retrieveMessage(id);
        message.setRead(true);
        message.setDateRead(new Date());
        dao.update(message);
        return 0;
    }

    /**
     * Sends message contained in the MessageInfo to the specified recipients. It some of the
     * recipients do not exist, the routine does its best to deliver as many as possible
     *
     * @param info details of message including recipient(s)
     * @return false if the message fails to be sent to all the intended recipients
     */
    public boolean send(MessageInfo info) {
        if (info == null || info.getAccounts().isEmpty() && info.getUserGroups().isEmpty())
            return false;
        boolean success = true;

        Message message = new Message();
        message.setDateSent(new Date());
        message.setFromEmail(this.userId);
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

        if (!success)
            return false;

        if (message.getDestinationAccounts().isEmpty() && message.getDestinationGroups().isEmpty())
            return false;

        return dao.create(message) != null;
    }

    public Results<MessageInfo> get(int start, int limit) {
        Account account = accountDAO.getByEmail(this.userId);
        List<Group> groups = DAOFactory.getGroupDAO().retrieveMemberGroups(account);
        List<Message> messages = new ArrayList<>(dao.retrieveMessages(account, groups, start, limit));

        Results<MessageInfo> results = new Results<>();

        for (Message message : messages) {
            Account from = accountDAO.getByEmail(message.getFromEmail());
            if (from == null)
                continue;

            MessageInfo info = new MessageInfo();
            info.setId(message.getId());
            info.setFrom(message.getFromEmail());
            info.setTitle(message.getTitle());
            info.setRead(message.isRead());
            info.setSent(message.getDateSent().getTime());
            results.getData().add(info);
        }

        int totalSize = dao.retrieveMessageCount(account, groups);
        results.setResultCount(totalSize);
        return results;
    }

    public MessageInfo get(long id) {
        Message message = dao.get(id);
        if (message == null)
            throw new IllegalArgumentException("Cannot retrieve message with id " + id);

        // todo : check permissions

        return message.toDataTransferObject();
    }

    public int getNewMessageCount(Account account) {
        return dao.retrieveNewMessageCount(account);
    }
}
