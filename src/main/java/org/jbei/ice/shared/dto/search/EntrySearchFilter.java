package org.jbei.ice.shared.dto.search;

import java.util.ArrayList;

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

    public FilterWidget getFilterWidget(SearchFilterType filterType) {
        FilterWidget filterWidget;

        switch (filterType) {
            case IDENTIFIER:
                filterWidget = new SearchFilterWidget(filterType);
                filterWidget.setInputPlaceholder("Name, Alias, RID, PartID");
                break;

            case HAS_ATTACHMENT:
            case HAS_SAMPLE:
            case HAS_SEQUENCE:
                filterWidget = new BooleanFilterWidget(filterType);
                break;

//            case OWNER:
//            case CREATOR:
//            case DESCRIPTION:
//            case PRINCIPAL_INVESTIGATOR:
//            case FUNDING_SOURCE:
//            case SELECTION_MARKER:
//            case BACKBONE:
            default:
                filterWidget = new SearchFilterWidget(filterType);
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
