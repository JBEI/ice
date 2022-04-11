import {User} from './User';

export class Permission {
    public id: number;
    public account: User;
    public canWrite: boolean;
    public article: string;
    public articleId: number;
    public canEdit: boolean;
    remote: boolean;
}
