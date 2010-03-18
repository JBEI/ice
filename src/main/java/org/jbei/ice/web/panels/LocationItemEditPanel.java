package org.jbei.ice.web.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Location;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.EntryViewPage;

public class LocationItemEditPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Location location = null;

    public LocationItemEditPanel(String id) {
        super(id);
    }

    public LocationItemEditPanel(String id, Location passedLocation) {
        super(id);

        setLocation(passedLocation);

        class LocationEditForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;

            private String locationString;
            private String barcode;
            private String notes;
            private int nColumns;
            private int nRows;
            private String wells;

            public LocationEditForm(String id) {
                super(id);

                setLocationString(location.getLocation());
                setBarcode(location.getBarcode());
                setNotes(location.getNotes());
                setnColumns(location.getnColumns());
                setnRows(location.getnRows());
                setWells(location.getWells());

                setModel(new CompoundPropertyModel<Object>(this));

                Button cancelButton = new Button("cancelButton", new Model<String>("Cancel")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onSubmit() {

                        setRedirect(true);
                        setResponsePage(EntryViewPage.class, new PageParameters("0="
                                + location.getSample().getEntry().getId() + ",1=samples"));
                    }
                };

                cancelButton.setDefaultFormProcessing(false);
                add(cancelButton);

                add(new TextField<String>("locationString"));
                add(new TextField<String>("barcode"));
                add(new TextField<String>("notes"));
                add(new TextField<String>("nColumns"));
                add(new TextField<String>("nRows"));
                add(new TextField<String>("wells"));
                add(new Button("saveLocationButton", new Model<String>("Save")));
            }

            @Override
            protected void onSubmit() {
                SampleController sampleController = new SampleController(IceSession.get()
                        .getAccount());

                LocationItemEditPanel thisPanel = (LocationItemEditPanel) getParent();
                Location location = thisPanel.getLocation();

                location.setBarcode(getBarcode());
                location.setLocation(getLocationString());
                location.setNotes(getNotes());
                location.setnColumns(getnColumns());
                location.setnRows(getnRows());
                location.setWells(getWells());

                try {
                    sampleController.saveLocation(location);

                    Sample sample = location.getSample();

                    /* Inserting into a LinkedHashSet puts the last entered location
                     * at the bottom, which is undesirable for displaying the locations by reverse 
                     * sorted id (i.e., new one at the top). To preserve displayed order, the locations
                     * are sorted by brute force here then repopulated into sample.
                    */
                    ArrayList<Location> locations = new ArrayList<Location>(sample.getLocations());

                    class LocationComparator implements Comparator<Location> {

                        public int compare(Location arg0, Location arg1) {
                            return arg1.getId() - arg0.getId();
                        }
                    }

                    LocationComparator locationComparator = new LocationComparator();
                    Collections.sort(locations, locationComparator);

                    sample.getLocations().clear();
                    sample.getLocations().addAll(locations);

                    /* end brute force sort */

                    setRedirect(true);

                    setResponsePage(EntryViewPage.class, new PageParameters("0="
                            + location.getSample().getEntry().getId() + ",1=samples"));
                } catch (ControllerException e) {
                    throw new ViewException(e);
                } catch (PermissionException e) {
                    throw new ViewPermissionException("No permissions to edit location!", e);
                }
            }

            public String getLocationString() {
                return locationString;
            }

            public void setLocationString(String locationString) {
                this.locationString = locationString;
            }

            public String getBarcode() {
                return barcode;
            }

            public void setBarcode(String barcode) {
                this.barcode = barcode;
            }

            public String getNotes() {
                return notes;
            }

            public void setNotes(String notes) {
                this.notes = notes;
            }

            public int getnColumns() {
                return nColumns;
            }

            public void setnColumns(int nColumns) {
                this.nColumns = nColumns;
            }

            public int getnRows() {
                return nRows;
            }

            public void setnRows(int nRows) {
                this.nRows = nRows;
            }

            public String getWells() {
                return wells;
            }

            public void setWells(String wells) {
                this.wells = wells;
            }

        }
        add(new FeedbackPanel("feedback"));
        add(new LocationEditForm("locationEditForm"));

    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
