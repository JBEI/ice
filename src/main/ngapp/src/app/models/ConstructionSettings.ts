export class ConstructionSettings {

    reuseMasterDirectSynthesis: boolean;
    reuseMasterOligos: boolean;
    reuseMasterPlasmids: boolean;
    reuseDownstreamAutomationParameters: boolean;
    masterPlasmidsListFilename: string;
    userRole?: string;
    masterOligosListFilename: string;
    masterDirectSynthesisListFilename: string;
    assemblyMethod: string;
    assemblyFilesList: string;
    automationTask: string;
    runId: string;

    downStreamAutomationParameters: { fieldName: string, value: string, defaultValue: string, description: string };

    constructor() {
        this.reuseMasterDirectSynthesis = false;
        this.reuseMasterOligos = false;
        this.reuseMasterPlasmids = false;
        this.masterPlasmidsListFilename = 'j5_plasmids.csv';
        this.reuseDownstreamAutomationParameters = false;

        this.masterOligosListFilename = 'j5_oligos.csv';
        this.masterDirectSynthesisListFilename = 'j5_directsyntheses.csv';
        this.assemblyMethod = 'Mock';
    }
}
