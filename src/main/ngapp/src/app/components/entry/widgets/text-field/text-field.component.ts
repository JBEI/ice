import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {Part} from "../../../../models/Part";
import {EntryFieldService} from "../../../../services/entry-field.service";

@Component({
    selector: 'app-text-field',
    templateUrl: './text-field.component.html',
    styleUrls: ['./text-field.component.css']
})
export class TextFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Input() longText: Boolean;
    @Input() part: Part;

    updating: boolean;

    constructor(private fieldService: EntryFieldService) {
    }

    ngOnInit(): void {
    }

    switchEditMode(): void {
        this.fieldService.setQuickEdit(this.field);
    }

    cancelEdit(): void {
        this.field.editMode = undefined;
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
