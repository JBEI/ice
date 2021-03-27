import {Part} from "./Part";
import {User} from "./User";
import {RegistryPartner} from "./registry-partner";

export class Folder {
    id: number;
    type: string;
    folderName: string;
    description: string;
    creationTime: number;
    count: number;
    resultCount: number
    canEdit: boolean;
    publicReadAccess: boolean;
    owner: User;
    remotePartner: RegistryPartner;

    entries: Part[];
    accessPermissions: any[];
}
