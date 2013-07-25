package org.jbei.ice.client.entry.display;

import org.jbei.ice.client.entry.display.detail.ArabidopsisDataView;
import org.jbei.ice.client.entry.display.detail.EntryDataView;
import org.jbei.ice.client.entry.display.detail.PartDataView;
import org.jbei.ice.client.entry.display.detail.PlasmidDataView;
import org.jbei.ice.client.entry.display.detail.StrainDataView;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedData;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.PlasmidData;
import org.jbei.ice.lib.shared.dto.entry.StrainData;

/**
 * Factory class for instantiating the different kinds of views for the part types
 *
 * @author Hector Plahar
 */
public class ViewFactory {

    /**
     * Creates the view for the specified part and displays the data contained in the object
     *
     * @param part part object whose details are to be rendered in the view
     * @return Detail view for part
     */
    public static EntryDataView createDetailView(PartData part) {

        switch (part.getType()) {

            case PLASMID:
                PlasmidData plasmidData = (PlasmidData) part;
                return new PlasmidDataView(plasmidData);

            case PART:
                return new PartDataView(part);

            case ARABIDOPSIS:
                ArabidopsisSeedData seedData = (ArabidopsisSeedData) part;
                return new ArabidopsisDataView(seedData);

            case STRAIN:
                StrainData strainData = (StrainData) part;
                return new StrainDataView(strainData);

            default:
                return null;
            // TODO : proper handling of unknown types
        }
    }
}
