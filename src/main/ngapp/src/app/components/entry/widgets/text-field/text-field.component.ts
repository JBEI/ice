import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";

@Component({
    selector: 'app-text-field',
    templateUrl: './text-field.component.html',
    styleUrls: ['./text-field.component.css']
})
export class TextFieldComponent implements OnInit {

    @Input() field: CustomField;
    @Output() fieldChange: EventEmitter<any> = new EventEmitter<any>();

    inEditMode: boolean = false;

    constructor() {
    }

    ngOnInit(): void {
    }

    switchEditMode(): void {
        this.inEditMode = !this.inEditMode;
    }

    textInputFocusOut(field: CustomField): void {
        if (!field.value || !field.value.trim())
            return;

        console.log(field.value);
        // if (!this.newPart.id) {
        //     this.createPartData().subscribe((result: Part) => {
        //         console.log(result);
        //         this.newPart = result;
        //         this.updateField(field);
        //     });
        // } else {
        //     this.updateField(field);
        // }
    }

}
