import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {EntryField} from "../../../models/entry-field";
import {ActivatedRoute} from "@angular/router";
import Handsontable from "handsontable";
import {EntryFieldOption} from "../../../models/entry-field-option";
import {Part} from "../../../models/Part";
import {BreakpointObserver} from "@angular/cdk/layout";
import {CellChange, ChangeSource} from "handsontable/common";
import {BulkUpload} from "../../../models/bulk-upload";
import {Location} from "@angular/common";

@Component({
    selector: 'app-upload',
    templateUrl: './upload.component.html',
    styleUrls: ['./upload.component.css']
})
export class UploadComponent implements OnInit {

    type: string;
    linkedType: string;
    fields: EntryField[];
    partsMap: Map<number, Part>;
    upload: BulkUpload;

    hotSettings: Handsontable.GridSettings = {
        startRows: 100,
        rowHeaders: true,
        colWidths: 100,
        stretchH: 'all',
        height: 'auto',
        licenseKey: 'non-commercial-and-evaluation',
        afterChange: (changes, source) => {
            this.manageChanges(changes, source);
        }
    };
    hotid = 'hotid';

    constructor(private route: ActivatedRoute, private http: HttpService, private breakpointObserver: BreakpointObserver,
                private location: Location) {
        this.partsMap = new Map<number, Part>();
        this.upload = new BulkUpload();
    }

    ngOnInit(): void {
        this.type = this.route.snapshot.paramMap.get('type');

        if (!isNaN(parseInt(this.type))) {
            // retrieve upload from server
            console.log('retrieving upload with id', this.type);
            this.http.get('uploads/' + parseInt(this.type)).subscribe((result: BulkUpload) => {
                this.upload = result;
                this.type = this.upload.type;
                this.retrieveTypeFields(this.upload.type);
            });
        } else {
            this.retrieveTypeFields(this.type);
        }

        // this.newPart = JSON.parse(sessionStorage.getItem('in-progress-entry'));
        // if (!this.newPart) {
        //     this.http.post('parts', {type: this.type.toUpperCase()}).subscribe({
        //         next: (part: Part) => {
        //             this.newPart = part;
        //             sessionStorage.setItem('in-progress-entry', JSON.stringify(this.newPart));
        //         }
        //     });
        // }
    }

    // retrieve fields for specified type. This (resulting fields) is used for generating the upload table
    retrieveTypeFields(type: string): void {
        this.http.get('parts/fields/' + type).subscribe((result: EntryField[]) => {
            this.fields = result;

            // add sequence file
            this.fields.push({
                custom: false,
                entryType: "",
                fieldInputType: "FILE",
                fieldType: "",
                id: 0,
                options: [],
                required: false,
                label: "Sequence File"
            })

            // value: T, index: number, array: T[]) => void, thisArg?: any
            // this.hotSettings.colHeaders = function(index: number): string {
            //     return "<b>a</b>";
            // }
        });
    }

    manageChanges(changes: CellChange[] | null, source: ChangeSource): void {
        if ("loadData" === source)
            return;

        console.log(changes, source);

        if (!this.upload.id) {
            // create new upload
            console.log("creating new upload");
            this.upload.type = this.type;
            this.http.put('uploads', this.upload).subscribe((result: BulkUpload) => {
                this.upload = result;
                this.location.go("/upload/" + this.upload.id);
                this.manageEntryChange(changes, source);
            });
        } else {
            this.manageEntryChange(changes, source);
        }
    }

    manageEntryChange(changes: CellChange[] | null, source: ChangeSource): void {
        console.log(changes, source);
        if ("edit" === source) {
            // manual edit
        } else if ("Autofill.fill" === source) {
            // drag and fill
        } else if ("CopyPaste.paste === source") {
            // copy and paste
        }

        if (!changes)
            return;

        changes?.forEach(([row, col, oldValue, newValue]) => {
            // Some logic...
            console.log(row, col, oldValue, newValue);
            this.checkParts(row);
        });

        // for (let i = 0; i < changes.length; i += 1) {
        //     const change: any = changes[i];
        //     [row, col, oldValue, newValue]
        // this.checkParts(change[0]);
        // }
    }

    checkParts(row: number): void {
        console.log("parts", this.partsMap);

        const part = this.partsMap.get(row);
        if (!part) {
            // create new Part
        }
    }

    getFieldLabel(field: EntryField): string {
        let label = field.label;
        if (field.required)
            label += ' <span class=\"text-danger font-12em\">*</span>';
        return label;

    }

    getSelectOptions(field: EntryField): string[] {
        if (field.fieldInputType !== 'SELECT')
            return [];

        const result = [];
        for (let i = 0; i < field.options.length; i += 1) {
            const option: EntryFieldOption = field.options[i];
            result.push(option.value);
        }

        return result;
    }

    getFieldType(field: EntryField): any {
        if (field.fieldInputType === 'SELECT' && field.options.length) {
            return "dropdown";
        }

        switch (field.fieldInputType) {
            case 'BOOLEAN':
                return "checkbox";
        }
        return "text";
    }
}
