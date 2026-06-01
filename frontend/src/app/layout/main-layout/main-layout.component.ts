import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HeaderComponent } from '../header/header.component';
import { ToastComponent } from '../../shared/components/toast/toast.component';

@Component({
  selector: 'sms-main-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, HeaderComponent, ToastComponent],
  template: `
    <!-- Mobile overlay backdrop -->
    @if (mobileOpen()) {
      <div class="sidebar-overlay" (click)="mobileOpen.set(false)"></div>
    }
    <sms-sidebar
      [collapsed]="sidebarCollapsed()"
      [mobileOpen]="mobileOpen()"
      (toggleCollapse)="sidebarCollapsed.set(!sidebarCollapsed())"
      (closeMobile)="mobileOpen.set(false)" />
    <div class="layout-body"
         [class.layout-body--collapsed]="sidebarCollapsed()">
      <sms-header
        (toggleSidebar)="onToggleSidebar()"
        [sidebarCollapsed]="sidebarCollapsed()" />
      <main class="main-content">
        <router-outlet />
      </main>
    </div>
    <sms-toast />
  `,
  styles: [`
    :host { display: flex; min-height: 100vh; }
    .sidebar-overlay {
      display: none;
      position: fixed; inset: 0; background: rgba(0,0,0,.55);
      z-index: 99; backdrop-filter: blur(2px);
    }
    .layout-body {
      flex: 1;
      margin-left: var(--sidebar-width);
      transition: margin-left 0.28s cubic-bezier(0.16, 1, 0.3, 1);
      min-height: 100vh;
      background: var(--bg);
      background-image:
        radial-gradient(ellipse 80vw 70vh at 15% 0%,   rgba(var(--accent-rgb), 0.055) 0%, transparent 60%),
        radial-gradient(ellipse 55vw 55vh at 90% 100%, rgba(var(--purple-rgb), 0.045) 0%, transparent 60%);
    }
    .layout-body--collapsed { margin-left: var(--sidebar-collapsed); }
    .main-content {
      padding: calc(var(--header-height) + 24px) 28px 40px;
      max-width: 1400px;
    }
    @media (max-width: 768px) {
      .sidebar-overlay { display: block; }
      .layout-body { margin-left: 0 !important; }
      .main-content { padding: calc(var(--header-height) + 16px) 16px 32px; }
    }
  `],
})
export class MainLayoutComponent {
  sidebarCollapsed = signal(false);
  mobileOpen = signal(false);

  onToggleSidebar(): void {
    const mobile = window.innerWidth <= 768;
    if (mobile) {
      this.mobileOpen.update(v => !v);
    } else {
      this.sidebarCollapsed.update(v => !v);
    }
  }
}
