package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.io.FileUtils;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Represents a locally stored sequence file
 *
 * @author Hector Plahar
 */
public class SequenceFile {

    private final Path path;
    private String fileName;
    private boolean deleted;

    public SequenceFile() throws IOException {
        Path directoryPath = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), "sequences");
        if (!Files.exists(directoryPath))
            Files.createDirectory(directoryPath);

        fileName = UUID.randomUUID().toString();
        path = Paths.get(directoryPath.toString(), fileName);
        Files.createFile(path);
    }

    public SequenceFile(String fileName) throws IOException {
        Path directoryPath = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), "sequences");
        if (!Files.exists(directoryPath))
            Files.createDirectory(directoryPath);
        this.fileName = fileName;
        path = Paths.get(directoryPath.toString(), fileName);
    }

    public InputStream getStream() throws FileNotFoundException {
        return new FileInputStream(path.toFile());
    }

    // write a new line in the sequence file
    public void writeLine(String line) throws IOException {
        if (line == null)
            return;

        if (deleted)
            throw new IOException("File has been deleted");

        line += System.lineSeparator();
        FileUtils.writeByteArrayToFile(path.toFile(), line.getBytes(), true);
    }

    public void delete() throws IOException {
        deleted = true;
        Files.deleteIfExists(path);
        fileName = null;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getFileName() {
        return fileName;
    }
}
