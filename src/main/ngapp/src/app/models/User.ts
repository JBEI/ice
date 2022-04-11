export class User {
    id?: number;
    firstName: string;
    lastName: string;
    email: string;
    password?: string;
    newPassword?: string;
    sessionId?: string;
    commercial: boolean;
    roles: string[];
    public creationTime: string;
    public lastUpdateTime: string;
    public lastLoginTime: string;
    public j5Enabled: boolean;
    public disabled: boolean;
    public description: string;
    updatingActiveStatus?: boolean;
    updatingType?: boolean;
    updatingJ5Status?: boolean;
    allowedToChangePassword: boolean;
    usingTemporaryPassword: boolean;
    type?: string;
}
