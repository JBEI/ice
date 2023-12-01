import {Component} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {Group} from "../../../models/group";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Paging} from "../../../models/paging";

@Component({
    selector: 'app-admin-public-groups',
    templateUrl: './admin-public-groups.component.html',
    styleUrls: ['./admin-public-groups.component.css']
})
export class AdminPublicGroupsComponent {

    groups = undefined;
    group: Group;
    loadingPage: boolean;
    paging: Paging;
    pageNumber: number;
    pageCount: number;

    adminGroupsPagingParams = {
        offset: 0,
        limit: 10,
        available: 0,
        currentPage: 1,
        maxSize: 5,
        type: 'PUBLIC'
    };

    constructor(private http: HttpService, private modalService: NgbModal) {
        this.groupListPageChanged();
        this.paging = new Paging();
    }

    groupListPageChanged(): void {
        this.http.get("rest/groups", this.adminGroupsPagingParams).subscribe({
            next: (result: any) => {
                this.groups = result.data;
                this.adminGroupsPagingParams.available = result.resultCount;
            }
        })
    };

    pageChange(page: number): void {
        this.paging.offset = ((page - 1) * this.paging.limit);
    }


    openCreatePublicGroupModal(group: Group): void {
        //
        //     const modalInstance = this.modalService.open();
        //     modalInstance.result.then(function (result) {
        //         if (!result)
        //             return;
        //
        //         var msg = "Group successfully ";
        //         if (group && group.id)
        //             msg += "updated";
        //         else
        //             msg += "created";
        //         // Util.setFeedback(msg, "success");
        //         this.groupListPageChanged();
        //     })
    };

    deletePublicGroup(group: Group): void {
        this.http.delete("rest/groups/" + group.id).subscribe({
            next: () => {
                const i = this.groups.indexOf(group);
                if (i !== -1)
                    this.groups.splice(i, 1);
            }
        });
    }
}
