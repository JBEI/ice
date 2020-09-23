import {Design} from './Design';

export class Construct {
    id: number;
    name: string;
    state: string;
    summary: string;
    strainSummary: string;
    selectionMarkers: string;
    strainSelectionMarkers: string;
    runId: string;
    registryUrl: string;
    design: Design;
    history?: History[];
}
