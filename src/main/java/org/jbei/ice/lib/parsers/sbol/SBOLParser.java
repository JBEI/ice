package org.jbei.ice.lib.parsers.sbol;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.entry.Entries;
import org.jbei.ice.lib.entry.EntryLinks;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.lib.entry.LinkType;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
import org.jbei.ice.lib.entry.sequence.SequenceUtil;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.parsers.genbank.GenBankParser;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Parse SBOL files that are imported by the user.
 * As an initial support for version 2, it converts the file to genBank and then the ICE genBank parser
 * is used to convert to the data model
 *
 * @author Hector Plahar
 */
public class SBOLParser extends AbstractParser {

    protected final Entry entry;
    protected final PartData partData;
    protected final String userId;
    private boolean extractHierarchy;
    // map of component identity to entry id
    private Map<String, Long> identityEntryMap = new HashMap<>();

    public SBOLParser(String userId, String entryId, boolean extractHierarchy) {
        super();

        this.extractHierarchy = extractHierarchy;
        this.entry = new HasEntry().getEntry(entryId);
        if (this.entry == null)
            throw new IllegalArgumentException("Could not retrieve entry with id " + entryId);
        this.partData = ModelToInfoFactory.getInfo(entry);
        this.userId = userId;
    }

    public SequenceInfo parseToEntry(InputStream stream, String fileName) throws InvalidFormatParserException {
        SBOLDocument document;
        try {
            document = SBOLReader.read(stream);
        } catch (SBOLValidationException e) {
            Logger.error(e);
            throw new InvalidFormatParserException("Invalid SBOL file: " + e.getMessage());
        } catch (IOException e) {
            Logger.error(e);
            throw new InvalidFormatParserException("Server error parsing file");
        } catch (SBOLConversionException e) {
            Logger.error(e);
            throw new InvalidFormatParserException("Error converting file to SBOL 2.0");
        }

        // parse raw document and return
        SequenceInfo sequenceInfo = parseToGenBank(document, fileName, entry, null);
        if (!this.extractHierarchy)
            return sequenceInfo;

        // document parsed successfully, go through module definitions
        for (ModuleDefinition moduleDefinition : document.getModuleDefinitions()) {
            try {
                createICEModuleDefinitionRecord(document, moduleDefinition);
            } catch (SBOLValidationException e) {
                Logger.error("Could not import module definition", e);
            }
        }

        // go through component definitions
        for (ComponentDefinition componentDefinition : document.getComponentDefinitions()) {
            try {
                createICEComponentDefinitionRecord(document, componentDefinition);
            } catch (SBOLValidationException e) {
                Logger.error("Could not import component definition", e);
            }
        }

        return sequenceInfo;
    }

    /**
     * Parse the SBOL document to genbank format, save and associate with entry
     *
     * @param sbolDocument SBOL document to parse
     * @param fileName     name of file that was parsed to extract the SBOL information
     * @param entry        ICE entry to associate sequence document with
     * @param uri          optional uri to associate with sequence
     * @return Sequence info data transfer object for saved sequence
     */
    private SequenceInfo parseToGenBank(SBOLDocument sbolDocument, String fileName, Entry entry, String uri) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String rdf;

        // convert to rdf string to save raw document
        try {
            sbolDocument.write(out);
            rdf = out.toString();
        } catch (SBOLConversionException e) {
            rdf = null;
        }

        // convert to genbank
        Sequence sequence = null;
        FeaturedDNASequence dnaSequence = null;

        try {
            out.reset();
            SBOLWriter.write(sbolDocument, out, "GENBANK");
            if (out.size() > 0) {
                GenBankParser parser = new GenBankParser();
                dnaSequence = parser.parse(IOUtils.lineIterator(new ByteArrayInputStream(out.toByteArray()), Charset.defaultCharset()));
                sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
            }
        } catch (SBOLConversionException | IOException e) {
            Logger.error("Error converting SBOL to genBank: " + e.getMessage());
        }

        // convert to ice data model (sequence)
        if (sequence == null) {
            sequence = new Sequence();
        }

        if (!StringUtils.isEmpty(rdf))
            sequence.setSequenceUser(rdf);
        sequence.setFormat(SequenceFormat.SBOL2);
        sequence.setEntry(entry);
        if (fileName != null)
            sequence.setFileName(fileName);
        if (!StringUtils.isEmpty(uri))
            sequence.setUri(uri);

        sequence = DAOFactory.getSequenceDAO().create(sequence);
        SequenceInfo sequenceInfo = new SequenceInfo();
        sequenceInfo.setEntryId(entry.getId());
        sequenceInfo.setSequence(dnaSequence);
        sequenceInfo.setFormat(sequence.getFormat());
        if (fileName != null)
            sequenceInfo.setFilename(fileName);
        return sequenceInfo;
    }

    private long createNewEntry(TopLevel moduleDefinition, SBOLDocument document) {
        String identity = moduleDefinition.getIdentity().toString();
        String description = moduleDefinition.getDescription();
        String name = moduleDefinition.getName();

        PartData partData = new PartData(EntryType.PART);
        partData.setOwner(partData.getOwner());
        partData.setOwnerEmail(partData.getOwnerEmail());
        partData.setCreator(partData.getCreator());
        partData.setCreatorEmail(partData.getCreatorEmail());
        partData.setPrincipalInvestigator(partData.getPrincipalInvestigator());
        partData.setPrincipalInvestigatorEmail(partData.getPrincipalInvestigatorEmail());
        partData.setBioSafetyLevel(partData.getBioSafetyLevel());
        partData.setStatus(partData.getStatus());
        description = StringUtils.isEmpty(description) ? partData.getShortDescription() : description;
        name = StringUtils.isEmpty(name) ? moduleDefinition.getDisplayId() : name;
        partData.setShortDescription(description);
        partData.setName(name);

        Entries entryCreator = new Entries(partData.getCreatorEmail());
        partData = entryCreator.create(partData);
        Entry entry = DAOFactory.getEntryDAO().get(partData.getId());
        parseToGenBank(document, entry.getName(), entry, moduleDefinition.getIdentity().toString());

        identityEntryMap.put(identity, entry.getId());
        return entry.getId();
    }

    protected void createICEModuleDefinitionRecord(SBOLDocument document, ModuleDefinition moduleDefinition) throws SBOLValidationException {
        SBOLDocument rootedDocument = document.createRecursiveCopy(moduleDefinition);
        Logger.debug("Creating ICE record for ModuleDefinition: " + moduleDefinition.getIdentity());
        String identity = moduleDefinition.getIdentity().toString();

        Long partId = identityEntryMap.get(identity);
        if (partId == null) {
            Logger.debug("Creating " + moduleDefinition.getDisplayId());
            createNewEntry(moduleDefinition, rootedDocument);
        }

        for (FunctionalComponent functionalComponent : moduleDefinition.getFunctionalComponents()) {
            ComponentDefinition componentDefinition = functionalComponent.getDefinition();

            // add link to ice record for referenced component definition
            // Note: record may not exist yet, so you might need to create it now too
            if (componentDefinition == null)
                continue;

            Long componentId = identityEntryMap.get(componentDefinition.getIdentity().toString());
            if (componentId == null) {
                SBOLDocument sbolDocument = document.createRecursiveCopy(componentDefinition);
                componentId = createNewEntry(componentDefinition, sbolDocument);
            }

            EntryLinks links = new EntryLinks(userId, componentId.toString());
            links.addLink(this.partData, LinkType.PARENT);
        }

        // add a link to the ICE record for the referenced ModuleDefinition
        // Note: record may not exist yet, so you might need to create it now too
        for (Module module : moduleDefinition.getModules()) {
            if (module.getDefinition() == null)
                continue;

            Long moduleId = identityEntryMap.get(module.getDefinition().getIdentity().toString());
            if (moduleId == null) {
                SBOLDocument moduleDocument = document.createRecursiveCopy(module.getDefinition());
                moduleId = createNewEntry(module.getDefinition(), moduleDocument);
            }

            EntryLinks links = new EntryLinks(userId, moduleId.toString());
            links.addLink(this.partData, LinkType.PARENT);
            Logger.debug("    Link to ModuleDefinition: " + module.getDefinition().getIdentity());
        }
    }

    protected void createICEComponentDefinitionRecord(SBOLDocument document, ComponentDefinition componentDefinition)
            throws SBOLValidationException {
        SBOLDocument rootedDocument = document.createRecursiveCopy(componentDefinition);
        String identity = componentDefinition.getIdentity().toString();

        Long partId = identityEntryMap.get(identity);
        if (partId == null) {
            createNewEntry(componentDefinition, rootedDocument);
        }

        // Add rootedDocument as the SBOL associated with this record
        for (Component component : componentDefinition.getComponents()) {
            if (component.getDefinition() == null)
                continue;

            Long componentId = identityEntryMap.get(component.getDefinition().getIdentity().toString());
            if (componentId == null) {
                SBOLDocument sbolDocument = document.createRecursiveCopy(component.getDefinition());
                componentId = createNewEntry(component.getDefinition(), sbolDocument);
            }

            EntryLinks links = new EntryLinks(userId, componentId.toString());
            links.addLink(this.partData, LinkType.PARENT);
            Logger.debug("    Link to ComponentDefinition: " + component.getDefinition().getIdentity());
        }
    }
}
