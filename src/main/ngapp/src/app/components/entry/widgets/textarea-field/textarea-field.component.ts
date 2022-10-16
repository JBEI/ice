import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";

@Component({
    selector: 'app-textarea-field',
    templateUrl: './textarea-field.component.html',
    styleUrls: ['./textarea-field.component.css']
})
export class TextareaFieldComponent implements OnInit {

    @Input() field: CustomField;

    constructor() {
    }

    ngOnInit(): void {
    }

}
