import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {Part} from "../../../../models/Part";
import {EntryFieldService} from "../../../../services/entry-field.service";

@Component({
    selector: 'app-textarea-field',
    templateUrl: './textarea-field.component.html',
    styleUrls: ['./textarea-field.component.css']
})
export class TextareaFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Input() part: Part;

    constructor(private fieldService: EntryFieldService) {
    }

    ngOnInit(): void {
    }

    switchEditMode(): void {
        this.fieldService.setQuickEdit(this.field);
    }

    textInputFocusOut(field: CustomField): void {
        field.active = false;
        if (field.required)
            field.isInvalid = !field.value;
    }

    textInputFocusIn(field: CustomField): void {
        field.active = true;
    }
}
