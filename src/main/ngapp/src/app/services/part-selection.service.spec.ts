import {TestBed} from '@angular/core/testing';

import {PartSelectionService} from './part-selection.service';

describe('PartSelectionService', () => {
    let service: PartSelectionService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(PartSelectionService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
