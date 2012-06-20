package org.jbei.ice.lib.utils;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAOException;

import java.util.Collection;

/**
 * @author Hector Plahar
 */
public class UtilsController {

    private final UtilsDAO dao;

    public UtilsController() {
        dao = new UtilsDAO();
    }

    public Collection<? extends String> getUniqueSelectionMarkers() throws ControllerException {
        try {
            return dao.getUniqueSelectionMarkers();
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Collection<? extends String> getUniqueOriginOfReplications() {
        return dao.getUniqueOriginOfReplications();
    }

    public Collection<? extends String> getUniquePublicPlasmidNames() {
        return dao.getUniquePublicPlasmidNames();
    }

    public Collection<? extends String> getUniquePromoters() {
        return dao.getUniquePromoters();
    }
}
