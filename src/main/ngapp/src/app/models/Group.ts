import {User} from "./User";

export class Group {
    public id?: number;
    public display?: string;
    public description?: string;
    public created?: number;
    public type: string;
    public memberCount?: number;
    members?: User[];
    owner?: User;
}
