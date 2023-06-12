import {User} from "./User";

export class BulkUpload {
    id: number;
    name: string;
    type: string;
    created: number;
    count: number;
    account: User;
    status: string;
    file: string;
}
