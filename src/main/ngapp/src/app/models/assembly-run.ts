import {User} from './User';
import {Design} from './Design';

export class AssemblyRun {
    id: number;
    runTime: number;
    completionTime: number;
    runId: string;
    taskId: string;
    resultFileName: string;
    assemblyMethod: string;
    status: string;
    account: User;
    design: Design;
    errorMsg: string;
}
