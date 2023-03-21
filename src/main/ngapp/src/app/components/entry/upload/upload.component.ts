import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {EntryField} from "../../../models/entry-field";
import {ActivatedRoute} from "@angular/router";
import Handsontable from "handsontable";
import {EntryFieldOption} from "../../../models/entry-field-option";
import {Part} from "../../../models/Part";
import {BreakpointObserver} from "@angular/cdk/layout";

@Component({
    selector: 'app-upload',
    templateUrl: './upload.component.html',
    styleUrls: ['./upload.component.css']
})
export class UploadComponent implements OnInit {

    type: string;
    linkedType: string;
    fields: EntryField[];
    parts: Part[];

    hotSettings: Handsontable.GridSettings = {
        startRows: 20,
        rowHeaders: true,
        colWidths: 100,
        stretchH: 'all',
        height: 'auto',
        licenseKey: 'non-commercial-and-evaluation',
        // colHeaders(index): string {
        //     return "";
        // }
        afterChange: (changes, source) => {
            console.log(changes, source);
            if ("edit" === source) {
                // manual edit
            } else if ("Autofill.fill" === source) {
                // drag and fill

            } else if ("CopyPaste.paste === source") {
                // copy and paste
            }


            changes?.forEach(([row, col, oldValue, newValue]) => {
                // Some logic...
                console.log(row, col, oldValue, newValue);
            });
        }
    };
    hotid = 'hotid';

    constructor(private route: ActivatedRoute, private http: HttpService, private breakpointObserver: BreakpointObserver) {
        this.parts = [];
        window.innerHeight;
    }

    ngOnInit(): void {
        this.type = this.route.snapshot.paramMap.get('type');
        //.updateSettings(this.hotSettings);

        this.http.get('parts/fields/' + this.type).subscribe((result: EntryField[]) => {
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
