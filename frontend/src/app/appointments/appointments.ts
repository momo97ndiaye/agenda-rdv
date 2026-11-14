import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Appointment, AppointmentService } from './appointment.service';
import { fromLocalInput, toLocalInput } from './date-input';

@Component({
  selector: 'app-appointments',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './appointments.html',
})
export class Appointments implements OnInit {
  private readonly service = inject(AppointmentService);

  protected readonly appointments = signal<Appointment[]>([]);
  protected readonly editingId = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    startsAt: ['', Validators.required],
    endsAt: ['', Validators.required],
    notes: ['', Validators.maxLength(1000)],
  });

  ngOnInit(): void {
    this.load();
  }

  protected edit(appointment: Appointment): void {
    this.editingId.set(appointment.id);
    this.error.set(null);
    this.form.setValue({
      title: appointment.title,
      startsAt: toLocalInput(appointment.startsAt),
      endsAt: toLocalInput(appointment.endsAt),
      notes: appointment.notes ?? '',
    });
  }

  protected cancel(): void {
    this.editingId.set(null);
    this.error.set(null);
    this.form.reset();
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const input = {
      title: value.title.trim(),
      startsAt: fromLocalInput(value.startsAt),
      endsAt: fromLocalInput(value.endsAt),
      notes: value.notes.trim() || null,
    };
    const id = this.editingId();
    const request = id === null ? this.service.create(input) : this.service.update(id, input);

    request.subscribe({
      next: () => {
        this.cancel();
        this.load();
      },
      error: (e: { status: number }) =>
        this.error.set(
          e.status === 409
            ? 'Ce créneau chevauche un autre rendez-vous.'
            : e.status === 400
              ? 'La fin doit être après le début.'
              : 'Enregistrement impossible, réessayez.',
        ),
    });
  }

  protected remove(appointment: Appointment): void {
    this.service.delete(appointment.id).subscribe(() => {
      if (this.editingId() === appointment.id) {
        this.cancel();
      }
      this.load();
    });
  }

  private load(): void {
    this.service.list().subscribe((list) => this.appointments.set(list));
  }
}
