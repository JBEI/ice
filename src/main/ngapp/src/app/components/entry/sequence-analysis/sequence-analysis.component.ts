import {Component} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {ActivatedRoute, Router} from "@angular/router";
import {LocalStorageService} from "../../../services/local-storage.service";
import {DatePipe, DecimalPipe, JsonPipe, NgClass, NgForOf} from "@angular/common";
import {
    NgbPagination,
    NgbPaginationFirst,
    NgbPaginationLast,
    NgbPaginationNext,
    NgbPaginationPrevious
} from "@ng-bootstrap/ng-bootstrap";
import {ReactiveFormsModule} from "@angular/forms";
import {Paging} from "../../../models/paging";
import {HttpEventType, HttpResponse} from "@angular/common/http";
import {Part} from "../../../models/Part";
import {UploadService} from "../../../services/upload.service";

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
        NgClass,
        JsonPipe,
        DatePipe
    ],
    styleUrls: ['./sequence-analysis.component.css']
})
export class SequenceAnalysisComponent {

    paging: Paging = new Paging('created');
    pageNumber: number;
    pageCount: number;
    part: Part;

    // results
    shotgunSequences: any[] = [];

    constructor(private http: HttpService, private route: ActivatedRoute, private local: LocalStorageService,
                private router: Router, private upload: UploadService) {

        route.url.subscribe(() => {
            console.log(route.snapshot.url[0].path);
        });

        this.route.parent.data.subscribe((data) => {
            this.part = data.entry;
        });

        this.pageShotgunSequences();
    }

    pageShotgunSequences(): void {
        if (!this.part)
            return;

        this.http.get('parts/' + this.part.id + '/shotgunsequences').subscribe((result: any[]) => {
            console.log(result);
            this.shotgunSequences = result;
        })

    }

    pageChange(page: number): void {
        this.paging.offset = ((page - 1) * this.paging.limit);
    }

    onFileChange(event: any, isTraces = false) {
        let files: FileList = event.target.files;
        if (files.length === 0) {
            console.log('No file selected!');
            return;
        }

        //
        const seqUrl = isTraces ? "traces" : "shotgunsequences";
        const url = '/parts/' + this.part.id + '/' + seqUrl;
        this.upload.uploadFile(url, files)
            .subscribe(event => {
                if (event.type === HttpEventType.UploadProgress) {
                } else if (event instanceof HttpResponse) {
                    const data: any = event.body; // {id, runTime,completionTime, runId, resultFileName}
                    console.log('server response', data);
                }
            });
    }

    deleteShotgun(id: string): void {
// delete to /parts/{{partId}}/shotgunsequences/{{id}}
    }
}
