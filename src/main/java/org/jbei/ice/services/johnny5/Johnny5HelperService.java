package org.jbei.ice.services.johnny5;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jbei.ice.lib.logging.UsageLogger;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.johnny5.vo.FileInfo;

public class Johnny5HelperService {
    public byte[] archiveJohnny5Files(String prefix, String partFile, String targetFile,
            String seqFile, List<FileInfo> fileList) {
        final int BUFFER = 4096;

        byte[] resultBytes = null;

        UsageLogger.info("Johnny5HelperService: archiving johnny5 files...");

        String dataDirectory = JbeirSettings.getSetting("DATA_DIRECTORY");

        String sourceName = prefix + "_seqListFile.csv";
        String source = dataDirectory + "/" + sourceName;

        String source2Name = prefix + "_partListFile.csv";
        String source2 = dataDirectory + "/" + source2Name;

        String source3Name = prefix + "_targetListFile.csv";
        String source3 = dataDirectory + "/" + source3Name;

        String target = dataDirectory + "/" + prefix + "_completeOutput-" + Utils.generateUUID()
                + ".zip";

        try {
            // Write first three arguments to a files
            // Create file
            FileWriter fstream1 = new FileWriter(source);
            BufferedWriter out1 = new BufferedWriter(fstream1);
            out1.write(seqFile);
            // Close the output stream
            out1.close();

            // Create file
            FileWriter fstream2 = new FileWriter(source2);
            BufferedWriter out2 = new BufferedWriter(fstream2);
            out2.write(partFile);
            // Close the output stream
            out2.close();

            // Create file
            FileWriter fstream3 = new FileWriter(source3);
            BufferedWriter out3 = new BufferedWriter(fstream3);
            out3.write(targetFile);
            //Close the output stream
            out3.close();

            for (FileInfo fileInfo : fileList) {
                // Create file
                FileWriter fstream = new FileWriter(dataDirectory + "/" + fileInfo.getName());
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(fileInfo.getFile());
                //Close the output stream
                out.close();
            }

            FileOutputStream dest = new FileOutputStream(target);
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];

            for (int i = 0; i < 3 + fileList.size(); i++) {
                FileInputStream fis;
                ZipEntry entry;
                if (i == 0) {
                    fis = new FileInputStream(source);
                    entry = new ZipEntry(sourceName);
                } else if (i == 1) {
                    fis = new FileInputStream(source2);
                    entry = new ZipEntry(source2Name);
                } else if (i == 2) {
                    fis = new FileInputStream(source3);
                    entry = new ZipEntry(source3Name);
                } else {
                    FileInfo fi = fileList.get(i - 3);
                    fis = new FileInputStream(dataDirectory + "/" + fi.getName());
                    entry = new ZipEntry(fi.getName());
                }

                BufferedInputStream origin = new BufferedInputStream(fis, BUFFER);
                zos.putNextEntry(entry);

                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    zos.write(data, 0, count);
                }

                origin.close();

            }

            // Finish zip process
            zos.close();

            //clean up
            File del1 = new File(source);
            File del2 = new File(source2);
            File del3 = new File(source3);
            del1.delete();
            del2.delete();
            del3.delete();

            for (FileInfo fileInfo : fileList) {
                File del = new File(dataDirectory + "/" + fileInfo.getName());
                del.delete();
            }

            // Reading bytes
            FileInputStream fis = new FileInputStream(target);
            FileChannel fc = fis.getChannel();

            resultBytes = new byte[(int) (fc.size())];
            ByteBuffer bb = ByteBuffer.wrap(resultBytes);
            fc.read(bb);
        } catch (Exception e) {
            UsageLogger.error(Utils.stackTraceToString(e));
        }

        UsageLogger.info("Johnny5HelperService: archived successfully. " + target);

        return resultBytes;
    }
}