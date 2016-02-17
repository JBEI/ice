package org.jbei.ice.lib.manuscript;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.ManuscriptModelDAO;
import org.jbei.ice.storage.model.ManuscriptModel;

import java.util.Date;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class Manuscripts {

    //    private final String userId;
    private final ManuscriptModelDAO dao;

    public Manuscripts(String userId) {
//        this.userId = userId;
        this.dao = DAOFactory.getManuscriptModelDAO();
        if (!new AccountController().isAdministrator(userId))
            throw new PermissionException("Admin feature");
    }

    public boolean delete(long id) {
        ManuscriptModel manuscriptModel = this.dao.get(id);
        if (manuscriptModel == null)
            return false;

        this.dao.delete(manuscriptModel);
        return true;
    }

    public Manuscript add(Manuscript manuscript) {
        // todo : validation
        ManuscriptModel model = new ManuscriptModel();
        model.setCreationTime(new Date());
        model.setStatus(manuscript.getStatus());
        model.setParagonUrl(manuscript.getParagonUrl());
        model.setTitle(manuscript.getTitle());
        model.setAuthors(manuscript.getAuthors());
        return dao.create(model).toDataTransferObject();
    }

    public Results<Manuscript> get(int offset, int size) {
        Results<Manuscript> results = new Results<>();
        List<ManuscriptModel> list = dao.list(offset, size);
        if (!list.isEmpty()) {
            for (ManuscriptModel manuscriptModel : list)
                results.getData().add(manuscriptModel.toDataTransferObject());
        }
        return results;
    }
}
