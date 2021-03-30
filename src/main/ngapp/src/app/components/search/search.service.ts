import {Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})

export class SearchService {

    private query: string;

    setQuery(q: string) {
        this.query = q;
    }

    getQuery(): string {
        return this.query;
    }

}