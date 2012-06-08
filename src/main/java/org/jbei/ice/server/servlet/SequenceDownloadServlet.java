package org.jbei.ice.server.servlet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.composers.SequenceComposerException;
import org.jbei.ice.lib.composers.formatters.FastaFormatter;
import org.jbei.ice.lib.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.composers.formatters.SbolFormatter;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.permissions.PermissionException;

// will eventually attempt to consolidate the servlets
public class SequenceDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String TYPE = "type";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Logger.info(SequenceDownloadServlet.class.getSimpleName()
                + ": attempt to download sequence");
        Account account;
        String entryId = request.getParameter("entry");
        String type = request.getParameter(TYPE);
        String sid = request.getParameter("sid");

        try {
            account = isLoggedIn(request.getCookies());
            if (account == null) {
                if (!AccountController.isAuthenticated(sid))
                    return;
                account = AccountController.getAccountBySessionKey(sid);
                if (account == null)
                    return;
            }

        } catch (ControllerException ce) {
            Logger.error(ce);
            String url = request.getRequestURL().toString();
            String path = request.getServletPath();
            url = url.substring(0, url.indexOf(path));
            response.sendRedirect(url);
            Logger.info(FileDownloadServlet.class.getSimpleName()
                    + ": authenication failed. Redirecting user to " + url);
            return;
        }

        EntryController entryController = new EntryController(account);
        Entry entry = null;
        try {
            entry = entryController.get(Long.parseLong(entryId));
        } catch (NumberFormatException e) {
            Logger.error(e);
            return;
        } catch (ControllerException e) {
            Logger.error(e);
            return;
        } catch (PermissionException e) {
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
            getGenbank(response, entry, account);
        else if ("fasta".equals(type))
            getFasta(response, entry, account);
        else if ("sbol".equals(type))
            getSBOL(response, entry, account);
        else
            Logger.error("Unrecognized sequence download type " + type);
    }

    private Account isLoggedIn(Cookie[] cookies) throws ControllerException {
        for (Cookie cookie : cookies) {
            if ("gd-ice".equals(cookie.getName())) {
                String sid = cookie.getValue();
                if (sid == null || sid.isEmpty())
                    return null;

                if (!AccountController.isAuthenticated(sid))
                    return null;
                return AccountController.getAccountBySessionKey(sid);
            }
        }
        return null;
    }

    private void getOriginal(HttpServletResponse response, Entry entry, Account account) {

        SequenceController sequenceController = new SequenceController(account);
        Sequence sequence = null;

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

        String filename = entry.getPartNumbersAsString() + ".seq";

        try {
            byte[] bytes = sequenceString.getBytes();
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);

            response.setContentType("text/plain");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");

            OutputStream os = response.getOutputStream();
            DataInputStream is = new DataInputStream(byteInputStream);

            int read = 0;

            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }
            os.flush();
            os.close();

        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void getGenbank(HttpServletResponse response, Entry entry, Account account) {

        SequenceController sequenceController = new SequenceController(account);
        GenbankFormatter genbankFormatter = new GenbankFormatter(entry.getNamesAsString());
        genbankFormatter.setCircular((entry instanceof Plasmid) ? ((Plasmid) entry).getCircular()
                : false); // TODO

        Sequence sequence = null;

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

        String sequenceString = null;
        try {
            sequenceString = SequenceController.compose(sequence, genbankFormatter);
        } catch (SequenceComposerException e) {
            Logger.error("Failed to generate genbank file for download!", e);
            return;
        }

        if (sequenceString == null || sequenceString.isEmpty()) {
            Logger.info("Sequence string is empty for entry " + entry.getId() + " and sequence "
                    + sequence.getId());
            return;
        }

        String filename = entry.getPartNumbersAsString() + ".gb";

        try {
            byte[] bytes = sequenceString.getBytes();
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);

            response.setContentType("text/plain");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");

            OutputStream os = response.getOutputStream();
            DataInputStream is = new DataInputStream(byteInputStream);

            int read = 0;

            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }
            os.flush();
            os.close();

        } catch (IOException e) {
            Logger.error(e);
        }

    }

    private void getFasta(HttpServletResponse response, Entry entry, Account account) {
        SequenceController sequenceController = new SequenceController(account);
        Sequence sequence = null;

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

        String sequenceString = null;
        try {
            sequenceString = SequenceController.compose(sequence, new FastaFormatter(sequence
                    .getEntry().getNamesAsString()));
        } catch (SequenceComposerException e) {
            Logger.error("Failed to generate fasta file for download!", e);
            return;
        }

        String filename = entry.getPartNumbersAsString() + ".fasta";

        try {
            byte[] bytes = sequenceString.getBytes();
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);

            response.setContentType("text/plain");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");

            OutputStream os = response.getOutputStream();
            DataInputStream is = new DataInputStream(byteInputStream);

            int read = 0;

            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }
            os.flush();
            os.close();

        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void getSBOL(HttpServletResponse response, Entry entry, Account account) {
        SequenceController sequenceController = new SequenceController(account);
        Sequence sequence = null;

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

        String sequenceString = null;
        try {
            sequenceString = SequenceController.compose(sequence, new SbolFormatter());
        } catch (SequenceComposerException e) {
            Logger.error("Failed to generate sbol file for download!", e);
            return;
        }

        String filename = entry.getPartNumbersAsString() + ".xml";

        try {
            byte[] bytes = sequenceString.getBytes();
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);

            response.setContentType("text/xml");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");

            OutputStream os = response.getOutputStream();
            DataInputStream is = new DataInputStream(byteInputStream);

            int read = 0;

            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }
            os.flush();
            os.close();

        } catch (IOException e) {
            Logger.error(e);
        }
    }
}
