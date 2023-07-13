import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {User} from "../../../../models/User";
import {Part} from "../../../../models/Part";
import {EntryFieldService} from "../../../../services/entry-field.service";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";

@Component({
    selector: 'app-user-with-email-field',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './user-with-email-field.component.html',
    styleUrls: ['./user-with-email-field.component.css']
})
export class UserWithEmailFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Input() user: User;
    appendString: string = ' Email';
    @Input() part: Part;

    constructor(private fieldService: EntryFieldService) {
    }

    ngOnInit(): void {
    }

    switchEditMode(): void {
        this.fieldService.setQuickEdit(this.field);
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
