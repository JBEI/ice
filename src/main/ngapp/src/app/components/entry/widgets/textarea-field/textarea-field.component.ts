import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {Part} from "../../../../models/Part";

@Component({
    selector: 'app-textarea-field',
    templateUrl: './textarea-field.component.html',
    styleUrls: ['./textarea-field.component.css']
})
export class TextareaFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Input() part: Part;
    @Input() inEditMode: boolean = false;

    constructor() {
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
}
