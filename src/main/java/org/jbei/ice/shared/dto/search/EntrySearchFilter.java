package org.jbei.ice.shared.dto.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jbei.ice.client.common.BooleanFilterWidget;
import org.jbei.ice.client.common.EnumFilterWidget;
import org.jbei.ice.client.common.FilterWidget;
import org.jbei.ice.client.common.search.BlastFilterWidget;
import org.jbei.ice.client.common.search.SearchFilterWidget;
import org.jbei.ice.client.model.OperandValue;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.SearchFilterType;
import org.jbei.ice.shared.StatusType;

/**
 * @author Hector Plahar
 */
public class EntrySearchFilter {

    public List<SearchFilterType> getSearchFilters() {
        // for each filter type here, there needs to be a corresponding "case" in the getFilterWidget() method
        // that creates and returns the associated widget
        return Arrays.asList(SearchFilterType.IDENTIFIER, SearchFilterType.FUNDING_SOURCE,
                             SearchFilterType.HAS_ATTACHMENT, SearchFilterType.BIO_SAFETY_LEVEL,
                             SearchFilterType.HAS_SAMPLE, SearchFilterType.HAS_SEQUENCE, SearchFilterType.STATUS,
                             SearchFilterType.DESCRIPTION, SearchFilterType.PRINCIPAL_INVESTIGATOR);
    }

    public FilterWidget getFilterWidget(SearchFilterType filterType) {
        FilterWidget filterWidget;

        switch (filterType) {
            case IDENTIFIER:
            default:
                filterWidget = new SearchFilterWidget(filterType, false);
                filterWidget.setInputPlaceholder("Name, Alias, RID, PartID");
                break;

            case HAS_ATTACHMENT:
            case HAS_SAMPLE:
            case HAS_SEQUENCE:
                filterWidget = new BooleanFilterWidget(filterType);
                break;

            case DESCRIPTION:
            case PRINCIPAL_INVESTIGATOR:
            case FUNDING_SOURCE:
                filterWidget = new SearchFilterWidget(filterType, false);
                break;

            case STATUS:
                ArrayList<OperandValue> statusOperands = new ArrayList<OperandValue>();
                for (StatusType statusType : StatusType.values())
                    statusOperands.add(new OperandValue(statusType.getDisplayName(), statusType.getDisplayName()));
                filterWidget = new EnumFilterWidget(filterType, statusOperands);
                break;

            case BIO_SAFETY_LEVEL:
                ArrayList<OperandValue> bioSafetyOperands = new ArrayList<OperandValue>();
                for (BioSafetyOption option : BioSafetyOption.values())
                    bioSafetyOperands.add(new OperandValue(option.getDisplayName(), option.getValue()));
                return new EnumFilterWidget(filterType, bioSafetyOperands);

            case BLAST:
                return new BlastFilterWidget();
        }

        filterWidget.setWidth("200px");
        return filterWidget;
    }
}
