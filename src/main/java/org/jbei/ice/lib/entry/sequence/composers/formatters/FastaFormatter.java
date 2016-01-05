package org.jbei.ice.lib.entry.sequence.composers.formatters;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.SimpleRichSequence;
import org.jbei.ice.storage.model.Sequence;

import java.io.IOException;
import java.io.OutputStream;

/*
 * >gi|<identifier>|<namespace>|<accession>.<version>|<name> <description>
 * ><namespace>|<accession>.<version>|<name> <description> 
 * */

/**
 * Formatter for creating a FASTA formatted output.
 * <p>
 *
 * @author Zinovii Dmytriv
 */
public class FastaFormatter extends AbstractFormatter {
    private final String name;
    private final String accessionNumber;
    private int version = 1;
    private double seqVersion = 1.0;

    /**
     * Constructor using only the name. Uses the name as the accession number.
     *
     * @param name
     */
    public FastaFormatter(String name) {
        this(name, name, 1, 1.0);
    }

    /**
     * Constructor using all required fields for FASTA format.
     *
     * @param name
     * @param accessionNumber
     * @param version
     * @param seqVersion
     */
    public FastaFormatter(String name, String accessionNumber, int version, double seqVersion) {
        super();

        this.name = name;
        this.accessionNumber = accessionNumber;
        this.version = version;
        this.seqVersion = seqVersion;
    }

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException,
            IOException {
        SimpleRichSequence simpleRichSequence = null;
        try {
            simpleRichSequence = new SimpleRichSequence(getNamespace(), name, accessionNumber,
                    version, DNATools.createDNA(sequence.getSequence()),
                    seqVersion);
        } catch (IllegalSymbolException e) {
            throw new FormatterException("Failed to create generate fasta file", e);
        } catch (ChangeVetoException e) {
            throw new FormatterException("Failed to create generate fasta file", e);
        }

        RichSequence.IOTools.writeFasta(outputStream, simpleRichSequence, getNamespace());
    }
}
