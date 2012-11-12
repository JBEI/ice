package org.jbei.ice.lib.message;

import java.util.ArrayList;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
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

    public ArrayList<MessageInfo> retrieveMessages(String owner, int count, int start) throws ControllerException {
        return new ArrayList<MessageInfo>();
    }
}
