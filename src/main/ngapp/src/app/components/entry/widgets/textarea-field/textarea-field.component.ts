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

    constructor() {
    }

    ngOnInit(): void {
    }

    textInputFocusOut(field: CustomField): void {
        field.active = false;
    }

    textInputFocusIn(field: CustomField): void {
        field.active = true;
    }
}
