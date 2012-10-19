package org.jbei.ice.lib.utils;

import java.util.Collection;
import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.shared.AutoCompleteField;

/**
 * @author Hector Plahar
 */
public class UtilsController {

    private final UtilsDAO dao;

    public UtilsController() {
        dao = new UtilsDAO();
    }

    public Set<String> getMatchingAutoCompleteField(AutoCompleteField field, String token, int limit)
            throws ControllerException {

        // TODO : check the field
        try {
            return dao.getMatchingSelectionMarkers(token, limit);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
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
