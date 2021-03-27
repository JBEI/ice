import {Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class VectorEditorService {

    constructor() {
    }

    toolList(): string[] {
        return [
            'cutsiteTool',
            'featureTool',
            'orfTool',
            'findTool',
            'visibilityTool'
        ];
    }

    propertiesList(): string [] {
        return [
            'features',
            'translations',
            'cutsites',
            'orfs'
        ];
    }

    panelsList(): any {
        return [
            [
                {id: 'circular', name: 'Plasmid', active: true},
                {id: 'rail', name: 'Linear Map', active: false},
                {id: 'sequence', name: 'Sequence Map', active: false}
            ]
        ];
    }

    // converts the JBEI features list to a model for visualization in openVE
    convertFeaturesToTSModel(features: any[]): any[] {
        const openVEFeatures = [];
        if (!features || !features.length) {
            return openVEFeatures;
        }

        for (const feature of features) {
            if (!feature.locations.length) {
                continue;
            }

            const location = feature.locations[0];
            const notes = feature.notes.length ? feature.notes[0].value : '';

            openVEFeatures.push({
                start: location.genbankStart - 1,
                end: location.end - 1,
                fid: feature.id,
                forward: feature.strand === 1,
                type: feature.type,
                name: feature.name,
                notes,
                annotationType: feature.type,
            });
        }

        return openVEFeatures;
    }
}
