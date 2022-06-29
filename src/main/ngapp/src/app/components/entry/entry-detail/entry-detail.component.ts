import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {HttpService} from "../../../services/http.service";
import {Part} from "../../../models/Part";

@Component({
    selector: 'app-entry-detail',
    templateUrl: './entry-detail.component.html',
    styleUrls: ['./entry-detail.component.css']
})
export class EntryDetailComponent implements OnInit {

    entry: Part;

    constructor(private http: HttpService, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.data.subscribe((data) => {
            this.entry = data.entry;

            // retrieve sequence
            // if (!this.entry.hasSequence)
            //     return;
            //
            // this.http.get('parts/' + this.entry.id + '/sequence').subscribe((result: Sequence) => {
            //     this.sequence = result;
            // })
        });
    }
}
