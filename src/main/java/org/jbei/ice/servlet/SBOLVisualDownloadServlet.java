package org.jbei.ice.servlet;

import org.apache.commons.io.IOUtils;
import org.jbei.ice.lib.account.UserSessions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.sequence.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servlet for serving the SBOL visual icon in ICE.
 * <p/>
 * A separate servlet is in use because of latency issues with pigeon
 * when generating the images. Doing it this way prevents these issues
 * from delaying loading of ICE entries
 *
 * @author Hector Plahar
 */
public class SBOLVisualDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sid = request.getParameter("sid");
        String entryId = request.getParameter("rid");

        String userId = UserSessions.getUserIdBySession(sid);
        getSBOLVisualType(userId, entryId, response);
    }

    private void getSBOLVisualType(String userId, String entryId, HttpServletResponse response) {
        Entry entry = DAOFactory.getEntryDAO().getByRecordId(entryId);
        if (entry == null)
            return;

        Sequence sequence = DAOFactory.getSequenceDAO().getByEntry(entry);

        if (sequence == null) {
            return;
        }

        EntryAuthorization authorization = new EntryAuthorization();
        authorization.expectRead(userId, entry);

        // retrieve cached pigeon image or generate and cache
        String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        Path filePath = Paths.get(tmpDir, sequence.getFwdHash() + ".png");
        URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
        if (uri != null) {
            try {
                IOUtils.copy(uri.toURL().openStream(), new FileOutputStream(filePath.toFile()));
            } catch (IOException e) {
                Logger.error(e);
                return;
            }
        }

        response.setContentType("image/png");
        if (!Files.exists(filePath) || !Files.isReadable(filePath))
            return;

        response.setContentLength((int) filePath.toFile().length());
        try {
            IOUtils.copy(new FileInputStream(filePath.toFile()), response.getOutputStream());
        } catch (IOException ioe) {
            Logger.error(ioe);
        }
    }
}
