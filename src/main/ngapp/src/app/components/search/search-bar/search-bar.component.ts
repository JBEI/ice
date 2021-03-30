import {Component, OnInit} from '@angular/core';
import {SearchService} from "../search.service";
import {Router} from "@angular/router";

@Component({
    selector: 'app-search-bar',
    templateUrl: './search-bar.component.html',
    styleUrls: ['./search-bar.component.css']
})
export class SearchBarComponent implements OnInit {

    terms: string;

    constructor(private search: SearchService, private router: Router) {
    }

    ngOnInit(): void {
        this.terms = this.search.getQuery();
    }

    submitSearch(): void {
        console.log(this.terms);
        this.search.setQuery(this.terms);
        this.router.navigate(['/search']);
    }
}
