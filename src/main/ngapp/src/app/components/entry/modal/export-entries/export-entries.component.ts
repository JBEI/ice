import {Component, Input} from '@angular/core';
import {Folder} from "../../../../models/folder";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpService} from "../../../../services/http.service";
import {RegistryPartner} from "../../../../models/registry-partner";
import {WebOfRegistries} from "../../../../models/web-of-registries";

@Component({
    selector: 'app-export-entries',
    templateUrl: './export-entries.component.html',
    styleUrls: ['./export-entries.component.css']
})
export class ExportEntriesComponent {

    @Input() folder: Folder;
    registryPartners: WebOfRegistries;
    selectedPartner: any;

    constructor(public activeModal: NgbActiveModal, private http: HttpService) {
        this.retrieveRegistryPartners();
    }

    retrieveRegistryPartners(): void {
        this.http.get("web").subscribe({
            next: (result: any) => {
                this.registryPartners = result;
            }
        });
    };

    transferEntriesToRegistry(): void {
        const entrySelection = {all: true, selectionType: 'FOLDER', folderId: this.folder.id};
        this.http.post('partners/' + this.selectedPartner.id + '/entries', entrySelection).subscribe({
            next: (result: any) => {
                this.activeModal.close(result);
            }
        });
    };

    selectPartnerForTransfer(partner: RegistryPartner): void {
        partner.selected = !partner.selected;
        this.selectedPartner = partner;
    };
}
