import {Component} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {ActivatedRoute, Router} from "@angular/router";
import {LocalStorageService} from "../../../services/local-storage.service";
import {DecimalPipe, NgClass, NgForOf} from "@angular/common";
import {
    NgbPagination,
    NgbPaginationFirst,
    NgbPaginationLast,
    NgbPaginationNext,
    NgbPaginationPrevious
} from "@ng-bootstrap/ng-bootstrap";
import {ReactiveFormsModule} from "@angular/forms";
import {Paging} from "../../../models/paging";

@Component({
    selector: 'app-sequence-analysis',
    templateUrl: './sequence-analysis.component.html',
    standalone: true,
    imports: [
        DecimalPipe,
        NgForOf,
        NgbPagination,
        NgbPaginationFirst,
        NgbPaginationLast,
        NgbPaginationNext,
        NgbPaginationPrevious,
        ReactiveFormsModule,
        NgClass
    ],
    styleUrls: ['./sequence-analysis.component.css']
})
export class SequenceAnalysisComponent {

    paging: Paging = new Paging('created');
    pageNumber: number;
    pageCount: number;

    constructor(private http: HttpService, private route: ActivatedRoute, private local: LocalStorageService,
                private router: Router) {

        route.url.subscribe(() => {
            console.log(route.snapshot.url[0].path);
        });

        this.route.parent.data.subscribe((data) => {
            console.log(data.entry);
        });
    }

    pageChange(page: number): void {
        this.paging.offset = ((page - 1) * this.paging.limit);
    }
}
