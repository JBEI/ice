package org.jbei.ice.lib.composers.formatters;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.jbei.ice.lib.models.Sequence;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLFactory;

public class SbolFormatter extends AbstractFormatter {

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException,
            IOException {
        DnaComponent dnaComponent = SBOLFactory.createDnaComponent();
        dnaComponent.setURI(URI.create("http://partsregistry.org/Part:BBa_I0462"));
        dnaComponent.setDisplayId("BBa_I0462");
        dnaComponent.setName("I0462");
        dnaComponent.setDescription("LuxR protein generator");
        /*
        dnaComponent.setDnaSequence(createDnaSequence());
        dnaComponent.addAnnotation(createAnnotation1());
        dnaComponent.addAnnotation(createAnnotation2());
        dnaComponent.addAnnotation(createAnnotation3());
        return dnaComponent;*/
    }
}
