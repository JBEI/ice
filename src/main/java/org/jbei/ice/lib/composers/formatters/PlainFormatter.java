package org.jbei.ice.lib.composers.formatters;

import java.io.IOException;
import java.io.OutputStream;

import org.jbei.ice.lib.models.Sequence;

public class PlainFormatter extends AbstractFormatter {
    public PlainFormatter() {
    }

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException,
            IOException {

        String dnaSequence = sequence.getSequence();
        if (dnaSequence == null || dnaSequence.isEmpty()) {
            return;
        }

        outputStream.write(dnaSequence.getBytes());
    }
}
