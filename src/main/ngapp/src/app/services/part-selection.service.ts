import {Injectable} from '@angular/core';
import {Part} from "../models/Part";

@Injectable({
    providedIn: 'root'
})
export class PartSelectionService {

    selectedParts: number[];
    allSelection: boolean;
    collection: string;
    folder: number;

    canEdit: boolean;  // whether current user is able to edit all selected entries

    constructor() {
        this.selectedParts = [];
    }

    hasSelection(): boolean {
        return this.selectedParts.length > 0;
    }

    select(part: Part): void {
        if (!part.id)
            return;

        const index = this.selectedParts.indexOf(part.id);
        if (index !== -1) {
            this.selectedParts.splice(index, 1);
        } else {
            this.selectedParts.push(part.id);
        }

        console.log('selected',);
    }

    isSelected(part: Part): boolean {
        if (this.allSelection)
            return true;

        return this.selectedParts.indexOf(part.id) !== -1;
    }

    setSelectedCollection(collection: string): void {
        this.collection = collection;
    }

    setSelectedFolder(folderId: number): void {
        this.folder = folderId;
    }

    selectAll(): void {
        if (this.selectedParts.length) {
            this.selectedParts = [];
            return;
        }

        this.allSelection = !this.allSelection;
    }

    allSelected(): boolean {
        return this.allSelection;
    }

    canEditAll(): boolean {
        return this.canEdit;
    }

    hasAtLeastOneSelected(): boolean {
        return this.selectedParts.length > 0 && !this.allSelection;
    }
}
