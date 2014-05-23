package org.jbei.ice.lib.entry.sequence.composers.formatters;

import java.io.IOException;
import java.io.OutputStream;

import org.jbei.ice.lib.models.Sequence;

/**
 * Interface for Formatters.
 *
 * @author Zinovii Dmytriv
 */
public interface IFormatter {
    /**
     * Interface method to take a {@link Sequence} object and output the formatted file to the
     * {@link OutputStream}.
     *
     * @param sequence
     * @param outputStream
     * @throws FormatterException
     * @throws IOException
     */
    void format(Sequence sequence, OutputStream outputStream) throws FormatterException,
            IOException;
}
