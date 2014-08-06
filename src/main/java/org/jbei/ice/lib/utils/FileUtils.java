package org.jbei.ice.lib.utils;

import java.io.*;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;

import org.apache.commons.io.IOUtils;

/**
 * Utility methods for file handling.
 *
 * @author Zinovii Dmytriv, Timothy Ham, Hector Plahar
 */
public class FileUtils {
    /**
     * Read file specified by the path into a string.
     *
     * @param path Path of the file.
     * @return Content of file as String.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static String readFileToString(String path) throws IOException {
        return readFileToString(new File(path));
    }

    /**
     * Read the given {@link File} into a string.
     *
     * @param file File to read.
     * @return File content as string.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static String readFileToString(File file) throws IOException {
        StringBuilder contents = new StringBuilder();

        BufferedReader input = new BufferedReader(new FileReader(file));

        try {
            String line = null; //not declared within while loop
            /*
            * readLine is a bit quirky :
            * it returns the content of a line MINUS the newline.
            * it returns null only for the END of the stream.
            * it returns an empty String if two newlines appear in a row.
            */
            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        } finally {
            input.close();
        }

        return contents.toString();
    }

    /**
     * Write the given string content into a file with the specified path.
     *
     * @param path    Path of the file to be written.
     * @param content Content of the file.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static void writeStringToFile(String path, String content) throws IOException, IllegalArgumentException {
        writeStringToFile(new File(path), content);
    }

    /**
     * Write the given string content into the given {@link File}.
     *
     * @param file    File to write.
     * @param content Content of the file.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static void writeStringToFile(File file, String content) throws IOException, IllegalArgumentException {
        if (file == null) {
            throw new IllegalArgumentException("File should not be null.");
        }

        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file);
        }

        if (!file.isFile()) {
            throw new IllegalArgumentException("Should not be a directory: " + file);
        }

        if (!file.canWrite()) {
            throw new IllegalArgumentException("File cannot be written: " + file);
        }

        try (Writer output = new BufferedWriter(new FileWriter(file))) {
            //FileWriter always assumes default encoding is OK!
            output.write(content);
        }
    }

    /**
     * Write the given {@link java.io.InputStream} to the file with the given fileName.
     *
     * @param fileName
     * @param inputStream
     * @throws java.io.IOException
     */
    public static void writeFile(File attDir, String fileName, InputStream inputStream) throws IOException {
        File file = new File(attDir + File.separator + fileName);
        if (!attDir.exists()) {
            if (!attDir.mkdirs()) {
                throw new IOException("Could not create attachment directory " + attDir.getAbsolutePath());
            }
        }

        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Could not create attachment file " + file.getName());
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    /**
     * Save the given text to a file in the temporary directory, and alert the admin.
     * <p/>
     * Use this to capture interesting files and alert the admin of the fact.
     *
     * @param message
     * @param fileText
     * @param e
     * @throws UtilityException
     */
    public static void recordAndReportFile(String message, String fileText, Exception e) throws UtilityException {
        String tempFileDirectory = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        String filePath = tempFileDirectory + File.separator + Utils.generateUUID();
        message = "File has been written to: " + filePath + ". The caller message is :\n" + message;
        Logger.error(message, e);
        try {
            FileUtils.writeStringToFile(filePath, fileText);
        } catch (IllegalArgumentException | IOException e1) {
            throw new UtilityException(e1);
        }
    }
}
