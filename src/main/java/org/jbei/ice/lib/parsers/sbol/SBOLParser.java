package org.jbei.ice.lib.parsers.sbol;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.entry.EntryLinks;
import org.jbei.ice.lib.entry.LinkType;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.parsers.genbank.GenBankParser;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Part;
import org.jbei.ice.storage.model.Sequence;
import org.sbolstandard.core2.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Parse SBOL files that are imported by the user.
 * As an initial support for version 2, it converts the file to genBank and then the ICE genBank parser
 * is used to convert to the data model
 *
 * @author Hector Plahar
 */
public class SBOLParser {

    private final PartData partData;
    private final String userId;

    public SBOLParser(PartData partData) {
        this.partData = partData;
        this.userId = this.partData.getOwnerEmail();
    }

    // map of component identity to entry id
    private Map<String, Long> identityEntryMap = new HashMap<>();

    public SequenceInfo parse(InputStream inputStream, String fileName) throws InvalidFormatParserException {
        SBOLDocument document;
        try {
            document = SBOLReader.read(inputStream);
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
        Entry entry = DAOFactory.getEntryDAO().get(partData.getId());
        SequenceInfo sequenceInfo = parseToGenBank(document, fileName, entry, null);

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
    protected SequenceInfo parseToGenBank(SBOLDocument sbolDocument, String fileName, Entry entry, String uri) {
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
        DNASequence dnaSequence = null;

        try {
            out.reset();
            SBOLWriter.write(sbolDocument, out, "GENBANK");
            if (out.size() > 0) {
                GenBankParser parser = new GenBankParser();

                String genBankString = out.toString();
                dnaSequence = parser.parse(genBankString);
                sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
            }
        } catch (InvalidFormatParserException e) {
            Logger.error("Error parsing generated genBank: " + e.getMessage());
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
        sequence.setFileName(fileName);
        if (!StringUtils.isEmpty(uri))
            sequence.setUri(uri);

        sequence = DAOFactory.getSequenceDAO().saveSequence(sequence);
        SequenceInfo sequenceInfo = new SequenceInfo();
        sequenceInfo.setEntryId(entry.getId());
        sequenceInfo.setSequence(dnaSequence);
        sequenceInfo.setFormat(sequence.getFormat());
        sequenceInfo.setFilename(fileName);
        return sequenceInfo;
    }

    protected long createNewEntry(TopLevel moduleDefinition, SBOLDocument document) {
        String identity = moduleDefinition.getIdentity().toString();
        String description = moduleDefinition.getDescription();
        String name = moduleDefinition.getName();

        Part part = new Part();
        part.setOwner(partData.getOwner());
        part.setOwnerEmail(partData.getOwnerEmail());
        part.setCreator(partData.getCreator());
        part.setCreatorEmail(partData.getCreatorEmail());
        part.setPrincipalInvestigator(partData.getPrincipalInvestigator());
        part.setPrincipalInvestigatorEmail(partData.getPrincipalInvestigatorEmail());
        part.setBioSafetyLevel(partData.getBioSafetyLevel());
        part.setStatus(partData.getStatus());
        description = StringUtils.isEmpty(description) ? partData.getShortDescription() : description;
        name = StringUtils.isEmpty(name) ? moduleDefinition.getDisplayId() : name;
        part.setShortDescription(description);
        part.setName(name);

        EntryCreator entryCreator = new EntryCreator();
        Account account = DAOFactory.getAccountDAO().getByEmail(part.getCreatorEmail());
        Entry entry = entryCreator.createEntry(account, part, null);
        parseToGenBank(document, entry.getName(), entry, moduleDefinition.getIdentity().toString());

        identityEntryMap.put(identity, entry.getId());
        return entry.getId();
    }

    public void createICEModuleDefinitionRecord(SBOLDocument document, ModuleDefinition moduleDefinition) throws SBOLValidationException {
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

            EntryLinks links = new EntryLinks(userId, componentId);
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

            EntryLinks links = new EntryLinks(userId, moduleId);
            links.addLink(this.partData, LinkType.PARENT);
            Logger.debug("    Link to ModuleDefinition: " + module.getDefinition().getIdentity());
        }
    }

    public void createICEComponentDefinitionRecord(SBOLDocument document, ComponentDefinition componentDefinition)
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

            EntryLinks links = new EntryLinks(userId, componentId);
            links.addLink(this.partData, LinkType.PARENT);
            Logger.debug("    Link to ComponentDefinition: " + component.getDefinition().getIdentity());
        }
    }
}
