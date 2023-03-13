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
    @Input() inEditMode: boolean = false;

    constructor(private fields: EntryFieldService) {
    }

    ngOnInit(): void {
    }

    switchEditMode(): void {
        this.inEditMode = !this.inEditMode;
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
        this.fields.updateField(this.part.id, this.field).subscribe(result => {
            this.inEditMode = !this.inEditMode;
        })
    }
}
