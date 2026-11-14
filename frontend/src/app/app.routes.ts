import { Routes } from '@angular/router';

import { Appointments } from './appointments/appointments';
import { authGuard } from './auth/auth.guard';
import { Login } from './auth/login';

export const routes: Routes = [
  { path: 'connexion', component: Login },
  { path: 'rendez-vous', component: Appointments, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'rendez-vous' },
  { path: '**', redirectTo: 'rendez-vous' },
];
