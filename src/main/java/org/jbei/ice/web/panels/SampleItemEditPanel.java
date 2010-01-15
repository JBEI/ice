package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.models.Sample;

public class SampleItemEditPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private Sample sample = null;

	public SampleItemEditPanel(String id, Sample passedSample) {
		super(id);
		sample = passedSample;
		class SampleEditForm extends StatelessForm<Object> {

			private static final long serialVersionUID = 1L;
			
			private String label;
			private String depositor;
			private String notes;

			public SampleEditForm(String id) {
				super(id);

				setLabel(sample.getLabel());
				setDepositor(sample.getDepositor());
				setNotes(sample.getNotes());
				
				setModel(new CompoundPropertyModel<Object>(this));

				Button cancelButton = new Button("cancelButton",
						new Model<String>("Cancel")) {

					private static final long serialVersionUID = 1L;

					public void onSubmit() {
						System.out.println("Cancel pushed");
						SampleItemEditPanel temp = (SampleItemEditPanel) getParent()
								.getParent();
						// This edit panel is usually inside ListView inside
						// SampleListView inside SampleViewPanel.
						// Is there a better way to get the parent sample view
						// panel?
						SampleViewPanel sampleViewPanel = (SampleViewPanel) temp
								.getParent().getParent().getParent();
						sampleViewPanel.populatePanels();
						getPage().replace(sampleViewPanel);
					}
				};

				cancelButton.setDefaultFormProcessing(false);
				add(cancelButton);

				add(new TextField<String>("label").setRequired(true).setLabel(
						new Model<String>("Label")));
				add(new TextField<String>("depositor").setRequired(true)
						.setLabel(new Model<String>("Depositor")));
				add(new TextArea<String>("notes"));
				add(new Button("saveSampleButton", new Model<String>("Save")) {
					private static final long serialVersionUID = 1L;

					public void onSubmit() {
						System.out.println("Save was pushed");
					};
				});

			}

			protected void onSubmit() {
				SampleItemEditPanel sampleItemEditPanel = (SampleItemEditPanel) getParent();
				Sample sample = sampleItemEditPanel.getSample();
				sample.setLabel(getLabel());
				sample.setDepositor(getDepositor());
				sample.setNotes(getNotes());
				boolean newPanel = false;
				if (sample.getUuid() == null || sample.getUuid().equals("")) {
					newPanel = true;
				}
				
				try {
					SampleManager.save(sampleItemEditPanel.getSample());
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					SampleViewPanel sampleViewPanel = (SampleViewPanel) sampleItemEditPanel
							.getParent().getParent().getParent();
					if (newPanel) {
						sampleViewPanel.getSamples().add(0, sampleItemEditPanel.getSample());
					}
					sampleViewPanel.populatePanels();
					getPage().replace(sampleViewPanel);
				}

			}

			public String getLabel() {
				return label;
			}

			@SuppressWarnings("unused")
			public void setLabel(String label) {
				this.label = label;
			}

			public String getDepositor() {
				return depositor;
			}

			@SuppressWarnings("unused")
			public void setDepositor(String depositor) {
				this.depositor = depositor;
			}

			public String getNotes() {
				return notes;
			}

			@SuppressWarnings("unused")
			public void setNotes(String notes) {
				this.notes = notes;
			}

		}
		add(new SampleEditForm("sampleEditForm"));
		add(new FeedbackPanel("feedback"));
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}

	public Sample getSample() {
		return sample;
	}

}
