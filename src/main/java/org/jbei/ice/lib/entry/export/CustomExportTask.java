package org.jbei.ice.lib.entry.export;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.email.EmailFactory;
import org.jbei.ice.lib.entry.EntriesAsCSV;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class CustomExportTask extends Task {

    private final String userId;
    private final EntrySelection selection;
    private final SequenceFormat format;
    private final Path tmp;
    private final boolean onePerFolder;

    public CustomExportTask(String userId, EntrySelection selection, SequenceFormat format, boolean onePerFolder) {
        this.userId = userId;
        this.selection = selection;
        this.format = format;
        this.onePerFolder = onePerFolder;
        tmp = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY));
    }

    @Override
    public void execute() {
        // check for the export folder
        Path exportDirectory = Paths.get(tmp.toString(), "export");
        if (!Files.exists(exportDirectory)) {
            try {
                Files.createDirectory(exportDirectory);
            } catch (IOException e) {
                Logger.error(e);
                return;
            }
        }

        // create file name unique to user
        final String uniqueFileIdentifier = UUID.randomUUID().toString();
        String fileName = userId + "_" + uniqueFileIdentifier + "_export-data.zip";
        Path exportPath = Paths.get(exportDirectory.toString(), fileName);

        EntriesAsCSV entriesAsCSV = new EntriesAsCSV(userId);
        try {
            ByteArrayOutputStream byteArrayOutputStream = entriesAsCSV.customize(selection, format, this.onePerFolder);
            try (OutputStream outputStream = new FileOutputStream(exportPath.toString())) {
                byteArrayOutputStream.writeTo(outputStream);
            }
        } catch (IOException e) {
            Logger.error(e);
            return;
        }

        if (!"Administrator".equalsIgnoreCase(userId))
            sendEmail(uniqueFileIdentifier);
        Logger.info("Export file available: " + exportPath.toString());
    }

    private void sendEmail(String uuid) {
        String subject = "Exported file available";
        String body = "Dear " + userId + ",";
        body += "\n\nYour export request has completed successfully. ";
        body += "You may download the exported file using the personalized link below :";
        body += "\n\n\thttps://" + Utils.getConfigValue(ConfigurationKey.URI_PREFIX) + "/download/" + uuid;
        body += "\n\nPlease report any issues you have with this download to your site administrator. ";
        body += "Be sure to include the unique identifier \"" + uuid + "\" in your report.";
        body += "\n\nThank you.";
        EmailFactory.getEmail().send(userId, subject, body);
    }
}
