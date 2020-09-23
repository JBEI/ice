export class MasterFile {

    name: string;
    reuse: boolean;
    exists: boolean;
    userFileName?: string;
    errorUploading?: boolean;

    constructor(_name: string, _reuse: boolean = false, _exists: boolean = false) {
        this.name = _name;
        this.reuse = _reuse;
        this.errorUploading = false;
    }
}

export class J5Parameter {
    fieldName: string;
    name: string;
    defaultValue: string;
    currentValue: any;
    tooltip: string;
    options?: string[];
    isDirty: boolean;

    constructor(_fieldName: string, _name: string, _defaultValue: string, _tooltip: string, _options?: string[]) {
        this.fieldName = _fieldName;
        this.name = _name;
        this.defaultValue = _defaultValue;
        this.tooltip = _tooltip;
        this.options = _options;
        this.isDirty = false;
    }
}

export class AssemblyMethod {
    name: string;
    value: string;

    constructor(_name: string, _value: string) {
        this.name = _name;
        this.value = _value;
    }
}
