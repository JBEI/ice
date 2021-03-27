import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {ActivatedRoute} from "@angular/router";
import {Part} from "../../../models/Part";

@Component({
    selector: 'app-part-details',
    templateUrl: './part-details.component.html',
    styleUrls: ['./part-details.component.css']
})
export class PartDetailsComponent implements OnInit {

    part: Part;
    context: any;
    selection: string;
    active: number;
    details: boolean;

    constructor(private http: HttpService, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.data.subscribe((data) => {
            this.part = data.partDetails;
        });

        this.selection = 'general';
        this.details = false;
    }

    showDetails(): void {
        this.details = true;
    }

    backTo(): void {

    }

    nextEntryInContext(): void {

    }

    prevEntryInContext(): void {

    }

    createCopyOfEntry(): void {

    }

    addLink(part: Part, type: string): void {

    }

    restoreSelectedEntries(): void {

    }

    submitSelectedImportEntry(): void {
    }
}
