package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.controllers.BlastController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.search.blast.BlastResult;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.web.common.CustomChoice;

public class BlastFormPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private String blastQuery;
    private final String BLAST_RESULT_PANEL_NAME = "blastResultPanel";
    private final String BLAST_FORM_NAME = "blastForm";

    public BlastFormPanel(String id) {
        super(id);

        class BlastForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;

            private final int NUMBER_OF_ENTRIES_PER_PAGE = 15;

            private String query = "";
            private CustomChoice blastProgram;

            public BlastForm(String id) {
                super(id);

                setModel(new CompoundPropertyModel<Object>(this));
                add(new TextArea<String>("query"));

                CustomChoice blastn = new CustomChoice("blastn (nucleotide search)", "blastn");
                CustomChoice tblastx = new CustomChoice("tblastx (translated search)", "tblastx");

                ArrayList<CustomChoice> blastProgramChoices = new ArrayList<CustomChoice>();
                blastProgramChoices.add(blastn);
                blastProgramChoices.add(tblastx);
                setBlastProgram(blastn);

                add(new DropDownChoice<CustomChoice>("blastProgram",
                        new PropertyModel<CustomChoice>(this, "blastProgram"), blastProgramChoices,
                        new ChoiceRenderer<CustomChoice>("name", "value")));

                add(new Button("submit"));
            }

            @Override
            public void onSubmit() {
                BlastFormPanel thisPanel = (BlastFormPanel) getParent();

                String query = getQuery();
                String program = getBlastProgram().getValue();

                if (query != null && !query.isEmpty() && program != null && !program.isEmpty()) {
                    ArrayList<BlastResult> blastResults = null;

                    try {
                        blastResults = BlastController.query(query, program);
                    } catch (ProgramTookTooLongException e) {
                        Panel resultPanel = new EmptyMessagePanel(BLAST_RESULT_PANEL_NAME,
                                "Blast took too long to finish. Try a different query");
                        thisPanel.replace(resultPanel);
                    }

                    if (blastResults != null && blastResults.size() > 0) {
                        Panel resultPanel;
                        if (program.equals("tblastx")) {
                            String proteinQuery;
                            try {
                                proteinQuery = SequenceUtils.translateToProtein(query);
                            } catch (Exception e) { // TODO: Check this later
                                proteinQuery = "";

                                Logger.error("Failed to translate dna to protein!", e);
                            }

                            resultPanel = new BlastResultPanel(BLAST_RESULT_PANEL_NAME,
                                    proteinQuery, blastResults, NUMBER_OF_ENTRIES_PER_PAGE, false);
                        } else {
                            resultPanel = new BlastResultPanel(BLAST_RESULT_PANEL_NAME, query,
                                    blastResults, NUMBER_OF_ENTRIES_PER_PAGE, true);
                        }
                        thisPanel.replace(resultPanel);
                    } else {
                        Panel resultPanel = new EmptyMessagePanel(BLAST_RESULT_PANEL_NAME,
                                "No matches found");
                        thisPanel.replace(resultPanel);
                    }
                }
            }

            @SuppressWarnings("unused")
            public void setQuery(String query) {
                this.query = query;
            }

            public void setBlastProgram(CustomChoice blastProgram) {
                this.blastProgram = blastProgram;
            }

            public CustomChoice getBlastProgram() {
                return blastProgram;
            }

            public String getQuery() {
                return query;
            }
        }

        add(new BlastForm(BLAST_FORM_NAME));
        add(new EmptyMessagePanel(BLAST_RESULT_PANEL_NAME, ""));
    }

    public void setBlastQuery(String blastQuery) {
        this.blastQuery = blastQuery;
    }

    public String getBlastQuery() {
        return blastQuery;
    }
}
