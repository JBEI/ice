export class Entry {
    alias: string;
    accessPermissions: [];
    bioSafetyLevel: number;
    canEdit: boolean;
    creationTime: number;
    creator: string;
    creatorEmail: string;
    creatorId: number;
    customFields: [];
    featureCount: number;
    hasAttachment: boolean;
    hasOriginalSequence: boolean;
    hasSample: boolean;
    hasSequence: boolean;
    id: number;
    index: number;
    linkedParts: [];
    modificationTime: number;
    name: string;

    owner: string;
    ownerEmail: string;
    ownerId: number;

    parents: [];
    partId: string;
    principalInvestigatorId: number;
    publicRead: boolean;
    recordId: string;
    shortDescription: string;
    longDescription: string;
    status: string;
    type: string;
    viewCount: number;
    keywords: string;
    references: string;
}
