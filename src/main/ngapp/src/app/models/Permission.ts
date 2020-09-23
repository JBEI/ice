import {User} from './User';
import {Group} from './Group';
import {Project} from './Project';

export class Permission {
    public id: number;
    public account: User;
    public group?: Group;
    public project?: Project;
    public canWrite: boolean;
    public article: string;
    public articleId: number;
    public isPrincipalInvestigator: boolean;
    public canEdit: boolean;
    remote: boolean;
}
