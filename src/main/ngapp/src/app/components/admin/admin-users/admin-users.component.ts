import {Component} from '@angular/core';
import {HttpService} from "../../../services/http.service";
import {Paging} from "../../../models/paging";
import {Result} from "../../../models/result";
import {User} from "../../../models/User";

@Component({
    selector: 'app-admin-users',
    templateUrl: './admin-users.component.html',
    styleUrls: ['./admin-users.component.css']
})
export class AdminUsersComponent {

    paging: Paging;
    users: User[];
    filter: string;

    constructor(private http: HttpService) {
        this.paging = new Paging();
        this.filter = '';
        this.fetchUsers();
    }

    fetchUsers(): void {
        this.paging.filter = this.filter;
        this.http.get('users', this.paging).subscribe({
            next: (result: Result<User>) => {
                console.log(this.filter, this.paging.filter, this.filter !== this.paging.filter);
                if (this.filter && this.filter !== this.paging.filter)
                    return;

                console.log(result);
                this.paging.available = result.resultCount;
                this.users = result.data;
            }
        })
    }

    pageChange(page: number): void {
        this.paging.offset = ((page - 1) * this.paging.limit);
        this.fetchUsers();
    }

}
