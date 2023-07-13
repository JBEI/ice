import {Component, Input, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpService} from "../../../services/http.service";
import {ActivatedRoute} from "@angular/router";
import {LocalStorageService} from "../../../services/local-storage.service";
import {Part} from "../../../models/Part";
import {CustomField} from "../../../models/custom-field";
import {TextFieldComponent} from "../widgets/text-field/text-field.component";
import {SelectFieldComponent} from "../widgets/select-field/select-field.component";
import {DatePickerComponent} from "../../date-picker/date-picker.component";
import {UserWithEmailFieldComponent} from "../widgets/user-with-email-field/user-with-email-field.component";
import {TextareaFieldComponent} from "../widgets/textarea-field/textarea-field.component";
import {BooleanFieldComponent} from "../widgets/boolean-field/boolean-field.component";
import {MultiTextFieldComponent} from "../widgets/multi-text-field/multi-text-field.component";
import {PartVisualizationComponent} from "../../part-visualization/part-visualization.component";

@Component({
    selector: 'app-general-information',
    standalone: true,
    imports: [CommonModule, TextFieldComponent, SelectFieldComponent, DatePickerComponent, UserWithEmailFieldComponent, TextareaFieldComponent, BooleanFieldComponent, MultiTextFieldComponent, PartVisualizationComponent],
    templateUrl: './general-information.component.html',
    styleUrls: ['./general-information.component.css']
})
export class GeneralInformationComponent implements OnInit {

    @Input() entry: Part;
    expandMetaData: boolean;
    summaryFields: CustomField[];
    summaryFieldTypes: string[];
    active: number = 1;

    constructor(private http: HttpService, private route: ActivatedRoute, private local: LocalStorageService) {
        this.summaryFieldTypes = ['NAME', 'STATUS', 'SUMMARY', 'BIO_SAFETY_LEVEL', 'CREATOR'];
        this.summaryFields = [];
    }

    ngOnInit(): void {
        this.route.parent.data.subscribe((data) => {
            this.entry = data.entry;

            this.expandMetaData = (this.local.getDisplayMetaData() === 'true');
            if (!this.expandMetaData)
                this.retrieveSummaryDataFields();
        });
    }

    expandCollapseMetaData(): void {
        this.expandMetaData = !this.expandMetaData;
        this.local.storeDisplayMetadata(this.expandMetaData);

        if (!this.expandMetaData && !this.summaryFields.length) {
            this.retrieveSummaryDataFields();
        }
    }

    retrieveSummaryDataFields(): void {
        for (let i = 0; i < this.entry.fields.length; i += 1) {
            const customField: CustomField = this.entry.fields[i];
            if (this.summaryFieldTypes.indexOf(customField.fieldType) !== -1) {
                this.summaryFields.push(customField);
            }
        }
    }
}
