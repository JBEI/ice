package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * General information for part data
 *
 * @author Hector Plahar
 */
public class PartInformation implements IDataTransferModel {

    private String recordId;
    private String name;
    private String owner;
    private String ownerEmail;
    private long ownerId;
    private String creator;
    private String creatorEmail;
    private long creatorId;
    private String alias;
    private String keywords;
    private String status;
    private String shortDescription;
    private String longDescription;
    private String references;
    private long creationTime;
    private long modificationTime;
    private Integer bioSafetyLevel;
    private String intellectualProperty;
    private String partId;
    private ArrayList<String> links; // comma separated list of links
    private String principalInvestigator;
    private String principalInvestigatorEmail;
    private ArrayList<String> selectionMarkers;
    private String fundingSource;
}
