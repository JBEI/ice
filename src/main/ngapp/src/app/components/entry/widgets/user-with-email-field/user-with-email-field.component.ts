import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {User} from "../../../../models/User";
import {Part} from "../../../../models/Part";
import {HttpService} from "../../../../services/http.service";

@Component({
    selector: 'app-user-with-email-field',
    templateUrl: './user-with-email-field.component.html',
    styleUrls: ['./user-with-email-field.component.css']
})
export class UserWithEmailFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Input() user: User;
    appendString: string = ' Email';
    @Input() part: Part;
    @Input() inEditMode: boolean = false;

    constructor(private http: HttpService) {
    }

    ngOnInit(): void {
    }

    switchEditMode(): void {
        this.inEditMode = !this.inEditMode;
    }

    textInputFocusOut(field: CustomField, removeAppend: boolean = false): void {
        field.active = false;
        if (removeAppend && field.label.endsWith(this.appendString)) {
            field.label = field.label.slice(0, field.label.length - this.appendString.length);
        }
        if (field.required)
            field.isInvalid = !field.value;
    }

    textInputFocusIn(field: CustomField, append: boolean = false): void {
        field.active = true;
        if (append && !field.label.endsWith(this.appendString))
            field.label += this.appendString;
    }
}
