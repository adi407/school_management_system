import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LoadingService } from '../../core/services/loading.service';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'sms-header',
  standalone: true,
  imports: [RouterModule],
  template: `
    <header class="header">
      <button class="header__menu-btn" (click)="toggleSidebar.emit()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="3" y1="6"  x2="21" y2="6"/>
          <line x1="3" y1="12" x2="21" y2="12"/>
          <line x1="3" y1="18" x2="21" y2="18"/>
        </svg>
      </button>

      <!-- Loading bar -->
      @if (loading.isLoading()) {
        <div class="header__loading-bar"></div>
      }

      <div class="header__spacer"></div>

      <!-- School badge (non-super-admin) -->
      @if (user()?.schoolId) {
        <div class="header__school-badge">
          <span class="dot dot--green"></span>
          <span>{{ schoolDomain() }}</span>
        </div>
      }

      <!-- Theme toggle -->
      <button class="header__icon-btn" (click)="theme.toggle()" [title]="theme.isDark() ? 'Switch to light mode' : 'Switch to dark mode'">
        @if (theme.isDark()) {
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
        } @else {
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
        }
      </button>

      <!-- Role badge -->
      <div class="header__role-badge">{{ user()?.role?.replace('_', ' ') }}</div>

      <!-- User avatar -->
      <div class="header__avatar">{{ user()?.fullName?.[0] ?? '?' }}</div>
    </header>
  `,
  styles: [`
    .header {
      position: fixed; top: 0; left: var(--sidebar-width); right: 0;
      height: var(--header-height); z-index: 110;
      background: var(--nav-bg); backdrop-filter: var(--blur-md);
      -webkit-backdrop-filter: var(--blur-md);
      border-bottom: 1px solid var(--border);
      display: flex; align-items: center; gap: 12px; padding: 0 20px;
      transition: left 0.28s cubic-bezier(0.16,1,0.3,1);
    }
    @media (max-width: 768px) {
      .header { left: 0 !important; padding: 0 16px; }
      .header__school-badge { display: none; }
    }
    .header__loading-bar {
      position: absolute; bottom: -1px; left: 0; right: 0; height: 2px;
      background: linear-gradient(90deg, var(--accent), var(--accent-5), var(--accent));
      background-size: 200% 100%;
      animation: shimmer 1.2s linear infinite;
    }
    @keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
    .header__menu-btn {
      width: 30px; height: 30px; border-radius: 8px; background: transparent;
      border: 1px solid var(--border); color: var(--text-muted); cursor: pointer;
      display: flex; align-items: center; justify-content: center;
      transition: all var(--transition);
      &:hover { background: var(--bg-hover); color: var(--text-primary); }
    }
    .header__spacer { flex: 1; }
    .header__school-badge {
      display: flex; align-items: center; gap: 6px; padding: 4px 10px;
      background: var(--input-bg); border: 1px solid var(--border);
      border-radius: 9999px; font-size: 11px; color: var(--text-secondary);
    }
    .dot { width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0; }
    .dot--green { background: var(--success); animation: pulse-ring 2s infinite; }
    @keyframes pulse-ring {
      0%   { box-shadow: 0 0 0 0 rgba(48,209,88,.4); }
      70%  { box-shadow: 0 0 0 5px rgba(48,209,88,0); }
      100% { box-shadow: 0 0 0 0 rgba(48,209,88,0); }
    }
    .header__role-badge {
      font-size: 10px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase;
      color: var(--accent); background: var(--accent-soft);
      padding: 3px 10px; border-radius: 9999px;
      border: 1px solid rgba(var(--accent-rgb),.22);
    }
    .header__avatar {
      width: 30px; height: 30px; border-radius: 8px; background: var(--gradient);
      display: flex; align-items: center; justify-content: center;
      font-size: 12px; font-weight: 700; color: #fff; flex-shrink: 0;
    }
    .header__icon-btn {
      width: 30px; height: 30px; border-radius: 8px; background: transparent;
      border: 1px solid var(--border); color: var(--text-muted); cursor: pointer;
      display: flex; align-items: center; justify-content: center;
      transition: all var(--transition); flex-shrink: 0;
      &:hover { background: var(--bg-hover); color: var(--text-primary); border-color: var(--border-strong); }
    }
  `],
})
export class HeaderComponent {
  @Output() toggleSidebar    = new EventEmitter<void>();
  @Input()  sidebarCollapsed = false;
  auth    = inject(AuthService);
  loading = inject(LoadingService);
  theme   = inject(ThemeService);
  user    = this.auth.user;

  schoolDomain(): string {
    const parts = this.user()?.email?.split('@');
    return parts && parts.length > 1 ? parts[1] : 'School';
  }
}
