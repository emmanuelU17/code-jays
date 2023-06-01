import { TestBed } from '@angular/core/testing';

import { AuthMenuService } from './auth-menu.service';

describe('AuthmenuService', () => {
  let service: AuthMenuService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthMenuService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
