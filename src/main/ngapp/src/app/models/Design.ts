import {Project} from './Project';
import {Bin} from './Bin';
import {History} from './History';
import {User} from './User';

export class Design {
    id: number;
    name: string;
    circular: boolean;
    description: string;
    state: string;
    onePerRowLayout: boolean;
    projects: Project[];
    bins: Bin[];
    history: History[];
    owner: User;
    lastUpdateTime: number;
    canMakeDIVATeamChanges: boolean;
    readOnly: boolean;
    creationTime: boolean;
    constructCount: number;
    assemblyRunCount: number;
    isDivaTeam?: boolean;
    isPI?: boolean;
    hasNonMockJ5Run?: boolean;
    canEdit?: boolean;
    hodgepodgeInfo?: any;

    constructor() {
        this.name = '';
        this.description = '';
    }
}
