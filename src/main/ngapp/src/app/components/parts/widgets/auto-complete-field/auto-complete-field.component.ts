import {Component, Input, OnInit} from '@angular/core';
import {Part} from "../../../../models/Part";
import {PartField} from "../../../../models/part-field";

@Component({
    selector: 'app-auto-complete-field',
    templateUrl: './auto-complete-field.component.html',
    styleUrls: ['./auto-complete-field.component.css']
})
export class AutoCompleteFieldComponent implements OnInit {
    @Input() part: Part;
    @Input() field: PartField;

    constructor() {
    }

    ngOnInit(): void {
        let values: string[] = this.part[this.field.schema];
        if (values && values.length) {
            this.field.value = values.join(', ');
        }
    }
}
