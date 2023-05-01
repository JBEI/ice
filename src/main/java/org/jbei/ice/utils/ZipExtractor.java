package org.jbei.ice.utils;

import org.jbei.ice.config.ConfigurationSettings;
import org.jbei.ice.dto.ConfigurationKey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {

    private final Path tmpDir;

    public ZipExtractor() {
        String tmpDir = new ConfigurationSettings().getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        this.tmpDir = Paths.get(tmpDir);
    }

    public Path extract(InputStream inputStream) throws IOException {
        final String directory = UUID.randomUUID().toString();
        final Path path;
        if (this.tmpDir == null)
            path = Files.createTempDirectory(directory);
        else
            path = Files.createDirectory(Paths.get(tmpDir.toString(), directory));

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            // Loop through each entry in the zip file
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                // Get the name of the entry and create a file in the temporary directory with that name
                String fileName = zipEntry.getName();
                Path filePath = path.resolve(fileName);
                File file = filePath.toFile();

                if (zipEntry.isDirectory()) {
                    // Create directories for directory entries
                    file.mkdirs();
                } else {
                    // Create the file for file entries
                    file.createNewFile();

                    // Write the contents of the entry to the file
                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }

                // Move to the next entry in the zip file
                zipEntry = zipInputStream.getNextEntry();
            }
        }

        return path;
    }
}
