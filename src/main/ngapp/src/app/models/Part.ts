import {Sequence} from './Sequence';

export class Part {
    id: string;
    position: number;
    name: string;
    hasRules: boolean;
    partDataId: string;
    fas: string;
    partId: number;
    fivePrimeInternalPreferredOverhangs: string;
    threePrimeInternalPreferredOverhangs: string;
    genbankStartBP: number;
    endBP: number;
    revComp: boolean;
    sequence: Sequence;

    showEdit: boolean;
    editName: boolean;
    rules: any[];

    sequenceID: number;
}
