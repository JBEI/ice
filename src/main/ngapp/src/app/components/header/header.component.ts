import {Component, OnInit} from '@angular/core';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

    searchTypes = {all: true, strain: true, plasmid: true, part: true, seed: true, protein: true};
    fieldFilters = [];
    blastSearchType = '';
    hasAttachment = false;
    hasSequence = false;
    hasSample = false;
    bioSafetyLevelOption: string;
    sequenceText: string;
    searchFilters: any;
    queryText: string;

    constructor() {
    }

    ngOnInit(): void {
    }


    addFieldFilter(): void {
        this.fieldFilters.push({field: "", filter: ""});
    };

    removeFieldFilter(index): void {
        this.fieldFilters.splice(1, index);
    };

    check(selection): void {
        let allTrue = true;
        for (let type in this.searchTypes) {
            if (this.searchTypes.hasOwnProperty(type) && type !== 'all') {
                if (selection === 'all')
                    this.searchTypes[type] = this.searchTypes.all;
                allTrue = (allTrue && this.searchTypes[type] === true);
            }
        }
        this.searchTypes.all = allTrue;
    };

    defineQuery(): any {
        const searchQuery = {
            queryString: '',
            entryTypes: [],
            bioSafetyOption: '',
            parameters: {
                start: 0,
                retrieveCount: 30,
                sortField: "RELEVANCE",
                hasSequence: false,
                hasSample: false,
                hasAttachment: false
            },
            blastQuery: {blastProgram: '', sequence: ''}
        };

        // check search types  : {all: false, strain: true, plasmid: false, part: true, seed: true}
        for (let type in this.searchTypes) {
            if (this.searchTypes.hasOwnProperty(type) && type !== 'all') {
                if (this.searchTypes[type]) {
                    searchQuery.entryTypes.push(type.toUpperCase());
                }
            }
        }

        // check blast search type
        if (this.blastSearchType) {
            searchQuery.blastQuery.blastProgram = this.blastSearchType;
        }

        // check "has ..."
        if (this.hasAttachment)
            searchQuery.parameters.hasAttachment = this.hasAttachment;

        if (this.hasSample)
            searchQuery.parameters.hasSample = this.hasSample;

        if (this.hasSequence)
            searchQuery.parameters.hasSequence = this.hasSequence;

        // bio safety
        if (this.bioSafetyLevelOption) {
            switch (this.bioSafetyLevelOption) {
                case "1":
                default:
                    searchQuery.bioSafetyOption = "LEVEL_ONE";
                    break;

                case "2":
                    searchQuery.bioSafetyOption = "LEVEL_TWO";
                    break;

                case "-1":
                    searchQuery.bioSafetyOption = "RESTRICTED";
                    break;
            }
        }
        // todo include above with fieldFilters
        // searchQuery.fieldFilters = fieldFilters;

        //sequence
        if (this.sequenceText) {
            searchQuery.blastQuery.sequence = this.sequenceText;
            if (!searchQuery.blastQuery.blastProgram)
                searchQuery.blastQuery.blastProgram = "BLAST_N";
        }

        searchQuery.queryString = this.queryText;
        return searchQuery;
    };

    search(isWebSearch: boolean) : void {
        this.searchFilters = this.defineQuery();
        // this.searchFilters.webSearch = isWebSearch;
        //
        // var searchUrl = "/search";
        // if ($location.path().slice(0, searchUrl.length) != searchUrl) {
        //     // triggers search controller which uses search filters to perform search
        //     $location.path(searchUrl, false);
        // } else {
        //     this.$broadcast("RunSearch", this.searchFilters);
        // }
        // this.advancedMenu.isOpen = false;
    };

    isWebSearch(): boolean {
        return this.searchFilters.webSearch === true;
    };

    // this.advancedMenu = {
    //     isOpen: false
    // };

    toggleAdvancedMenuDropdown($event) : void {
        $event.preventDefault();
        $event.stopPropagation();
        // this.advancedMenu.isOpen = !this.advancedMenu.isOpen;
    };

    canReset() : boolean {
        if (this.queryText || this.sequenceText || this.hasSample || this.hasSequence || this.hasAttachment)
            return true;

        if (this.blastSearchType || this.bioSafetyLevelOption)
            return true;

        for (var searchType in this.searchTypes) {
            if (this.searchTypes.hasOwnProperty(searchType)) {
                if (this.searchTypes[searchType] != true)
                    return true;
            }
        }

        return false;
        // return this.fieldFilters.length;
    };

    //
    // resets the search filters to the defaults setting
    //
    reset() : void {
        this.sequenceText = "";
        this.queryText = "";
        this.fieldFilters = [];
        // $location.url($location.path());
        this.blastSearchType = "";
        this.bioSafetyLevelOption = "";
        this.hasSample = false;
        this.hasSequence = false;
        this.hasAttachment = false;
        for (var searchType in this.searchTypes) {
            if (this.searchTypes.hasOwnProperty(searchType)) {
                this.searchTypes[searchType] = true;
            }
        }
    };

    sortResults(sortType): void {
        console.log("sort", sortType);
        sortType = sortType.toUpperCase();

        if (!this.searchFilters.parameters) {
            this.searchFilters.parameters = {sortAscending: false};
        } else {
            if (sortType === this.searchFilters.parameters.sortField) {
                this.searchFilters.parameters.sortAscending = !this.searchFilters.parameters.sortAscending;
            } else
                this.searchFilters.parameters.sortAscending = false;
        }

        this.searchFilters.parameters.sortField = sortType;
        this.searchFilters.parameters.start = 0;
        // this.loadingSearchResults = true;
        // this.$broadcast("RunSearch", this.searchFilters);
    };
}