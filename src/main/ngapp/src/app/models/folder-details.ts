import {User} from "./User";
import {Part} from "./Part";

export class FolderDetails {
    id: number;
    folderName: string;
    count: number;
    description: number;
    propagatePermission: boolean;
    type: string;
    owner: User;    // owner or person sharing this folder
    publicReadAccess: boolean;
    canEdit: boolean;
    parent: FolderDetails;
    //  RegistryPartner remotePartner;
    //  SampleRequest sampleRequest;
    entries: Part[];
    contentTypes: string[];
}
