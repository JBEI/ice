package org.jbei.ice.lib.entry;

import java.util.Set;

import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.shared.dto.entry.EntryType;

/**
 * Utility class for operating on entries
 *
 * @author Hector Plahar
 */
public class EntryUtil {

    public static String principalInvestigatorToString(Set<EntryFundingSource> fundingSources) {
        if (fundingSources == null || fundingSources.isEmpty())
            return "";

        String str = null;
        for (EntryFundingSource source : fundingSources) {
            if (str != null)
                str += (", " + source.getFundingSource().getPrincipalInvestigator());
            else
                str = source.getFundingSource().getPrincipalInvestigator();
        }

        return str;
    }

    public static String fundingSourceToString(Set<EntryFundingSource> fundingSources) {
        if (fundingSources == null || fundingSources.isEmpty())
            return "";

        String str = null;
        for (EntryFundingSource source : fundingSources) {
            if (str != null)
                str += (", " + source.getFundingSource().getFundingSource());
            else
                str = source.getFundingSource().getFundingSource();
        }

        return str;
    }

    public static Entry createEntryFromType(EntryType type, String name, String email) {
        Entry entry;

        switch (type) {
            case PLASMID:
                entry = new Plasmid();
                break;
            case STRAIN:
                entry = new Strain();
                break;
            case PART:
                entry = new Part();
                break;
            case ARABIDOPSIS:
                entry = new ArabidopsisSeed();
                break;

            default:
                return null;
        }

        entry.setOwner(name);
        entry.setOwnerEmail(email);
        entry.setCreator(name);
        entry.setCreatorEmail(email);
        return entry;
    }
}
