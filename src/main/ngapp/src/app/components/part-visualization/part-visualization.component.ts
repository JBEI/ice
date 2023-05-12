import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {VectorEditorService} from './vector-editor.service';
import {HttpService} from "../../services/http.service";
import {Paging} from "../../models/paging";
import {Sequence} from "../../models/sequence";
import {Part} from "../../models/Part";
import {UploadService} from "../../services/upload.service";
import {HttpEventType, HttpResponse} from "@angular/common/http";
import {ConfirmActionComponent} from "../shared/confirm-action/confirm-action.component";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'app-part-visualization',
    templateUrl: './part-visualization.component.html',
    styleUrls: ['./part-visualization.component.css']
})
export class PartVisualizationComponent implements OnInit {

    @Input() entry: Part;
    @Input() sequence: Sequence;
    @Output() retrieveSequence: EventEmitter<any> = new EventEmitter<any>();

    data: any;
    edit: any;
    params: Paging;
    partSources: any;
    names: any;
    editor: any;
    feedback: any = {};
    loadingSequence: boolean;

    constructor(private http: HttpService, private vectorEditor: VectorEditorService, private upload: UploadService,
                private modalService: NgbModal) {
    }

    ngOnInit(): void {
        if (!this.sequence) {
            this.loadingSequence = true;
            this.http.get('parts/' + this.entry.id + '/sequence').subscribe((result: Sequence) => {
                this.sequence = result;
                this.loadingSequence = false;
                this.showSequenceVisualization(this.sequence);
            });
        } else {
            this.showSequenceVisualization(this.sequence);
        }
    }

    // sequence visualization
    showSequenceVisualization(file: Sequence): void {
        if (!file)
            return;

        this.data = {
            sequenceData: {
                sequence: file.sequence,
                features: this.vectorEditor.convertFeaturesToTSModel(file.features),
                name: file.name,
                circular: true,
            }
        };

        this.editor = (window as any).createVectorEditor(document.getElementById('part-preview-root'), {
            readOnly: true,
            doNotUseAbsolutePosition: true,
            PropertiesProps: {propertiesList: this.vectorEditor.propertiesList()},
            ToolBarProps: {toolList: this.vectorEditor.toolList()}
        });

        this.editor.updateEditor({
            readOnly: true,
            sequenceData: this.data.sequenceData,
            annotationVisibility: {parts: false, orfs: false, cutsites: false, translations: false},
            panelsShown: this.vectorEditor.panelsList(),
            // selectionLayer: {
            //     start: this.entry.genbankStartBP - 1,
            //     end: this.entry.endBP - 1
            // },
        });
    }

    // validatePartSource(): void {
    //     this.feedback.partSourceInvalid = undefined;
    //     const sequence = !this.entry.sequence ? this.sequence : this.entry.sequence;
    //     if (!sequence) {
    //         return;
    //     }
    //
    //     if (!this.partSources) {
    //         return;
    //     }
    //
    //     if (this.partSources.indexOf(sequence.partSource) !== -1) {
    //         this.feedback.partSourceInvalid = 'Part source duplicated in design';
    //     }
    // }

    // updatePartSource(): void {
    //     if (this.feedback.partSourceInvalid) {
    //         return;
    //     }
    //
    //     if (this.feedback.updating) {
    //         return;
    //     }
    //
    //     if (!this.entry.id) {
    //         return;
    //     }
    //
    //     this.feedback.updating = true;
    //     const url = 'designs/' + this.design.id + '/sequences/' + this.entry.sequence.id + '/part-sources/' + this.entry.sequence.partSource;
    //     this.http.put(url, {})
    //         .subscribe(() => {
    //             this.feedback.updating = undefined;
    //             this.data.sequenceData.name = this.entry.sequence.partSource;
    //             this.editor.updateEditor({
    //                 sequenceData: this.data.sequenceData,
    //             });
    //         }, (error) => {
    //             console.error(error);
    //             this.feedback.updating = undefined;
    //         });
    // }

    // private checkBP(basePair): any {
    //     const regEx = /^[1-9]\d*$/;
    //     if (!regEx.test(String(basePair))) {
    //         return undefined;
    //     }
    //
    //     if (basePair < 1) {
    //         return 1;
    //     }
    //
    //     if (basePair >= this.entry.sequence.bpLength) {
    //         return this.entry.sequence.bpLength;
    //     }
    //
    //     return basePair;
    // }

    selectPartClick(): void {
        // this.selectClick(this.part);
    }

    selectSequenceFile(event): void {
        this.uploadSequenceFile(event.target.files);
    }

    uploadSequenceFile(files: FileList): void {
        if (files.length === 0) {
            console.log('No file selected!');
            return;
        }

        const url = '/file/sequence?entryRecordId=' + this.entry.recordId;

        this.upload.uploadFile(url, files)
            .subscribe(event => {
                if (event.type === HttpEventType.UploadProgress) {
                } else if (event instanceof HttpResponse) {
                    const data: any = event.body; // {id, runTime,completionTime, runId, resultFileName}
                    console.log('server response', data);
                    if (data && data.entryId == this.entry.id && data.sequence) {
                        this.sequence = data.sequence;
                    }
                }
            });
    }

    deleteSequence(): void {
        const options: NgbModalOptions = {backdrop: 'static', size: 'sm'};
        const modalRef = this.modalService.open(ConfirmActionComponent, options);
        modalRef.componentInstance.resourceName = "sequence";
        modalRef.componentInstance.resourceIdentifier = this.sequence.identifier;
        modalRef.result.then((confirmed) => {
            if (confirmed) {
                this.deleteSequenceOnServer();
            }
        });
    }

    private deleteSequenceOnServer(): void {
        console.log("delete");
        this.sequence = undefined;
    }
}
