import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'sms-upgrade',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div class="upgrade-page">
      <div class="upgrade-page__icon">🔒</div>
      <h2>Feature Not Available</h2>
      <p>This feature is not enabled for your school's subscription plan.</p>
      <p class="text-muted text-sm">Contact your school administrator or support to upgrade.</p>
      <button class="btn btn--outline" onclick="history.back()">Go Back</button>
    </div>
  `,
  styles: [`
    .upgrade-page { min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; background: var(--bg); text-align: center; padding: 24px; }
    .upgrade-page__icon { font-size: 56px; margin-bottom: 8px; }
    h2 { color: var(--text-primary); font-size: 24px; }
    p  { color: var(--text-secondary); max-width: 360px; }
  `],
})
export class UpgradeComponent {}
