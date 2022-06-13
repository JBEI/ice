import {Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class EntryService {

    private types: string[] = ['Plasmid', 'Strain', 'Part', 'Seed', 'Protein'];

    constructor() {
    }

    getEntryTypes(): string[] {
        return this.types;
    }
}
