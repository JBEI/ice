package org.jbei.ice.lib.parsers;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;

public abstract class AbstractParser extends HasEntry {

    protected final Entry entry;
    protected final String userId;
    protected final PartData partData;
    protected String fileName;
    private final SequenceDAO sequenceDAO;

    public AbstractParser(String userId, String entryId) {
        this.entry = getEntry(entryId);
        if (this.entry == null)
            throw new IllegalArgumentException("Could not retrieve entry with id " + entryId);
        this.partData = ModelToInfoFactory.getInfo(entry);
        this.userId = userId;
        this.sequenceDAO = DAOFactory.getSequenceDAO();
    }

    public AbstractParser() {
        this.entry = null;
        this.userId = null;
        this.partData = null;
        sequenceDAO = DAOFactory.getSequenceDAO();
    }

    public DNASequence parse(String textSequence) throws InvalidFormatParserException {
        throw new UnsupportedOperationException("Not implemented for this parser");
    }

    /**
     * Replace different line termination characters with the newline character (\n).
     *
     * @param sequence Text to clean.
     * @return String with only newline character (\n).
     */
    protected String cleanSequence(String sequence) {
        sequence = sequence.trim();
        sequence = sequence.replace("\n\n", "\n"); // *nix
        sequence = sequence.replace("\n\r\n\r", "\n"); // win
        sequence = sequence.replace("\r\r", "\n"); // mac
        sequence = sequence.replace("\n\r", "\n"); // *win
        return sequence;
    }

    protected SequenceInfo save(DNASequence dnaSequence, String sequenceString) {
        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setSequenceUser(sequenceString);
        sequence.setEntry(entry);
        if (!StringUtils.isBlank(fileName))
            sequence.setFileName(fileName);

        Sequence result = sequenceDAO.saveSequence(sequence);
        BlastPlus.scheduleBlastIndexRebuildTask(true);
        SequenceInfo info = result.toDataTransferObject();
        info.setSequence(dnaSequence);
        return info;
    }
}
