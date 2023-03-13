import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {Part} from "../../../../models/Part";

@Component({
    selector: 'app-boolean-field',
    templateUrl: './boolean-field.component.html',
    styleUrls: ['./boolean-field.component.css']
})
export class BooleanFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Input() part: Part;
    @Input() inEditMode: boolean = false;

    constructor() {
    }

    ngOnInit(): void {
    }

    toggleField(): void {
        if (!this.field)
            return;

        if (this.field.value === 'true')
            this.field.value = 'false';
        else
            this.field.value = 'true';
    }
}
