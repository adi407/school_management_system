import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DecimalPipe, DatePipe } from '@angular/common';
import { SchoolService } from '../../../../core/services/school.service';
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
        this.toast.success(`School ${school.isActive ? 'suspended' : 'activated'} successfully`);
        this.load();
        this.actionLoading.set(null);
      },
      error: () => {
        this.toast.error('Failed to update school status');
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

  get totalPages() { return Math.ceil(this.totalElements() / this.size); }
  prevPage() { if (this.page > 0) { this.page--; this.load(); } }
  nextPage() { if (this.page < this.totalPages - 1) { this.page++; this.load(); } }
}
