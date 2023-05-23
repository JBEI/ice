import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {Part} from "../../models/Part";
import {Paging} from "../../models/paging";
import {HttpService} from "../../services/http.service";
import {Result} from "../../models/result";
import {PartSelectionService} from "../../services/part-selection.service";
import {Folder} from "../../models/folder";

@Component({
    selector: 'app-parts-table',
    templateUrl: './parts-table.component.html',
    styleUrls: ['./parts-table.component.css']
})
export class PartsTableComponent implements OnInit {

    @Input() parts: Part[];
    paging: Paging = new Paging('created');
    pageCount: number;
    pageNumber: number;
    pagingOptions = [15, 30, 50];

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

    PAGING_LIMIT_KEY = 'ice.paging.limit';

    // detect changes to the routing for folder and collection
    // this is used to clear selection

    ngOnChanges(changes: SimpleChanges) {
        console.log("changes", changes);

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

    constructor(private http: HttpService, public selection: PartSelectionService) {
    }

    ngOnInit(): void {
        const storedLimit = localStorage.getItem(this.PAGING_LIMIT_KEY);
        this.paging.limit = storedLimit ? Number.parseInt(storedLimit) : 15;
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
        this.http.get('folders/' + this.folderId + '/entries', this.paging).subscribe((result: Folder) => {
            this.parts = result.entries;
            this.paging.processing = false;
            this.paging.available = result.count;
            this.setDisplayCounts();
        });
    }

    getCollectionEntries(): void {
        this.paging.processing = true;
        this.http.get('collections/' + this.collection + '/entries', this.paging).subscribe(
            {
                next: (result: Result<Part>) => {
                    this.paging.processing = false;
                    if (!result)
                        return;

                    this.paging.available = result.resultCount;
                    this.parts = result.data;

                    this.setDisplayCounts();
                }, error: (error: any) => {
                    this.paging.processing = false;
                }
            });
    }

    setDisplayCounts(): void {
        localStorage.setItem(this.PAGING_LIMIT_KEY, this.paging.limit.toString());
        this.pageNumber = ((this.paging.currentPage - 1) * this.paging.limit) + 1;
        this.pageCount = (this.paging.currentPage * this.paging.limit) > this.paging.available ? this.paging.available : (this.paging.currentPage * this.paging.limit);
    }

    sort(sortField: string): void {
        this.paging.sort = sortField;
        this.paging.offset = 0;
        this.paging.asc = !this.paging.asc;

        this.getParts();
    }

    isSelected(part: Part): boolean {
        return this.selection.isSelected(part);
    }

    select(part: Part): void {
        this.selection.select(part, this.paging.available);
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
