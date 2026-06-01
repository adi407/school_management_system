import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private _toasts = signal<Toast[]>([]);
  readonly toasts = this._toasts.asReadonly();

  success(message: string) { this.add('success', message); }
  error(message: string)   { this.add('error', message); }
  warning(message: string) { this.add('warning', message); }
  info(message: string)    { this.add('info', message); }

  remove(id: string) {
    this._toasts.update(ts => ts.filter(t => t.id !== id));
  }

  private add(type: Toast['type'], message: string) {
    const id = Math.random().toString(36).slice(2);
    this._toasts.update(ts => [...ts, { id, type, message }]);
    setTimeout(() => this.remove(id), 4000);
  }
}
