import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {Part} from "../../../../models/Part";

@Component({
    selector: 'app-multi-text-field',
    templateUrl: './multi-text-field.component.html',
    styleUrls: ['./multi-text-field.component.css']
})
export class MultiTextFieldComponent implements OnInit {

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
