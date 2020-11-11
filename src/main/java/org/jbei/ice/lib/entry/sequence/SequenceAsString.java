package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.entry.sequence.composers.formatters.*;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Plasmid;
import org.jbei.ice.storage.model.Sequence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Part Sequence retrieved and represented as a string (in bytes)
 *
 * @author Hector Plahar
 */
public class SequenceAsString {

    private String sequenceString;
    private String name;
    private InputStreamWrapper wrapper;

    public SequenceAsString(SequenceFormat format, long partId, boolean useFileName) {
        Entry entry = DAOFactory.getEntryDAO().get(partId);

        Sequence sequence = DAOFactory.getSequenceDAO().getByEntry(entry);
        if (sequence == null) {
            return;
        }

        switch (format) {
            case ORIGINAL:
                sequenceString = sequence.getSequenceUser();
                if (!useFileName && !StringUtils.isEmpty(sequence.getFileName())) {
                    name = entry.getPartNumber() + ".gb";
                } else {
                    name = sequence.getFileName();
                }

                try {
                    SequenceFile sequenceFile = new SequenceFile(sequenceString);
                    wrapper = new InputStreamWrapper(sequenceFile.getStream(), name);
                } catch (Exception e) {
                    Logger.error(e.getMessage());
                }
                break;

            case GENBANK:
            default:
                GenbankFormatter genbankFormatter = new GenbankFormatter(entry.getName());
                genbankFormatter.setCircular((entry instanceof Plasmid) ? ((Plasmid) entry).getCircular() : false);
                sequenceString = compose(sequence, genbankFormatter);
                name = entry.getPartNumber() + ".gb";
                break;

            case FASTA:
                FastaFormatter formatter = new FastaFormatter();
                sequenceString = compose(sequence, formatter);
                name = entry.getPartNumber() + ".fa";
                break;

            case SBOL1:
                sequenceString = compose(sequence, new SBOLFormatter());
                name = entry.getPartNumber() + ".xml";
                break;

            case SBOL2:
                sequenceString = compose(sequence, new SBOL2Formatter());
                name = entry.getPartNumber() + ".xml";
                break;

            case GFF3:
                sequenceString = compose(sequence, new GFF3Formatter());
                name = entry.getPartNumber() + ".gff3";
                break;
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(sequenceString.getBytes());
        wrapper = new InputStreamWrapper(stream, name);
    }

    /**
     * Generate a formatted text of a given {@link IFormatter} from the given {@link Sequence}.
     *
     * @param sequence  sequence
     * @param formatter formatter
     * @return Text of a formatted sequence.
     */
    protected String compose(Sequence sequence, IFormatter formatter) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            formatter.format(sequence, byteStream);
        } catch (IOException e) {
            Logger.error(e);
        }
        return byteStream.toString();
    }

    public InputStreamWrapper get() {
        return wrapper;
    }
}
