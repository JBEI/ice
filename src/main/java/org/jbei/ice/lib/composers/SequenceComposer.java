package org.jbei.ice.lib.composers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jbei.ice.lib.composers.formatters.FormatterException;
import org.jbei.ice.lib.composers.formatters.IFormatter;
import org.jbei.ice.lib.models.Sequence;

public class SequenceComposer {
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

    public static void compose(Sequence sequence, IFormatter formatter, OutputStream stream)
            throws SequenceComposerException, IOException {

        try {
            formatter.format(sequence, stream);
        } catch (FormatterException e) {
            throw new SequenceComposerException("Failed to format sequence", e);
        }
    }
}
