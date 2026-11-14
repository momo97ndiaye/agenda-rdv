import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly mode = signal<'login' | 'register'>('login');
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group({
    fullName: [''],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  protected toggleMode(): void {
    this.mode.update((m) => (m === 'login' ? 'register' : 'login'));
    this.error.set(null);
  }

  protected submit(): void {
    const { fullName, email, password } = this.form.getRawValue();
    if (this.form.invalid || (this.mode() === 'register' && !fullName.trim())) {
      this.form.markAllAsTouched();
      this.error.set('Vérifiez les champs du formulaire.');
      return;
    }
    this.loading.set(true);
    this.error.set(null);

    const done = {
      next: () => this.router.navigate(['/rendez-vous']),
      error: (e: { status: number }) => {
        this.loading.set(false);
        this.error.set(
          e.status === 401
            ? 'Email ou mot de passe incorrect.'
            : e.status === 409
              ? 'Un compte existe déjà avec cet email.'
              : 'Une erreur est survenue, réessayez.',
        );
      },
    };

    if (this.mode() === 'login') {
      this.auth.login(email, password).subscribe(done);
    } else {
      this.auth.register({ fullName, email, password }).subscribe({
        next: () => this.auth.login(email, password).subscribe(done),
        error: done.error,
      });
    }
  }
}
