import {User} from './User';
import {Project} from './Project';
import {Design} from './Design';

export class History {

    id: number;
    user: User;  // user carrying out action. null if a system action
    project: Project;
    design: Design;
    creationTime: number;
    comment: string;
    state: string;
}
