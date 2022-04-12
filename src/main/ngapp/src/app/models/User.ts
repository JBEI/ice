export class User {
    id?: number;
    userId?: string;
    firstName: string | undefined;
    lastName: string | undefined;
    email: string | undefined;
    password?: string;
    newPassword?: string;
    sessionId?: string;
    public creationTime: string | undefined;
    public lastUpdateTime: string | undefined;
    public lastLoginTime: string | undefined;
    public disabled: boolean | undefined;
    public description: string | undefined;
    updatingActiveStatus?: boolean;
    usingTemporaryPassword: boolean;
    updatingType?: boolean;
    updatingJ5Status?: boolean;
    type?: string;

    constructor() {
        this.usingTemporaryPassword = false;
    }
}
