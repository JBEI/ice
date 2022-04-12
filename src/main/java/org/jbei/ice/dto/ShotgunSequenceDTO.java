package org.jbei.ice.dto;

import org.jbei.ice.account.Account;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.IDataTransferModel;
import org.jbei.ice.storage.hibernate.dao.ShotgunSequenceDAO;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.ShotgunSequence;
import org.jbei.ice.utils.Utils;

import java.io.*;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ShotgunSequenceDTO implements IDataTransferModel {

    public long id;
    public String filename;
    public String call;
    public double score;
    public String fileId;
    public Account depositor;
    public long created;
    private boolean canEdit;


    public ShotgunSequenceDTO(ShotgunSequence s) {
        created = s.getCreationTime().getTime();
        filename = s.getFilename();
        fileId = s.getFileId();
        id = s.getId();
        setQuality(fileId);

        AccountModel a = DAOFactory.getAccountDAO().getByEmail(s.getDepositor());
        depositor = new Account();
        depositor.setEmail(a.getEmail());
        depositor.setFirstName(a.getFirstName());
        depositor.setLastName(a.getLastName());
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String name) {
        this.filename = name;
    }

    public Account getDepositor() {
        return depositor;
    }

    public void setDepositor(Account depositor) {
        this.depositor = depositor;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created.getTime();
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    private void setQuality(String fileId) {
        call = "fakestring";
        score = -1;

        String dataDirectory = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        File traceFilesDirectory = Paths.get(dataDirectory, ShotgunSequenceDAO.SHOTGUN_SEQUENCES_DIR).toFile();
        File file = new File(traceFilesDirectory + File.separator + fileId);

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Logger.error(e);
            return;
        }

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;
            while (true) {
                zipEntry = zis.getNextEntry();

                if (zipEntry != null) {
                    if (!zipEntry.isDirectory() && zipEntry.getName().startsWith("call")) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int c;
                        while ((c = zis.read()) != -1) {
                            byteArrayOutputStream.write(c);
                        }

                        this.call = byteArrayOutputStream.toString().trim();
                    } else if (!zipEntry.isDirectory() && zipEntry.getName().startsWith("score")) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int c;
                        while ((c = zis.read()) != -1) {
                            byteArrayOutputStream.write(c);
                        }

                        Scanner scan = new Scanner(byteArrayOutputStream.toString());
                        this.score = scan.nextDouble();
                    }
                } else {
                    return;
                }
            }
        } catch (IOException e) {
            String errMsg = ("Could not parse zip file.");
            Logger.error(errMsg);
        }
    }

}
