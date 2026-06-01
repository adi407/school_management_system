import { Component, OnInit, signal, computed } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { DatePipe, DecimalPipe, CurrencyPipe } from '@angular/common';
import { FeeService } from '../../../core/services/fee.service';
import { StudentService } from '../../../core/services/student.service';
import { AcademicService } from '../../../core/services/academic.service';
import { ToastService } from '../../../core/services/toast.service';
import { FeeStructureDto, FeePaymentDto } from '../../../core/models/fee.model';
import { StudentSummaryDto } from '../../../core/models/student.model';

type Tab = 'payments' | 'structures';

@Component({
  selector: 'sms-fees',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, DatePipe, DecimalPipe, CurrencyPipe],
  templateUrl: './fees.component.html',
  styleUrls: ['./fees.component.scss'],
})
export class FeesComponent implements OnInit {
  activeTab   = signal<Tab>('payments');
  search      = signal('');
  loading     = signal(false);
  saving      = signal(false);

  payments    = signal<FeePaymentDto[]>([]);
  structures  = signal<FeeStructureDto[]>([]);
  students    = signal<StudentSummaryDto[]>([]);
  classes     = signal<any[]>([]);
  academicYears = signal<any[]>([]);

  showPaymentModal    = signal(false);
  showStructureModal  = signal(false);

  filteredPayments = computed(() => {
    const q = this.search().toLowerCase();
    return q
      ? this.payments().filter(p =>
          p.studentName?.toLowerCase().includes(q) ||
          p.admissionNo?.toLowerCase().includes(q) ||
          p.receiptNo?.toLowerCase().includes(q)
        )
      : this.payments();
  });

  totalCollected = computed(() =>
    this.payments().reduce((s, p) => s + p.amountPaid, 0)
  );

  paymentForm = this.fb.group({
    studentId:      ['', Validators.required],
    feeStructureId: [null as string | null],
    amountPaid:     [null as number | null, [Validators.required, Validators.min(1)]],
    paymentDate:    [new Date().toISOString().substring(0, 10), Validators.required],
    paymentMode:    ['CASH', Validators.required],
    remarks:        [''],
  });

  structureForm = this.fb.group({
    feeType:        ['', Validators.required],
    amount:         [null as number | null, [Validators.required, Validators.min(1)]],
    classId:        [null as string | null],
    academicYearId: [null as string | null],
    dueDate:        [null as string | null],
    isRecurring:    [false],
    frequency:      ['ONE_TIME'],
    description:    [''],
  });

  readonly paymentModes = ['CASH', 'ONLINE', 'CHEQUE', 'DD', 'UPI'];
  readonly frequencies  = ['ONE_TIME', 'MONTHLY', 'QUARTERLY', 'ANNUAL'];

  constructor(
    private fb: FormBuilder,
    private feeService: FeeService,
    private studentService: StudentService,
    private academicService: AcademicService,
    private toast: ToastService,
  ) {}

  ngOnInit() {
    this.loadAll();
  }

  setTab(tab: Tab) {
    this.activeTab.set(tab);
  }

  loadAll() {
    this.loading.set(true);
    this.feeService.listStructures().subscribe({
      next: s => this.structures.set(s),
      error: () => {},
    });

    // Load all payments (school-wide) by fetching per school via structures list
    // For now, load payments from recent school payments
    this.loading.set(false);

    // Load students for dropdown (first page)
    this.studentService.list({ page: 0, size: 200 }).subscribe({
      next: page => this.students.set(page.content),
      error: () => {},
    });

    this.academicService.listClasses().subscribe({
      next: c => this.classes.set(c),
      error: () => {},
    });

    this.academicService.listYears().subscribe({
      next: y => this.academicYears.set(y),
      error: () => {},
    });
  }

  openPaymentModal() {
    this.paymentForm.reset({
      paymentDate: new Date().toISOString().substring(0, 10),
      paymentMode: 'CASH',
    });
    this.showPaymentModal.set(true);
  }

  closePaymentModal() {
    this.showPaymentModal.set(false);
  }

  openStructureModal() {
    this.structureForm.reset({ isRecurring: false, frequency: 'ONE_TIME' });
    this.showStructureModal.set(true);
  }

  closeStructureModal() {
    this.showStructureModal.set(false);
  }

  submitPayment() {
    if (this.paymentForm.invalid) { this.paymentForm.markAllAsTouched(); return; }
    const v = this.paymentForm.value;
    this.saving.set(true);
    this.feeService.recordPayment({
      studentId:      v.studentId!,
      feeStructureId: v.feeStructureId || null,
      amountPaid:     v.amountPaid!,
      paymentDate:    v.paymentDate!,
      paymentMode:    v.paymentMode!,
      remarks:        v.remarks || null,
    }).subscribe({
      next: payment => {
        this.payments.update(list => [payment, ...list]);
        this.saving.set(false);
        this.closePaymentModal();
        this.toast.success('Payment recorded — Receipt: ' + payment.receiptNo);
      },
      error: () => {
        this.saving.set(false);
        this.toast.error('Failed to record payment');
      },
    });
  }

  submitStructure() {
    if (this.structureForm.invalid) { this.structureForm.markAllAsTouched(); return; }
    const v = this.structureForm.value;
    this.saving.set(true);
    this.feeService.createStructure({
      feeType:        v.feeType!,
      amount:         v.amount!,
      classId:        v.classId || null,
      academicYearId: v.academicYearId || null,
      dueDate:        v.dueDate || null,
      isRecurring:    v.isRecurring ?? false,
      frequency:      v.frequency || null,
      description:    v.description || null,
    }).subscribe({
      next: s => {
        this.structures.update(list => [...list, s]);
        this.saving.set(false);
        this.closeStructureModal();
        this.toast.success('Fee structure created');
      },
      error: () => {
        this.saving.set(false);
        this.toast.error('Failed to create fee structure');
      },
    });
  }

  deleteStructure(id: string) {
    if (!confirm('Delete this fee structure?')) return;
    this.feeService.deleteStructure(id).subscribe({
      next: () => {
        this.structures.update(list => list.filter(s => s.id !== id));
        this.toast.success('Fee structure deleted');
      },
      error: () => this.toast.error('Cannot delete — may be linked to payments'),
    });
  }

  get f() { return this.paymentForm.controls; }
  get sf() { return this.structureForm.controls; }

  modeLabel(mode: string) {
    const map: Record<string, string> = { CASH: 'Cash', ONLINE: 'Online', CHEQUE: 'Cheque', DD: 'DD', UPI: 'UPI' };
    return map[mode] ?? mode;
  }
}
