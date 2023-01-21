import {DnaFeature} from "./dna-feature";

export class Sequence {
    sequence: string;
    name: string;
    identifier: string;
    canEdit: boolean;
    features: DnaFeature[];
    genbankStartBP: number;
    endBP: number;
}
