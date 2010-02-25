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
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.logging.UsageLogger;
import org.jbei.ice.lib.search.Blast;
import org.jbei.ice.lib.search.BlastResult;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.web.common.CustomChoice;

public class BlastFormPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private String blastQuery;

    public BlastFormPanel(String id) {
        super(id);

        class BlastForm extends StatelessForm<Object> {

            private static final long serialVersionUID = 1L;
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
                ArrayList<BlastResult> blastResults = new ArrayList<BlastResult>();
                BlastFormPanel thisPanel = (BlastFormPanel) getParent();

                if (getQuery() != null) {
                    blastResults = new Blast().queryDistinct(getQuery(), getBlastProgram()
                            .getValue());
                }

                UsageLogger.info(blastResults.size() + " results for blast query.");
                Logger.info(blastResults.size() + " results for blast query.");

                if (blastResults.size() > 0) {
                    Panel resultPanel;

                    if (getBlastProgram().getValue().equals("tblastx")) {
                        String proteinQuery;

                        try {
                            proteinQuery = SequenceUtils.translateToProtein(getQuery());
                        } catch (Exception e) {
                            proteinQuery = "";
                            e.printStackTrace();
                        }

                        resultPanel = new BlastResultPanel("blastResultPanel", proteinQuery,
                                blastResults, 15, false);
                    } else {
                        resultPanel = new BlastResultPanel("blastResultPanel", getQuery(),
                                blastResults, 15, true);
                    }

                    thisPanel.replace(resultPanel);
                } else {
                    Panel resultPanel = new EmptyMessagePanel("blastResultPanel",
                            "No matches found");

                    thisPanel.replace(resultPanel);
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

        add(new BlastForm("blastForm"));
        add(new EmptyMessagePanel("blastResultPanel", ""));
    }

    public void setBlastQuery(String blastQuery) {
        this.blastQuery = blastQuery;
    }

    public String getBlastQuery() {
        return blastQuery;
    }
}
