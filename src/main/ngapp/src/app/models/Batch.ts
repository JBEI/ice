import {User} from './User';

export class Batch {
    id: number;
    label: string;
    description: string;
    created: number;
    updated: number;
    owner: User;
    constructCount: number;
    designCount: number;
    state: string;

    constructor() {
        this.label = '';
        this.description = '';
    }
}
