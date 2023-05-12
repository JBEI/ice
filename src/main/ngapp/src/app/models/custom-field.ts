import {FieldOption} from "./part-field";

export class CustomField {

    id: number;
    label: string;
    entryType: string;          // e.g. PLASMID
    custom: boolean;
    fieldType: string;          // EXISTING or TEXT_INPUT or MULTI_CHOICE or MULTI_CHOICE_PLUS (multi + text input)
    existingField: string;      // maps to enum "EntryFieldLabel" on backend
    required: boolean;
    options: FieldOption [];
    disabled: boolean;
    value: string;
    fieldInputType: string;
    field: string;
    subField: string;

    // ui helper fields
    active: boolean;
    isInvalid: boolean;
    editMode: string;           // FULL or QUICK
}
