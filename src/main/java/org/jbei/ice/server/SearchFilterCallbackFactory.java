package org.jbei.ice.server;

import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import java.util.ArrayList;
import java.util.List;

/**
 * A search filter is represented by the filter type (e.g. NAME_OR_ALIAS),
 * operator(IS, CONTAINS), a user entered operand and a method/set of instructions
 * to execute. This factory is responsible for returning the custom callback
 * mechanism for running such a method/set or instructions
 *
 * @author Hector Plahar
 * @see SearchFilterType
 */
public class SearchFilterCallbackFactory {

    public static List<QueryFilterParams> getFilterParameters(SearchFilterType type,
            QueryOperator operator, String operand) {

        ArrayList<QueryFilterParams> params = new ArrayList<QueryFilterParams>();

        FilterCallback[] callbackList = getQueryFilters(type);
        if (callbackList == null)
            return params;

        for (FilterCallback callback : callbackList) {
            String criterion = callback.createCriterion(operator, operand);
            String from = callback.getFrom();
            String selection = callback.getSelection();

            params.add(new QueryFilterParams(selection, from, criterion));
        }
        return params;
    }

    protected static FilterCallback[] getQueryFilters(SearchFilterType type) {

        if (!type.isComposite()) {
            FilterCallback nonCompositeCallback = getQueryFilter(type);
            if (nonCompositeCallback == null)
                return null;

            return new FilterCallback[]{nonCompositeCallback};
        }

        FilterCallback[] callback;
        switch (type) {
            case NAME_OR_ALIAS:
                callback = new FilterCallback[2];
                callback[0] = new NameFilterCallback();
                callback[1] = new EntryFilterCallback("lower(entry.alias)");
                break;

            case OWNER:
                callback = new FilterCallback[2];
                callback[0] = new EntryFilterCallback("lower(entry.owner)");
                callback[1] = new EntryFilterCallback("lower(entry.ownerEmail)");
                break;

            case CREATOR:
                callback = new FilterCallback[2];
                callback[0] = new EntryFilterCallback("lower(entry.creator)");
                callback[1] = new EntryFilterCallback("lower(entry.creatorEmail)");
                break;

            case DESCRIPTION:
                callback = new FilterCallback[3];
                callback[0] = new EntryFilterCallback("lower(entry.shortDescription)");
                callback[1] = new EntryFilterCallback("lower(entry.longDescription)");
                callback[2] = new EntryFilterCallback("lower(entry.references)");
                break;

            default:
                callback = null;
        }

        return callback;
    }

    private static FilterCallback getQueryFilter(SearchFilterType type) {

        switch (type) {

            case PART_NUMBER:
                return new FilterCallback() {

                    @Override
                    public String getField() {
                        return "lower(partNumber.partNumber)";
                    }

                    @Override
                    public String getSelection() {
                        return "partNumber.entry.id";
                    }

                    @Override
                    public String getFrom() {
                        return "PartNumber partNumber";
                    }
                };

            case TYPE:
                return new EntryFilterCallback("lower(entry.recordType)");

            case STATUS:
                return new EntryFilterCallback("lower(entry.status)");

            case KEYWORDS:
                return new EntryFilterCallback("lower(entry.keywords)");

            case HAS_ATTACHMENT:
                return new FilterCallback() {

                    @Override
                    public String getSelection() {
                        return "attachment.entry.id";
                    }

                    @Override
                    public String getFrom() {
                        return "Attachment attachment";
                    }

                    @Override
                    public String getField() {
                        return null;
                    }
                };

            case HAS_SAMPLE:
                return new FilterCallback() {

                    @Override
                    public String getSelection() {
                        return "sample.entry.id";
                    }

                    @Override
                    public String getFrom() {
                        return "Sample sample";
                    }

                    @Override
                    public String getField() {
                        return null;
                    }
                };

            case HAS_SEQUENCE:
                return new FilterCallback() {

                    @Override
                    public String getSelection() {
                        return "sequence.entry.id";
                    }

                    @Override
                    public String getFrom() {
                        return "Sequence sequence";
                    }

                    @Override
                    public String getField() {
                        return null;
                    }
                };

            case BIO_SAFETY_LEVEL:
                return new EntryFilterCallback("entry.bioSafetyLevel");

            case INTELLECTUAL_PROPERTY:
                return new EntryFilterCallback("lower(entry.intellectualProperty)");

            case PRINCIPAL_INVESTIGATOR:
                return new FilterCallback() {

                    @Override
                    public String getSelection() {
                        return "entry.id";
                    }

                    @Override
                    public String getFrom() {
                        return EntryFundingSource.class.getName() + " entryFundingSource";
                    }

                    @Override
                    public String getField() {
                        return "lower(entryFundingSource.fundingSource.principalInvestigator)";
                    }
                };

            case FUNDING_SOURCE:
                return new FilterCallback() {

                    @Override
                    public String getSelection() {
                        return "entry.id";
                    }

                    @Override
                    public String getFrom() {
                        return EntryFundingSource.class.getName() + " entryFundingSource";
                    }

                    @Override
                    public String getField() {
                        return "lower(entryFundingSource.fundingSource.fundingSource)";
                    }
                };

            case RECORD_ID:
                return new EntryFilterCallback("lower(entry.recordId)");

            default:
                return null;
        }
    }
}
