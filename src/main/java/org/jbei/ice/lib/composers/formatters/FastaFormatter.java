package org.jbei.ice.lib.composers.formatters;

import java.io.IOException;
import java.io.OutputStream;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.SimpleRichSequence;
import org.jbei.ice.lib.models.Sequence;

/*
 * >gi|<identifier>|<namespace>|<accession>.<version>|<name> <description>
 * ><namespace>|<accession>.<version>|<name> <description> 
 * */

public class FastaFormatter extends AbstractFormatter {
    private String name;
    private String accessionNumber;
    private int version = 1;
    private double seqVersion = 1.0;

    public FastaFormatter(String name) {
        this(name, name, 1, 1.0);
    }

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
                    version, DNATools.createDNA(sequence.getSequence()), seqVersion);
        } catch (IllegalSymbolException e) {
            throw new FormatterException("Failed to create generate fasta file", e);
        } catch (ChangeVetoException e) {
            throw new FormatterException("Failed to create generate fasta file", e);
        }

        RichSequence.IOTools.writeFasta(outputStream, simpleRichSequence, getNamespace());
    }
}
