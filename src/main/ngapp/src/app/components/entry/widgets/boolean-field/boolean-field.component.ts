import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";

@Component({
    selector: 'app-boolean-field',
    templateUrl: './boolean-field.component.html',
    styleUrls: ['./boolean-field.component.css']
})
export class BooleanFieldComponent implements OnInit {

    @Input() field: CustomField;

    constructor() {
    }

    ngOnInit(): void {
    }

}
