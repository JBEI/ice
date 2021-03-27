import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Part} from "../../models/Part";
import {Paging} from "../../models/paging";
import {Result} from "../../models/Result";
import {HttpService} from "../../services/http.service";

@Component({
    selector: 'app-parts-table',
    templateUrl: './parts-table.component.html',
    styleUrls: ['./parts-table.component.css']
})
export class PartsTableComponent implements OnInit {

    loadingPage: boolean;
    partHeaders: string[];
    selectedParts: number[];
    allSelected: boolean;

    @Input() parts: Part[];
    paging: Paging;

    @Output() retrieveParts: EventEmitter<any> = new EventEmitter<any>();
    @Output() partsChange: EventEmitter<any> = new EventEmitter<any>();

    // changeValue() {
    //     //     console.log(this.paging);
    //     //     this.pagingChange.emit(this.paging);
    //     console.log(this.parts);
    //     this.partsChange.emit(this.parts);
    // }

    constructor(private http: HttpService) {
    }

    ngOnInit(): void {
        this.selectedParts = [];
        this.paging = new Paging();
        this.paging.sort = 'created';
        this.getParts();
    }

    getParts(): void {
        this.paging.offset = (this.paging.currentPage - 1) * this.paging.limit;
        console.log(this.paging);
        // this.retrieveParts.emit();

        this.http.get("collections/personal/entries", this.paging).subscribe((result: Result<Part>) => {
            console.log(result);
            this.paging.available = result.resultCount;
            this.parts = result.data;
        }, (err) => {
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
}
