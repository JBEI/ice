import {EntryFieldOption} from "./entry-field-option";

export class EntryField {
    custom: boolean;
    entryType: string;
    id: number;
    label: string;
    options: EntryFieldOption[];
    required: boolean;
}
