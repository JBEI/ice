import {Injectable} from '@angular/core';
import {HttpService} from "./http.service";
import {User} from "../models/User";
import {Folder} from "../models/folder";
import {UserService} from "./user.service";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class FolderService {

    constructor(private http: HttpService, private users: UserService) {
    }

    getUserFolders(): Observable<Folder[]> {
        const user: User = this.users.getUser();
        if (!user)
            return null;

        return this.http.get('users/' + user.id + '/folders');
        // .subscribe((result: Folder[]) => {
        //     this.userFolders = result;
        // });
    }
}
