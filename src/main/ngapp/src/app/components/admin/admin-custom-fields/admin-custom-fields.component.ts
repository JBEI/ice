import {Component} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {EditCustomFieldModalComponent} from "../../modal/edit-custom-field-modal/edit-custom-field-modal.component";

@Component({
    selector: 'app-admin-custom-fields',
    templateUrl: './admin-custom-fields.component.html',
    styleUrls: ['./admin-custom-fields.component.css']
})
export class AdminCustomFieldsComponent {

    selection = 'plasmid';
    loading: boolean;
    partCustomFields: any;

    constructor(private http: HttpService, private modalService: NgbModal) {
        this.retrievePartFields();
    }

    options = [
        {name: "Built in field", value: 'EXISTING'},
        {name: 'Text', value: 'TEXT_INPUT'},
        {name: 'Options', value: 'MULTI_CHOICE'},
        {name: 'Options with Text', value: 'MULTI_CHOICE_PLUS'}];

    optionsText(value: string): string {
        for (let i = 0; i < this.options.length; i += 1) {
            if (value === this.options[i].value) {
                return this.options[i].name;
            }
        }
        return value;
    }

    retrievePartFields(): void {
        this.partCustomFields = undefined;
        this.loading = true;
        this.http.get("rest/fields/" + this.selection).subscribe({
            next: (result: any) => {
                this.partCustomFields = result.data;
            }
        });
    };

    selectedTab(selection: string): void {
        if (this.selection === selection)
            return;

        this.selection = selection;
        this.retrievePartFields();
    };

    deleteCustomField(customField: any): void {
        this.http.delete("rest/fields/" + customField.entryType + "/" + customField.id)
            .subscribe({
                next: (result: any) => {
                    const index = this.partCustomFields.indexOf(customField);
                    if (index !== -1)
                        this.partCustomFields.splice(index, 1);
                }
            });
    };

    addNewCustomEntryField() {
        const options: NgbModalOptions = {backdrop: 'static', size: 'md'};
        const modalInstance = this.modalService.open(EditCustomFieldModalComponent, options);
        modalInstance.componentInstance.entryType = this.selection;

        modalInstance.result.then((result) => {
            if (!result)
                return;

            if (!this.partCustomFields)
                this.partCustomFields = [];
            this.partCustomFields.push(result);
        });
    };
}


