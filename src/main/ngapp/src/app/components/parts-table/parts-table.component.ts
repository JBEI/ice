import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {Part} from "../../models/Part";
import {Paging} from "../../models/paging";
import {HttpService} from "../../services/http.service";
import {FolderDetails} from "../../models/folder-details";
import {Result} from "../../models/result";

@Component({
    selector: 'app-parts-table',
    templateUrl: './parts-table.component.html',
    styleUrls: ['./parts-table.component.css']
})
export class PartsTableComponent implements OnInit {

    selectedParts: number[];
    allSelected: boolean;

    @Input() parts: Part[];
    paging: Paging = new Paging('created');

    @Output() retrieveParts: EventEmitter<any> = new EventEmitter<any>();
    @Output() partsChange: EventEmitter<any> = new EventEmitter<any>();

    // changeValue() {
    //     //     console.log(this.paging);
    //     //     this.pagingChange.emit(this.paging);
    //     console.log(this.parts);
    //     this.partsChange.emit(this.parts);
    // }

    @Input() folderId: number;
    @Input() collection: string;

    ngOnChanges(changes: SimpleChanges) {
        if (changes.collection) {
            // check if there is a change from a previous value
            if (!changes.collection.previousValue)
                return;

            // handle the new value
            this.collection = changes.collection.currentValue;
            this.getCollectionEntries();
        } else {
            // changes to the folder
        }
    }

    constructor(private http: HttpService) {
    }

    ngOnInit(): void {
        this.selectedParts = [];
        this.getParts();
    }

    private getParts(): void {
        if (this.collection) {
            this.getCollectionEntries();
        } else if (this.folderId) {
            this.getFolderEntries();
        }
    }

    getFolderEntries(): void {
        this.paging.processing = true;
        this.http.get('folders/' + this.folderId + '/entries', this.paging).subscribe((result: FolderDetails) => {
            this.parts = result.entries;
            this.paging.processing = false;
            this.paging.available = result.count;
        });
    }

    getCollectionEntries(): void {
        this.paging.processing = true;
        this.http.get('collections/' + this.collection + '/entries', this.paging).subscribe((result: Result<Part>) => {
            this.paging.processing = false;
            if (!result)
                return;

            this.paging.available = result.resultCount;
            this.parts = result.data;
        }, error => {
            this.paging.processing = false;
        });
    }

    isSelected(part: Part): boolean {
        if (this.allSelected)
            return true;

        return this.selectedParts.indexOf(part.id) !== -1;
    }

    select(part: Part): void {
        if (!part.id)
            return;

        const index = this.selectedParts.indexOf(part.id);
        if (index !== -1)
            this.selectedParts.splice(index, 1);
        else
            this.selectedParts.push(part.id);
    }

    pageCounts(currentPage, resultCount, maxPageCount = 15): string {
        const pageNum = ((currentPage - 1) * maxPageCount) + 1;

        // number on this page
        const pageCount = (currentPage * maxPageCount) > resultCount ? resultCount : (currentPage * maxPageCount);
        return pageNum + " - " + (pageCount) + " of " + (resultCount);
    };

    pageChange(page: number): void {
        this.paging.offset = ((page - 1) * this.paging.limit);
        this.getParts();
    }
}
