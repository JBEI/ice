import {Component, OnInit} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {Paging} from "../../../models/paging";
import {ActivatedRoute} from "@angular/router";
import {FolderDetails} from "../../../models/folder-details";

@Component({
    selector: 'app-folder',
    templateUrl: './folder.component.html',
    styleUrls: ['./folder.component.css']
})
export class FolderComponent implements OnInit {

    paging: Paging = new Paging('created');
    folderDetails: FolderDetails;

    constructor(private http: HttpService, private activatedRoute: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.activatedRoute.params.subscribe(params => {
            const folderId = params['id'];

            // retrieve folder details
            this.getFolderEntries(folderId);

        });
    }

    getFolderEntries(folderId: number): void {
        this.paging.processing = true;
        this.http.get('folders/' + folderId + '/entries', this.paging).subscribe((result: FolderDetails) => {
            this.folderDetails = result;
            this.paging.processing = false;
        });
    }
}
