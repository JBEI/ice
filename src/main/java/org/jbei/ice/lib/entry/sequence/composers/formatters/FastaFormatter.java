package org.jbei.ice.lib.entry.sequence.composers.formatters;

import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Formatter for creating a FASTA formatted output.
 * <p/>
 *
 * @author Hector Plahar
 */
public class FastaFormatter extends AbstractFormatter {

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException,
            IOException {
        if (sequence == null)
            throw new IllegalArgumentException("Cannot write null sequence");

        StringBuilder builder = new StringBuilder();
        Entry entry = sequence.getEntry();
        if (entry == null)
            throw new IOException("Cannot retrieve entry for sequence");

        builder.append(">")
                .append(entry.getPartNumber())
                .append(System.lineSeparator());
        for (int i = 1; i <= sequence.getSequence().length(); i += 1) {
            builder.append(sequence.getSequence().charAt(i - 1));
            if (i % 80 == 0)
                builder.append(System.lineSeparator());
        }

        outputStream.write(builder.toString().getBytes());
    }
}
