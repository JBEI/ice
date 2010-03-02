package org.jbei.ice.lib.composers.formatters;

import java.io.IOException;
import java.io.OutputStream;

import org.jbei.ice.lib.models.Sequence;

public interface IFormatter {
    void format(Sequence sequence, OutputStream outputStream) throws FormatterException,
            IOException;
}
