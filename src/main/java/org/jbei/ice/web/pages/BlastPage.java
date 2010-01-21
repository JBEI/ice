package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.web.panels.BlastFormPanel;

public class BlastPage extends ProtectedPage {
    
    public BlastPage(PageParameters parameters) {
        super(parameters);
        
        add(new BlastFormPanel("blastFormPanel"));
    }

}
