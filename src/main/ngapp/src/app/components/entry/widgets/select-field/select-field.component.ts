import {Component, Input} from '@angular/core';
import {CustomField} from "../../../../models/custom-field";
import {Part} from "../../../../models/Part";
import {EntryFieldService} from "../../../../services/entry-field.service";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";

@Component({
    selector: 'app-select-field',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './select-field.component.html',
    styleUrls: ['./select-field.component.css']
})
export class SelectFieldComponent {

    @Input() field: CustomField;
    @Input() part: Part;
    updating: boolean;

    constructor(private fieldService: EntryFieldService) {
    }

    ngOnInit(): void {
    }

    switchEditMode(): void {
        this.fieldService.setQuickEdit(this.field);
    }

    displayForValue(): string {
        for (const option of this.field.options) {
            if (option.name === this.field.value)
                return option.value;
        }
        return "";
    }

    textInputFocusOut(field: CustomField): void {
        field.active = false;
        if (field.required)
            field.isInvalid = !field.value;
    }

    textInputFocusIn(field: CustomField): void {
        field.active = true;
    }

    updateField(): void {
        this.updating = true;
        this.fieldService.updateField(this.part.id, this.field).subscribe(result => {
            this.field.editMode = undefined;
            this.updating = false;
        })
    }
}
