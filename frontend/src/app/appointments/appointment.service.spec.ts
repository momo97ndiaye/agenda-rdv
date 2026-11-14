import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AppointmentService } from './appointment.service';

describe('AppointmentService', () => {
  let service: AppointmentService;
  let http: HttpTestingController;

  const input = {
    title: 'Consultation',
    startsAt: '2026-12-01T09:00:00Z',
    endsAt: '2026-12-01T09:30:00Z',
    notes: null,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AppointmentService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('crée un rendez-vous', () => {
    service.create(input).subscribe((a) => expect(a.id).toBe(1));
    const req = http.expectOne('/api/appointments');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(input);
    req.flush({ id: 1, ...input });
  });

  it('modifie et supprime par identifiant', () => {
    service.update(7, input).subscribe();
    expect(http.expectOne('/api/appointments/7').request.method).toBe('PUT');

    service.delete(7).subscribe();
    expect(http.expectOne((r) => r.method === 'DELETE' && r.url === '/api/appointments/7')).toBeTruthy();
  });
});
