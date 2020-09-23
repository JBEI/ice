export class Paging {
    currentPage: number;
    available: number;
    asc: boolean;
    limit: number;
    start: number;
    index?: number;
    filterText: string;
    processing?: boolean;
    type?: string;
    sort: string;

    constructor() {
        this.currentPage = 1;
        this.available = 0;
        this.limit = 10;
        this.start = 0;
        this.filterText = '';
        this.processing = false;
        this.sort = 'id';
        this.asc = false;
    }
}
