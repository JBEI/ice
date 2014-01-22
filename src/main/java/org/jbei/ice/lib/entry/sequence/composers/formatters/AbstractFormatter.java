package org.jbei.ice.lib.entry.sequence.composers.formatters;

import java.io.IOException;
import java.io.OutputStream;

import org.jbei.ice.lib.models.Sequence;

import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;

/**
 * Abstract formatter implementing {@link IFormatter} interface.
 *
 * @author Zinovii Dmytriv
 */
public class AbstractFormatter implements IFormatter {
    public static final String DEFAULT_NAMESPACE = "org.jbei";

    private String namespaceName = DEFAULT_NAMESPACE;

    @Override
    /**
     * Format the {@link Sequence} and output to the {@link OutputStream}.
     */
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException,
            IOException {
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String value) {
        namespaceName = value;
    }

    public Namespace getNamespace() {
        return new SimpleNamespace(getNamespaceName());
    }
}
