package org.jbei.ice.server.servlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.composers.formatters.FastaFormatter;
import org.jbei.ice.lib.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.composers.formatters.SBOLFormatter;
import org.jbei.ice.lib.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Sequence;

import org.apache.commons.io.IOUtils;

// will eventually attempt to consolidate the servlets
public class SequenceDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String TYPE = "type";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Logger.info(SequenceDownloadServlet.class.getSimpleName() + ": attempt to download sequence");
        Account account;
        String entryId = request.getParameter("entry");
        String type = request.getParameter(TYPE);
        String sid = request.getParameter("sid");
        AccountController controller = ControllerFactory.getAccountController();

        try {
            account = isLoggedIn(request.getCookies());
            if (account == null) {
                if (!AccountController.isAuthenticated(sid))
                    return;
                account = controller.getAccountBySessionKey(sid);
                if (account == null)
                    return;
            }
        } catch (ControllerException ce) {
            Logger.error(ce);
            String url = request.getRequestURL().toString();
            String path = request.getServletPath();
            url = url.substring(0, url.indexOf(path));
            response.sendRedirect(url);
            Logger.info("Authentication failed. Redirecting user to " + url);
            return;
        }

        EntryController entryController = ControllerFactory.getEntryController();
        Entry entry;
        try {
            entry = entryController.get(account, Long.parseLong(entryId));
        } catch (NumberFormatException | ControllerException e) {
            Logger.error(e);
            return;
        }

        if (entry == null) {
            Logger.info(account.getEmail() + " could not locate entry with id " + entryId);
            return;
        }

        Logger.info(SequenceDownloadServlet.class.getSimpleName() + " " + account.getEmail() + ": "
                            + "entryid = " + entryId + ", type=" + type);

        if ("original".equals(type))
            getOriginal(response, entry, account);
        else if ("genbank".equals(type))
            getGenbank(response, entry);
        else if ("fasta".equals(type))
            getFasta(response, entry);
        else if ("sbol".equals(type))
            getSBOL(response, entry);
        else if ("pigeonI".equalsIgnoreCase(type))
            getSBOLv(response, entry);
        else if ("pigeonS".equalsIgnoreCase(type))
            getPigeonScript(response, entry);
        else
            Logger.error("Unrecognized sequence download type " + type);
    }

    private Account isLoggedIn(Cookie[] cookies) throws ControllerException {
        AccountController controller = ControllerFactory.getAccountController();

        for (Cookie cookie : cookies) {
            if ("gd-ice".equals(cookie.getName())) {
                String sid = cookie.getValue();
                if (sid == null || sid.isEmpty())
                    return null;

                if (!AccountController.isAuthenticated(sid))
                    return null;
                return controller.getAccountBySessionKey(sid);
            }
        }
        return null;
    }

    private void getOriginal(HttpServletResponse response, Entry entry, Account account) {
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        Sequence sequence;

        try {
            sequence = sequenceController.getByEntry(entry);
            if (sequence == null) {
                Logger.info("No sequence associated with entry " + entry.getId());
                return;
            }
        } catch (ControllerException e) {
            Logger.error(e);
            return;
        }

        String sequenceString = sequence.getSequenceUser();
        if (sequenceString == null || sequenceString.isEmpty()) {
            Logger.info("Sequence user parameter (sequence string) is empty for entry "
                                + entry.getId() + " and sequence " + sequence.getId());
            return;
        }

        String filename = getFileName(entry) + ".seq";
        try {
            byte[] bytes = sequenceString.getBytes();
            response.setContentType("text/plain");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
            IOUtils.write(bytes, response.getOutputStream());
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void getGenbank(HttpServletResponse response, Entry entry) {
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        GenbankFormatter genbankFormatter = new GenbankFormatter(entry.getName());
        genbankFormatter.setCircular((entry instanceof Plasmid) ? ((Plasmid) entry).getCircular() : false); // TODO

        Sequence sequence;
        try {
            sequence = sequenceController.getByEntry(entry);
            if (sequence == null) {
                Logger.info("No sequence associated with entry " + entry.getId());
                return;
            }
        } catch (ControllerException e) {
            Logger.error(e);
            return;
        }

        String sequenceString;
        try {
            sequenceString = sequenceController.compose(sequence, genbankFormatter);
        } catch (ControllerException e) {
            Logger.error("Failed to generate genbank file for download!", e);
            return;
        }

        if (sequenceString == null || sequenceString.isEmpty()) {
            Logger.info("Sequence string is empty for entry " + entry.getId() + " and sequence " + sequence.getId());
            return;
        }

        String filename = getFileName(entry) + ".gb";

        try {
            byte[] bytes = sequenceString.getBytes();
            response.setContentType("text/plain");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
            IOUtils.write(bytes, response.getOutputStream());
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void getFasta(HttpServletResponse response, Entry entry) {
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        Sequence sequence;

        try {
            sequence = sequenceController.getByEntry(entry);
            if (sequence == null) {
                Logger.info("No sequence associated with entry " + entry.getId());
                return;
            }
        } catch (ControllerException e) {
            Logger.error(e);
            return;
        }

        String sequenceString;
        try {
            FastaFormatter formatter = new FastaFormatter(sequence.getEntry().getName());
            sequenceString = sequenceController.compose(sequence, formatter);
        } catch (ControllerException e) {
            Logger.error("Failed to generate fasta file for download!", e);
            return;
        }

        String filename = getFileName(entry) + ".fasta";

        try {
            byte[] bytes = sequenceString.getBytes();
            response.setContentType("text/plain");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
            IOUtils.write(bytes, response.getOutputStream());
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void getSBOL(HttpServletResponse response, Entry entry) {
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        Sequence sequence;

        try {
            sequence = sequenceController.getByEntry(entry);
            if (sequence == null) {
                Logger.warn("No sequence associated with entry " + entry.getId());
                return;
            }
        } catch (ControllerException e) {
            Logger.error(e);
            return;
        }

        String sequenceString;
        try {
            sequenceString = sequenceController.compose(sequence, new SBOLFormatter());
        } catch (ControllerException e) {
            Logger.error("Failed to generate sbol file for download!", e);
            return;
        }

        String filename = getFileName(entry) + ".xml";

        try {
            byte[] bytes = sequenceString.getBytes();
            response.setContentType("text/xml");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
            IOUtils.write(bytes, response.getOutputStream());
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void getSBOLv(HttpServletResponse response, Entry entry) {
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        Sequence sequence;

        try {
            sequence = sequenceController.getByEntry(entry);
        } catch (ControllerException e) {
            Logger.error(e);
            return;
        }

        URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
        response.setContentType("image/png");
        String filename = getFileName(entry) + ".png";
        response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
        try (BufferedInputStream in = new BufferedInputStream(uri.toURL().openStream())) {
            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                response.getOutputStream().write(data, 0, count);
            }
            response.getOutputStream().flush();
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void getPigeonScript(HttpServletResponse response, Entry entry) {
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        Sequence sequence;

        try {
            sequence = sequenceController.getByEntry(entry);
        } catch (ControllerException e) {
            Logger.error(e);
            return;
        }

        String pigeonScript = PigeonSBOLv.generatePigeonScript(sequence);
        try {
            byte[] bytes = pigeonScript.getBytes();
            response.setContentType("text/plain");
            String filename = getFileName(entry) + ".txt";
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
            IOUtils.write(bytes, response.getOutputStream());
            response.setContentType("text/plain");
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    /**
     * Retrieves the entry name for use as the filename
     *
     * @param entry entry whose name is desired to be used as the filename
     * @return string to be used as a filename
     */
    private String getFileName(Entry entry) {
        return entry.getName();
    }
}
