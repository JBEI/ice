package org.jbei.ice.lib.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;

/**
 * IceXML serializer/deserializer.
 * <p/>
 * See ice.xsd and seq.xsd in the /docs directory for the xml schema.
 *
 * @author Timothy Ham, Hector Plahar
 */
public class IceXmlSerializer {

    private static final String MODIFICATION_TIME_STAMP = "modificationTimeStamp";
    private static final String CREATION_TIME_STAMP = "creationTimeStamp";
    private static final String TIME_STAMP = "timeStamp";
    private static final String DEPOSITOR_EMAIL = "depositorEmail";
    private static final String SEQUENCE_TRACE_FILE = "sequenceTraceFile";
    private static final String SEQUENCE_TRACES = "sequenceTraces";
    private static final String EXP = "exp";
    private static final String DESCRIPTION = "description";
    private static final String FILE_ID = "fileId";
    private static final String URL = "url";
    private static final String PLASMID = "plasmid";
    private static final String STRAIN = "strain";
    private static final String ARABIDOPSIS = "arabidopsis";
    private static final String PART = "part";
    private static final String ARABIDOPSIS_SEED_FIELDS = "arabidopsisSeedFields";
    private static final String PART_FIELDS = "partFields";
    private static final String STRAIN_FIELDS = "strainFields";
    private static final String PLASMID_FIELDS = "plasmidFields";
    private static final String SELECTION_MARKER = "selectionMarker";
    private static final String SELECTION_MARKERS = "selectionMarkers";
    private static final String PLANT_TYPE = "plantType";
    private static final String GENERATION = "generation";
    private static final String PARENTS = "parents";
    private static final String HARVEST_DATE = "harvestDate";
    private static final String ECOTYPE = "ecotype";
    private static final String HOMOZYGOSITY = "homozygosity";
    private static final String PLASMIDS = "plasmids";
    private static final String GENOTYPE_PHENOTYPE = "genotypePhenotype";
    private static final String HOST = "host";
    private static final String IS_CIRCULAR = "isCircular";
    private static final String PROMOTERS = "promoters";
    private static final String REPLICATES_IN = "replicatesIn";
    private static final String ORIGIN_OF_REPLICATION = "originOfReplication";
    private static final String BACKBONE = "backbone";
    private static final String FILE_NAME = "fileName";
    private static final String ATTACHMENT = "attachment";
    private static final String ATTACHMENTS = "attachments";
    private static final String PRINCIPAL_INVESTIGATOR = "principalInvestigator";
    private static final String FUNDING_SOURCE = "fundingSource";
    private static final String FUNDING_SOURCES = "fundingSources";
    private static final String INTELLECTUAL_PROPERTY = "intellectualProperty";
    private static final String BIO_SAFETY_LEVEL = "bioSafetyLevel";
    private static final String REFERENCES = "references";
    private static final String LONG_DESCRIPTION = "longDescription";
    private static final String SHORT_DESCRIPTION = "shortDescription";
    private static final String STATUS = "status";
    private static final String LINK = "link";
    private static final String LINKS = "links";
    private static final String CREATOR = "creator";
    private static final String EMAIL = "email";
    private static final String PERSON_NAME = "personName";
    private static final String OWNER = "owner";
    private static final String PART_NAME = "partName";
    private static final String PART_NUMBER = "partNumber";
    private static final String RECORD_TYPE = "recordType";
    private static final String RECORD_ID = "recordId";

    public static final Namespace iceNamespace = new Namespace("ice", "http://jbei.org/ice");
    public static final Namespace xsiNamespace = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    public static final Namespace expNamespace = new Namespace(EXP, "http://jbei.org/exp");
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Generate ice-xml from given List of {@link Entry}s.
     *
     * @param entries Entries to serialize.
     * @return xml document as string.
     * @throws UtilityException
     */
    public static String serializeToJbeiXml(Account account, List<Entry> entries) throws UtilityException {
        ArrayList<Sequence> sequences = new ArrayList<>();
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        for (Entry entry : entries) {
            try {
                sequences.add(sequenceController.getByEntry(entry));
            } catch (ControllerException e) {
                throw new UtilityException(e);
            }
        }

        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            writer = new XMLWriter(byteArrayOutputStream, format);
            writer.write(serializeToJbeiXml(account, entries, sequences));
            return byteArrayOutputStream.toString("utf8");
        } catch (IOException e) {
            throw new UtilityException(e);
        }
    }

    /**
     * Generate ice-xml from the given List of {@link Entry}s and {@link Sequence}s.
     *
     * @param entries   Entries to serialize.
     * @param sequences Corresponding sequences to serialize.
     * @return xml {@link Document}.
     * @throws UtilityException
     */
    public static Document serializeToJbeiXml(Account account, List<Entry> entries, List<Sequence> sequences)
            throws UtilityException {

        if (entries.size() != sequences.size()) {
            return null;
        }
        Document document = DocumentHelper.createDocument();

        QName iceRoot = new QName("ice", iceNamespace);
        Element root = document.addElement(iceRoot);
        root.add(iceNamespace);
        root.add(SeqXmlSerializer.seqNamespace);
        root.add(xsiNamespace);
        root.addAttribute(new QName("schemaLocation", xsiNamespace), "http://jbei.org/ice ice.xsd");

        for (int index = 0; index < entries.size(); index++) {
            Logger.debug("Serialize to XML " + entries.get(index).getRecordId());
            Element entryElement = toEntryElement(account, entries.get(index), sequences.get(index));
            root.add(entryElement);
        }

        return document;
    }

    /**
     * Generate single "entry" xml {@link Element} from the given {@link Entry} and {@link Sequence}
     *
     * @param entry    Entry to serialize.
     * @param sequence Sequence to serialize.
     * @return xml Element.
     * @throws UtilityException
     */
    private static Element toEntryElement(Account account, Entry entry, Sequence sequence) throws UtilityException {
        if (entry == null) {
            return null;
        }

        DefaultElement entryRoot = new DefaultElement("entry", iceNamespace);

        entryRoot.add(new DefaultElement(RECORD_ID, iceNamespace).addText(entry.getRecordId()));
        entryRoot.add(new DefaultElement(RECORD_TYPE, iceNamespace).addText(entry.getRecordType()));
        entryRoot.add(new DefaultElement(CREATION_TIME_STAMP, iceNamespace)
                              .addText(simpleDateFormat.format(entry.getCreationTime())));
        if (entry.getModificationTime() != null) {
            entryRoot.add(new DefaultElement(MODIFICATION_TIME_STAMP, iceNamespace)
                                  .addText(simpleDateFormat.format(entry.getModificationTime())));
        } else {
            entryRoot.add(new DefaultElement(MODIFICATION_TIME_STAMP, iceNamespace)
                                  .addText(simpleDateFormat.format(entry.getCreationTime())));
        }
        entryRoot.add(new DefaultElement(PART_NUMBER, iceNamespace).addText(entry.getPartNumber()));
        entryRoot.add(new DefaultElement(PART_NAME, iceNamespace).addText(entry.getName()));

        DefaultElement owner = new DefaultElement(OWNER, iceNamespace);
        owner.add(new DefaultElement(PERSON_NAME, iceNamespace).addText(emptyStringify(entry.getOwner())));
        owner.add(new DefaultElement(EMAIL, iceNamespace).addText(emptyStringify(entry.getOwnerEmail())));
        entryRoot.add(owner);

        DefaultElement creator = new DefaultElement(CREATOR, iceNamespace);
        creator.add(new DefaultElement(PERSON_NAME, iceNamespace).addText(emptyStringify(entry.getCreator())));
        creator.add(new DefaultElement(EMAIL, iceNamespace).addText(emptyStringify(entry.getCreatorEmail())));
        entryRoot.add(creator);

        if (entry.getLinks().size() > 0) {
            DefaultElement links = new DefaultElement(LINKS, iceNamespace);
            for (Link link : entry.getLinks()) {
                links.add(new DefaultElement(LINK, iceNamespace)
                                  .addAttribute(URL, emptyStringify(link.getUrl()))
                                  .addText(emptyStringify(link.getLink())));
            }
            entryRoot.add(links);
        }

        entryRoot.add(new DefaultElement(STATUS, iceNamespace).addText(emptyStringify(entry.getStatus())));

        entryRoot.add(new DefaultElement(LONG_DESCRIPTION, iceNamespace)
                              .addText(emptyStringify(entry.getLongDescription())));
        entryRoot.add(new DefaultElement(SHORT_DESCRIPTION, iceNamespace).addText(
                entry.getShortDescription()));
        entryRoot.add(new DefaultElement(REFERENCES, iceNamespace).addText(emptyStringify(entry.getReferences())));
        entryRoot.add(getEntryTypeSpecificFields(entry));

        entryRoot.add(new DefaultElement(BIO_SAFETY_LEVEL, iceNamespace)
                              .addText(emptyStringify(entry.getBioSafetyLevel().toString())));
        entryRoot.add(new DefaultElement(INTELLECTUAL_PROPERTY, iceNamespace)
                              .addText(emptyStringify(entry.getIntellectualProperty())));

        if (entry.getFundingSources().size() > 0) {
            DefaultElement fundingSources = new DefaultElement(FUNDING_SOURCES, iceNamespace);
            for (EntryFundingSource fundingSource : entry.getFundingSources()) {
                fundingSources.add(new DefaultElement(FUNDING_SOURCE, iceNamespace)
                                           .addText(emptyStringify(fundingSource.getFundingSource().getFundingSource()))
                                           .addAttribute(
                                                   PRINCIPAL_INVESTIGATOR,
                                                   emptyStringify(fundingSource.getFundingSource()
                                                                               .getPrincipalInvestigator())));
            }
            entryRoot.add(fundingSources);
        }

        if (sequence != null) {
            entryRoot.add(SeqXmlSerializer.serializeToSeqXmlAsElement(sequence));
        }

        ArrayList<Attachment> attachments;
        AttachmentController attachmentController = new AttachmentController();

        try {
            attachments = attachmentController.getByEntry(account, entry);
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }
        if (attachments != null && attachments.size() > 0) {
            DefaultElement attachmentsRoot = new DefaultElement(ATTACHMENTS, iceNamespace);
            for (Attachment attachment : attachments) {
                File file;
                String fileString;
                try {
                    file = attachmentController.getFile(account, attachment);
                    fileString = SerializationUtils
                            .serializeBytesToBase64String(org.apache.commons.io.FileUtils.readFileToByteArray(file));
                } catch (IOException | ControllerException | PermissionException e) {
                    throw new UtilityException(e);
                }

                attachmentsRoot.add(new DefaultElement(ATTACHMENT, iceNamespace)
                                            .addCDATA(fileString).addAttribute(FILE_NAME, attachment.getFileName())
                                            .addAttribute(FILE_ID, attachment.getFileId())
                                            .addAttribute(DESCRIPTION, attachment.getDescription()));
            }
            entryRoot.add(attachmentsRoot);
        }

        Element expElement = getExperimentElement(entry);
        if (expElement != null) {
            entryRoot.add(expElement);
        }
        return entryRoot;
    }

    /**
     * Generate type specific fields as xml {@link Element} from the given {@link Entry}.
     *
     * @param entry Entry to serialize.
     * @return xml Element containing type specific fields.
     */
    private static Element getEntryTypeSpecificFields(Entry entry) {
        String fieldName = null;
        if (entry.getRecordType().equals(PLASMID)) {
            fieldName = PLASMID_FIELDS;
        } else if (entry.getRecordType().equals(STRAIN)) {
            fieldName = STRAIN_FIELDS;
        } else if (entry.getRecordType().equals(PART)) {
            fieldName = PART_FIELDS;
        } else if (entry.getRecordType().equals(ARABIDOPSIS)) {
            fieldName = ARABIDOPSIS_SEED_FIELDS;
        }
        DefaultElement fields = new DefaultElement(fieldName, iceNamespace);

        if (entry.getRecordType().equals(PLASMID)) {
            Plasmid plasmid = (Plasmid) entry;
            if (getSelectionMarkers(plasmid) != null) {
                fields.add(getSelectionMarkers(plasmid));
            }
            fields.add(new DefaultElement(BACKBONE, iceNamespace).addText(emptyStringify(plasmid.getBackbone())));
            fields.add(new DefaultElement(ORIGIN_OF_REPLICATION, iceNamespace)
                               .addText(emptyStringify(plasmid.getOriginOfReplication())));
            fields.add(new DefaultElement(PROMOTERS, iceNamespace).addText(emptyStringify(plasmid.getPromoters())));
            fields.add(new DefaultElement(REPLICATES_IN, iceNamespace).addText(
                    emptyStringify(plasmid.getReplicatesIn())));
            fields.add(new DefaultElement(IS_CIRCULAR, iceNamespace).addText((plasmid.getCircular() ? "true"
                    : "false")));
        } else if (entry.getRecordType().equals(STRAIN)) {
            Strain strain = (Strain) entry;
            if (getSelectionMarkers(strain) != null) {
                fields.add(getSelectionMarkers(strain));
            }
            fields.add(new DefaultElement(HOST, iceNamespace).addText(emptyStringify(strain.getHost())));
            fields.add(new DefaultElement(GENOTYPE_PHENOTYPE, iceNamespace)
                               .addText(emptyStringify(strain.getGenotypePhenotype())));
            fields.add(new DefaultElement(PLASMIDS, iceNamespace).addText(emptyStringify(strain.getPlasmids())));
        } else if (entry.getRecordType().equals(ARABIDOPSIS)) {
            ArabidopsisSeed seed = (ArabidopsisSeed) entry;
            fields.add(new DefaultElement(HOMOZYGOSITY, iceNamespace).addText(emptyStringify(seed.getHomozygosity())));
            fields.add(new DefaultElement(ECOTYPE, iceNamespace).addText(emptyStringify(seed.getEcotype())));

            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            fields.add(new DefaultElement(HARVEST_DATE, iceNamespace).addText(simpleDateFormat
                                                                                      .format(seed.getHarvestDate())));

            fields.add(new DefaultElement(PARENTS, iceNamespace).addText(emptyStringify(seed.getParents())));
            fields.add(new DefaultElement(GENERATION, iceNamespace).addText(seed.getGeneration().toString()));
            fields.add(new DefaultElement(PLANT_TYPE, iceNamespace).addText(seed.getPlantType().toString()));
        }

        return fields;
    }

    /**
     * Generate selection marker xml {@link Element} from the given {@link Entry}.
     *
     * @param entry Entry to serialize.
     * @return xml Element containing selection markers.
     */
    private static Element getSelectionMarkers(Entry entry) {
        if (entry.getSelectionMarkers().size() == 0) {
            return null;
        }
        DefaultElement selectionMarkers = new DefaultElement(SELECTION_MARKERS, iceNamespace);
        for (SelectionMarker marker : entry.getSelectionMarkers()) {
            selectionMarkers.add(new DefaultElement(SELECTION_MARKER, iceNamespace).addText(marker.getName()));
        }

        return selectionMarkers;
    }

    /**
     * Generate experiment xml {@link Element} from the given {@link Entry}.
     * <p/>
     * Currently generates sequence traces. They use a different schema called exp.xsd. Move this
     * out as experimental data schema develops.
     *
     * @param entry Entry to serialize.
     * @return xml Element.
     * @throws UtilityException
     */
    private static Element getExperimentElement(Entry entry) throws UtilityException {
        SequenceAnalysisController controller = ControllerFactory.getSequenceAnalysisController();
        Element result = null;
        DefaultElement expElement = new DefaultElement(EXP, expNamespace);
        DefaultElement tracesElement = new DefaultElement(SEQUENCE_TRACES, expNamespace);
        List<TraceSequence> traces;
        try {
            traces = controller.getTraceSequences(entry);
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }

        if (traces != null) {
            int counter = 0;
            for (TraceSequence trace : traces) {

                File traceFile;
                String traceString;
                try {
                    traceFile = controller.getFile(trace);
                    traceString = SerializationUtils.serializeBytesToBase64String(org.apache.commons.io.FileUtils

















                                                                                                       .readFileToByteArray(
                                                                                                               traceFile));
                } catch (IOException | ControllerException e) {
                    // skip this one
                    Logger.error("Could not serialize trace file " + trace.getFileId());
                    continue;
                }

                Element traceElement = new DefaultElement(SEQUENCE_TRACE_FILE, expNamespace)
                        .addCDATA(traceString).addAttribute(FILE_NAME, trace.getFilename())
                        .addAttribute(FILE_ID, trace.getFileId());
                traceElement.addAttribute(DEPOSITOR_EMAIL, trace.getDepositor());
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                traceElement.addAttribute(TIME_STAMP,
                                          simpleDateFormat.format(trace.getCreationTime()));
                tracesElement.add(traceElement);
                counter++;
            }

            if (counter > 0) {
                expElement.add(tracesElement);
                result = expElement;
            }
        }

        return result;
    }

    /**
     * Replace null value of a string object into an empty string. Non-null value is returned unaltered.
     */
    private static String emptyStringify(String string) {
        if (string == null) {
            return "";
        } else {
            return string;
        }
    }
}
