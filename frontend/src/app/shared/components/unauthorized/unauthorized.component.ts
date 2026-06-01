import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'sms-unauthorized',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div class="error-page">
      <div class="error-page__code">403</div>
      <h1>Access Denied</h1>
      <p>You don't have permission to view this page.</p>
      <a routerLink="/" class="btn btn--primary">Go Home</a>
    </div>
  `,
  styles: [`
    .error-page { min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; background: var(--bg); text-align: center; }
    .error-page__code { font-size: 96px; font-weight: 800; background: var(--gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; line-height: 1; }
    h1 { color: var(--text-primary); font-size: 28px; }
    p  { color: var(--text-muted); margin-bottom: 8px; }
  `],
})
export class UnauthorizedComponent {}
