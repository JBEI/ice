package org.jbei.ice.web.panels;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;

public class SampleViewPanel extends Panel {

	private static final long serialVersionUID = 1L;

	Entry entry = null;
	ArrayList<Sample> samples = new ArrayList<Sample>();
	ArrayList<Panel> panels = new ArrayList<Panel>();

	@SuppressWarnings("unchecked")
	public SampleViewPanel(String id, Entry entry) {
		super(id);
		
		this.entry = entry;
		class AddSampleLink extends AjaxFallbackLink {
			private static final long serialVersionUID = 1L;

			public AddSampleLink(String id) {
				super(id);
			}

			public void onClick(AjaxRequestTarget target) {
				// if first item is already an edit form, do nothing				
				SampleViewPanel temp = (SampleViewPanel) getParent();
				ArrayList<Panel> tempPanels = temp.getPanels();
				if (tempPanels.size() > 0 && tempPanels.get(0) instanceof SampleItemEditPanel) { 
				} else {
					Sample newSample = new Sample();
					SampleViewPanel sampleViewPanel = (SampleViewPanel) getParent();
					newSample.setEntry(sampleViewPanel.getEntry());
					Panel newSampleEditPanel = new SampleItemEditPanel(
							"sampleItemPanel", newSample);
					newSampleEditPanel.setOutputMarkupId(true);
					
					panels.add(0, newSampleEditPanel);
					
					target.getPage().replace(getParent());
					target.addComponent(getParent());
				}
			}
		}

		add(new AddSampleLink("addSampleLink"));

		try {
			samples.addAll(SampleManager.get(entry));
		} catch (ManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Object[] temp = samples.toArray();
		if (temp.length == 0) {
			Panel sampleItemPanel = new EmptyMessagePanel("sampleItemPanel", "No sample provided");
			sampleItemPanel.setOutputMarkupId(true);
			panels.add(sampleItemPanel);
		} else {
			populatePanels();
		}
		
		ListView samplesList = generateSamplesList("samplesListView");
		samplesList.setOutputMarkupId(true);
		add(samplesList);
	}
	
	public void populatePanels() {
		Integer counter = 1;
		panels.clear();
		for (Sample sample : samples) {
			Panel sampleItemPanel = new SampleItemViewPanel(
					"sampleItemPanel", counter, sample);
			sampleItemPanel.setOutputMarkupId(true);
			panels.add(sampleItemPanel);
			counter = counter + 1;
		}
	}

	@SuppressWarnings("unchecked")
	public ListView generateSamplesList(String id) {

		ListView samplesListView = new ListView(id, panels) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem item) {
				Panel panel = (Panel) item.getModelObject();
				item.add(panel);
			}
		};

		return samplesListView;
	}

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}

	public ArrayList<Panel> getPanels() {
		return panels;
	}

	public ArrayList<Sample> getSamples() {
		return samples;
	}

}
