import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
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
    summaryFields: CustomField[];
    summaryFieldTypes: string[];
    active: string = 'general';

    constructor(private http: HttpService, private route: ActivatedRoute, private local: LocalStorageService,
                private router: Router) {
        this.summaryFieldTypes = ['NAME', 'STATUS', 'SUMMARY', 'BIO_SAFETY_LEVEL', 'CREATOR'];
        this.summaryFields = [];

        route.url.subscribe(() => {
            this.active = route.snapshot.children[0].url[0].path;
            console.log(this.active);
        });
    }

    ngOnInit(): void {
        this.route.data.subscribe((data) => {
            this.entry = data.entry;
        });
    }

    editAction(): void {
        for (let i = 0; i < this.entry.fields.length; i += 1) {
            const customField: CustomField = this.entry.fields[i];
            customField.editMode = 'FULL';
        }
        if (this.active !== 'general')
            this.goTo('general');
    }

    goTo(path: string): void {
        this.router.navigate((['/entry', this.entry.id, path]));
    }
}
