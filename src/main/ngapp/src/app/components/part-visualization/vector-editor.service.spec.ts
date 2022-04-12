import {TestBed} from '@angular/core/testing';

import {VectorEditorService} from './vector-editor.service';

describe('VectorEditorService', () => {
  let service: VectorEditorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VectorEditorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
