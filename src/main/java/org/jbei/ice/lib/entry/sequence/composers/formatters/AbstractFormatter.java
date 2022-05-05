package org.jbei.ice.lib.entry.sequence.composers.formatters;

import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.jbei.ice.storage.model.Sequence;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract formatter implementing {@link IFormatter} interface.
 *
 * @author Zinovii Dmytriv
 */
public class AbstractFormatter implements IFormatter {
    public static final String DEFAULT_NAMESPACE = "org.jbei";

    @Override
    /**
     * Format the {@link Sequence} and output to the {@link OutputStream}.
     */
    public void format(Sequence sequence, OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException("Unsupported");
    }

    public Namespace getNamespace() {
        return new SimpleNamespace(DEFAULT_NAMESPACE);
    }
}
