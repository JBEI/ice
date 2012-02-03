package org.jbei.ice.client.search.blast;

import java.util.HashMap;

import org.jbei.ice.shared.BlastProgram;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

public interface IBlastView {

    BlastResultsTable getResultsTable();

    String getSequence();

    BlastProgram getProgram();

    Button getSubmit();

    void setResultsDisplayVisible(boolean visible);

    void setProgramOptions(HashMap<String, String> options);

    Widget asWidget();
}
