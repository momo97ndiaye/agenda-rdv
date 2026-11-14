import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface Appointment {
  id: number;
  title: string;
  startsAt: string;
  endsAt: string;
  notes: string | null;
}

export type AppointmentInput = Omit<Appointment, 'id'>;

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/appointments';

  list(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(this.url);
  }

  create(input: AppointmentInput): Observable<Appointment> {
    return this.http.post<Appointment>(this.url, input);
  }

  update(id: number, input: AppointmentInput): Observable<Appointment> {
    return this.http.put<Appointment>(`${this.url}/${id}`, input);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
