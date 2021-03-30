import {Component, Input, OnInit} from '@angular/core';
import {Part} from "../../../models/Part";
import {VectorEditorService} from "../../visualization/sequence/vector-editor.service";
import {Sequence} from "../../../models/Sequence";
import {PartField} from "../../../models/part-field";
import {PartInformationService} from "./part-information.service";

@Component({
    selector: 'app-part-general-information',
    templateUrl: './part-general-information.component.html',
    styleUrls: ['./part-general-information.component.css']
})
export class PartGeneralInformationComponent implements OnInit {

    @Input() part: Part;
    data: any;
    editor: any;
    partFields: PartField[];

    shortFields: PartField[];
    longFields: PartField[];

    constructor(private vectorEditor: VectorEditorService, private partService: PartInformationService) {
    }

    ngOnInit(): void {
        this.shortFields = [];
        this.longFields = [];

        switch (this.part.type) {
            case 'PLASMID':
                this.partFields = this.partService.getPlasmidFields(this.part.customFields);
                break;

            case 'STRAIN':
                this.partFields = this.partService.getStrainFields(this.part.customFields);
                break;

            case 'PROTEIN':
                this.partFields = this.partService.getProteinFields(this.part.customFields);
                break;

            case 'SEED':
                this.partFields = this.partService.getSeedFields(this.part.customFields);
                break;

            default:
            case 'PART':
                this.partFields = this.partService.getPartFields()
                break;
        }

        if (this.partFields) {
            for (let i = 0; i < this.partFields.length; i += 1) {
                const field = this.partFields[i];

                // these fields are specifically named out and displayed in ui
                if (field.schema === 'name' || field.schema === 'shortDescription')
                    continue;

                if (field.subSchema) {
                    field.value = this.part[field.subSchema][field.schema];
                } else {
                    field.value = this.part[field.schema];
                }

                if (!field.value || (Array.isArray(field.value) && !field.value.length)) {
                    console.log('skipping', field);
                    continue;
                }

                switch (field.inputType) {
                    default:
                    case 'short':
                    case 'medium':
                        this.shortFields.push(field);
                        break;

                    // case 'add':
                    //     if (this.part[field.schema] && this.part[field.schema].length)
                    //         this.shortFields.push(field);
                    //     break;

                    case 'long':
                        this.longFields.push(field);
                        break;
                }
            }
        }

        // add custom fields
        for (let customField of this.part.customFields) {
            if (!customField.value || customField.fieldType === 'EXISTING')
                continue

            const partField: PartField = new PartField(customField.label);
            partField.value = customField.value;
            this.shortFields.push(partField);
        }
    }

    showSequenceVisualization(sequence: Sequence): void {
        this.data = {
            sequenceData: {
                sequence: sequence.sequence,
                features: this.vectorEditor.convertFeaturesToTSModel(sequence.features),
                name: sequence.name,
                circular: true,
            }
        };

        this.editor = (window as any).createVectorEditor(document.getElementById('part-preview-root'), {
            readOnly: true,
            doNotUseAbsolutePosition: true,
            onSelectionOrCaretChanged: (selection) => {
                if (selection.selectionLayer.start !== -1) {
                    sequence.genbankStartBP = selection.selectionLayer.start + 1;
                }

                if (selection.selectionLayer.end !== -1) {
                    sequence.endBP = selection.selectionLayer.end + 1;
                }
            },
            PropertiesProps: {propertiesList: this.vectorEditor.propertiesList()},
            ToolBarProps: {toolList: this.vectorEditor.toolList()}
        });

        this.editor.updateEditor({
            readOnly: true,
            sequenceData: this.data.sequenceData,
            annotationVisibility: {parts: false, orfs: false, cutsites: false, translations: false},
            panelsShown: this.vectorEditor.panelsList(),
            selectionLayer: {
                start: sequence.genbankStartBP - 1,
                end: sequence.endBP - 1
            },
        });
    }
}
