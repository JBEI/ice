import {User} from "./User";
import {Part} from "./Part";

export class Folder {
    id: number;
    folderName: string;
    description: string;
    count: number;
    canEdit: boolean;
    creationTime: number;

    type: string;
    owner: User;    // owner or person sharing this folder
    publicReadAccess: boolean;
    parent: Folder;
    entries: Part[];
}
