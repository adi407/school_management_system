import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  RegistrationService,
  SchoolRegistrationDto,
} from '../../../core/services/registration.service';

@Component({
  selector: 'sms-registration-list',
  standalone: true,
  imports: [RouterModule, DatePipe, FormsModule],
  templateUrl: './registration-list.component.html',
  styleUrls: ['./registration-list.component.scss'],
})
export class RegistrationListComponent implements OnInit {
  private svc = inject(RegistrationService);

  registrations = signal<SchoolRegistrationDto[]>([]);
  loading = signal(true);
  totalElements = signal(0);

  statusFilter = signal<string>('');
  page = signal(0);

  // Modal state
  selected = signal<SchoolRegistrationDto | null>(null);
  modalMode = signal<'view' | 'approve' | 'reject'>('view');
  approvePassword = signal('');
  approveTier = signal('');
  rejectReason = signal('');
  actionLoading = signal(false);
  actionError = signal('');

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.svc
      .listRegistrations({
        status: this.statusFilter() || undefined,
        page: this.page(),
        size: 20,
      })
      .subscribe({
        next: (res) => {
          this.registrations.set(res.content);
          this.totalElements.set(res.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  onFilterChange() {
    this.page.set(0);
    this.load();
  }

  openView(reg: SchoolRegistrationDto) {
    this.selected.set(reg);
    this.modalMode.set('view');
    this.actionError.set('');
  }

  openApprove(reg: SchoolRegistrationDto) {
    this.selected.set(reg);
    this.modalMode.set('approve');
    this.approvePassword.set('Welcome@' + Math.floor(1000 + Math.random() * 9000));
    this.approveTier.set(reg.requestedTier ?? 'FREE');
    this.actionError.set('');
  }

  openReject(reg: SchoolRegistrationDto) {
    this.selected.set(reg);
    this.modalMode.set('reject');
    this.rejectReason.set('');
    this.actionError.set('');
  }

  closeModal() {
    this.selected.set(null);
    this.actionError.set('');
  }

  submitApprove() {
    const reg = this.selected();
    if (!reg || !this.approvePassword()) return;
    this.actionLoading.set(true);
    this.actionError.set('');
    this.svc.approve(reg.id, this.approvePassword(), this.approveTier() || undefined).subscribe({
      next: (updated) => {
        this.registrations.update((list) =>
          list.map((r) => (r.id === updated.id ? updated : r))
        );
        this.actionLoading.set(false);
        this.closeModal();
      },
      error: (err) => {
        this.actionError.set(err.error?.message || err.error?.error || 'Approval failed. Please try again.');
        this.actionLoading.set(false);
      },
    });
  }

  submitReject() {
    const reg = this.selected();
    if (!reg || !this.rejectReason()) return;
    this.actionLoading.set(true);
    this.actionError.set('');
    this.svc.reject(reg.id, this.rejectReason()).subscribe({
      next: (updated) => {
        this.registrations.update((list) =>
          list.map((r) => (r.id === updated.id ? updated : r))
        );
        this.actionLoading.set(false);
        this.closeModal();
      },
      error: (err) => {
        this.actionError.set(err.error?.message || err.error?.error || 'Rejection failed. Please try again.');
        this.actionLoading.set(false);
      },
    });
  }

  statusBadge(status: string): string {
    switch (status) {
      case 'PENDING_APPROVAL': return 'yellow';
      case 'APPROVED': return 'green';
      case 'REJECTED': return 'red';
      default: return 'neutral';
    }
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'PENDING_APPROVAL': return 'Pending';
      case 'APPROVED': return 'Approved';
      case 'REJECTED': return 'Rejected';
      default: return status;
    }
  }
}
