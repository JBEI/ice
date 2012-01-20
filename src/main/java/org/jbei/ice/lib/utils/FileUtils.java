package org.jbei.ice.lib.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.jbei.ice.lib.logging.Logger;

/**
 * Utility methods for file handling.
 * 
 * @author Zinovii Dmytriv, Timothy Ham
 * 
 */
public class FileUtils {
    /**
     * Read file specified by the path into a string.
     * 
     * @param path
     *            Path of the file.
     * @return Content of file as String.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static String readFileToString(String path) throws IOException, FileNotFoundException {
        return readFileToString(new File(path));
    }

    /**
     * Read the given {@link File} into a string.
     * 
     * @param file
     *            File to read.
     * @return File content as string.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static String readFileToString(File file) throws IOException, FileNotFoundException {
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
     * @param path
     *            Path of the file to be written.
     * @param content
     *            Content of the file.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static void writeStringToFile(String path, String content) throws FileNotFoundException,
            IOException, IllegalArgumentException {
        writeStringToFile(new File(path), content);
    }

    /**
     * Write the given string content into the given {@link File}.
     * 
     * @param file
     *            File to write.
     * @param content
     *            Content of the file.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static void writeStringToFile(File file, String content) throws FileNotFoundException,
            IOException, IllegalArgumentException {
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

        Writer output = new BufferedWriter(new FileWriter(file));
        try {
            //FileWriter always assumes default encoding is OK!
            output.write(content);
        } finally {
            output.close();
        }
    }

    /**
     * Save the given text to a file in the temporary directory, and alert the admin.
     * <p>
     * Use this to capture interesting files and alert the admin of the fact.
     * 
     * @param message
     * @param fileText
     * @param e
     * @throws UtilityException
     */
    public static void recordAndReportFile(String message, String fileText, Exception e)
            throws UtilityException {
        String tempFileDirectory = JbeirSettings.getSetting("TEMPORARY_DIRECTORY");
        String filePath = tempFileDirectory + File.separator + Utils.generateUUID();

        message = "File has been written to: " + filePath + ". The caller message is :\n" + message;
        Logger.error(message, e);

        try {
            FileUtils.writeStringToFile(filePath, fileText);
        } catch (FileNotFoundException e1) {
            throw new UtilityException(e1);
        } catch (IllegalArgumentException e1) {
            throw new UtilityException(e1);
        } catch (IOException e1) {
            throw new UtilityException(e1);
        }

    }

}
