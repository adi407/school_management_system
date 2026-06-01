import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { FeatureFlagsMap } from '../models/feature-flag.model';
import { FeatureKey } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FeatureFlagService {
  private _flags = signal<FeatureFlagsMap>({});
  readonly flags = this._flags.asReadonly();

  constructor(private http: HttpClient) {}

  load() {
    return this.http.get<FeatureFlagsMap>(`${environment.apiUrl}/features`).pipe(
      tap(flags => this._flags.set(flags))
    );
  }

  isEnabled(key: FeatureKey): boolean {
    return this._flags()[key]?.isEnabled ?? false;
  }

  setFlags(flags: FeatureFlagsMap) {
    this._flags.set(flags);
  }
}
