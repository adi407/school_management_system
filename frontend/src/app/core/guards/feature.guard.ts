import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { FeatureFlagService } from '../services/feature-flag.service';
import { FeatureKey } from '../models/user.model';

export const featureGuard = (key: FeatureKey): CanActivateFn => () => {
  const flags = inject(FeatureFlagService);
  if (flags.isEnabled(key)) return true;
  inject(Router).navigate(['/upgrade']);
  return false;
};
