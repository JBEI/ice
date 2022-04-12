import {DnaFeatureLocation} from './dna-feature-location';
import {DnaFeatureNote} from "./dna-feature-note";

export class DnaFeature {
    id: number;
    type: string;
    name: string;
    strand = 1;
    annotationType: string;
    uri: string;
    identifier: string;
    locations: DnaFeatureLocation[];
    notes: DnaFeatureNote[];

    constructor() {
        this.locations = [];
    }
}
