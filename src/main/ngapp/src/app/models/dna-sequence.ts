import {DnaFeature} from './dna-feature';

export class DnaSequence {
    features: DnaFeature[];
    identifier: string;
    isCircular: boolean;
    description: string;
    uri: string;
    dcUri: string;
    canEdit: boolean;
    sequence: string;
    name: string;

    constructor() {
        this.features = [];
    }
}
