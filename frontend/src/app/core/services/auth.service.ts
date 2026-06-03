import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap, catchError, throwError } from 'rxjs';
import { AuthResponse, UserInfo } from '../models/user.model';
import { MyModuleDto, StaffModule } from '../models/module.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY   = 'sms_access_token';
  private readonly REFRESH_KEY = 'sms_refresh_token';
  private readonly USER_KEY    = 'sms_user';
  private readonly MODULES_KEY = 'sms_modules';

  private _user    = signal<UserInfo | null>(this.loadUser());
  private _modules = signal<MyModuleDto[]>(this.loadModules());
  private _loading = signal(false);

  readonly user    = this._user.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly isLoggedIn  = computed(() => this._user() !== null);
  readonly role        = computed(() => this._user()?.role ?? null);
  readonly myModules   = this._modules.asReadonly();

  /** Set of active module names for quick O(1) lookup */
  readonly moduleSet = computed(() =>
    new Set(this._modules().map(m => m.module))
  );

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string) {
    this._loading.set(true);
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, { email, password })
      .pipe(
        tap(res => {
          this.handleAuthResponse(res);
          // Fetch modules in background — don't block navigation on this
          this.http
            .get<MyModuleDto[]>(`${environment.apiUrl}/my-modules`)
            .subscribe(modules => this.cacheModules(modules));
        }),
        tap(() => this._loading.set(false)),
        catchError(err => { this._loading.set(false); return throwError(() => err); })
      );
  }

  refresh() {
    const refreshToken = localStorage.getItem(this.REFRESH_KEY);
    if (!refreshToken) return throwError(() => new Error('No refresh token'));
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/refresh`, { refreshToken })
      .pipe(tap(res => this.handleAuthResponse(res)));
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

  /** True if user holds this module (or is SUPER_ADMIN who bypasses all checks) */
  hasModule(module: StaffModule): boolean {
    if (this._user()?.role === 'SUPER_ADMIN') return true;
    return this.moduleSet().has(module);
  }

  /** Refresh modules from the server — call after admin changes their own assignments */
  refreshModules() {
    return this.http.get<MyModuleDto[]>(`${environment.apiUrl}/my-modules`).pipe(
      tap(modules => this.cacheModules(modules))
    );
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

  private handleAuthResponse(res: AuthResponse) {
    localStorage.setItem(this.TOKEN_KEY,   res.accessToken);
    localStorage.setItem(this.REFRESH_KEY, res.refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(res.user));
    this._user.set(res.user);
  }

  private cacheModules(modules: MyModuleDto[]) {
    localStorage.setItem(this.MODULES_KEY, JSON.stringify(modules));
    this._modules.set(modules);
  }

  private clearSession() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.MODULES_KEY);
    this._user.set(null);
    this._modules.set([]);
  }

  private loadUser(): UserInfo | null {
    try {
      const raw = localStorage.getItem(this.USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }

  private loadModules(): MyModuleDto[] {
    try {
      const raw = localStorage.getItem(this.MODULES_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch { return []; }
  }
}
