import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { SchoolService } from '../../../core/services/school.service';
import { SchoolDto, SubscriptionTier } from '../../../core/models/school.model';

@Component({
  selector: 'sms-sa-dashboard',
  standalone: true,
  imports: [RouterModule, DecimalPipe],
  templateUrl: './sa-dashboard.component.html',
  styleUrls: ['./sa-dashboard.component.scss'],
})
export class SaDashboardComponent implements OnInit {
  private schoolService = inject(SchoolService);

  stats = signal({
    totalSchools: 0,
    activeSchools: 0,
    trialExpiring: 0,
    totalStudents: 0,
  });

  recentSchools = signal<SchoolDto[]>([]);
  loading = signal(true);

  tierColor(tier: SubscriptionTier): string {
    const map: Record<SubscriptionTier, string> = {
      FREE: 'neutral', BASIC: 'blue', PREMIUM: 'purple', ENTERPRISE: 'green',
    };
    return map[tier] ?? 'neutral';
  }

  ngOnInit() {
    this.schoolService.listSchools({ page: 0, size: 5 }).subscribe({
      next: (res) => {
        this.recentSchools.set(res.content);
        this.stats.set({
          totalSchools: res.totalElements,
          activeSchools: res.content.filter(s => s.isActive).length,
          trialExpiring: res.content.filter(s => s.subscriptionExpiry &&
            new Date(s.subscriptionExpiry) < new Date(Date.now() + 30 * 86400000)).length,
          totalStudents: res.content.reduce((a, s) => a + s.studentCount, 0),
        });
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
