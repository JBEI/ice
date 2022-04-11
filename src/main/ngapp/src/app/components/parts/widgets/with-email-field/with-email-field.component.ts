import {Component, Input, OnInit} from '@angular/core';
import {PartField} from "../../../../models/part-field";
import {Part} from "../../../../models/Part";

@Component({
    selector: 'app-with-email-field',
    templateUrl: './with-email-field.component.html',
    styleUrls: ['./with-email-field.component.css']
})
export class WithEmailFieldComponent implements OnInit {

    @Input() field: PartField;
    @Input() part: Part;
    id: number;
    email: string;

    constructor() {
    }

    ngOnInit(): void {
        this.id = this.part[this.field.schema + 'Id'];
        this.email = this.part[this.field.schema + 'Email'];
    }
}
