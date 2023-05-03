import {CustomField} from "./custom-field";

export class Part {
    id: number;
    creationTime: number;
    status: string;
    viewCount: number;
    type: string;
    partId: string;
    recordId: string;
    visible: string;
    hasSample: boolean;
    hasSequence: boolean;
    owner: string;
    ownerEmail: string;
    ownerId: number;
    canEdit: boolean;

    linkedParts: Part[];
    parents: Part[];
    fields: CustomField[];
}
