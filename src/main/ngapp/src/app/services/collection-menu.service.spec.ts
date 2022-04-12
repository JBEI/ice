import {TestBed} from '@angular/core/testing';

import {CollectionMenuService} from './collection-menu.service';

describe('CollectionMenuService', () => {
  let service: CollectionMenuService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CollectionMenuService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
