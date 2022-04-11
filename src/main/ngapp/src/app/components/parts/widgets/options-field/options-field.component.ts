import {Component, Input, OnInit} from '@angular/core';
import {PartField} from "../../../../models/part-field";

@Component({
    selector: 'app-options-field',
    templateUrl: './options-field.component.html',
    styleUrls: ['./options-field.component.css']
})
export class OptionsFieldComponent implements OnInit {

    @Input() field: PartField;
    className: string;
    displayValue: string;

    constructor() {
    }

    ngOnInit(): void {
        for (let option of this.field.options) {
            if (option.name == this.field.value) {
                this.displayValue = option.value;
                break;
            }
        }

        switch (this.field.schema) {
            case 'status':
                this.displayValue = this.field.value;

                if (this.field.value.toLowerCase() === 'planned') {
                    this.className = 'p-2 badge badge-info';
                } else if (this.field.value.toLowerCase() === 'complete') {
                    this.className = 'p-2 badge badge-success';
                } else if (this.field.value.toLowerCase() === 'abandoned') {
                    this.className = 'p-2 badge badge-danger';
                } else {
                    this.className = 'p-2 badge badge-secondary';
                }
                break;
        }
    }
}
