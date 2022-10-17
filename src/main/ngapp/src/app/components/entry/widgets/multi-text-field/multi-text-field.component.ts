import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";

@Component({
    selector: 'app-multi-text-field',
    templateUrl: './multi-text-field.component.html',
    styleUrls: ['./multi-text-field.component.css']
})
export class MultiTextFieldComponent implements OnInit {

    @Input() field: CustomField;

    constructor() {
    }

    ngOnInit(): void {
    }

}
