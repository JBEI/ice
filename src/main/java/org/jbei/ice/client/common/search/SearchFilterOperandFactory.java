package org.jbei.ice.client.common.search;

import java.util.ArrayList;

import org.jbei.ice.client.common.BooleanFilterOperand;
import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.client.common.TextInputOperand;
import org.jbei.ice.client.common.TypeOperand;
import org.jbei.ice.client.model.OperandValue;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.EntryType;

/**
 * Factory for retrieving the operands and operations for
 * Search filter types
 *
 * @author Hector Plahar
 */

public class SearchFilterOperandFactory {

    public static FilterOperand getOperand(SearchFilterType type) {

        TextInputOperand textInputOperand = new TextInputOperand(type);
        ArrayList<OperandValue> operands = new ArrayList<OperandValue>();

        switch (type) {

            case NAME_OR_ALIAS:
                return textInputOperand;

            case PART_NUMBER:
                return textInputOperand;

            case TYPE:
                operands.clear();
                for (EntryType value : EntryType.values())
                    operands.add(new OperandValue(value.getDisplay(), value.getName()));
                return new TypeOperand(type, operands, QueryOperator.IS, QueryOperator.IS_NOT);

            case STATUS:
                operands.clear();
                for (StatusType statusType : StatusType.values())
                    operands.add(new OperandValue(statusType.getDisplayName(), statusType
                            .getDisplayName()));
                return new TypeOperand(type, operands, QueryOperator.IS, QueryOperator.IS_NOT);

            case OWNER:
                return textInputOperand;

            case CREATOR:
                return textInputOperand;

            case KEYWORDS:
                return textInputOperand;

            case DESCRIPTION:
                return textInputOperand;

            case HAS_ATTACHMENT:
            case HAS_SEQUENCE:
            case HAS_SAMPLE:
                return new BooleanFilterOperand(type);

            case BIO_SAFETY_LEVEL:
                operands.clear();
                for (BioSafetyOption option : BioSafetyOption.values())
                    operands.add(new OperandValue(option.getDisplayName(), option.getValue()));
                return new TypeOperand(type, operands, QueryOperator.IS, QueryOperator.IS_NOT);

            case INTELLECTUAL_PROPERTY:
                return textInputOperand;

            case PRINCIPAL_INVESTIGATOR:
                return textInputOperand;

            case FUNDING_SOURCE:
                return textInputOperand;

            case SELECTION_MARKER:
                // TODOs
                return textInputOperand;

            case BACKBONE:
                // TODO : plasmids only
                return textInputOperand;

            case PROMOTERS:
                // TODO : Plasmids only (is, long list)
                return textInputOperand;

            case ORIGIN:
                // TODO : same as above
                return textInputOperand;

            case HOST:
                return textInputOperand;

            case STRAIN_PLASMIDS:
                return textInputOperand;

            case GEN_PHEN:
                return textInputOperand;

            case RECORD_ID:
                return textInputOperand;

            case BLAST:
                return new BlastFilterOperand();

            default:
                throw new IllegalArgumentException("Could not handle case for type " + type);
        }
    }
}
