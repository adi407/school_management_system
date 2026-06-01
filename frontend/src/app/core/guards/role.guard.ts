import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/user.model';

export const roleGuard = (allowedRoles: Role[]): CanActivateFn => () => {
  const auth = inject(AuthService);
  const role = auth.role();
  if (role && allowedRoles.includes(role)) return true;
  inject(Router).navigate(['/unauthorized']);
  return false;
};
