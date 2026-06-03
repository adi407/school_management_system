import { Component, inject, effect } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'sms-toast',
  standalone: true,
  template: `
    <!--
      role="status" + aria-live="polite" announces new toasts to screen readers
      without interrupting ongoing speech (WCAG 4.1.3 Status Messages).
    -->
    <div class="toast-container"
         role="status"
         aria-live="polite"
         aria-atomic="false">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast toast--{{ toast.type }}"
             role="alert"
             [attr.aria-label]="toastLabel(toast.type) + ': ' + toast.message"
             (click)="toastService.remove(toast.id)">
          <span class="toast-icon" aria-hidden="true">
            @switch (toast.type) {
              @case ('success') { ✓ }
              @case ('error')   { ✕ }
              @case ('warning') { ⚠ }
              @default          { ℹ }
            }
          </span>
          <span>{{ toast.message }}</span>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-icon { font-weight: 700; font-size: 14px; flex-shrink: 0; }
    .toast { cursor: pointer; }
  `],
})
export class ToastComponent {
  toastService = inject(ToastService);

  toastLabel(type: string): string {
    const map: Record<string, string> = {
      success: 'Success',
      error:   'Error',
      warning: 'Warning',
      info:    'Info',
    };
    return map[type] ?? 'Notification';
  }
}
