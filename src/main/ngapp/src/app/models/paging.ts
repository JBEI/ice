export class Paging {
    currentPage: number;
    available: number;
    asc: boolean;
    limit: number;
    offset: number;
    index?: number;
    filter: string;
    processing?: boolean;
    type?: string;
    sort: string;

    constructor(sort?: string) {
        this.currentPage = 1;
        this.available = 0;
        this.limit = 10;
        this.offset = 0;
        this.filter = '';
        this.processing = false;
        if (!sort)
            this.sort = 'id';
        else
            this.sort = sort;
        this.asc = false;
    }
}
