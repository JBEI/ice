import {Part} from './Part';

export class Bin {
    public id: string;
    public position: number;    // bin position in the collection
    public name: string;
    public iconID: string;
    public directionForward: boolean;
    public dsf = false; // direct synthesis firewall
    public fro: string; // forcedRelative overhang
    public extra3PrimeBps: string;
    public extra5PrimeBps: string;
    public fas: string;  // forced assembly strategy
    public parts: Part[];
    public itemCount: number;
    public editExtra3PrimeBps: boolean;
    public editFro: boolean;
    public editExtra5PrimeBps: boolean;
    nonUniqueName: boolean;
    edit: boolean;
}
