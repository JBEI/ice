package org.jbei.ice.lib.news;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.NewsDAO;

/**
 * @auther Hector Plahar
 */
public class NewsController {
    private final NewsDAO dao;

    public NewsController() {
        this.dao = DAOFactory.getNewsDAO();
    }

    public News update(News news) throws ControllerException {
        news.setModificationTime(new Date(System.currentTimeMillis()));
        try {
            return dao.update(news);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public News save(News news) throws ControllerException {
        news.setCreationTime(new Date(System.currentTimeMillis()));
        try {
            return dao.create(news);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public ArrayList<News> retrieveAll() throws ControllerException {
        try {
            return dao.retrieveAll();
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
