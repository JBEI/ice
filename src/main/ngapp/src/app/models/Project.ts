import {Design} from './Design';
import {Group} from './Group';
import {User} from './User';

export class Project {
    public id: number;
    public name: string;
    public description: string;
    public canEdit: boolean;
    public creationTime: number;
    public lastUpdateTime: number;
    public designCount: number;
    public collaboratorCount: number;
    public designs: Design[];
    public principalInvestigators?: User[];
    public divaTeam: Group;

    constructor() {
        this.name = '';
        this.description = '';
    }
}
