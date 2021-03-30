import {CustomField} from "./custom-field";

export class Part {
    id: number;
    name: string;
    alias: string;
    creationTime: number;
    status: string;
    viewCount: number;
    type: string;
    shortDescription: string;
    partId: string;
    visible: string;
    hasSample: boolean;
    hasSequence: boolean;
    owner: string;
    ownerEmail: string;
    ownerId: number;
    canEdit: boolean;
    longDescription: string;

    linkedParts: Part[];
    parents: Part[];
    customFields: CustomField[];
}
