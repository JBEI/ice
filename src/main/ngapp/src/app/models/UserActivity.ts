import {User} from './User';
import {Design} from './Design';

export class UserActivity {
    user: User;
    design: Design;
    state: string;
    creationTime?: number;
}
