import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute} from "@angular/router";
import {User} from "../../../models/User";

@Component({
    selector: 'app-general',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './general.component.html',
    styleUrls: ['./general.component.css']
})
export class GeneralComponent {

    user: User;

    constructor(private route: ActivatedRoute) {
        this.route.parent.data.subscribe((data) => {
            this.user = data.profile;
        });
    }
}
