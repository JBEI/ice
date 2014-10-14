package org.jbei.ice.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.Utils;

import org.apache.commons.io.IOUtils;

/**
 * Servlet for serving the different kinds of files
 * available on gd-ice. Requires a valid session id as
 * a parameter // TODO : currently only being used for sbol visual download
 *
 * @author Hector Plahar
 */
public class FileDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = request.getParameter("type");
        String sid = request.getParameter("sid");
        String entryId = request.getParameter("rid");

        String userId = SessionHandler.getUserIdBySession(sid);
        getSBOLVisualType(userId, entryId, response);
    }

    private void getSBOLVisualType(String userId, String entryId, HttpServletResponse response) {
        Entry entry = DAOFactory.getEntryDAO().getByRecordId(entryId);
        Sequence sequence = DAOFactory.getSequenceDAO().getByEntry(entry);
        if (entry == null || sequence == null) {
            return;
        }

        EntryAuthorization authorization = new EntryAuthorization();
        authorization.expectRead(userId, entry);

        // retrieve cached pigeon image or generate and cache
        String fileId = null;

        String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        String hash = sequence.getFwdHash();
        if (Paths.get(tmpDir, hash + ".png").toFile().exists()) {
            fileId = hash + ".png";
        } else {
            URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
            if (uri != null) {
                try {
                    IOUtils.copy(uri.toURL().openStream(),
                                 new FileOutputStream(tmpDir + File.separatorChar + hash + ".png"));
                    fileId = hash + ".png";
                } catch (IOException e) {
                    Logger.error(e);
                    return;
                }
            }
        }

        response.setContentType("image/png");
        File file = Paths.get(tmpDir, fileId).toFile();
        if (file.exists() && file.canRead()) {
            response.setContentLength((int) file.length());
            try {
                IOUtils.copy(new FileInputStream(file), response.getOutputStream());
            } catch (IOException ioe) {
                Logger.error(ioe);
            }
        }
    }
}
