import { Component, OnInit, signal, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { AcademicService } from '../../../core/services/academic.service';
import { ToastService } from '../../../core/services/toast.service';
import { AcademicYearDto } from '../../../core/models/academic.model';

@Component({
  selector: 'sms-academic-years',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, DatePipe],
  templateUrl: './academic-years.component.html',
  styleUrls: ['./academic-years.component.scss'],
})
export class AcademicYearsComponent implements OnInit {
  private svc   = inject(AcademicService);
  private toast = inject(ToastService);
  private fb    = inject(FormBuilder);

  years     = signal<AcademicYearDto[]>([]);
  loading   = signal(false);
  saving    = signal(false);
  showModal = signal(false);
  editingId = signal<string | null>(null);

  form = this.fb.group({
    name:      ['', Validators.required],
    startDate: ['', Validators.required],
    endDate:   ['', Validators.required],
    isCurrent: [false],
  });
  get f() { return this.form.controls; }

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.svc.listYears().subscribe({
      next: y => { this.years.set(y); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Failed to load academic years'); },
    });
  }

  openCreate() {
    this.editingId.set(null);
    this.form.reset({ isCurrent: this.years().length === 0 });
    this.showModal.set(true);
  }

  openEdit(y: AcademicYearDto) {
    this.editingId.set(y.id);
    this.form.patchValue({
      name:      y.name,
      startDate: y.startDate,
      endDate:   y.endDate,
      isCurrent: y.isCurrent,
    });
    this.showModal.set(true);
  }

  closeModal() { this.showModal.set(false); }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const payload = {
      name:      v.name!,
      startDate: v.startDate!,
      endDate:   v.endDate!,
      isCurrent: v.isCurrent ?? false,
    };
    this.saving.set(true);
    const id = this.editingId();

    const req$ = id
      ? this.svc.updateYear(id, payload)
      : this.svc.createYear(payload);

    req$.subscribe({
      next: result => {
        if (id) {
          // If set as current, unset others in local state
          this.years.update(list =>
            list.map(y => ({ ...y, isCurrent: result.isCurrent ? y.id === id : y.isCurrent }))
                .map(y => y.id === id ? result : y)
          );
        } else {
          if (result.isCurrent) {
            this.years.update(list => list.map(y => ({ ...y, isCurrent: false })));
          }
          this.years.update(list => [result, ...list]);
        }
        this.saving.set(false);
        this.closeModal();
        this.toast.success(id ? 'Academic year updated' : 'Academic year created');
      },
      error: err => { this.saving.set(false); this.toast.error(err?.error?.message ?? 'Save failed'); },
    });
  }

  setCurrent(y: AcademicYearDto) {
    if (y.isCurrent) return;
    this.svc.updateYear(y.id, {
      name: y.name, startDate: y.startDate, endDate: y.endDate, isCurrent: true,
    }).subscribe({
      next: () => {
        this.years.update(list => list.map(yr => ({ ...yr, isCurrent: yr.id === y.id })));
        this.toast.success(`${y.name} set as current year`);
      },
      error: () => this.toast.error('Failed to set current year'),
    });
  }
}
