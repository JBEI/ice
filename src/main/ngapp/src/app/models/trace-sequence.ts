import {User} from "./User";

export class TraceSequence {
    id: number;
    filename: string;
    depositor: User;
    created: number;
    fileId: string;
    sequence: string;
    canEdit: boolean;
}
