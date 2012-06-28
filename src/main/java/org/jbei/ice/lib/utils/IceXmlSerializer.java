package org.jbei.ice.lib.utils;

import com.ibm.icu.util.Calendar;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed.Generation;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Link;
import org.jbei.ice.lib.entry.model.Name;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Part.AssemblyStandard;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.TraceSequenceDAO;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.vo.AttachmentData;
import org.jbei.ice.lib.vo.CompleteEntry;
import org.jbei.ice.lib.vo.SequenceTraceFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

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
    private static final String PACKAGE_FORMAT = "packageFormat";
    private static final String PLASMIDS = "plasmids";
    private static final String GENOTYPE_PHENOTYPE = "genotypePhenotype";
    private static final String HOST = "host";
    private static final String IS_CIRCULAR = "isCircular";
    private static final String PROMOTERS = "promoters";
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
    private static final String LONG_DESCRIPTION_MARKUP_TYPE = "longDescriptionMarkupType";
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
    private static final String PART_NAMES = "partNames";
    private static final String PART_NUMBER = "partNumber";
    private static final String PART_NUMBERS = "partNumbers";
    private static final String RECORD_TYPE = "recordType";
    private static final String RECORD_ID = "recordId";
    private static final String INDEX = "index";
    private static final String SEQ = "seq";

    public static Namespace iceNamespace = new Namespace("ice", "http://jbei.org/ice");
    public static Namespace xsiNamespace = new Namespace("xsi",
                                                         "http://www.w3.org/2001/XMLSchema-instance");
    public static Namespace expNamespace = new Namespace(EXP, "http://jbei.org/exp");

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final ArrayList<CompleteEntry> completeEntries = new ArrayList<CompleteEntry>();

    /**
     * Generate ice-xml from given List of {@link Entry}s.
     *
     * @param entries Entries to serialize.
     * @return xml document as string.
     * @throws UtilityException
     */
    public static String serializeToJbeiXml(Account account, List<Entry> entries) throws UtilityException {
        ArrayList<Sequence> sequences = new ArrayList<Sequence>();
        SequenceController sequenceController = new SequenceController();
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
            String temp = byteArrayOutputStream.toString("utf8");
            return temp;
        } catch (UnsupportedEncodingException e) {
            throw new UtilityException(e);
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
            entryElement.addAttribute(INDEX, Integer.toString(index));
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
        DefaultElement partNumbers = new DefaultElement(PART_NUMBERS, iceNamespace);
        for (PartNumber partNumber : entry.getPartNumbers()) {
            partNumbers.add(new DefaultElement(PART_NUMBER, iceNamespace).addText(partNumber
                                                                                          .getPartNumber()));
        }
        entryRoot.add(partNumbers);

        DefaultElement partNames = new DefaultElement(PART_NAMES, iceNamespace);
        for (Name name : entry.getNames()) {
            partNames.add(new DefaultElement(PART_NAME, iceNamespace).addText(name.getName()));
        }
        entryRoot.add(partNames);

        DefaultElement owner = new DefaultElement(OWNER, iceNamespace);
        owner.add(new DefaultElement(PERSON_NAME, iceNamespace).addText(emptyStringify(entry
                                                                                               .getOwner())));
        owner.add(new DefaultElement(EMAIL, iceNamespace).addText(emptyStringify(entry
                                                                                         .getOwnerEmail())));
        entryRoot.add(owner);

        DefaultElement creator = new DefaultElement(CREATOR, iceNamespace);
        creator.add(new DefaultElement(PERSON_NAME, iceNamespace).addText(emptyStringify(entry
                                                                                                 .getCreator())));
        creator.add(new DefaultElement(EMAIL, iceNamespace).addText(emptyStringify(entry
                                                                                           .getCreatorEmail())));
        entryRoot.add(creator);

        if (entry.getLinks().size() > 0) {
            DefaultElement links = new DefaultElement(LINKS, iceNamespace);
            for (Link link : entry.getLinks()) {
                links.add(new DefaultElement(LINK, iceNamespace).addAttribute(URL,
                                                                              emptyStringify(link.getUrl())).addText(
                        emptyStringify(link.getLink())));
            }
            entryRoot.add(links);
        }

        entryRoot.add(new DefaultElement(STATUS, iceNamespace).addText(emptyStringify(entry.getStatus())));

        entryRoot.add(new DefaultElement(SHORT_DESCRIPTION, iceNamespace)
                              .addText(emptyStringify(entry.getShortDescription())));
        entryRoot.add(new DefaultElement(LONG_DESCRIPTION, iceNamespace)
                              .addText(emptyStringify(entry.getLongDescription())));
        entryRoot.add(new DefaultElement(LONG_DESCRIPTION_MARKUP_TYPE, iceNamespace).addText(
                entry.getLongDescriptionType()));
        entryRoot.add(new DefaultElement(REFERENCES, iceNamespace).addText(emptyStringify(entry
                                                                                                  .getReferences())));
        entryRoot.add(getEntryTypeSpecificFields(entry));

        entryRoot.add(new DefaultElement(BIO_SAFETY_LEVEL, iceNamespace)
                              .addText(emptyStringify(entry.getBioSafetyLevel().toString())));
        entryRoot.add(new DefaultElement(INTELLECTUAL_PROPERTY, iceNamespace)
                              .addText(emptyStringify(entry.getIntellectualProperty())));

        if (entry.getEntryFundingSources().size() > 0) {
            DefaultElement fundingSources = new DefaultElement(FUNDING_SOURCES, iceNamespace);
            for (EntryFundingSource fundingSource : entry.getEntryFundingSources()) {
                fundingSources.add(new DefaultElement(FUNDING_SOURCE, iceNamespace).addText(
                        emptyStringify(fundingSource.getFundingSource().getFundingSource()))
                                                                                   .addAttribute(
                                                                                           PRINCIPAL_INVESTIGATOR,
                                                                                           emptyStringify(fundingSource

















































































































































































































                                                                                                                  .getFundingSource()
                                                                                                                  .getPrincipalInvestigator())));
            }
            entryRoot.add(fundingSources);
        }

        if (sequence != null) {
            entryRoot.add(SeqXmlSerializer.serializeToSeqXmlAsElement(sequence));
        }

        ArrayList<Attachment> attachments = null;
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
                            .serializeBytesToBase64String(org.apache.commons.io.FileUtils
                                                                               .readFileToByteArray(file));

                } catch (FileNotFoundException e) {
                    throw new UtilityException(e);

                } catch (IOException e) {
                    throw new UtilityException(e);
                } catch (ControllerException e) {
                    throw new UtilityException(e);
                } catch (PermissionException e) {
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
            fields.add(new DefaultElement(BACKBONE, iceNamespace).addText(emptyStringify(plasmid
                                                                                                 .getBackbone())));
            fields.add(new DefaultElement(ORIGIN_OF_REPLICATION, iceNamespace)
                               .addText(emptyStringify(plasmid.getOriginOfReplication())));
            fields.add(new DefaultElement(PROMOTERS, iceNamespace).addText(emptyStringify(plasmid
                                                                                                  .getPromoters())));
            fields.add(new DefaultElement(IS_CIRCULAR, iceNamespace).addText((plasmid.getCircular() ? "true"
                    : "false")));
        } else if (entry.getRecordType().equals(STRAIN)) {
            Strain strain = (Strain) entry;
            if (getSelectionMarkers(strain) != null) {
                fields.add(getSelectionMarkers(strain));
            }
            fields.add(new DefaultElement(HOST, iceNamespace).addText(emptyStringify(strain
                                                                                             .getHost())));
            fields.add(new DefaultElement(GENOTYPE_PHENOTYPE, iceNamespace)
                               .addText(emptyStringify(strain.getGenotypePhenotype())));
            fields.add(new DefaultElement(PLASMIDS, iceNamespace).addText(emptyStringify(strain
                                                                                                 .getPlasmids())));
        } else if (entry.getRecordType().equals(PART)) {
            Part part = (Part) entry;
            fields.add(new DefaultElement(PACKAGE_FORMAT, iceNamespace).addText(emptyStringify(part
                                                                                                       .getPackageFormat()
                                                                                                       .toString())));
        } else if (entry.getRecordType().equals(ARABIDOPSIS)) {
            ArabidopsisSeed seed = (ArabidopsisSeed) entry;
            fields.add(new DefaultElement(HOMOZYGOSITY, iceNamespace).addText(emptyStringify(seed
                                                                                                     .getHomozygosity())));
            fields.add(new DefaultElement(ECOTYPE, iceNamespace).addText(emptyStringify(seed
                                                                                                .getEcotype())));

            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            fields.add(new DefaultElement(HARVEST_DATE, iceNamespace).addText(simpleDateFormat
                                                                                      .format(seed.getHarvestDate())));

            fields.add(new DefaultElement(PARENTS, iceNamespace).addText(emptyStringify(seed
                                                                                                .getParents())));
            fields.add(new DefaultElement(GENERATION, iceNamespace).addText(seed.getGeneration()
                                                                                .toString()));
            fields.add(new DefaultElement(PLANT_TYPE, iceNamespace).addText(seed.getPlantType()
                                                                                .toString()));
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
            selectionMarkers.add(new DefaultElement(SELECTION_MARKER, iceNamespace).addText(marker
                                                                                                    .getName()));
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
        Element result = null;
        DefaultElement expElement = new DefaultElement(EXP, expNamespace);
        DefaultElement tracesElement = new DefaultElement(SEQUENCE_TRACES, expNamespace);
        List<TraceSequence> traces = null;
        try {
            traces = TraceSequenceDAO.getByEntry(entry);
        } catch (ManagerException e) {
            throw new UtilityException(e);
        }


        if (traces != null) {
            int counter = 0;
            for (TraceSequence trace : traces) {

                File traceFile;
                String traceString;
                try {
                    traceFile = TraceSequenceDAO.getFile(trace);
                    traceString = SerializationUtils
                            .serializeBytesToBase64String(org.apache.commons.io.FileUtils
                                                                               .readFileToByteArray(traceFile));
                } catch (DAOException e) {
                    // skip this one
                    Logger.error("Could not read trace file " + trace.getFileId());
                    continue;
                } catch (IOException e) {
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
     * Deserialize given xml to list of {@link CompleteEntry}s.
     *
     * @param xml xml to parse.
     * @return List of CompleteEntries.
     * @throws UtilityException
     */
    public List<CompleteEntry> deserializeJbeiXml(String xml) throws UtilityException {
        completeEntries.clear();
        SAXReader reader = new SAXReader();
        reader.addHandler("/ice/entry", new ElementHandler() {
            @Override
            public void onStart(ElementPath path) {

            }

            @Override
            public void onEnd(ElementPath path) {
                // process one entry
                Element entry = path.getCurrent();
                try {
                    completeEntries.add(parseEntry(entry));
                } catch (UtilityException e) {
                    throw new RuntimeException(e);
                }

                // prune the tree
                entry.detach();
            }
        });
        try {
            reader.read(new ByteArrayInputStream(xml.getBytes("utf8")));
        } catch (DocumentException e1) {
            throw new UtilityException(e1);
        } catch (UnsupportedEncodingException e) {
            throw new UtilityException(e);
        }

        return completeEntries;
    }

    /**
     * Deserialize given trace element xml Element into List of {@link SequenceTraceFile}s.
     *
     * @param tracesElement xml element.
     * @return List of SequenceTracefiles.
     */
    private static List<SequenceTraceFile> parseSequenceTraces(Element tracesElement) {
        ArrayList<SequenceTraceFile> traceFileList = new ArrayList<SequenceTraceFile>();

        for (Object element : tracesElement.elements(SEQUENCE_TRACE_FILE)) {
            SequenceTraceFile traceFile = new SequenceTraceFile();
            traceFile.setBase64Data(((Element) element).getText());
            traceFile.setDepositorEmail(((Element) element).attributeValue(DEPOSITOR_EMAIL));
            traceFile.setFileName(((Element) element).attributeValue(FILE_NAME));
            traceFile.setFileId(((Element) element).attributeValue(FILE_ID));
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                traceFile.setTimeStamp(simpleDateFormat.parse(((Element) element)
                                                                      .attributeValue(TIME_STAMP)));
            } catch (ParseException e) {
                traceFile.setTimeStamp(Calendar.getInstance().getTime());
            }

            traceFileList.add(traceFile);
        }
        if (traceFileList.size() == 0) {
            traceFileList = null;
        }

        return traceFileList;
    }

    /**
     * Deserialize given entry xml element into {@link CompleteEntry}.
     *
     * @param entryDocument xml element to parse.
     * @return CompleteEntry.
     * @throws UtilityException
     */
    private static CompleteEntry parseEntry(Element entryDocument) throws UtilityException {
        CompleteEntry completeEntry = new CompleteEntry();

        // do record type specific fields first
        Logger.debug("Parsing xml " + entryDocument.elementText(RECORD_ID));
        String recordType = entryDocument.element(RECORD_TYPE).getText();
        if (PLASMID.equals(recordType)) {
            Plasmid plasmid = new Plasmid();
            completeEntry.setEntry(plasmid);
            Element plasmidFields = entryDocument.element(PLASMID_FIELDS);
            if (plasmidFields.element(SELECTION_MARKERS) != null) {
                Set<SelectionMarker> selectionMarkers = parseSelectionMarkers(plasmidFields
                                                                                      .element(SELECTION_MARKERS));
                for (SelectionMarker selectionMarker : selectionMarkers) {
                    selectionMarker.setEntry(plasmid);
                }
                plasmid.getSelectionMarkers().addAll(selectionMarkers);
            }
            plasmid.setBackbone(plasmidFields.elementText(BACKBONE));
            plasmid.setOriginOfReplication(plasmidFields.elementText(ORIGIN_OF_REPLICATION));
            plasmid.setPromoters(plasmidFields.elementText(PROMOTERS));
            String circular = plasmidFields.elementText(IS_CIRCULAR);
            plasmid.setCircular("true".equals(circular) ? true : false);
        } else if (STRAIN.equals(recordType)) {
            Strain strain = new Strain();
            completeEntry.setEntry(strain);
            Element strainFields = entryDocument.element(STRAIN_FIELDS);
            if (strainFields.element(SELECTION_MARKERS) != null) {
                Set<SelectionMarker> selectionMarkers = parseSelectionMarkers(strainFields
                                                                                      .element(SELECTION_MARKERS));
                for (SelectionMarker selectionMarker : selectionMarkers) {
                    selectionMarker.setEntry(strain);
                }
                strain.getSelectionMarkers().addAll(selectionMarkers);
            }
            strain.setHost(strainFields.elementText(HOST));
            strain.setGenotypePhenotype(strainFields.elementText(GENOTYPE_PHENOTYPE));
            strain.setPlasmids(strainFields.elementText(PLASMIDS));
        } else if (PART.equals(recordType)) {
            Part part = new Part();
            completeEntry.setEntry(part);
            Element partFields = entryDocument.element(PART_FIELDS);
            part.setPackageFormat(AssemblyStandard.valueOf(partFields.elementText(PACKAGE_FORMAT)));

        } else if (ARABIDOPSIS.equals(recordType)) {
            ArabidopsisSeed arabidopsis = new ArabidopsisSeed();
            completeEntry.setEntry(arabidopsis);
            Element arabidopsisFields = entryDocument.element(ARABIDOPSIS_SEED_FIELDS);
            arabidopsis.setHomozygosity(arabidopsisFields.elementText(HOMOZYGOSITY));
            arabidopsis.setEcotype(arabidopsisFields.elementText(ECOTYPE));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy-MM-ddTHH:mm:ssZ");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = null;
            try {
                date = simpleDateFormat.parse(arabidopsisFields.elementText(HARVEST_DATE));
            } catch (ParseException e) {
                throw new UtilityException(e);
            }
            arabidopsis.setHarvestDate(date);
            arabidopsis.setParents(arabidopsisFields.elementText(PARENTS));
            Generation generation = Generation.valueOf(arabidopsisFields.elementText(GENERATION));
            arabidopsis.setGeneration(generation);
        } else {
            throw new UtilityException("Unrecognized recordType " + recordType);
        }

        // Now general entry fields
        Entry entry = completeEntry.getEntry();

        entry.setRecordId(entryDocument.elementText(RECORD_ID));
        entry.setRecordType(entryDocument.elementText(RECORD_TYPE));
        Date creationTime = Calendar.getInstance().getTime();
        Date modificationTime = Calendar.getInstance().getTime();
        try {
            creationTime = simpleDateFormat.parse(entryDocument.elementText(CREATION_TIME_STAMP));
            modificationTime = simpleDateFormat.parse(entryDocument
                                                              .elementText(MODIFICATION_TIME_STAMP));
        } catch (ParseException e) {
            // could not parse time stamps. Continue
        }
        entry.setCreationTime(creationTime);
        entry.setModificationTime(modificationTime);

        HashSet<PartNumber> partNumbers = new HashSet<PartNumber>();
        if (entryDocument.element(PART_NUMBERS) != null) {
            for (Object element : entryDocument.element(PART_NUMBERS).elements(PART_NUMBER)) {
                partNumbers.add(new PartNumber(((Element) element).getText(), entry));
            }
            entry.getPartNumbers().addAll(partNumbers);
        }
        if (entryDocument.element(PART_NAMES) != null) {
            HashSet<Name> partNames = new HashSet<Name>();
            for (Object element : entryDocument.element(PART_NAMES).elements(PART_NAME)) {
                partNames.add(new Name(((Element) element).getText(), entry));
            }
            entry.getNames().addAll(partNames);
        }

        if (entryDocument.element(OWNER) != null) {
            entry.setOwner(entryDocument.element(OWNER).elementText(PERSON_NAME));
            entry.setOwnerEmail(entryDocument.element(OWNER).elementText(EMAIL));
        }
        if (entryDocument.element(CREATOR) != null) {
            entry.setCreator(entryDocument.element(CREATOR).elementText(PERSON_NAME));
            entry.setCreatorEmail(entryDocument.element(CREATOR).elementText(EMAIL));
        }

        HashSet<Link> links = new HashSet<Link>();
        if (entryDocument.element(LINKS) != null) {
            for (Object element : entryDocument.element(LINKS).elements(LINK)) {
                Link link = new Link();
                link.setEntry(entry);
                link.setLink(((Element) element).getText());
                link.setUrl(((Element) element).attributeValue(URL));
                links.add(link);
            }
            entry.getLinks().addAll(links);
        }

        entry.setStatus(entryDocument.elementText(STATUS));
        entry.setShortDescription(entryDocument.elementText(SHORT_DESCRIPTION));
        entry.setLongDescription(entryDocument.elementText(LONG_DESCRIPTION));
        entry.setLongDescriptionType(entryDocument.elementText(LONG_DESCRIPTION_MARKUP_TYPE));
        entry.setReferences(entryDocument.elementText(REFERENCES));

        entry.setBioSafetyLevel(Integer.parseInt(entryDocument.elementText(BIO_SAFETY_LEVEL)));
        entry.setIntellectualProperty(entryDocument.elementText(INTELLECTUAL_PROPERTY));

        if (entryDocument.element(FUNDING_SOURCES) != null) {
            HashSet<EntryFundingSource> entryFundingSources = new HashSet<EntryFundingSource>();
            for (Object element : entryDocument.element(FUNDING_SOURCES).elements(FUNDING_SOURCE)) {
                FundingSource fundingSource = new FundingSource();
                fundingSource.setFundingSource(((Element) element).getText());
                fundingSource.setPrincipalInvestigator(((Element) element)
                                                               .attributeValue(PRINCIPAL_INVESTIGATOR));
                EntryFundingSource entryFundingSource = new EntryFundingSource();
                entryFundingSource.setEntry(entry);
                entryFundingSource.setFundingSource(fundingSource);
                entryFundingSources.add(entryFundingSource);
            }
            entry.getEntryFundingSources().addAll(entryFundingSources);
        }

        if (entryDocument.element(ATTACHMENTS) != null) {
            completeEntry.getAttachments().addAll(
                    parseAttachments(entryDocument.element(ATTACHMENTS)));
        }

        if (entryDocument.element(EXP) != null) {
            Element tracesElement = entryDocument.element(EXP).element(SEQUENCE_TRACES);
            if (tracesElement != null) {
                completeEntry.getTraceFiles().addAll(parseSequenceTraces(tracesElement));
            }
        }

        if (entryDocument.element(SEQ) != null) {
            completeEntry.setSequence(SeqXmlSerializer.parseSeqXml(entryDocument.element(SEQ)));
        }

        return completeEntry;
    }

    /**
     * Deserialize selection marker xml Element into a set of {@link SelectionMarker}s.
     *
     * @param selectionMarkers xml element.
     * @return Set of SelectionMarkers
     */
    private static Set<SelectionMarker> parseSelectionMarkers(Element selectionMarkers) {
        HashSet<SelectionMarker> result = new HashSet<SelectionMarker>();
        for (Object marker : selectionMarkers.elements(SELECTION_MARKER)) {
            SelectionMarker newMarker = new SelectionMarker();
            newMarker.setName(((Element) marker).getText());
            result.add(newMarker);
        }

        return result;
    }

    /**
     * Deserialize attachment xml Element into a List of {@link AttachmentData}.
     *
     * @param attachments xml element.
     * @return List of AttachmentData.
     */
    private static List<AttachmentData> parseAttachments(Element attachments) {
        ArrayList<AttachmentData> attachmentsList = new ArrayList<AttachmentData>();

        for (Object element : attachments.elements(ATTACHMENT)) {
            AttachmentData attachmentData = new AttachmentData();
            attachmentData.setBase64Data(((Element) element).getText());
            attachmentData.setFileName(((Element) element).attributeValue(FILE_NAME));
            attachmentData.setFileId(((Element) element).attributeValue(FILE_ID));
            attachmentData.setDescription(((Element) element).attributeValue(DESCRIPTION));
            attachmentsList.add(attachmentData);
        }

        if (attachmentsList.size() == 0) {
            attachmentsList = null;
        }
        return attachmentsList;
    }

    /**
     * Replace null value of a string object into an empty string. Non-null value is returned
     * unaltered.
     *
     * @param string
     * @return
     */
    private static String emptyStringify(String string) {
        if (string == null) {
            return "";
        } else {
            return string;
        }
    }
}
