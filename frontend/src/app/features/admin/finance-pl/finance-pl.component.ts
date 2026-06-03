import { Component, OnInit, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecimalPipe } from '@angular/common';
import { PayrollService } from '../../../core/services/payroll.service';
import { ToastService } from '../../../core/services/toast.service';
import { ProfitLossReportDto } from '../../../core/models/payroll.model';

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

@Component({
  selector: 'sms-finance-pl',
  standalone: true,
  imports: [FormsModule, DecimalPipe],
  templateUrl: './finance-pl.component.html',
  styleUrls: ['./finance-pl.component.scss'],
})
export class FinancePlComponent implements OnInit {

  loading  = signal(false);
  report   = signal<ProfitLossReportDto | null>(null);

  // Filter controls
  year      = new Date().getFullYear();
  fromMonth = 1;
  toMonth   = new Date().getMonth() + 1;  // up to current month

  readonly years   = this.buildYears();
  readonly months  = MONTHS;

  // Computed bar widths for visual bars
  revenueBarW   = computed(() => {
    const r = this.report();
    if (!r || r.totalRevenue === 0) return 100;
    return 100;
  });
  expenseBarW = computed(() => {
    const r = this.report();
    if (!r || r.totalRevenue === 0) return 0;
    return Math.min(100, (r.totalExpenses / r.totalRevenue) * 100);
  });

  // Breakdown bars relative to total expenses
  payrollBarW    = computed(() => this.barPct('totalPayrollCost',    'totalExpenses'));
  opexBarW       = computed(() => this.barPct('totalOperationalExpenses', 'totalExpenses'));

  constructor(
    private payrollService: PayrollService,
    private toast: ToastService,
  ) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.payrollService.getProfitLoss(this.year, this.fromMonth, this.toMonth).subscribe({
      next:  r  => { this.report.set(r); this.loading.set(false); },
      error: () => { this.toast.error('Failed to load P&L report'); this.loading.set(false); },
    });
  }

  periodLabel(): string {
    return this.fromMonth === this.toMonth
      ? `${MONTHS[this.fromMonth - 1]} ${this.year}`
      : `${MONTHS[this.fromMonth - 1]} – ${MONTHS[this.toMonth - 1]} ${this.year}`;
  }

  profitClass(): string {
    const r = this.report();
    if (!r) return '';
    return r.netProfit >= 0 ? 'text-profit' : 'text-loss';
  }

  catLabel(c: string): string {
    return c.charAt(0) + c.slice(1).toLowerCase().replace(/_/g, ' ');
  }

  expenseBarPct(amount: number): number {
    const r = this.report();
    if (!r || r.totalExpenses === 0) return 0;
    return Math.min(100, (amount / r.totalExpenses) * 100);
  }

  private barPct(numKey: keyof ProfitLossReportDto, denomKey: keyof ProfitLossReportDto): number {
    const r = this.report();
    if (!r) return 0;
    const num   = r[numKey]   as number;
    const denom = r[denomKey] as number;
    return denom === 0 ? 0 : Math.min(100, (num / denom) * 100);
  }

  private buildYears(): number[] {
    const y = new Date().getFullYear();
    return [y - 2, y - 1, y];
  }
}
