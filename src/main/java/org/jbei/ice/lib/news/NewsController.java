package org.jbei.ice.lib.news;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.News;

import java.util.ArrayList;
import java.util.Date;

/**
 * @auther Hector Plahar
 */
public class NewsController {
    private final NewsDAO dao;

    public NewsController() {
        this.dao = new NewsDAO();
    }

    public News update(News news) throws ControllerException {
        news.setModificationTime(new Date(System.currentTimeMillis()));
        try {
            return dao.save(news);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public News save(News news) throws ControllerException {
        news.setCreationTime(new Date(System.currentTimeMillis()));
        try {
            return dao.save(news);
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
