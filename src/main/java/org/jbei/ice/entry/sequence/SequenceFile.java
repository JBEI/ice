package org.jbei.ice.entry.sequence;

import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.utils.Utils;

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
        if (!Files.exists(path))
            throw new IOException("Invalid sequence file path. Might be using sequence string instead");
    }

    public InputStream getStream() throws FileNotFoundException {
        return new FileInputStream(path.toFile());
    }

    public Path getFilePath() {
        return this.path;
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
