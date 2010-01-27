package org.jbei.ice.lib.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FileUtils {
    public static String readFileToString(String path) throws IOException, FileNotFoundException {
        return readFileToString(new File(path));
    }

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

    public static void writeStringToFile(String path, String content) throws FileNotFoundException,
            IOException, IllegalArgumentException {
        writeStringToFile(new File(path), content);
    }

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
}
