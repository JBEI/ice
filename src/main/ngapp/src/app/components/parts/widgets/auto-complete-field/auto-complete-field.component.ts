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
    value: string;

    constructor() {
    }

    ngOnInit(): void {
        let values: string[] = this.part[this.field.schema];
        this.value = values.join(', ');
    }
}
