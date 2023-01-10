import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {Part} from "../../../../models/Part";

@Component({
    selector: 'app-select-field',
    templateUrl: './select-field.component.html',
    styleUrls: ['./select-field.component.css']
})
export class SelectFieldComponent implements OnInit {

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
