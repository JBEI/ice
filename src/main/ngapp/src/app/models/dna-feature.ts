import {DnaFeatureLocation} from './dna-feature-location';

export class DnaFeature {
    type: string;
    name: string;
    strand = 1;
    annotationType: string;
    uri: string;
    identifier: string;
    locations: DnaFeatureLocation[];

    constructor() {
        this.locations = [];
    }
}
