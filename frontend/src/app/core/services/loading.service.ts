import { Injectable, signal, computed } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private _count = signal(0);
  readonly isLoading = computed(() => this._count() > 0);

  increment() { this._count.update(c => c + 1); }
  decrement() { this._count.update(c => Math.max(0, c - 1)); }
}
