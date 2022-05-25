import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {HttpService} from "../../../services/http.service";
import {CustomField} from "../../../models/custom-field";

@Component({
    selector: 'app-create-new-entry',
    templateUrl: './create-new-entry.component.html',
    styleUrls: ['./create-new-entry.component.css']
})
export class CreateNewEntryComponent implements OnInit {

    private type: string;
    fields: CustomField[];

    constructor(private route: ActivatedRoute, private http: HttpService) {
    }

    ngOnInit(): void {
        this.type = this.route.snapshot.paramMap.get('type');
        this.http.get('parts/fields/' + this.type).subscribe((any: CustomField[]) => {
            this.fields = any;
        })
    }
}
