import {Component, Input} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {EntryService} from "../../../services/entry.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'app-edit-custom-field-modal',
    templateUrl: './edit-custom-field-modal.component.html',
    styleUrls: ['./edit-custom-field-modal.component.css']
})
export class EditCustomFieldModalComponent {

    @Input() entryType: string;

    field: any;
    options = [
        {name: "Built in field", value: 'EXISTING'},
        {name: 'Text', value: 'TEXT_INPUT'},
        {name: 'Options', value: 'MULTI_CHOICE'},
        {name: 'Options with Text', value: 'MULTI_CHOICE_PLUS'}];
    existingOptions: any;

    constructor(public activeModal: NgbActiveModal, private http: HttpService, private entryService: EntryService) {
        this.field = {required: false, options: [], entryType: this.entryType.toUpperCase()};
        // this.existingOptions = this.entryService.getFieldsForType(entryType);
    }


    // adds option
    addOption(afterIndex) {
        this.field.options.push({});
    };

    removeOption(index) {
        this.field.options.splice(index, 1);
    };

    change() {
        switch (this.field.fieldType.value) {
            case "MULTI_CHOICE":
                this.field.options = [{}];
                break;

            case "MULTI_CHOICE_PLUS":
                this.field.options = [{}];
                break;
        }
    };

    existingFieldSelected() {
        this.field.required = this.field.existingFieldObject.required;
        this.field.label = this.field.existingFieldObject.label;
        this.field.options = [{name: "schema", value: this.field.existingFieldObject.schema}]
        this.field.existingField = this.field.existingFieldObject.label.toUpperCase();
    };

    createCustomLink() {
        this.field.fieldType = this.field.fieldType.value;
        this.http.post("rest/fields/" + this.entryType, this.field).subscribe({
            next: (result: any) => {
                this.activeModal.close(result);
            }
        })
    };

    // determines whether the "create" button in the form should be enabled or disabled based on information
    // entered by user
    disableCreateButton() {
        if (!this.field.label)
            return true;

        if (!this.field.fieldType)
            return true;

        switch (this.field.fieldType.value) {
            case "MULTI_CHOICE":
            case "MULTI_CHOICE_PLUS":
                for (let i = 0; i < this.field.options.length; i += 1) {
                    if (!this.field.options[i].value)
                        return true;
                }
                break;
        }

        return false;
    }

}
