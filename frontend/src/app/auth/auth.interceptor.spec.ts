import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('agenda.token', 'jeton-test');
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('ajoute le jeton aux appels de l’API', () => {
    http.get('/api/appointments').subscribe();
    const req = backend.expectOne('/api/appointments');
    expect(req.request.headers.get('Authorization')).toBe('Bearer jeton-test');
    req.flush([]);
  });

  it('n’ajoute pas le jeton aux routes de connexion', () => {
    http.post('/api/auth/login', {}).subscribe();
    const req = backend.expectOne('/api/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({ token: 'x' });
  });

  it('déconnecte et renvoie vers la connexion sur une réponse 401', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    http.get('/api/appointments').subscribe({ error: () => undefined });

    backend.expectOne('/api/appointments').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(TestBed.inject(AuthService).isLoggedIn()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/connexion']);
  });
});
