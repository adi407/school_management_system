import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DecimalPipe, DatePipe } from '@angular/common';
import { SchoolService, DeleteSchoolResponse } from '../../../../core/services/school.service';
import { ToastService } from '../../../../core/services/toast.service';
import { SchoolDto, SubscriptionTier } from '../../../../core/models/school.model';

@Component({
  selector: 'sms-school-list',
  standalone: true,
  imports: [RouterModule, FormsModule, DecimalPipe, DatePipe],
  templateUrl: './school-list.component.html',
  styleUrls: ['./school-list.component.scss'],
})
export class SchoolListComponent implements OnInit {
  private schoolSvc = inject(SchoolService);
  private toast     = inject(ToastService);

  schools       = signal<SchoolDto[]>([]);
  totalElements = signal(0);
  loading       = signal(true);
  actionLoading = signal<string | null>(null);

  // Delete confirmation modal
  deleteModal    = signal(false);
  deleteTarget   = signal<SchoolDto | null>(null);
  deleteType     = signal<'soft' | 'hard'>('soft');
  deleteResult   = signal<DeleteSchoolResponse | null>(null);
  confirmText    = '';

  search   = '';
  tier     = '';
  isActive = '';
  page     = 0;
  size     = 20;

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.schoolSvc.listSchools({
      search:   this.search   || undefined,
      tier:     this.tier     || undefined,
      isActive: this.isActive !== '' ? this.isActive === 'true' : undefined,
      page: this.page,
      size: this.size,
    }).subscribe({
      next: (res) => {
        this.schools.set(res.content);
        this.totalElements.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onSearch() { this.page = 0; this.load(); }

  toggleStatus(school: SchoolDto) {
    this.actionLoading.set(school.id);
    this.schoolSvc.setStatus(school.id, !school.isActive).subscribe({
      next: () => {
        this.toast.success(`School ${school.isActive ? 'suspended' : 'activated'}`);
        this.load();
        this.actionLoading.set(null);
      },
      error: () => {
        this.toast.error('Failed to update school status');
        this.actionLoading.set(null);
      },
    });
  }

  // ── Delete flow ────────────────────────────────────────────
  openDeleteModal(school: SchoolDto, type: 'soft' | 'hard') {
    this.deleteTarget.set(school);
    this.deleteType.set(type);
    this.deleteResult.set(null);
    this.confirmText = '';
    this.deleteModal.set(true);
  }

  closeDeleteModal() {
    this.deleteModal.set(false);
    this.deleteTarget.set(null);
    this.deleteResult.set(null);
    this.confirmText = '';
  }

  get canConfirmDelete(): boolean {
    if (this.deleteType() === 'hard') {
      return this.confirmText === this.deleteTarget()?.name;
    }
    return true;
  }

  executeDelete() {
    const school = this.deleteTarget();
    if (!school) return;
    this.actionLoading.set(school.id);

    const call = this.deleteType() === 'hard'
      ? this.schoolSvc.hardDelete(school.id)
      : this.schoolSvc.softDelete(school.id);

    call.subscribe({
      next: (res) => {
        this.deleteResult.set(res);
        this.actionLoading.set(null);
        if (this.deleteType() === 'hard') {
          this.toast.success(`${school.name} permanently deleted — ${res.totalRecordsDeleted} records removed`);
        } else {
          this.toast.success(`${school.name} soft-deleted — ${res.usersAffected} users + ${res.studentsAffected} students deactivated`);
        }
        this.load();
      },
      error: (err) => {
        this.toast.error(err?.error?.message ?? 'Delete failed');
        this.actionLoading.set(null);
      },
    });
  }

  tierColor(tier: SubscriptionTier): string {
    const map: Record<SubscriptionTier, string> = {
      FREE: 'neutral', BASIC: 'blue', PREMIUM: 'purple', ENTERPRISE: 'green',
    };
    return map[tier] ?? 'neutral';
  }

  tierIcon(tier: SubscriptionTier): string {
    const map: Record<SubscriptionTier, string> = {
      FREE: '○', BASIC: '◐', PREMIUM: '◉', ENTERPRISE: '★',
    };
    return map[tier] ?? '○';
  }

  get totalPages() { return Math.ceil(this.totalElements() / this.size); }
  prevPage() { if (this.page > 0) { this.page--; this.load(); } }
  nextPage() { if (this.page < this.totalPages - 1) { this.page++; this.load(); } }
}
