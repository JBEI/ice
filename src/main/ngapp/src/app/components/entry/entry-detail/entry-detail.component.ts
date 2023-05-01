import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {HttpService} from "../../../services/http.service";
import {Part} from "../../../models/Part";
import {CustomField} from "../../../models/custom-field";
import {LocalStorageService} from "../../../services/local-storage.service";

@Component({
    selector: 'app-entry-detail',
    templateUrl: './entry-detail.component.html',
    styleUrls: ['./entry-detail.component.css']
})
export class EntryDetailComponent implements OnInit {

    entry: Part;
    expandMetaData: boolean;
    summaryFields: CustomField[];
    summaryFieldTypes: string[];

    constructor(private http: HttpService, private route: ActivatedRoute, private local: LocalStorageService) {
        this.summaryFieldTypes = ['NAME', 'STATUS', 'SUMMARY', 'BIO_SAFETY_LEVEL', 'CREATOR'];
        this.summaryFields = [];
    }

    ngOnInit(): void {
        this.route.data.subscribe((data) => {
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
