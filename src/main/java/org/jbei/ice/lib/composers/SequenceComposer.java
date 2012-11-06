package org.jbei.ice.lib.composers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jbei.ice.lib.composers.formatters.FormatterException;
import org.jbei.ice.lib.composers.formatters.IFormatter;
import org.jbei.ice.lib.models.Sequence;

/**
 * Wrapper for formatters. To be used to generate sequence files of various formats, as specified by
 * the {@link IFormatter} formatter.
 *
 * @author Zinovii Dmytriv
 */
public class SequenceComposer {
    /**
     * Create a string representation of {@link Sequence} using the {@link IFormatter} formatter.
     *
     * @param sequence
     * @param formatter
     * @return Text of formatted output.
     * @throws SequenceComposerException
     */
    public static String compose(Sequence sequence, IFormatter formatter)
            throws SequenceComposerException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        try {
            compose(sequence, formatter, byteStream);
        } catch (IOException e) {
            throw new SequenceComposerException("Failed to write into Byte Stream sequence", e);
        }

        return byteStream.toString();
    }

    /**
     * Output a representation of {@link Sequence} using the {@link IFormatter} formatter, to the
     * {@link OutputStream} stream.
     *
     * @param sequence
     * @param formatter
     * @param stream
     * @throws SequenceComposerException
     * @throws IOException
     */
    public static void compose(Sequence sequence, IFormatter formatter, OutputStream stream)
            throws SequenceComposerException, IOException {

        try {
            formatter.format(sequence, stream);
        } catch (FormatterException e) {
            throw new SequenceComposerException("Failed to format sequence", e);
        }
    }
}
