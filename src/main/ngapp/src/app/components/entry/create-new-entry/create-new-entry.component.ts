import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {HttpService} from "../../../services/http.service";
import {CustomField} from "../../../models/custom-field";
import {EntryService} from "../../../services/entry.service";

@Component({
    selector: 'app-create-new-entry',
    templateUrl: './create-new-entry.component.html',
    styleUrls: ['./create-new-entry.component.css']
})
export class CreateNewEntryComponent implements OnInit {

    type: string;
    fields: CustomField[];

    constructor(private route: ActivatedRoute, private http: HttpService, private entryService: EntryService,
                private router: Router) {
    }

    /**
     * Obtain type of entry being created from url and retrieve (after validation) appropriate fields for user entry
     */
    ngOnInit(): void {
        this.type = this.route.snapshot.paramMap.get('type');
        if (!this.entryService.getEntryTypes().includes(this.type.charAt(0).toUpperCase() + this.type.slice(1))) {
            this.router.navigate((['create', this.entryService.getEntryTypes()[0].toLowerCase()]));
            return;
        }

        this.http.get('parts/fields/' + this.type).subscribe((any: CustomField[]) => {
            this.fields = any;
        })
    }
}
