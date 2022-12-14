import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";

@Component({
    selector: 'app-text-field',
    templateUrl: './text-field.component.html',
    styleUrls: ['./text-field.component.css']
})
export class TextFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Input() longText: Boolean;
    @Output() fieldChange: EventEmitter<any> = new EventEmitter<any>();

    inEditMode: boolean = false;

    constructor() {
    }

    ngOnInit(): void {
    }

    switchEditMode(): void {
        this.inEditMode = !this.inEditMode;
    }

    textInputFocusOut(field: CustomField): void {
        field.active = false;
    }

    textInputFocusIn(field: CustomField): void {
        field.active = true;
    }
}
