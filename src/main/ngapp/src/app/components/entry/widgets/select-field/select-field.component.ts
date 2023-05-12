import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {Part} from "../../../../models/Part";
import {EntryFieldService} from "../../../../services/entry-field.service";

@Component({
    selector: 'app-select-field',
    templateUrl: './select-field.component.html',
    styleUrls: ['./select-field.component.css']
})
export class SelectFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Input() part: Part;
    updating: boolean;

    constructor(private fieldService: EntryFieldService) {
    }

    ngOnInit(): void {
    }

    switchEditMode(): void {
        this.fieldService.setQuickEdit(this.field);
    }

    displayForValue(): string {
        for (const option of this.field.options) {
            if (option.name === this.field.value)
                return option.value;
        }
        return "";
    }

    textInputFocusOut(field: CustomField): void {
        field.active = false;
        if (field.required)
            field.isInvalid = !field.value;
    }

    textInputFocusIn(field: CustomField): void {
        field.active = true;
    }

    updateField(): void {
        this.updating = true;
        this.fieldService.updateField(this.part.id, this.field).subscribe(result => {
            this.field.editMode = undefined;
            this.updating = false;
        })
    }
}
