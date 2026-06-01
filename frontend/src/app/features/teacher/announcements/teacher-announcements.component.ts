import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AnnouncementService } from '../../../core/services/announcement.service';
import { AnnouncementDto } from '../../../core/models/announcement.model';

@Component({
  selector: 'sms-teacher-announcements',
  standalone: true,
  imports: [DatePipe],
  template: `
    <div class="page-header">
      <div>
        <span class="page-header__tag">Teacher Portal</span>
        <h1 class="page-header__title">Announcements</h1>
        <p class="page-header__subtitle">School-wide notices and updates</p>
      </div>
    </div>

    @if (loading()) {
      <div class="text-muted text-sm" style="padding:40px 0;text-align:center">Loading announcements…</div>
    } @else if (items().length === 0) {
      <div class="card" style="padding:60px;text-align:center">
        <div style="font-size:48px;margin-bottom:12px">📢</div>
        <div class="fw-600 mb-8">No announcements</div>
        <div class="text-muted text-sm">No active announcements at the moment.</div>
      </div>
    } @else {
      <div class="ann-list">
        @for (item of items(); track item.id) {
          <div class="ann-card card" [class.ann-card--pinned]="item.isPinned">
            @if (item.isPinned) {
              <span class="pin-badge">📌 Pinned</span>
            }
            <h3 class="ann-card__title">{{ item.title }}</h3>
            <p class="ann-card__body">{{ item.body }}</p>
            <div class="ann-card__footer text-xs text-muted">
              Published {{ item.publishedAt | date:'d MMM yyyy, h:mm a' }}
              · For: {{ rolesLabel(item.targetRoles) }}
            </div>
          </div>
        }
      </div>
    }
  `,
  styles: [`
    .ann-list { display: flex; flex-direction: column; gap: 14px; }
    .ann-card { padding: 20px 24px; position: relative; }
    .ann-card--pinned { border-left: 3px solid var(--warning); }
    .ann-card__title { font-size: 16px; font-weight: 700; margin-bottom: 8px; color: var(--text-primary); }
    .ann-card__body  { font-size: 14px; color: var(--text-secondary); line-height: 1.6; margin-bottom: 12px; white-space: pre-wrap; }
    .pin-badge { display: inline-block; font-size: 11px; font-weight: 600; color: var(--warning); margin-bottom: 8px; }
  `],
})
export class TeacherAnnouncementsComponent implements OnInit {
  private annSvc = inject(AnnouncementService);

  loading = signal(true);
  items   = signal<AnnouncementDto[]>([]);

  ngOnInit(): void {
    this.annSvc.listAll().subscribe({
      next: d => { this.items.set(d); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  rolesLabel(roles: string[]): string {
    if (!roles?.length) return 'Everyone';
    return roles.map(r => r.charAt(0) + r.slice(1).toLowerCase()).join(', ');
  }
}
