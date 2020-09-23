import {DnaSequence} from './dna-sequence';

export class SequenceFile {
    id: string;                 // unique identifier for sequence (usually a hash)
    format: string;             // one of GENBANK, FASTA, JBEI_SEQ, JSON_STRING
    content: string;            // file contents
    fileName: string;
    partSource: string;
    icePartId: string;
    iceEntryURI: string;
    hash: string;
    isProtein: boolean;
    bpLength: number;          // length of base pairs
    sequence: DnaSequence;
}
