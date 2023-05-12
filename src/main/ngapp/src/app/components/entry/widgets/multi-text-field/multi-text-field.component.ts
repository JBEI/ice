import {Component, Input, OnInit} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {Part} from "../../../../models/Part";
import {EntryFieldService} from "../../../../services/entry-field.service";

@Component({
    selector: 'app-multi-text-field',
    templateUrl: './multi-text-field.component.html',
    styleUrls: ['./multi-text-field.component.css']
})
export class MultiTextFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Input() part: Part;

    values: number[];

    constructor(private fields: EntryFieldService) {
        this.values = [0];
    }

    ngOnInit(): void {
    }

    switchEditMode(): void {
        this.fields.setQuickEdit(this.field);
    }

    textInputFocusOut(field: CustomField): void {
        field.active = false;
        if (field.required)
            field.isInvalid = !field.value;
    }

    textInputFocusIn(field: CustomField): void {
        field.active = true;
    }

    addNewInput(): void {
        this.values.push(this.values.length);
    }

    removeInput(index): void {
        this.values.splice(index);
        console.log(index);
    }

    changed(e): void {
    }
}
