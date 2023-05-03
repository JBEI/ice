import {Component, Input} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpService} from "../../../services/http.service";
import {UserService} from "../../../services/user.service";
import {User} from "../../../models/User";
import {Folder} from "../../../models/folder";
import {PartSelectionService} from "../../../services/part-selection.service";

@Component({
    selector: 'app-add-to-folder-modal',
    templateUrl: './add-to-folder-modal.component.html',
    styleUrls: ['./add-to-folder-modal.component.css']
})
export class AddToFolderModalComponent {

    @Input() sourceFolder: number;

    userFolders: Folder[];
    selectedFolders: Folder[];
    term: string;

    constructor(private http: HttpService, private users: UserService, public activeModal: NgbActiveModal,
                private selection: PartSelectionService) {
        this.getUserFolders();
        this.selectedFolders = [];
    }

    getUserFolders(): void {
        const user: User = this.users.getUser();
        if (!user)
            return;

        this.http.get('users/' + user.id + '/folders').subscribe((result: Folder[]) => {
            this.userFolders = result;
        });
    }

    submitAddToFolder(): void {
        this.http.put('folders/entries', {
            entries: this.selection.selectedParts,
            destination: this.selectedFolders
        }).subscribe(result => {
            console.log(result);
        });
    }

    selectDeselectFolder(folder: Folder): void {
        const index = this.selectedFolders.indexOf(folder);
        if (index === -1)
            this.selectedFolders.push(folder);
        else
            this.selectedFolders.splice(index, 1);
    }
}
