import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('enregistre le jeton après connexion', () => {
    service.login('awa@exemple.sn', 'motdepasse123').subscribe();

    const req = http.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'awa@exemple.sn', password: 'motdepasse123' });
    req.flush({ token: 'jeton-test' });

    expect(service.isLoggedIn()).toBe(true);
    expect(localStorage.getItem('agenda.token')).toBe('jeton-test');
  });

  it('efface le jeton à la déconnexion', () => {
    service.login('awa@exemple.sn', 'motdepasse123').subscribe();
    http.expectOne('/api/auth/login').flush({ token: 'jeton-test' });

    service.logout();

    expect(service.isLoggedIn()).toBe(false);
    expect(localStorage.getItem('agenda.token')).toBeNull();
  });
});
