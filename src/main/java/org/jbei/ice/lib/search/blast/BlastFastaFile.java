package org.jbei.ice.lib.search.blast;

import org.apache.commons.io.FileUtils;
import org.jbei.ice.lib.common.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.stream.Stream;

/**
 * Fasta file containing all sequences used to create the blast database
 *
 * @author Hector Plahar
 */
public class BlastFastaFile {

    private final static String FILE_NAME = "BlastFastaFile";
    private static final String LOCK_FILE_NAME = "write.lock";
    private final Path filePath;
    private boolean exclude;
    private FileLock lock;

    /**
     * @param dir directory path for the fasta file
     */
    public BlastFastaFile(Path dir) {
        if (Files.exists(dir) && !Files.isDirectory(dir))
            throw new IllegalArgumentException("Cannot create fasta file in " + dir + " because it is not a directory");

        this.filePath = Paths.get(dir.toString(), FILE_NAME);
        this.exclude = false;
    }

    public Path getFilePath() {
        return this.filePath;
    }

    private File createLock() {
        Path directory = this.filePath.getParent();
        File lockFile = Paths.get(directory.toString(), LOCK_FILE_NAME).toFile();

        if (lockFile.exists()) {
            Logger.info("Blast db locked (lockfile - " + lockFile.getAbsolutePath() + "). Rebuild aborted!");
            return null;
        }

        try {
            if (!lockFile.createNewFile()) {
                Logger.warn("Could not create lock file for blast rebuild");
                return null;
            }

            FileOutputStream fos = new FileOutputStream(lockFile);
            lock = fos.getChannel().tryLock();
            if (lock == null) {
                Logger.error("Could not obtain lock on blast fasta file");
                return lockFile;
            }
        } catch (Exception e) {
            Logger.error(e);
        }

        return lockFile;
    }

    private void releaseLock(File lockFile) {
        if (lock != null) {
            try {
                lock.release();
            } catch (IOException e) {
                Logger.error("Exception releasing blast file lock");
            }
        }
        FileUtils.deleteQuietly(lockFile);
    }

    /**
     * Create a new (empty) fasta file
     */
    public void createNew() {
        try {
            Files.deleteIfExists(this.filePath);
            Files.createFile(this.filePath);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public boolean write(Iterable<String> iterable) {
        File lockFile = createLock();
        if (lockFile == null)
            return false;

        try {
            if (!Files.exists(this.filePath))
                Files.createFile(this.filePath);

            if (lock == null)
                return false;

            Files.write(this.filePath, iterable, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            Logger.error(e);
            return false;
        } finally {
            releaseLock(lockFile);
        }
    }

    public void delete(String partNumber) throws IOException {
        File lockFile = createLock();
        if (lockFile == null)
            return;

        try {
            Path tmpFile = Paths.get(filePath.getParent().toString(), FILE_NAME + ".tmp");
            Files.deleteIfExists(tmpFile);
            Files.createFile(tmpFile);
            if (!Files.exists(tmpFile))
                throw new IOException(tmpFile + " could not be created");

            Stream<String> lines = Files.lines(this.filePath);

            lines.forEach(line -> {

                if (line.startsWith(">")) {
                    String[] split = line.split(",");
                    if (split.length != 4)
                        return;

                    exclude = split[3].trim().equalsIgnoreCase(partNumber.trim());
                }

                if (exclude) return;

                try {
                    Files.write(tmpFile, (line + "\n").getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    Logger.error(e);
                }
            });

            Files.move(tmpFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            releaseLock(lockFile);
        }
    }

    public boolean isLocked() {
        return lock == null || lock.isValid();
    }
}
