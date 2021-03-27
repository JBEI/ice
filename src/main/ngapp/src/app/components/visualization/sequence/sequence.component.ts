import {Component, Input, OnInit} from '@angular/core';
import {Sequence} from '../../../models/Sequence';
import {VectorEditorService} from "./vector-editor.service";
import {Part} from "../../../models/Part";
import {HttpService} from "../../../services/http.service";

@Component({
    selector: 'app-sequence',
    templateUrl: './sequence.component.html',
    styleUrls: ['./sequence.component.css']
})

export class SequenceComponent implements OnInit {

    data: any;
    editor: any;
    @Input() part: Part;
    @Input() sequence: Sequence;

    constructor(private vectorEditor: VectorEditorService, private http: HttpService) {
    }

    ngOnInit(): void {
        if (this.sequence) {
            this.showSequenceVisualization(this.sequence);
        } else {
            // if ($scope.remote && $scope.remote.folderId) {
            //     url = "rest/parts/" + entryId + "/sequence?remote=true&folderId=" + $scope.remote.folderId;
            //     console.log("loading shared sequence for " + entryId + " remote: " + $scope.remote);
            // } else if ($scope.remote && $scope.remote.partner) {
            //     url = "rest/web/" + $scope.remote.partner + "/entries/" + entryId + "/sequence";
            //     console.log("loading remote sequence: " + $scope.remote);
            // } else {
            //     url = "rest/parts/" + entryId + "/sequence";
            //     console.log("loading local sequence for entry " + entryId);
            // }

            this.http.get('parts/' + this.part.id + '/sequence').subscribe(result => {
                this.showSequenceVisualization(result);
            });
        }
    }

    showSequenceVisualization(sequence): void {
        this.data = {
            sequenceData: {
                sequence: sequence.sequence,
                features: this.vectorEditor.convertFeaturesToTSModel(sequence.features),
                name: sequence.name,
                circular: true,
            }
        };

        let root = 'preview-root';
        this.editor = (window as any).createVectorEditor(document.getElementById(root), {
            readOnly: true,
            doNotUseAbsolutePosition: true,
            onSelectionOrCaretChanged: (selection) => {
                // todo : propagate this
                // if (selection.selectionLayer.start !== -1) {
                //     this.part.genbankStartBP = selection.selectionLayer.start + 1;
                // }
                //
                // if (selection.selectionLayer.end !== -1)
                //     this.part.endBP = selection.selectionLayer.end + 1;
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
                start: -1,
                end: -1
            },
        });
    }
}
