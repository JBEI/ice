import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {User} from "../../../../models/User";

@Component({
    selector: 'app-user-with-email-field',
    templateUrl: './user-with-email-field.component.html',
    styleUrls: ['./user-with-email-field.component.css']
})
export class UserWithEmailFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Input() user: User;

    constructor() {
    }

    ngOnInit(): void {
    }

}
