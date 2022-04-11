import {FieldOption} from "./part-field";

export class CustomField {

    id: number;
    label: string;
    fieldType: string;          // EXISTING or TEXT_INPUT or MULTI_CHOICE or MULTI_CHOICE_PLUS (multi + text input)
    existingField: string;
    required: boolean;
    options: FieldOption [];
    disabled: boolean;
    value: string;
}