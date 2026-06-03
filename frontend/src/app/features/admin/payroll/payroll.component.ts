import { Component, OnInit, signal, computed } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { DatePipe, DecimalPipe, CurrencyPipe, TitleCasePipe } from '@angular/common';
import { PayrollService } from '../../../core/services/payroll.service';
import { StaffService } from '../../../core/services/staff.service';
import { ToastService } from '../../../core/services/toast.service';
import {
  PayrollRunDto, PayslipDto,
  SalaryStructureDto, CreateSalaryStructureRequest,
  ExpenseEntryDto,
  ExpenseCategory, TaxRegime,
} from '../../../core/models/payroll.model';

type Tab = 'runs' | 'structures' | 'expenses';

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
const EXPENSE_CATEGORIES: ExpenseCategory[] = [
  'RENT','ELECTRICITY','WATER','INTERNET','MAINTENANCE',
  'STATIONARY','CLEANING','SECURITY','TRANSPORT','MARKETING',
  'EQUIPMENT','SOFTWARE','MISCELLANEOUS',
];

@Component({
  selector: 'sms-payroll',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, DatePipe, DecimalPipe, CurrencyPipe, TitleCasePipe],
  templateUrl: './payroll.component.html',
  styleUrls: ['./payroll.component.scss'],
})
export class PayrollComponent implements OnInit {

  // ── State ─────────────────────────────────────────────────────────────────
  activeTab = signal<Tab>('runs');
  loading   = signal(false);
  saving    = signal(false);

  // Runs
  runs          = signal<PayrollRunDto[]>([]);
  selectedRun   = signal<PayrollRunDto | null>(null);
  payslips      = signal<PayslipDto[]>([]);
  loadingSlips  = signal(false);
  showRunModal  = signal(false);
  showSlipPanel = signal(false);

  // Structures
  structures      = signal<SalaryStructureDto[]>([]);
  staffList       = signal<any[]>([]);
  showStructModal = signal(false);
  editStructId    = signal<string | null>(null);

  // Expenses
  expenses         = signal<ExpenseEntryDto[]>([]);
  showExpenseModal = signal(false);
  editExpenseId    = signal<string | null>(null);
  expenseFrom      = signal(this.monthStart());
  expenseTo        = signal(this.today());

  // Helpers
  readonly months   = MONTHS;
  readonly years    = this.buildYearRange();
  readonly expCats  = EXPENSE_CATEGORIES;

  // Computed
  totalNetPayout = computed(() =>
    this.runs().filter(r => r.status === 'PAID').reduce((s, r) => s + r.totalNetPayout, 0)
  );
  draftRuns = computed(() => this.runs().filter(r => r.status === 'DRAFT').length);

  // ── Forms ─────────────────────────────────────────────────────────────────

  runForm = this.fb.group({
    month:            [new Date().getMonth() + 1, [Validators.required, Validators.min(1), Validators.max(12)]],
    year:             [new Date().getFullYear(), [Validators.required]],
    totalWorkingDays: [26, [Validators.required, Validators.min(1), Validators.max(31)]],
    notes:            [''],
  });

  structForm = this.fb.group({
    staffId:                 ['', Validators.required],
    basicSalary:             [null as number | null, [Validators.required, Validators.min(0)]],
    hraAmount:               [0],
    daAmount:                [0],
    taAmount:                [0],
    medicalAllowance:        [0],
    otherAllowances:         [0],
    pfEnrolled:              [true],
    pfWageCeiling:           [15000],
    taxRegime:               ['NEW' as TaxRegime],
    declared80c:             [0],
    declaredHraExemption:    [0],
    declaredOtherExemptions: [0],
    effectiveFrom:           [this.today(), Validators.required],
    effectiveTo:             [null as string | null],
  });

  expenseForm = this.fb.group({
    category:    ['RENT' as ExpenseCategory, Validators.required],
    description: ['', Validators.required],
    amount:      [null as number | null, [Validators.required, Validators.min(0.01)]],
    expenseDate: [this.today(), Validators.required],
    referenceNo: [null as string | null],
  });

  constructor(
    private fb: FormBuilder,
    private payrollService: PayrollService,
    private staffService: StaffService,
    private toast: ToastService,
  ) {}

  ngOnInit() {
    this.loadRuns();
    this.loadStructures();
    this.loadStaff();
    this.loadExpenses();
  }

  // ── Tab ───────────────────────────────────────────────────────────────────
  setTab(tab: Tab) { this.activeTab.set(tab); }

  // ── Runs ──────────────────────────────────────────────────────────────────

  loadRuns() {
    this.loading.set(true);
    this.payrollService.listRuns().subscribe({
      next: r => { this.runs.set(r); this.loading.set(false); },
      error: () => { this.toast.error('Failed to load payroll runs'); this.loading.set(false); },
    });
  }

  openRunModal() {
    const now = new Date();
    // Default to previous month (that's when payroll is typically run)
    const prevMonth = now.getMonth() === 0 ? 12 : now.getMonth();
    const prevYear  = now.getMonth() === 0 ? now.getFullYear() - 1 : now.getFullYear();
    this.runForm.reset({ month: prevMonth, year: prevYear, totalWorkingDays: 26, notes: '' });
    this.showRunModal.set(true);
  }

  closeRunModal() { this.showRunModal.set(false); }

  submitRun() {
    if (this.runForm.invalid) { this.runForm.markAllAsTouched(); return; }
    const v = this.runForm.value;
    this.saving.set(true);
    this.payrollService.triggerRun({
      month: v.month!, year: v.year!,
      totalWorkingDays: v.totalWorkingDays ?? 26,
      notes: v.notes || null,
    }).subscribe({
      next: run => {
        this.runs.update(list => [run, ...list]);
        this.saving.set(false);
        this.closeRunModal();
        this.toast.success(`Payroll run for ${MONTHS[run.runMonth - 1]} ${run.runYear} created — ${run.payslipCount} payslips`);
      },
      error: (err) => {
        this.saving.set(false);
        const msg = err?.error?.message || 'Failed to trigger payroll run';
        this.toast.error(msg);
      },
    });
  }

  approveRun(run: PayrollRunDto) {
    if (!confirm(`Approve payroll for ${MONTHS[run.runMonth - 1]} ${run.runYear}?`)) return;
    this.payrollService.approveRun(run.id).subscribe({
      next: updated => {
        this.runs.update(list => list.map(r => r.id === updated.id ? updated : r));
        if (this.selectedRun()?.id === updated.id) this.selectedRun.set(updated);
        this.toast.success('Payroll approved');
      },
      error: () => this.toast.error('Approval failed'),
    });
  }

  markPaid(run: PayrollRunDto) {
    if (!confirm(`Mark ${MONTHS[run.runMonth - 1]} ${run.runYear} payroll as PAID?`)) return;
    this.payrollService.markPaid(run.id).subscribe({
      next: updated => {
        this.runs.update(list => list.map(r => r.id === updated.id ? updated : r));
        if (this.selectedRun()?.id === updated.id) this.selectedRun.set(updated);
        this.toast.success('Payroll marked as paid');
      },
      error: () => this.toast.error('Failed to mark as paid'),
    });
  }

  openPayslips(run: PayrollRunDto) {
    this.selectedRun.set(run);
    this.payslips.set([]);
    this.showSlipPanel.set(true);
    this.loadingSlips.set(true);
    this.payrollService.getPayslipsForRun(run.id).subscribe({
      next: slips => { this.payslips.set(slips); this.loadingSlips.set(false); },
      error: () => { this.toast.error('Failed to load payslips'); this.loadingSlips.set(false); },
    });
  }

  closeSlipPanel() { this.showSlipPanel.set(false); this.selectedRun.set(null); }

  // ── Salary Structures ─────────────────────────────────────────────────────

  loadStructures() {
    this.payrollService.listStructures().subscribe({
      next: s => this.structures.set(s),
      error: () => {},
    });
  }

  loadStaff() {
    this.staffService.list().subscribe({
      next: (s: any[]) => this.staffList.set(s),
      error: () => {},
    });
  }

  openStructModal(existing?: SalaryStructureDto) {
    this.editStructId.set(existing?.id ?? null);
    if (existing) {
      this.structForm.reset({
        staffId:                 existing.staffId,
        basicSalary:             existing.basicSalary,
        hraAmount:               existing.hraAmount,
        daAmount:                existing.daAmount,
        taAmount:                existing.taAmount,
        medicalAllowance:        existing.medicalAllowance,
        otherAllowances:         existing.otherAllowances,
        pfEnrolled:              existing.pfEnrolled,
        pfWageCeiling:           existing.pfWageCeiling,
        taxRegime:               existing.taxRegime,
        declared80c:             existing.declared80c,
        declaredHraExemption:    existing.declaredHraExemption,
        declaredOtherExemptions: existing.declaredOtherExemptions,
        effectiveFrom:           existing.effectiveFrom,
        effectiveTo:             existing.effectiveTo,
      });
    } else {
      this.structForm.reset({
        pfEnrolled: true, pfWageCeiling: 15000, taxRegime: 'NEW',
        effectiveFrom: this.today(),
        declared80c: 0, declaredHraExemption: 0, declaredOtherExemptions: 0,
        hraAmount: 0, daAmount: 0, taAmount: 0, medicalAllowance: 0, otherAllowances: 0,
      });
    }
    this.showStructModal.set(true);
  }

  closeStructModal() { this.showStructModal.set(false); this.editStructId.set(null); }

  submitStruct() {
    if (this.structForm.invalid) { this.structForm.markAllAsTouched(); return; }
    const v = this.structForm.value;
    const req: CreateSalaryStructureRequest = {
      staffId:                 v.staffId!,
      basicSalary:             v.basicSalary!,
      hraAmount:               v.hraAmount ?? 0,
      daAmount:                v.daAmount  ?? 0,
      taAmount:                v.taAmount  ?? 0,
      medicalAllowance:        v.medicalAllowance ?? 0,
      otherAllowances:         v.otherAllowances  ?? 0,
      pfEnrolled:              v.pfEnrolled ?? true,
      pfWageCeiling:           v.pfWageCeiling ?? null,
      taxRegime:               (v.taxRegime ?? 'NEW') as TaxRegime,
      declared80c:             v.declared80c ?? 0,
      declaredHraExemption:    v.declaredHraExemption ?? 0,
      declaredOtherExemptions: v.declaredOtherExemptions ?? 0,
      effectiveFrom:           v.effectiveFrom!,
      effectiveTo:             v.effectiveTo || null,
    };
    this.saving.set(true);
    const editId = this.editStructId();
    const obs = editId
      ? this.payrollService.updateStructure(editId, req)
      : this.payrollService.createStructure(req);

    obs.subscribe({
      next: s => {
        if (editId) {
          this.structures.update(list => list.map(x => x.id === s.id ? s : x));
        } else {
          this.structures.update(list => [s, ...list]);
        }
        this.saving.set(false);
        this.closeStructModal();
        this.toast.success(editId ? 'Salary structure updated' : 'Salary structure created');
      },
      error: () => { this.saving.set(false); this.toast.error('Failed to save salary structure'); },
    });
  }

  grossPreview(): number {
    const v = this.structForm.value;
    return (v.basicSalary ?? 0) + (v.hraAmount ?? 0) + (v.daAmount ?? 0)
      + (v.taAmount ?? 0) + (v.medicalAllowance ?? 0) + (v.otherAllowances ?? 0);
  }

  // ── Expenses ──────────────────────────────────────────────────────────────

  loadExpenses() {
    this.payrollService.listExpenses(this.expenseFrom(), this.expenseTo()).subscribe({
      next: e => this.expenses.set(e),
      error: () => {},
    });
  }

  onExpenseDateChange() { this.loadExpenses(); }

  totalExpenses = computed(() => this.expenses().reduce((s, e) => s + e.amount, 0));

  openExpenseModal(existing?: ExpenseEntryDto) {
    this.editExpenseId.set(existing?.id ?? null);
    if (existing) {
      this.expenseForm.reset({
        category: existing.category, description: existing.description,
        amount: existing.amount, expenseDate: existing.expenseDate,
        referenceNo: existing.referenceNo,
      });
    } else {
      this.expenseForm.reset({ category: 'RENT', expenseDate: this.today() });
    }
    this.showExpenseModal.set(true);
  }

  closeExpenseModal() { this.showExpenseModal.set(false); this.editExpenseId.set(null); }

  submitExpense() {
    if (this.expenseForm.invalid) { this.expenseForm.markAllAsTouched(); return; }
    const v = this.expenseForm.value;
    const req = {
      category:    v.category!,
      description: v.description!,
      amount:      v.amount!,
      expenseDate: v.expenseDate!,
      referenceNo: v.referenceNo || null,
      attachmentUrl: null,
    };
    this.saving.set(true);
    const editId = this.editExpenseId();
    const obs = editId
      ? this.payrollService.updateExpense(editId, req)
      : this.payrollService.createExpense(req);

    obs.subscribe({
      next: e => {
        if (editId) {
          this.expenses.update(list => list.map(x => x.id === e.id ? e : x));
        } else {
          this.expenses.update(list => [e, ...list]);
        }
        this.saving.set(false);
        this.closeExpenseModal();
        this.toast.success(editId ? 'Expense updated' : 'Expense logged');
      },
      error: () => { this.saving.set(false); this.toast.error('Failed to save expense'); },
    });
  }

  deleteExpense(id: string) {
    if (!confirm('Delete this expense?')) return;
    this.payrollService.deleteExpense(id).subscribe({
      next: () => {
        this.expenses.update(list => list.filter(e => e.id !== id));
        this.toast.success('Expense deleted');
      },
      error: () => this.toast.error('Failed to delete expense'),
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  get rf() { return this.runForm.controls; }
  get sf() { return this.structForm.controls; }
  get ef() { return this.expenseForm.controls; }

  monthLabel(m: number): string { return MONTHS[m - 1] ?? ''; }

  statusClass(status: string): string {
    return { DRAFT: 'badge--warning', APPROVED: 'badge--info', PAID: 'badge--success' }[status] ?? 'badge--default';
  }

  catLabel(c: string): string {
    return c.charAt(0) + c.slice(1).toLowerCase().replace(/_/g, ' ');
  }

  private today(): string { return new Date().toISOString().substring(0, 10); }
  private monthStart(): string {
    const d = new Date(); d.setDate(1);
    return d.toISOString().substring(0, 10);
  }
  private buildYearRange(): number[] {
    const y = new Date().getFullYear();
    return [y - 1, y, y + 1];
  }
}
