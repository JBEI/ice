package org.jbei.ice.client.common.search;

import java.util.ArrayList;

import org.jbei.ice.client.common.BooleanFilterWidget;
import org.jbei.ice.client.common.EnumFilterWidget;
import org.jbei.ice.client.common.FilterWidget;
import org.jbei.ice.client.model.OperandValue;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.SearchFilterType;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.EntryType;

/**
 * Factory for retrieving the operands and operations for
 * Search filter types
 *
 * @author Hector Plahar
 */

public class SearchFilterWidgetFactory {

    public static FilterWidget getWidget(SearchFilterType type) {

        SearchFilterWidget textInputOperand = new SearchFilterWidget(type, false);
        ArrayList<OperandValue> operands = new ArrayList<OperandValue>();

        switch (type) {

            case NAME_OR_ALIAS:
            case PART_NUMBER:
            case OWNER:
            case CREATOR:
            case KEYWORDS:
            case DESCRIPTION:
            case INTELLECTUAL_PROPERTY:
            case PRINCIPAL_INVESTIGATOR:
            case FUNDING_SOURCE:
            case SELECTION_MARKER:  // TODO : strain and plasmid only
            case BACKBONE:
            case PROMOTERS:
            case ORIGIN:
            case HOST:
            case STRAIN_PLASMIDS:
            case GEN_PHEN:
            case RECORD_ID:
                return textInputOperand;

            case TYPE:
                operands.clear();
                for (EntryType value : EntryType.values())
                    operands.add(new OperandValue(value.getDisplay(), value.getName()));
                return new EnumFilterWidget(type, operands);

            case STATUS:
                operands.clear();
                for (StatusType statusType : StatusType.values())
                    operands.add(new OperandValue(statusType.getDisplayName(), statusType
                            .getDisplayName()));
                return new EnumFilterWidget(type, operands);

            case HAS_ATTACHMENT:
            case HAS_SEQUENCE:
            case HAS_SAMPLE:
                return new BooleanFilterWidget(type);

            case BIO_SAFETY_LEVEL:
                operands.clear();
                for (BioSafetyOption option : BioSafetyOption.values())
                    operands.add(new OperandValue(option.getDisplayName(), option.getValue()));
                return new EnumFilterWidget(type, operands);

            case BLAST:
                return new BlastFilterWidget();

            default:
                throw new IllegalArgumentException("Could not handle case for type " + type);
        }
    }
}
