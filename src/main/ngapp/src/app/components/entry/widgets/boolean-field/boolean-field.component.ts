import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {Part} from "../../../../models/Part";
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-boolean-field',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './boolean-field.component.html',
    styleUrls: ['./boolean-field.component.css']
})
export class BooleanFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Input() part: Part;

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
