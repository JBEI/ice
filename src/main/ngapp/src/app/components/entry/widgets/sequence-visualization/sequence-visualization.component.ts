import {Component, Input, OnInit} from '@angular/core';
import {Part} from "../../../../models/Part";

@Component({
    selector: 'app-sequence-visualization',
    templateUrl: './sequence-visualization.component.html',
    styleUrls: ['./sequence-visualization.component.css']
})
export class SequenceVisualizationComponent implements OnInit {

    error: string;
    dragAreaClass: string;
    draggedFiles: any;
    @Input() part: Part;

    constructor() {
    }

    ngOnInit(): void {
    }

    saveFiles(files: FileList) {
        if (files.length > 1)
            this.error = "Only one file at time allowed";
        else {
            this.error = "";
            console.log(files[0].size, files[0].name, files[0].type);
            this.draggedFiles = files;
            console.log(files);
        }
    }

    onFileChange(event: any) {
        let files: FileList = event.target.files;
        this.saveFiles(files);
    }

}
