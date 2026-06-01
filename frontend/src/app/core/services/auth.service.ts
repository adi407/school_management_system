import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap, catchError, throwError } from 'rxjs';
import { AuthResponse, UserInfo } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY   = 'sms_access_token';
  private readonly REFRESH_KEY = 'sms_refresh_token';
  private readonly USER_KEY    = 'sms_user';

  private _user   = signal<UserInfo | null>(this.loadUser());
  private _loading = signal(false);

  readonly user    = this._user.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly isLoggedIn = computed(() => this._user() !== null);
  readonly role       = computed(() => this._user()?.role ?? null);

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string) {
    this._loading.set(true);
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, { email, password }).pipe(
      tap(res => this.handleAuthResponse(res)),
      tap(() => this._loading.set(false)),
      catchError(err => { this._loading.set(false); return throwError(() => err); })
    );
  }

  refresh() {
    const refreshToken = localStorage.getItem(this.REFRESH_KEY);
    if (!refreshToken) return throwError(() => new Error('No refresh token'));
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/refresh`, { refreshToken }).pipe(
      tap(res => this.handleAuthResponse(res))
    );
  }

  logout() {
    this.http.post(`${environment.apiUrl}/auth/logout`, {}).subscribe();
    this.clearSession();
    this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  hasFeature(key: string): boolean {
    return this._user()?.enabledFeatures.includes(key) ?? false;
  }

  private handleAuthResponse(res: AuthResponse) {
    localStorage.setItem(this.TOKEN_KEY,   res.accessToken);
    localStorage.setItem(this.REFRESH_KEY, res.refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(res.user));
    this._user.set(res.user);
  }

  private clearSession() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.USER_KEY);
    this._user.set(null);
  }

  private loadUser(): UserInfo | null {
    try {
      const raw = localStorage.getItem(this.USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }
}
