export class FieldOption {
    partId: number;
    name: string;
    value: string;

    constructor(name: string, value?: string) {
        this.name = name;
        if (!this.value)
            this.value = this.name;
        else
            this.value = value;
    }
}

export class PartField {

    label: string;
    schema: string;
    subSchema: string;
    inputType: string;
    autoComplete: string;
    value: string;
    options: FieldOption[];

    constructor(label: string, schema?: string) {
        this.label = label;
        if (schema) {
            this.schema = schema;
        } else {
            this.schema = label.replace(/\s+/g, '');
            this.schema = this.schema.charAt(0).toLowerCase() + this.schema.slice(1);
        }
    }

    setInputType(inputType: string): PartField {
        this.inputType = inputType;
        return this;
    }

    setOptions(options: FieldOption[]): PartField {
        this.options = options;
        this.inputType = 'option';
        return this;
    }

    setAutoComplete(field: string, isAdd: boolean = false): PartField {
        this.autoComplete = field;
        this.inputType = 'autoComplete';
        if (isAdd)
            this.inputType += 'Add';
        return this;
    }
}

export class PlasmidField extends PartField {

    constructor(label: string, schema?: string, subSchema?: string) {
        if (!schema)
            schema = label.toLowerCase();
        super(label, schema);
        if (subSchema === undefined)
            super.subSchema = 'plasmidData';
    }
}

export class StrainField extends PartField {
    constructor(label: string, schema?: string, subSchema?: string) {
        if (!schema)
            schema = label.toLowerCase();
        super(label, schema);

        // for fields (eg"selectionMarkers") which aren't part of the subschema but don't belong to the base class
        if (subSchema === undefined)
            super.subSchema = 'strainData';
    }
}

export class ProteinField extends PartField {
    constructor(label: string, schema?: string, subSchema?: string) {
        if (!schema)
            schema = label.toLowerCase();
        super(label, schema);

        // for fields (eg"selectionMarkers") which aren't part of the subschema but don't belong to the base class
        if (subSchema === undefined)
            super.subSchema = 'proteinData';
    }
}

export class SeedField extends PartField {
    constructor(label: string, schema?: string, subSchema?: string) {
        if (!schema)
            schema = label.toLowerCase();
        super(label, schema);

        // for fields (eg"selectionMarkers") which aren't part of the subschema but don't belong to the base class
        if (subSchema === undefined)
            super.subSchema = 'seedData';
    }
}