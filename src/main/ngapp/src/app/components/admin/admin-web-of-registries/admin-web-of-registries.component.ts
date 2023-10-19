import {Component} from '@angular/core';
import {HttpService} from "../../../services/http.service";

@Component({
    selector: 'app-admin-web-of-registries',
    templateUrl: './admin-web-of-registries.component.html',
    styleUrls: ['./admin-web-of-registries.component.css']
})
export class AdminWebOfRegistriesComponent {

    newPartner = undefined;
    partner: any;
    partnerStatusList = [
        {status: 'BLOCKED', action: 'Block'},
        {status: 'APPROVED', action: 'Approve'}
    ];

    //
    // retrieve web of registries partners
    //
    wor = {partners: []};
    isWorEnabled = false;
    restrictPublic = false;
    settings = {}

    constructor(private http: HttpService) {
        this.checkIsWorEnable();
    }

    checkIsWorEnable(): void {
        this.http.get('config/JOIN_WEB_OF_REGISTRIES').subscribe({
            next: (result: any) => {
                var joined = result.value === 'yes';
                this.isWorEnabled = joined;
                if (!this.settings)
                    this.settings = {};
                this.settings['JOIN_WEB_OF_REGISTRIES'] = joined;
            }
        });

        this.http.get("web").subscribe({
            next: (result: any) => {
                this.wor = result;
            }
        });

        // get admin only setting
        this.http.get("config/RESTRICT_PUBLIC_ENABLE").subscribe({
            next: (result: any) => {
                this.restrictPublic = (result.value.toLowerCase() == "yes");

            }
        });

    }


    restrictPublicEnable() {
        this.restrictPublic = !this.restrictPublic;
        var setting = {key: 'RESTRICT_PUBLIC_ENABLE', value: this.restrictPublic ? "yes" : "no"};
        this.http.put("config", setting, {}).subscribe();
        // {next: (result:any) => {
        //         console.log(result);
        //
        //     }});
    };

//
// enable or disable web of registries functionality
//
    enableDisableWor() {
        var value = this.isWorEnabled ? 'no' : 'yes';
        this.http.put("config", {key: 'JOIN_WEB_OF_REGISTRIES', value: value}, {}).subscribe({
            next: (result: any) => {
                var joined = result.value === 'yes';
                this.isWorEnabled = joined;
                if (!this.settings)
                    this.settings = {};
                this.settings['JOIN_WEB_OF_REGISTRIES'] = joined;
            }
        })
    };

//
// add remote partner to web of registries
//
    addWebPartner() {
        this.http.post("partners", this.newPartner).subscribe({
            next: (result: any) => {
                // if (!result) {
                // Util.setFeedback('Error adding web partner', 'danger');
                // return;
                // this.showAddRegistryForm = false;
                this.newPartner = undefined;

                this.http.get("web", {approved_only: false}).subscribe({
                    next: (result: any) => {
                        this.wor = result;
                    }
                });

            }
        });
    };

// update the keys
    refreshPartner(partner) {
        partner.refreshing = true;
        this.http.put("partners/" + partner.id + "/apiKey", {}, {}).subscribe({
            next: () => {
            }
        })
    };

// remove web of registries partner
    removePartner(partner) {
        this.http.delete("partners/" + partner.id).subscribe({
            next: () => {
                const index = this.wor.partners.indexOf(partner);
                if (index >= 0)
                    this.wor.partners.splice(index, 1);
            }
        });
    };

// set the status of a partner
    setPartnerStatus(partner, newStatus) {
        if (partner.status == newStatus)
            return;

        this.http.put("partners/" + partner.id, {status: newStatus}, {}).subscribe({
            next: (result: any) => {
                partner = result;
            }
        })
    };

    retryRemotePartnerContact() {
        // todo
    }
}
