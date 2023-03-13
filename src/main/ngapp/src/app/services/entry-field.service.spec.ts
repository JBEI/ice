import {TestBed} from '@angular/core/testing';

import {EntryFieldService} from './entry-field.service';

describe('EntryFieldService', () => {
    let service: EntryFieldService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(EntryFieldService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
